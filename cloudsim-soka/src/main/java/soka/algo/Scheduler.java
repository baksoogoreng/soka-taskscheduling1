package soka.algo;

import java.util.Random;

public interface Scheduler {
    /** @return mapping: indeks = cloudlet, nilai = indeks VM tujuan */
    int[] schedule(double[] mi, double[] vmMips, Random rnd);
    String name();
}