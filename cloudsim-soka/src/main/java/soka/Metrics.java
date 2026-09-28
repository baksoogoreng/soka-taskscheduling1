package soka;

public class Metrics {
    public static double[] vmBusyTime(int[] map, double[] mi, double[] vmMips) {
        double[] busy = new double[vmMips.length];
        for (int i = 0; i < map.length; i++) busy[map[i]] += mi[i] / vmMips[map[i]];
        return busy;
    }

    public static double makespan(int[] map, double[] mi, double[] vmMips) {
        double max = 0;
        for (double b : vmBusyTime(map, mi, vmMips)) max = Math.max(max, b);
        return max;
    }

    /** dalam persen */
    public static double utilization(int[] map, double[] mi, double[] vmMips) {
        double[] busy = vmBusyTime(map, mi, vmMips);
        double sum = 0, mk = 0;
        for (double b : busy) { sum += b; mk = Math.max(mk, b); }
        return sum / (mk * vmMips.length) * 100.0;
    }

    /** Rata-rata waktu tunggu; cloudlet di satu VM jalan berurutan (SpaceShared) */
    public static double avgWaiting(int[] map, double[] mi, double[] vmMips) {
        double[] clock = new double[vmMips.length];
        double total = 0;
        for (int i = 0; i < map.length; i++) {
            total += clock[map[i]];
            clock[map[i]] += mi[i] / vmMips[map[i]];
        }
        return total / map.length;
    }

    public static double fitness(int[] map, double[] mi, double[] vmMips, double w1, double w2) {
        double sumMi = 0, sumMips = 0;
        for (double x : mi) sumMi += x;
        for (double x : vmMips) sumMips += x;
        double lowerBound = sumMi / sumMips;
        double mkNorm = makespan(map, mi, vmMips) / lowerBound;
        double utNorm = utilization(map, mi, vmMips) / 100.0;
        return w1 * mkNorm - w2 * utNorm;
    }
}