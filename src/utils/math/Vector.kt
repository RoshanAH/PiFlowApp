package utils.math

class Vector(var x: Double, var y: Double) : Cloneable {
    public override fun clone(): Vector {
        return Vector(x, y)
    }

    val magnitude: Double
        get() = Math.sqrt(x * x + y * y)
    val theta: Double
        get() = Math.atan2(y, x)

    fun setMagnitude(r: Double): Vector {
        val clone = clone()
        val theta = theta
        clone.x = r * Math.cos(theta)
        clone.y = r * Math.sin(theta)
        return clone
    }

    fun setTheta(theta: Double): Vector {
        val clone = clone()
        val r = magnitude
        clone.x = r * Math.cos(theta)
        clone.y = r * Math.sin(theta)
        return clone
    }

    fun rotate(theta: Double): Vector {
        val clone = clone()
        clone.x = Math.cos(theta) * x - Math.sin(theta) * y
        clone.y = Math.sin(theta) * x + Math.cos(theta) * y
        return clone
    }

    fun add(other: Vector): Vector {
        val clone = clone()
        clone.x += other.x
        clone.y += other.y
        return clone
    }

    fun subtract(other: Vector): Vector {
        val clone = clone()
        clone.x -= other.x
        clone.y -= other.y
        return clone
    }

    fun translate(x: Double, y: Double): Vector {
        return add(Vector(x, y))
    }

    fun scale(scale: Double): Vector {
        val clone = clone()
        clone.x *= scale
        clone.y *= scale
        return clone
    }

    fun project(other: Vector): Vector {
        return scale(dot(other) / square())
    }

    fun dotInverse(): Vector {
        return Vector(1 / (2 * x), 1 / (2 * y))
    }

    fun normalize(): Vector {
        return scale(1 / magnitude)
    }

    fun dist(other: Vector): Double {
        return Math.sqrt(Math.pow(x - other.x, 2.0) + Math.pow(y - other.y, 2.0))
    }

    fun dot(other: Vector): Double {
        return x * other.x + y * other.y
    }

    fun square(): Double {
        return dot(this)
    }

    operator fun plus(other: Vector): Vector{
        return add(other)
    }

    operator fun minus(other: Vector): Vector{
        return subtract(other)
    }

    operator fun unaryPlus(): Vector{
        return clone()
    }

    operator fun unaryMinus(): Vector{
        return scale(-1.0)
    }

    operator fun times(scalar: Double) : Vector{
        return scale(scalar)
    }


    override fun toString(): String {
        return "($x, $y)"
    }

    companion object {
        fun Polar(r: Double, theta: Double): Vector {
            return Vector(Math.cos(theta) * r, Math.sin(theta) * r)
        }
    }
}