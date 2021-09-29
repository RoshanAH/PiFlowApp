package com.zypex.piflow.profile;

import utils.math.*;

public class Linear extends SingleProfileSegment {


//    For the format of ax^3 + bx^2 + cx + d
    private final double a;
    private final double b;
    private final double c;
    private final double d;
    private final Vector dir;


    Linear(SingleBoundedFunction<Derivatives<Vector>> function, double length) {
        super(function, length);

        this.d = function.get(0).position.getMagnitude();
        this.c = function.get(0).velocity.getMagnitude();
        this.b = function.get(0).acceleration.getMagnitude();
        this.a = function.get(0).jerk.getMagnitude();

        this.dir = function.get(upperBound()).position.subtract(function.get(lowerBound()).position).normalize();
    }

    Linear(SingleBoundedFunction<Derivatives<Vector>> function) {
        this(function, function.getUpper().position.subtract(function.getLower().position).getMagnitude());
    }

    @Override
    public double getT(Vector pos) {

        final Vector initial = function.get(0).position;
        final double lower = function.getLower().position.subtract(initial).dot(dir.dotInverse());
        final double upper = function.getUpper().position.subtract(initial).dot(dir.dotInverse());

        final double rawT = dir.dot(pos.subtract(initial));
        if (rawT <= lower) return lowerBound();
        else if(rawT >= upper) return upperBound();
        else return solve(rawT);
    }

//    Modified version of the newton's method
    private double solve(double output){
        final double initialGuess = (upperBound() + lowerBound()) / 2;

        double lastGuess;
        double guess = initialGuess;
        double error = -1;



        Function<Double> function = t -> a * t*t*t + b * t*t + c * t + d;
        Function<Double> derivative = t -> 3 * a * t*t + 2 * b * t + c;

        while(error < 0 || error > 1e-15){
            lastGuess = guess;
            guess = (output - function.get(guess)) / derivative.get(guess) + guess;
            error = Math.abs(guess - lastGuess);
        }

        return guess;
    }

    @Override
    public Derivatives<Vector> get(double input) {
        return function.get(input);
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
    public Linear offset(double offset) {
        return new Linear(function.offset(offset));
    }
}
