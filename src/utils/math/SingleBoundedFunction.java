package utils.math;

import com.zypex.piflow.profile.Linear;

public class SingleBoundedFunction<T> implements BoundedFunction<T>{

    private final double upper;
    private final double lower;
    private final Function<T> base;

    public SingleBoundedFunction(Function<T> baseFunction, double lower, double upper) {
        this.base = baseFunction;
        this.lower = lower;

        this.upper = upper;
    }

//    @Override
//    public <R extends BoundedFunction<T>> R offset(double offset) {
//        return null;
//    }

    @Override
    public SingleBoundedFunction<T> offset(double offset) {
        return new SingleBoundedFunction<>(t -> base.get(t - offset), lower + offset, upper + offset);
    }

    @Override
    public double upperBound() {
        return upper;
    }

    @Override
    public double lowerBound() {
        return lower;
    }

    @Override
    public T get(double input) {
        if(input >= lower && input <= upper) return base.get(input);
        throw new InputOutOfDomainException("Input " + input + " out of domain [" + lower + ", " + upper + "]");
    }


}
