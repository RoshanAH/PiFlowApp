package com.zypex.piflow

import com.zypex.piflow.profile.*
import kotlin.math.pow

class ProfileFollower(var veloPIDF: PIDFConstants, var getPosition: () -> Double, var follow: (Double) -> Unit) {

    private var lastError = 0.0
    private var lastTime = System.nanoTime() * 1e-9
    private var deltaError = 0.0
    private var lastPos = 0.0
    private var pos = 0.0
    private var deltaTime = 0.0

    var profile: DoubleProfile = DoubleProfile()
        set(value) {
            segNum = 0
            error = 0.0
            totalError = 0.0
            field = value
        }

    var getVelocity: () -> Double = { (pos - lastPos) / deltaTime }

    private var segNum = 0

    private var error = 0.0
    private var totalError = 0.0

    fun update() {
        val time = System.nanoTime() * 1e-9
        pos = getPosition()
        val vel = getVelocity()

        deltaTime = time - lastTime

        val derivatives = profile(getT(pos))

        error = derivatives.velocity - vel
        deltaError = (error - lastError) / deltaTime
        totalError += error

        follow(veloPIDF.p * error + veloPIDF.i * totalError - veloPIDF.d * deltaError + veloPIDF.f * derivatives.acceleration)

        lastTime = time
        lastPos = pos
    }

    private fun getT(pos: Double): Double {
        if (pos < profile.functions[segNum].lower.position) return profile.functions[segNum].lowerBound()

        for (i in segNum..profile.functions.size) {
            val f = profile.functions[i]
            if (pos <= f.upper.position) {
                val lower = f.lower
                val a = lower.jerk / 6.0
                val b = lower.acceleration / 2.0
                val c = lower.velocity
                val d = lower.position

                return newtonMethodSolve(
                    pos,
                    { a * it.pow(3.0) + b * it.pow(2.0) + c * it + d },
                    { 3.0 * a * it.pow(2.0) + 2.0 * b * it + c },
                    f.lowerBound(),
                    f.upperBound()
                )
            }

            segNum++
        }

        return profile.upperBound()
    }

    fun getTargetVel(pos: Double): Double = profile(getT(pos)).velocity
}