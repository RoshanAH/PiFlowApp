package com.zypex.piflow.profile;

import utils.math.BoundedFunction;
import utils.math.Function;
import utils.math.Vector;

public abstract class ProfileSegment implements BoundedFunction<Derivatives<Vector>> {

    public final double length;
    public final BoundedFunction<Derivatives<Vector>> function;

    public ProfileSegment(BoundedFunction<Derivatives<Vector>> function, double length){
        this.function = function;
        this.length = length;
    }

    public double getT(double x, double y){
        return getT(new Vector(x,y));
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

    public abstract double getT(Vector pos);

}
