package com.zypex.piflow.profile;

import utils.math.BoundedFunction;
import utils.math.Function;
import utils.math.Vector;

public abstract class ProfileSegment extends BoundedFunction<Derivatives<Vector>> {

    final double length;

    ProfileSegment(Function<Derivatives<Vector>> function, double lower, double upper, double length) {
        super(function, lower, upper);

        this.length = length;
    }

    public double getT(double x, double y){
        return getT(new Vector(x,y ));
    }

    public abstract double getT(Vector pos);

}
