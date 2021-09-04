package com.zypex.piflow.profile;

import utils.math.*;

import java.util.ArrayList;
import java.util.List;

public class Linear extends ProfileSegment {

    public final SingleBoundedFunction<Derivatives<Vector>> function;

//    For the format of ax^3 + bx^2 + cx + d
    public final double a;
    public final double b;
    public final double c;
    public final double d;
    public final Vector dir;


    public Linear(SingleBoundedFunction<Derivatives<Vector>> function, double length) {
        super(function, length);
        this.function = function;

        this.d = function.get(0).position.getMagnitude();
        this.c = function.get(0).velocity.getMagnitude();
        this.b = function.get(0).acceleration.getMagnitude();
        this.a = function.get(0).jerk.getMagnitude();

        this.dir = function.get(upperBound()).position.subtract(function.get(lowerBound()).position).normalize();
    }

    @Override
    public double getT(Vector pos) {
        return 0;
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
    public BoundedFunction<Derivatives<Vector>> offset(double offset) {
        return null;
    }
}
