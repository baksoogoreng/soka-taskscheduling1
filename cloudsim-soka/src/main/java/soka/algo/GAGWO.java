package soka.algo;

import java.util.Random;
import soka.Metrics;

public class GAGWO implements Scheduler {
    private final double w1, w2, pc, pm;
    private final int pop, iters;

    public GAGWO(double w1, double w2, int pop, int iters, double pc, double pm) {
        this.w1 = w1; this.w2 = w2; this.pop = pop; this.iters = iters;
        this.pc = pc; this.pm = pm;
    }

    @Override
    public int[] schedule(double[] mi, double[] vmMips, Random rnd) {
        int n = mi.length, m = vmMips.length;
        int[][] X = new int[pop][n];
        double[] fit = new double[pop];
        for (int i = 0; i < pop; i++) {
            for (int j = 0; j < n; j++) X[i][j] = rnd.nextInt(m);
            fit[i] = Metrics.fitness(X[i], mi, vmMips, w1, w2);
        }
        int[] order = sortIdx(fit);
        int[] best = X[order[0]].clone();
        double bestFit = fit[order[0]];

        for (int it = 0; it < iters; it++) {
            // ===== Fase GWO =====
            order = sortIdx(fit);
            int[][] leaders = { X[order[0]].clone(), X[order[1]].clone(), X[order[2]].clone() };
            double a = 2.0 * (1.0 - (double) it / iters);      // turun 2 -> 0
            for (int i = 0; i < pop; i++) {
                int[] cand = new int[n];
                for (int j = 0; j < n; j++) {
                    double sum = 0;
                    for (int[] L : leaders) {
                        double A = 2 * a * rnd.nextDouble() - a;
                        double C = 2 * rnd.nextDouble();
                        double D = Math.abs(C * L[j] - X[i][j]);
                        sum += L[j] - A * D;
                    }
                    int v = (int) Math.round(sum / 3.0);
                    cand[j] = Math.max(0, Math.min(m - 1, v));
                }
                X[i] = cand;
                fit[i] = Metrics.fitness(cand, mi, vmMips, w1, w2);
            }

            // ===== Fase GA =====
            order = sortIdx(fit);
            int[][] next = new int[pop][];
            next[0] = X[order[0]].clone();                     // elitism 2 terbaik
            next[1] = X[order[1]].clone();
            for (int k = 2; k < pop; k += 2) {
                int[] c1 = X[tournament(fit, rnd)].clone();
                int[] c2 = X[tournament(fit, rnd)].clone();
                if (rnd.nextDouble() < pc) {                   // uniform crossover
                    for (int j = 0; j < n; j++)
                        if (rnd.nextBoolean()) { int t = c1[j]; c1[j] = c2[j]; c2[j] = t; }
                }
                mutate(c1, m, rnd);
                mutate(c2, m, rnd);
                next[k] = c1;
                if (k + 1 < pop) next[k + 1] = c2;
            }
            X = next;
            for (int i = 0; i < pop; i++) fit[i] = Metrics.fitness(X[i], mi, vmMips, w1, w2);

            int b = argMin(fit);
            if (fit[b] < bestFit) { bestFit = fit[b]; best = X[b].clone(); }
        }
        return best;
    }

    private void mutate(int[] x, int m, Random rnd) {
        for (int j = 0; j < x.length; j++) if (rnd.nextDouble() < pm) x[j] = rnd.nextInt(m);
    }

    private int tournament(double[] fit, Random rnd) {
        int a = rnd.nextInt(fit.length), b = rnd.nextInt(fit.length);
        return fit[a] <= fit[b] ? a : b;
    }

    private int[] sortIdx(double[] f) {
        Integer[] idx = new Integer[f.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (p, q) -> Double.compare(f[p], f[q]));
        int[] r = new int[idx.length];
        for (int i = 0; i < r.length; i++) r[i] = idx[i];
        return r;
    }

    private int argMin(double[] a) { int b = 0; for (int i = 1; i < a.length; i++) if (a[i] < a[b]) b = i; return b; }

    @Override public String name() { return "GA-GWO"; }
}