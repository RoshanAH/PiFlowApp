package com.zypex.piflow.profile

import com.zypex.piflow.math.BoundedFunction
import com.zypex.piflow.math.Vector

abstract class ProfileSegment : BoundedFunction<Derivatives<Vector>> {
    abstract val length: Double
    fun getT(x: Double, y: Double): Double {
        return getT(Vector(x, y))
    }

    abstract override fun offset(offset: Double): ProfileSegment
    abstract fun getT(pos: Vector): Double
}