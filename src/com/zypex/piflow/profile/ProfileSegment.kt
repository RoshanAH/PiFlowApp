package com.zypex.piflow.profile

import kotlin.Throws
import java.lang.ClassCastException
import utils.math.*

abstract class ProfileSegment : BoundedFunction<Derivatives<Vector>> {
    abstract val length: Double
    fun getT(x: Double, y: Double): Double {
        return getT(Vector(x, y))
    }

    abstract override fun offset(offset: Double): ProfileSegment
    abstract fun getT(pos: Vector): Double
}