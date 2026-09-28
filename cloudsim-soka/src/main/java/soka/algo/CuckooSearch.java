package soka.algo;

import java.util.Random;
import soka.Metrics;

public class CuckooSearch implements Scheduler {
    private static final double BETA = 1.5;
    private static final double SIGMA_U = 0.6966; // Mantegna, untuk beta = 1.5

    private final double w1, w2, pa;
    private final int pop, iters;

    public CuckooSearch(double w1, double w2, int pop, int iters, double pa) {
        this.w1 = w1; this.w2 = w2;
        this.pop = pop; this.iters = iters; this.pa = pa;
    }

    @Override
    public int[] schedule(double[] mi, double[] vmMips, Random rnd) {
        int n = mi.length, m = vmMips.length;
        int[][] nest = new int[pop][n];
        double[] fit = new double[pop];

        for (int i = 0; i < pop; i++) {
            for (int j = 0; j < n; j++) nest[i][j] = rnd.nextInt(m);
            fit[i] = Metrics.fitness(nest[i], mi, vmMips, w1, w2);
        }
        int[] best = nest[argMin(fit)].clone();
        double bestFit = Metrics.fitness(best, mi, vmMips, w1, w2);

        for (int it = 0; it < iters; it++) {
            // 1. Lévy flight: telur baru di tiap sarang, ganti kalau lebih baik
            for (int i = 0; i < pop; i++) {
                int[] cand = nest[i].clone();
                int changes = Math.max(1, n / 10);
                for (int c = 0; c < changes; c++) {
                    int j = rnd.nextInt(n);
                    int delta = (int) Math.round(levy(rnd) * m);
                    if (delta == 0) delta = rnd.nextBoolean() ? 1 : -1;
                    cand[j] = Math.floorMod(cand[j] + delta, m);
                }
                double f = Metrics.fitness(cand, mi, vmMips, w1, w2);
                int k = rnd.nextInt(pop);            // sarang acak (sesuai CS asli)
                if (f < fit[k]) { nest[k] = cand; fit[k] = f; }
            }

            // 2. Sarang terburuk (sebanyak pa * pop) ditemukan dan dibuang
            int abandon = (int) Math.round(pa * pop);
            for (int a = 0; a < abandon; a++) {
                int worst = argMax(fit);
                int p = rnd.nextInt(pop), q = rnd.nextInt(pop);
                int[] cand = nest[worst].clone();
                for (int j = 0; j < n; j++) {
                    if (rnd.nextDouble() < pa) {
                        int v = cand[j] + (int) Math.round(rnd.nextDouble() * (nest[p][j] - nest[q][j]));
                        if (v == cand[j]) v = rnd.nextInt(m);
                        cand[j] = Math.floorMod(v, m);
                    }
                }
                nest[worst] = cand;
                fit[worst] = Metrics.fitness(cand, mi, vmMips, w1, w2);
            }

            int b = argMin(fit);
            if (fit[b] < bestFit) { bestFit = fit[b]; best = nest[b].clone(); }
        }
        return best;
    }

    /** Langkah Lévy (Mantegna), bisa positif/negatif */
    private double levy(Random rnd) {
        double u = rnd.nextGaussian() * SIGMA_U;
        double v = rnd.nextGaussian();
        return u / Math.pow(Math.abs(v), 1.0 / BETA);
    }

    private int argMin(double[] a) { int b = 0; for (int i = 1; i < a.length; i++) if (a[i] < a[b]) b = i; return b; }
    private int argMax(double[] a) { int b = 0; for (int i = 1; i < a.length; i++) if (a[i] > a[b]) b = i; return b; }

    @Override public String name() { return "CS"; }
}