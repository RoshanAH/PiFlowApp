package com.zypex.piflow.profile

import utils.math.*
import kotlin.math.*

class Linear @JvmOverloads internal constructor(function: SingleBoundedFunction<Derivatives<Vector>>, length: Double = function.upper.position.subtract(function.lower.position).magnitude) : SingleProfileSegment(function, length) {
    //    For the format of ax^3 + bx^2 + cx + d
    private val a: Double
    private val b: Double
    private val c: Double
    private val d: Double
    private val dir: Vector

    init {
        d = function.lower.position.magnitude
        c = function.lower.velocity.magnitude
        b = function.lower.acceleration.magnitude / 2.0
        a = function.lower.jerk.magnitude / 6.0
        dir = function.upper.position.subtract(function.lower.position).normalize()
    }

    override fun getT(pos: Vector): Double {
        val initial = function(0.0).position
        val lower: Double = function.lower.position.subtract(initial).dot(dir.dotInverse())
        val upper: Double = function.upper.position.subtract(initial).dot(dir.dotInverse())
        val rawT = dir.dot(pos.subtract(initial))
        return if (rawT <= lower) lowerBound() else if (rawT >= upper) upperBound() else solve(rawT)
    }

    //    Modified version of the newton's method
    private fun solve(output: Double): Double {
        val initialGuess = (upperBound() + lowerBound()) / 2
        var guess = initialGuess
        var error = -1.0
        val function: (Double) -> Double = {a * it.pow(3.0) + b * it.pow(2.0) + c * it + d }
        val derivative: (Double) -> Double = {3 * a * it * it + 2 * b * it + c }
        while (error < 0 || error > 1e-14) {
            val out = function(guess)
            guess += (output - out) / derivative(guess)
            error = abs(out - guess)
        }
        return guess
    }

    override fun invoke(input: Double): Derivatives<Vector> = function(input)

    override fun upperBound(): Double = function.upperBound()

    override fun lowerBound(): Double = function.lowerBound()

    override fun offset(offset: Double): Linear = Linear(function.offset(offset))
}