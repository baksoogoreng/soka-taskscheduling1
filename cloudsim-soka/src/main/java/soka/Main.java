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
        org.cloudbus.cloudsim.Log.disable();

        double[] vmMips = CloudEnv.VM_MIPS;

        double[][] W = {{0.5, 0.5}, {0.7, 0.3}, {0.3, 0.7}};
        int RUNS = 3, POP = 30, ITERS = 100;

        try (PrintWriter runs = new PrintWriter("results.csv");
             PrintWriter sum = new PrintWriter("summary.csv")) {
            runs.println("tasks,algo,w1,w2,run,makespan,util,waiting,fitness");
            sum.println("tasks,algo,w1,w2,runs,mk_mean,mk_std,util_mean,util_std,wait_mean,wait_std,fit_mean,fit_std,mk_improve_vs_RR_pct");

            System.out.println("Pengujian 100-1000 task, 3 run per kombinasi");
            for (int tasks = 100; tasks <= 1000; tasks += 100) {
                List<Long> miList = new ArrayList<>();
                for (String line : java.nio.file.Files.readAllLines(java.nio.file.Path.of("../GoCJ Google Cloud Jobs Dataset/GoCJ_Dataset_" + tasks + ".txt"))) {
                    if (!line.isBlank()) miList.add(Long.parseLong(line.trim()));
                }
                if (miList.size() != tasks) throw new IllegalStateException("Jumlah dataset tidak sesuai");
                double[] mi = miList.stream().mapToDouble(Long::doubleValue).toArray();

            for (double[] w : W) {
                List<Scheduler> algos = List.of(
                        new RoundRobin(),
                        new ABC(w[0], w[1], POP, ITERS, 20),
                        new CuckooSearch(w[0], w[1], POP, ITERS, 0.25),
                        new RRABC(w[0], w[1], POP, ITERS, 20, POP / 3),
                        new GAGWO(w[0], w[1], POP, ITERS, 0.8, 0.02));

                System.out.printf("%n=== %d TASK | Bobot w1=%.1f, w2=%.1f ===%n", tasks, w[0], w[1]);
                System.out.printf("%-10s %3s %14s %14s %14s %10s%n", "Algoritma", "Run", "Makespan (s)", "Util (%)", "Waiting (s)", "Fitness");
                System.out.println("-".repeat(71));
                StringBuilder summaryTable = new StringBuilder();
                double rrMk = 0;
                for (Scheduler s : algos) {
                    int n = RUNS;
                    double[] mk = new double[n], ut = new double[n], wt = new double[n], fi = new double[n];
                    int[] lastMap = null;
                    for (int r = 0; r < n; r++) {
                        int[] map = s.schedule(mi, vmMips, new Random(1000 + r));
                        mk[r] = Metrics.makespan(map, mi, vmMips);
                        ut[r] = Metrics.utilization(map, mi, vmMips);
                        wt[r] = Metrics.avgWaiting(map, mi, vmMips);
                        fi[r] = Metrics.fitness(map, mi, vmMips, w[0], w[1]);
                        runs.printf("%d,%s,%.1f,%.1f,%d,%.3f,%.3f,%.3f,%.5f%n",
                                tasks, s.name(), w[0], w[1], r + 1, mk[r], ut[r], wt[r], fi[r]);
                        System.out.printf("%-10s %3d %14.3f %14.3f %14.3f %10.5f%n", s.name(), r + 1, mk[r], ut[r], wt[r], fi[r]);
                        lastMap = map;
                    }
                    if (s.name().equals("RR")) rrMk = mean(mk);
                    double imp = (rrMk - mean(mk)) / rrMk * 100.0;
                    sum.printf("%d,%s,%.1f,%.1f,%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.5f,%.5f,%.2f%n",
                            tasks, s.name(), w[0], w[1], n, mean(mk), std(mk), mean(ut), std(ut),
                            mean(wt), std(wt), mean(fi), std(fi), imp);
                    summaryTable.append(String.format("%-10s %20s %21s %21s %10.5f %12.2f%n",
                            s.name(), String.format("%.3f +/- %.3f", mean(mk), std(mk)),
                            String.format("%.3f +/- %.3f", mean(ut), std(ut)),
                            String.format("%.3f +/- %.3f", mean(wt), std(wt)), mean(fi), imp));
                    if (w == W[0]) validate(s.name(), lastMap, miList, mi, vmMips);
                }
                System.out.println("\nRingkasan 3 pengulangan (rata-rata +/- standar deviasi):");
                System.out.printf("%-10s %20s %21s %21s %10s %12s%n", "Algoritma", "Makespan (s)", "Util (%)", "Waiting (s)", "Fitness", "vs RR (%)");
                System.out.println("-".repeat(105));
                System.out.print(summaryTable);
            }
            }
            System.out.println("Hasil lengkap: results.csv | Ringkasan: summary.csv");
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