package utils.math

interface BoundedFunction<T> : (Double) -> T {
    fun upperBound(): Double
    fun lowerBound(): Double

    val upper: T
        get() = this(upperBound())
    val lower: T
        get() = this(lowerBound())

    fun offset(offset: Double): BoundedFunction<T>
}