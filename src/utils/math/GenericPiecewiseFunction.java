package utils.math;

import java.util.ArrayList;
import java.util.List;

public class GenericPiecewiseFunction<T, R extends BoundedFunction<T>> implements BoundedFunction<T>{

    public final List<R> functions = new ArrayList<>();

    @SafeVarargs
    public GenericPiecewiseFunction(R... functions){
        for(R f : functions) addFunction(f);
    }

    public GenericPiecewiseFunction(){}

    public void addFunction(R f) {
        if (f instanceof GenericPiecewiseFunction) {
            GenericPiecewiseFunction<T, R> piecewise = (GenericPiecewiseFunction<T, R>) f;
            piecewise.functions.forEach(this::addFunction);
        } else {
            functions.add(f);
        }
    }

    public void appendFunction(R f) {

        if(functions.isEmpty()) {
            addFunction(f);
            return;
        }

        final double offset = upperBound();
        addFunction((R) f.offset(offset));
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
    public GenericPiecewiseFunction<T, R> offset(double offset) {
        GenericPiecewiseFunction<T, R> out = new GenericPiecewiseFunction<>();
        functions.forEach(f -> out.addFunction(f.offset(offset)));
        return out;
    }

    @SafeVarargs
    public static <T, R extends BoundedFunction<T>> GenericPiecewiseFunction<T, R> genericCreateAppended(R... functions){
        GenericPiecewiseFunction<T, R> out = new GenericPiecewiseFunction<>();
        for(R f : functions) out.appendFunction(f);
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
