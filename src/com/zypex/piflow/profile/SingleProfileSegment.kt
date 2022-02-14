package com.zypex.piflow.profile

import com.zypex.piflow.math.SingleBoundedFunction
import com.zypex.piflow.math.Vector


abstract class SingleProfileSegment(val function: SingleBoundedFunction<Derivatives<Vector>>, override val length: Double) : ProfileSegment() {
    override fun upperBound(): Double {
        return function.upperBound()
    }

    override fun lowerBound(): Double {
        return function.lowerBound()
    }

    override fun invoke(input: Double): Derivatives<Vector> {
        return function(input)
    }
}