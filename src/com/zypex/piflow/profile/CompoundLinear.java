package com.zypex.piflow.profile;

import utils.math.BoundedFunction;
import utils.math.Function;
import utils.math.PiecewiseFunction;
import utils.math.Vector;

import java.util.ArrayList;
import java.util.List;

public class CompoundLinear extends ProfileSegment {

    public final PiecewiseFunction<Derivatives<Vector>> function;

    CompoundLinear(PiecewiseFunction<Derivatives<Vector>> function, double length) {
        super(function, length);
        this.function = function;
    }

    @Override
    public double getT(Vector pos) {
        final double t;

//        for(Linear l : function.functions){
//
//        }

        return 0;
    }

    @Override
    public BoundedFunction<Derivatives<Vector>> offset(double offset) {
        PiecewiseFunction<Derivatives<Vector>> outFunction = new PiecewiseFunction<>();
        function.functions.forEach(f -> outFunction.addFunction(f.offset(offset)));
        return new CompoundLinear(outFunction, length);
    }
}
