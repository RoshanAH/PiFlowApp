package com.zypex.piflow.profile;

import utils.math.BoundedFunction;
import utils.math.Function;
import utils.math.PiecewiseFunction;
import utils.math.Vector;

import java.util.ArrayList;
import java.util.List;

public class Linear extends ProfileSegment {

    Linear(Function<Derivatives<Vector>> function, double lower, double upper, double length) {

        super(function, lower, upper, length);

    }

    @Override
    public double getT(Vector pos) {
        return 0;
    }
}
