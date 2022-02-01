package com.zypex.piflow

import com.zypex.piflow.profile.*
import utils.math.SingleBoundedFunction
import kotlin.math.pow

class ProfileFollower(var veloPIDF: PIDFConstants, var getPosition: () -> Double, var follow: (Double) -> Unit) {

    private var lastError = 0.0
    private var lastTime = System.nanoTime() * 1e-9
    private var deltaError = 0.0
    private var lastPos = 0.0
    private var pos = 0.0
    private var deltaTime = 0.0

    var t = 0.0
        private set

    var profile: DoubleProfile = DoubleProfile()
        set(value) {
            segNum = 0
            error = 0.0
            totalError = 0.0
            field = value
        }

    var getVelocity: () -> Double = { (pos - lastPos) / deltaTime }

    var segNum = 0
        private set

    private var error = 0.0
    private var totalError = 0.0

    fun update() {
        val time = System.nanoTime() * 1e-9
        deltaTime = time - lastTime


        pos = getPosition()
        val vel = getVelocity()

        t = getT(pos)

        val derivatives = profile(t)

        error = derivatives.velocity - vel
        deltaError = (error - lastError) / deltaTime
        totalError += error

        follow(veloPIDF.p * error + veloPIDF.i * totalError - veloPIDF.d * deltaError + veloPIDF.f * derivatives.acceleration)

        lastTime = time
        lastPos = pos
    }

    private fun getT(pos: Double): Double {
        if (profile.functions.isEmpty()) return 0.0
        if (pos < profile.functions[segNum].lower.position) return profile.functions[segNum].lowerBound()

        for (i in segNum until profile.functions.size) {
            val f = profile.functions[i]
            if (pos <= f.upper.position) {
                val lower = f.lower
                val lBound = f.lowerBound()
                val j = lower.jerk
                val a = lower.acceleration
                val v = lower.velocity
                val p = lower.position

                val function: (Double) -> Double = { j / 6.0 * (it - lBound).pow(3.0) + a / 2.0 * (it - lBound).pow(2.0) + v * (it - lBound) + p }
                val derivative: (Double) -> Double = { j / 2.0 * (it - lBound).pow(2.0) + a * (it - lBound) + v }

                return newtonMethodSolve(
                    pos,
                    function,
                    derivative,
                    f.lowerBound(),
                    f.upperBound(),
                    initialGuess = t.coerceAtLeast(1e-5)
                )
            }

            if (segNum < profile.functions.size - 1) segNum++
        }

        return profile.upperBound()
    }
}