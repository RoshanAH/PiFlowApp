package utils.math

import java.util.ArrayList

class PiecewiseFunction<T> : BoundedFunction<T> {

    val functions: MutableList<BoundedFunction<T>> = ArrayList()

    constructor(vararg functions: BoundedFunction<T>) {
        for (f in functions) addFunction(f)
    }

    constructor()

    fun addFunction(f: BoundedFunction<T>) {
        if (f is PiecewiseFunction<*>) {
            val piecewise = f as PiecewiseFunction<T>
            piecewise.functions.forEach { func: BoundedFunction<T> -> addFunction(func) }
        } else {
            functions.add(f)
        }
    }

    fun appendFunction(f: BoundedFunction<T>) {
        if (functions.isEmpty()) {
            addFunction(f)
            return
        }
        val offset = upperBound()
        addFunction(f.offset(offset))
    }

    override fun lowerBound(): Double {
        if (functions.isEmpty()) return Double.NEGATIVE_INFINITY
        var bound = functions[0].lowerBound()
        for (f in functions) {
            if (f.lowerBound() < bound) bound = f.lowerBound()
        }
        return bound
    }

    override fun upperBound(): Double {
        if (functions.isEmpty()) return Double.POSITIVE_INFINITY
        var bound = functions[0].upperBound()
        for (f in functions) {
            if (f.upperBound() > bound) bound = f.upperBound()
        }
        return bound
    }

    override fun offset(offset: Double): PiecewiseFunction<T> {
        val out = PiecewiseFunction<T>()
        functions.forEach{ f: BoundedFunction<T> -> out.addFunction(f.offset(offset)) }
        return out
    }

    //    Prioritize the most recently added functions if they overlap
    override fun invoke(input: Double): T{
        for (i in functions.indices.reversed()) {
            val f = functions[i]
            if (input >= f.lowerBound() && input <= f.upperBound()) return f(input)
        }
        throw InputOutOfDomainException("Input " + input + " out of domain [" + lowerBound() + ", " + upperBound() + "]")
    }
}

fun <T> createAppended(vararg functions: BoundedFunction<T>): PiecewiseFunction<T> {
    val out = PiecewiseFunction<T>()
    for (f in functions) out.appendFunction(f)
    return out
}