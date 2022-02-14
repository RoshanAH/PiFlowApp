package com.zypex.piflow.math

class Angle internal constructor(rad: Double) {
    private val theta: Double = (rad + Math.PI) % (2 * Math.PI) - Math.PI
    val rad: Double
        get() = theta
    val deg: Double
        get() = theta / Math.PI * 180
    val grad: Double
        get() = theta / Math.PI * 100

    operator fun plus(other: Angle): Angle{
        return Angle(theta + other.theta)
    }

    operator fun unaryMinus(): Angle{
        return Angle(-theta)
    }

    operator fun unaryPlus(): Angle{
        return this
    }

    operator fun minus(other: Angle): Angle {
        return Angle(theta - other.theta)
    }
}

val Double.rad : Angle
    get() = Angle(this)

val Double.deg : Angle
    get() = Angle(this / Math.PI * 180)

val Double.grad : Angle
    get() = Angle(this / Math.PI * 100)