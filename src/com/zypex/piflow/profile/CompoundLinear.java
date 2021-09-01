package com.zypex.piflow.profile;

import utils.math.BoundedFunction;
import utils.math.Function;
import utils.math.PiecewiseFunction;
import utils.math.Vector;

import java.util.ArrayList;
import java.util.List;

public class CompoundLinear extends ProfileSegment {

    List<Linear> segments;

    CompoundLinear(List<Linear> segments, Function<Derivatives<Vector>> function, double lower, double upper, double length) {
        super(function, lower, upper, length);
        if(segments.size() < 2) throw new IllegalArgumentException("Cannot create CompoundLinear of length " + segments.size());
        this.segments = segments;
    }

    @Override
    public double getT(Vector pos) {
        final double t;

        for(Linear l : segments){

        }

        return 0;
    }
}
