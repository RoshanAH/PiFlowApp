package utils.math;

import java.util.List;

public interface BoundedFunction<T> extends Function<T> {
    double upperBound();
    double lowerBound();

    default T getUpper(){
        return get(upperBound());
    }
    default T getLower(){
        return get(lowerBound());
    }

    BoundedFunction<T> offset(double offset) throws ClassCastException;
}
