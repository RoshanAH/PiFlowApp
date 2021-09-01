package utils.math;

public class BoundedFunction<T> implements Function<T> {

    public double lowerBound;
    public double upperBound;
    public final Function<T> function;
    public EndBehavior endBehavior = EndBehavior.NULL;
    public BoundedFunction(Function<T> function, double lower, double upper){
        upperBound = upper;
        lowerBound = lower;
        this.function = function;
    }

    @Override
    public T get(Double input) {
        if(input < lowerBound) return endBehavior == EndBehavior.NULL? null : function.get(lowerBound);
        if(input > upperBound) return endBehavior == EndBehavior.NULL? null : function.get(upperBound);

        return function.get(input);
    }


    public enum EndBehavior {
        NULL,
        CAP
    }
}
