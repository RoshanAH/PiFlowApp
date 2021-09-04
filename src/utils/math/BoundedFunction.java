package utils.math;

public interface BoundedFunction<T> extends Function<T> {
    double upperBound();
    double lowerBound();
    BoundedFunction<T> offset(double offset);
}
