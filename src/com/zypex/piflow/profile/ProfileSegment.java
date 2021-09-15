package com.zypex.piflow.profile;

import utils.math.BoundedFunction;
import utils.math.Function;
import utils.math.Vector;

public abstract class ProfileSegment implements BoundedFunction<Derivatives<Vector>> {

    public abstract double getLength();

    public double getT(double x, double y){
        return getT(new Vector(x,y));
    }

    @Override
    public abstract ProfileSegment offset(double offset) throws ClassCastException;

    public abstract double getT(Vector pos);

}
