package utils.math;

import java.util.ArrayList;
import java.util.List;

public class PiecewiseFunction<T> implements Function<T> {

    public final List<BoundedFunction<T>> functions = new ArrayList<>();

    public void addFunction(Function<T> f, double min, double max) {
        if (f instanceof BoundedFunction)
            addFunction((BoundedFunction<T>) f);
        else
            functions.add(new BoundedFunction<T>(f, min, max));
    }

    public void addFunction(BoundedFunction<T> f) {
        functions.add(f);
    }

    public void addFunction(PiecewiseFunction<T> f){
        for(BoundedFunction<T> function : f.functions)
            addFunction(function);
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
        addFunction((t) -> f.function.get(t - offset), f.lowerBound - offset, f.upperBound - offset);
    }

    public void appendFunction(PiecewiseFunction<T> f){

        if(!functions.isEmpty()){
            double offset = upperBound();
            for(BoundedFunction<T> function : f.functions)
                addFunction(t -> function.function.get(t - offset), function.lowerBound - offset, function.upperBound - offset);
        }else{
            for(BoundedFunction<T> function : f.functions)
                addFunction(function);
        }
    }

    public double lowerBound() {

        if (functions.isEmpty()) return Double.NEGATIVE_INFINITY;

        double bound = functions.get(0).lowerBound;
        for (BoundedFunction<T> f : functions) {
            if (f.lowerBound < bound)
                bound = f.lowerBound;
        }

        return bound;
    }

    public double upperBound() {

        if (functions.isEmpty()) return Double.POSITIVE_INFINITY;

        double bound = functions.get(0).upperBound;
        for (BoundedFunction<T> f : functions) {
            if (f.upperBound > bound)
                bound = f.upperBound;
        }

        return bound;
    }

    public BoundedFunction<T> toBounded() {
        return new BoundedFunction<>(this, lowerBound(), upperBound());
    }

    //    Prioritize the most recently added functions if they overlap
    @Override
    public T get(Double input) {
        for (int i = functions.size() - 1; i >= 0; i--) {
            T output = functions.get(i).get(input);
            if (output != null) return output;
        }

        return null;
    }
}
