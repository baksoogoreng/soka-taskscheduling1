package soka;

import java.io.PrintWriter;
import java.util.*;
import org.cloudbus.cloudsim.Cloudlet;
import soka.algo.*;

public class Main {
    static double mean(double[] a) { double s = 0; for (double x : a) s += x; return s / a.length; }
    static double std(double[] a) {
        if (a.length < 2) return 0;
        double mu = mean(a), s = 0;
        for (double x : a) s += (x - mu) * (x - mu);
        return Math.sqrt(s / (a.length - 1));
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        List<Long> miList = DatasetLoader.load("gocj_sample.txt");
        double[] mi = miList.stream().mapToDouble(Long::doubleValue).toArray();
        double[] vmMips = CloudEnv.VM_MIPS;

        double[][] W = {{0.5, 0.5}, {0.7, 0.3}, {0.3, 0.7}};
        int RUNS = 30, POP = 30, ITERS = 100;

        try (PrintWriter runs = new PrintWriter("results.csv");
             PrintWriter sum = new PrintWriter("summary.csv")) {
            runs.println("algo,w1,w2,run,makespan,util,waiting,fitness");
            sum.println("algo,w1,w2,mk_mean,mk_std,util_mean,util_std,wait_mean,wait_std,fit_mean,fit_std,mk_improve_vs_RR_pct");

            for (double[] w : W) {
                List<Scheduler> algos = List.of(
                        new RoundRobin(),
                        new ABC(w[0], w[1], POP, ITERS, 20),
                        new CuckooSearch(w[0], w[1], POP, ITERS, 0.25),
                        new RRABC(w[0], w[1], POP, ITERS, 20, POP / 3),
                        new GAGWO(w[0], w[1], POP, ITERS, 0.8, 0.02));

                System.out.printf("%n=== Bobot w1=%.1f, w2=%.1f ===%n", w[0], w[1]);
                double rrMk = 0;
                for (Scheduler s : algos) {
                    int n = s.name().equals("RR") ? 1 : RUNS;
                    double[] mk = new double[n], ut = new double[n], wt = new double[n], fi = new double[n];
                    int[] lastMap = null;
                    for (int r = 0; r < n; r++) {
                        int[] map = s.schedule(mi, vmMips, new Random(1000 + r));
                        mk[r] = Metrics.makespan(map, mi, vmMips);
                        ut[r] = Metrics.utilization(map, mi, vmMips);
                        wt[r] = Metrics.avgWaiting(map, mi, vmMips);
                        fi[r] = Metrics.fitness(map, mi, vmMips, w[0], w[1]);
                        runs.printf("%s,%.1f,%.1f,%d,%.3f,%.3f,%.3f,%.5f%n",
                                s.name(), w[0], w[1], r, mk[r], ut[r], wt[r], fi[r]);
                        lastMap = map;
                    }
                    if (s.name().equals("RR")) rrMk = mean(mk);
                    double imp = (rrMk - mean(mk)) / rrMk * 100.0;
                    sum.printf("%s,%.1f,%.1f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.5f,%.5f,%.2f%n",
                            s.name(), w[0], w[1], mean(mk), std(mk), mean(ut), std(ut),
                            mean(wt), std(wt), mean(fi), std(fi), imp);
                    System.out.printf("%-7s makespan %.2f ± %.2f | util %.2f ± %.2f %% | wait %.2f ± %.2f | fit %.4f | perbaikan vs RR %.2f %%%n",
                            s.name(), mean(mk), std(mk), mean(ut), std(ut), mean(wt), std(wt), mean(fi), imp);

                    // validasi CloudSim pada skenario pertama saja
                    if (w == W[0]) validate(s.name(), lastMap, miList, mi, vmMips);
                }
            }
        }
    }

    static void validate(String name, int[] map, List<Long> miList, double[] mi, double[] vmMips) throws Exception {
        List<Cloudlet> done = CloudEnv.simulate(map, miList);
        double minStart = Double.MAX_VALUE, maxFinish = 0;
        for (Cloudlet c : done) {
            minStart = Math.min(minStart, c.getExecStartTime());
            maxFinish = Math.max(maxFinish, c.getFinishTime());
        }
        System.out.printf("   [validasi %s] %d/%d selesai | CloudSim %.2f vs analitis %.2f%n",
                name, done.size(), mi.length, maxFinish - minStart, Metrics.makespan(map, mi, vmMips));
    }
}