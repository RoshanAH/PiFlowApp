package com.zypex.piflow.profile

import utils.math.*
import kotlin.math.abs

typealias SingleProfile = SingleBoundedFunction<Derivatives<Vector>>

class Arc(function: SingleProfile, coeff: Double, speed: Double, diff: Double, dir: Int, val center: Vector) : SingleProfileSegment(function, Math.abs(diff) / coeff) {
    val radius: Double
    val dir: Int

    //   TODO: Change these back to public
    private val coeff: Double
    private val speed: Double
    val diff: Double

    init {
        radius = speed / coeff
        this.dir = dir
        this.coeff = coeff
        this.speed = speed
        this.diff = diff
    }
    
    override fun offset(offset: Double): Arc {
        return Arc(function.offset(offset), coeff, speed, diff, dir, center)
    }

    override fun getT(pos: Vector): Double {
        val direction = pos.subtract(center).normalize()
        val theta: Double
        val iTheta = this(lowerBound()).position.subtract(center).theta
        val fTheta = this(upperBound()).position.subtract(center).theta
        theta = if (angleDiff(direction.theta, iTheta) + angleDiff(fTheta, direction.theta) == angleDiff(fTheta, iTheta)) {
            direction.theta
        } else {
            val initialDifference: Double = abs(findAngleDifference(direction.theta, iTheta))
            val finalDifference: Double = abs(findAngleDifference(fTheta, direction.theta))
            if (initialDifference < finalDifference) iTheta else fTheta
        }


//        System.out.println("( " + iTheta + ", " + fTheta + ")");
        return (theta - iTheta) / (dir * coeff) + lowerBound()
    }
    private fun angleDiff(finalTheta: Double, initialTheta: Double): Double {
        val pi2 = 2 * Math.PI
        val iMod = (initialTheta % pi2 + pi2) % pi2
        val fMod = (finalTheta % pi2 + pi2) % pi2
        return (dir * (fMod - iMod) % pi2 + pi2) % pi2
    }
}