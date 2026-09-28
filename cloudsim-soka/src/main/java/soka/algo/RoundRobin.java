package soka.algo;

import java.util.Random;

public class RoundRobin implements Scheduler {
    @Override
    public int[] schedule(double[] mi, double[] vmMips, Random rnd) {
        int[] map = new int[mi.length];
        for (int i = 0; i < mi.length; i++) map[i] = i % vmMips.length;
        return map;
    }
    @Override public String name() { return "RR"; }
}