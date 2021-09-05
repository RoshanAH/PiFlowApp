package utils.math;

import java.util.ArrayList;
import java.util.List;

public class PiecewiseFunction<T> implements BoundedFunction<T>{

    public final List<BoundedFunction<T>> functions = new ArrayList<>();

    @SafeVarargs
    public PiecewiseFunction(BoundedFunction<T>... functions){
        for(BoundedFunction<T> f : functions) addFunction(f);
    }

    public PiecewiseFunction(){}


    public void addFunction(Function<T> f, double min, double max) {
        if (f instanceof BoundedFunction)
            addFunction((BoundedFunction<T>) f);
        else
            functions.add(new SingleBoundedFunction<>(f, min, max));
    }

    public void addFunction(BoundedFunction<T> f) {
        if(f instanceof PiecewiseFunction){
            PiecewiseFunction<T> piecewise = (PiecewiseFunction<T>) f;
            piecewise.functions.forEach(this::addFunction);
        }else{
            functions.add(f);
        }
    }

    public void appendFunction(Function<T> f, double min, double max) {
        if (f instanceof BoundedFunction) {
            appendFunction((BoundedFunction<T>) f);
        } else {
            if(functions.isEmpty()) {
                addFunction(f, min, max);
                return;
            }
            final double offset = upperBound();
            addFunction(t -> f.get(t - offset), min + offset, max + offset);
        }
    }

    public void appendFunction(BoundedFunction<T> f) {

        if(functions.isEmpty()) {
            addFunction(f);
            return;
        }

        final double offset = upperBound();
        addFunction(f.offset(offset));
    }

    public double lowerBound() {

        if (functions.isEmpty()) return Double.NEGATIVE_INFINITY;

        double bound = functions.get(0).lowerBound();
        for (BoundedFunction<T> f : functions) {
            if (f.lowerBound() < bound)
                bound = f.lowerBound();
        }

        return bound;
    }

    public double upperBound() {

        if (functions.isEmpty()) return Double.POSITIVE_INFINITY;

        double bound = functions.get(0).upperBound();
        for (BoundedFunction<T> f : functions) {
            if (f.upperBound() > bound)
                bound = f.upperBound();
        }

        return bound;
    }

    @Override
    public PiecewiseFunction<T> offset(double offset) {
        PiecewiseFunction<T> out = new PiecewiseFunction<>();
        functions.forEach(f -> out.addFunction(f.offset(offset)));
        return out;
    }

    @SafeVarargs
    public static <T> PiecewiseFunction<T> createAppended(BoundedFunction<T>... functions){
        PiecewiseFunction<T> out = new PiecewiseFunction<>();
        for(BoundedFunction<T> f : functions) out.appendFunction(f);
        return out;
    }

    //    Prioritize the most recently added functions if they overlap
    @Override
    public T get(double input) {
        for (int i = functions.size() - 1; i >= 0; i--) {
            BoundedFunction<T> f = functions.get(i);
            if(input >= f.lowerBound() && input <= f.upperBound()) return f.get(input);
        }

        throw new InputOutOfDomainException("Input " + input + " out of domain [" + lowerBound() + ", " + upperBound() + "]");
    }

}
