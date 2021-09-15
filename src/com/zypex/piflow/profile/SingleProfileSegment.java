package com.zypex.piflow.profile;

import utils.math.BoundedFunction;
import utils.math.SingleBoundedFunction;
import utils.math.Vector;

import java.util.Locale;

public abstract class SingleProfileSegment extends ProfileSegment {

    private final double length;
    public final SingleBoundedFunction<Derivatives<Vector>> function;

    public SingleProfileSegment(SingleBoundedFunction<Derivatives<Vector>> function, double length) {
        this.function = function;
        this.length = length;
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public double upperBound() {
        return function.upperBound();
    }

    @Override
    public double lowerBound() {
        return function.lowerBound();
    }


    @Override
    public Derivatives<Vector> get(double input) {
        return function.get(input);
    }
}
