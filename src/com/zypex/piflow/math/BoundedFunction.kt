package com.zypex.piflow.math

interface BoundedFunction<T> : (Double) -> T {
    fun upperBound(): Double
    fun lowerBound(): Double

    val upper: T
        get() = this(upperBound())
    val lower: T
        get() = this(lowerBound())

    val bounds: ClosedFloatingPointRange<Double>
        get() = lowerBound()..upperBound()

    fun bounded(input: Double) : T = this(input.coerceIn(lowerBound()..upperBound()))

    fun offset(offset: Double): BoundedFunction<T>
}