package utils.math


class SingleBoundedFunction<T>(private val base: (Double) -> T,
                               private val lowerBound: Double,
                               private val upperBound: Double,
) : BoundedFunction<T>{

    override fun offset(offset: Double): SingleBoundedFunction<T> = SingleBoundedFunction({ t -> base(t - offset) }, lowerBound + offset, upperBound + offset)
//    override val upper: T
//        get() = base(upperBound)
//
//    override val lower: T
//        get() = base(lowerBound)

    override fun upperBound(): Double = upperBound
    override fun lowerBound(): Double = lowerBound

    override fun invoke(input: Double): T {
        if (input in lowerBound..upperBound) return base(input)
        throw InputOutOfDomainException("Input $input out of domain [$lowerBound, $upperBound]")
    }
}