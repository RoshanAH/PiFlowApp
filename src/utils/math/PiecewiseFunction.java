package utils.math;

public class PiecewiseFunction<T> extends GenericPiecewiseFunction<T, BoundedFunction<T>>{

    @SafeVarargs
    public static <T> PiecewiseFunction<T> createAppended(BoundedFunction<T>... functions){
        PiecewiseFunction<T> out = new PiecewiseFunction<>();
        for(BoundedFunction<T> f : functions) out.appendFunction(f);
        return out;
    }

}
