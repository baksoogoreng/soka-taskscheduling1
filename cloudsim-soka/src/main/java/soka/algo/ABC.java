package soka.algo;

import java.util.Random;
import soka.Metrics;

public class ABC implements Scheduler {
    private final double w1, w2;
    private final int pop, iters, limit;
    private final int rrSeeds;
    private final String label;

    // Konstruktor default
    public ABC(double w1, double w2, int pop, int iters, int limit) {
        this(w1, w2, pop, iters, limit, 0, "ABC");
    }

    // Konstruktor dengan RR seeds dan custom label
    public ABC(double w1, double w2, int pop, int iters, int limit,
               int rrSeeds, String label) {
        this.w1 = w1;
        this.w2 = w2;
        this.pop = pop;
        this.iters = iters;
        this.limit = limit;
        this.rrSeeds = rrSeeds;
        this.label = label;
    }

    @Override
    public int[] schedule(double[] mi, double[] vmMips, Random rnd) {
        int n = mi.length;
        int m = vmMips.length;

        int[][] food = new int[pop][n];
        double[] fit = new double[pop];
        int[] trial = new int[pop];

        // Inisialisasi populasi
        for (int i = 0; i < pop; i++) {
            if (i < rrSeeds) {
                // Solusi awal berdasarkan Round Robin dengan offset
                for (int j = 0; j < n; j++) {
                    food[i][j] = (j + i) % m;
                }
            } else {
                // Sisanya diinisialisasi secara random
                randomize(food[i], m, rnd);
            }

            fit[i] = Metrics.fitness(
                    food[i],
                    mi,
                    vmMips,
                    w1,
                    w2
            );
        }

        // Simpan solusi terbaik awal
        int[] best = food[argMin(fit)].clone();
        double bestFit = Metrics.fitness(
                best,
                mi,
                vmMips,
                w1,
                w2
        );

        // Main ABC loop
        for (int it = 0; it < iters; it++) {

            // 1. Employed bees
            for (int i = 0; i < pop; i++) {
                improve(
                        food,
                        fit,
                        trial,
                        i,
                        mi,
                        vmMips,
                        rnd
                );
            }

            // 2. Onlooker bees
            // Fitness kecil = peluang lebih besar
            double maxF = Double.NEGATIVE_INFINITY;

            for (double f : fit) {
                maxF = Math.max(maxF, f);
            }

            double[] prob = new double[pop];
            double sum = 0;

            for (int i = 0; i < pop; i++) {
                prob[i] = maxF - fit[i] + 1e-9;
                sum += prob[i];
            }

            for (int c = 0; c < pop; c++) {
                double r = rnd.nextDouble() * sum;
                double acc = 0;

                int pick = pop - 1;

                for (int i = 0; i < pop; i++) {
                    acc += prob[i];

                    if (acc >= r) {
                        pick = i;
                        break;
                    }
                }

                improve(
                        food,
                        fit,
                        trial,
                        pick,
                        mi,
                        vmMips,
                        rnd
                );
            }

            // 3. Scout bees
            for (int i = 0; i < pop; i++) {
                if (trial[i] > limit) {
                    randomize(
                            food[i],
                            m,
                            rnd
                    );

                    fit[i] = Metrics.fitness(
                            food[i],
                            mi,
                            vmMips,
                            w1,
                            w2
                    );

                    trial[i] = 0;
                }
            }

            // Update global best
            int b = argMin(fit);

            if (fit[b] < bestFit) {
                bestFit = fit[b];
                best = food[b].clone();
            }
        }

        return best;
    }

    private void improve(
            int[][] food,
            double[] fit,
            int[] trial,
            int i,
            double[] mi,
            double[] vmMips,
            Random rnd) {

        int n = mi.length;
        int m = vmMips.length;

        int k;

        // Pilih food source lain selain i
        do {
            k = rnd.nextInt(pop);
        } while (k == i);

        // Buat kandidat dari solusi saat ini
        int[] cand = food[i].clone();

        // Ubah sekitar 10% dimensi
        int changes = Math.max(1, n / 10);

        for (int c = 0; c < changes; c++) {
            int j = rnd.nextInt(n);

            // phi berada pada [-1, 1]
            double phi = rnd.nextDouble() * 2 - 1;

            int v = (int) Math.round(
                    food[i][j]
                            + phi * (food[i][j] - food[k][j])
            );

            // Pastikan ada perubahan
            if (v == food[i][j]) {
                v = rnd.nextInt(m);
            }

            // Pastikan VM index tetap valid
            cand[j] = Math.floorMod(v, m);
        }

        // Hitung fitness kandidat
        double f = Metrics.fitness(
                cand,
                mi,
                vmMips,
                w1,
                w2
        );

        // Greedy selection
        if (f < fit[i]) {
            food[i] = cand;
            fit[i] = f;
            trial[i] = 0;
        } else {
            trial[i]++;
        }
    }

    // Random initialization
    private void randomize(
            int[] x,
            int m,
            Random rnd) {

        for (int j = 0; j < x.length; j++) {
            x[j] = rnd.nextInt(m);
        }
    }

    // Cari index dengan fitness terkecil
    private int argMin(double[] a) {
        int b = 0;

        for (int i = 1; i < a.length; i++) {
            if (a[i] < a[b]) {
                b = i;
            }
        }

        return b;
    }

    @Override
    public String name() {
        return label;
    }
}