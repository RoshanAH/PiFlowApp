package com.zypex.piflow;

public class PIDFConstants {
    public final double p;
    public final double i;
    public final double d;
    public final double f;

    public PIDFConstants(double p, double i, double d, double f) {
        this.p = p;
        this.i = i;
        this.d = d;
        this.f = f;
    }
}
