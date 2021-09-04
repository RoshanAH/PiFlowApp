package utils.math;

public class SingleBoundedFunction<T> implements BoundedFunction<T>{

    private final double upper;
    private final double lower;
    private final Function<T> function;

    public SingleBoundedFunction(Function<T> f, double lower, double upper) {
        this.function = f;
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public SingleBoundedFunction<T> offset(double offset) {
        return new SingleBoundedFunction<>(t -> function.get(t - offset), lower + offset, upper + offset);
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
        if(input >= lower && input <= upper) return function.get(input);
        throw new InputOutOfDomainException("Input " + input + " out of domain [" + lower + ", " + upper + "]");
    }


}
