package com.zypex.piflow.profile

import com.zypex.piflow.math.SingleBoundedFunction
import com.zypex.piflow.math.Vector
import com.zypex.piflow.math.rad
import kotlin.math.abs

typealias SingleProfile = SingleBoundedFunction<Derivatives<Vector>>

class Arc(function: SingleProfile, coeff: Double, speed: Double, diff: Double, dir: Int, val center: Vector) : SingleProfileSegment(function, Math.abs(diff) / coeff) {
    val radius: Double
    val dir: Int

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
        theta = if ((direction.theta - iTheta) + (fTheta - direction.theta) == fTheta - iTheta) {
            direction.theta.rad
        } else {
            val initialDifference: Double = abs((direction.theta - iTheta).rad)
            val finalDifference: Double = abs((fTheta - direction.theta).rad)
            if (initialDifference < finalDifference) iTheta.rad else fTheta.rad
        }


//        System.out.println("( " + iTheta + ", " + fTheta + ")");
        return (theta - iTheta.rad)/ (dir * coeff) + lowerBound()
    }
    private fun angleDiff(finalTheta: Double, initialTheta: Double): Double {
        val pi2 = 2 * Math.PI
        val iMod = (initialTheta % pi2 + pi2) % pi2
        val fMod = (finalTheta % pi2 + pi2) % pi2
        return (dir * (fMod - iMod) % pi2 + pi2) % pi2
    }
}