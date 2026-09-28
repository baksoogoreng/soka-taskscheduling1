package soka.algo;

public class RRABC extends ABC {
    public RRABC(double w1, double w2, int pop, int iters, int limit, int rrSeeds) {
        super(w1, w2, pop, iters, limit, rrSeeds, "RR-ABC");
    }
}