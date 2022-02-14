package com.zypex.piflow

import com.zypex.piflow.profile.Derivatives
import com.zypex.piflow.profile.DoubleProfile

class ProfileFollower(var pidf: PIDFConstants, var getPosition: () -> Double, var follow: (Double) -> Unit) {

    private var lastError = 0.0
    private var lastTime = System.nanoTime() * 1e-9
    private var startTime = lastTime

    private var deltaError = 0.0
    private var lastPos = 0.0
    private var pos = 0.0
    private var deltaTime = 0.0

    var targetPosition: Derivatives<Double> = Derivatives(0.0, 0.0, 0.0, 0.0)
        private set
    var t = 0.0
        private set

    var profile: DoubleProfile = DoubleProfile()
        set(value) {
            startTime = System.nanoTime() * 1e-9
//            segNum = 0
            error = 0.0
            totalError = 0.0
            deltaError = 0.0
            field = value
        }

    private var error = 0.0
    private var totalError = 0.0

    fun update() {
        val time = System.nanoTime() * 1e-9
        t = time - startTime
        deltaTime = time - lastTime

        pos = getPosition()

        targetPosition = profile.bounded(t)

        error = targetPosition.position - pos
        deltaError = (error - lastError) / deltaTime
        totalError += error

        val pidPower = pidf.p * error + pidf.i * totalError + pidf.d * deltaError
        val ffPower = pidf.fV * targetPosition.velocity + pidf.fA * targetPosition.acceleration + pidf.fJ * targetPosition.jerk

        follow(pidPower + ffPower)

        lastTime = time
        lastPos = pos
    }

//    fun getT(pos: Double): Double {
//        if (profile.functions.isEmpty()) return 0.0
//        if (pos < profile.lower.position) return profile.lowerBound()
//
//        var lowest = profile.lowerBound()
//
//        for (i in 0 until profile.functions.size) {
//            val f = profile.functions[i]
//            if (pos <= f.upper.position) {
//                val lower = f.lower
//                val lBound = f.lowerBound()
//                val j = lower.jerk
//                val a = lower.acceleration
//                val v = lower.velocity
//                val p = lower.position
//
//                val function: (Double) -> Double = { j / 6.0 * (it - lBound).pow(3.0) + a / 2.0 * (it - lBound).pow(2.0) + v * (it - lBound) + p }
//                val derivative: (Double) -> Double = { j / 2.0 * (it - lBound).pow(2.0) + a * (it - lBound) + v }
//
//                val t = newtonMethodSolve(
//                    pos,
//                    function,
//                    derivative,
//                    f.lowerBound(),
//                    f.upperBound(),
//                    initialGuess = t.coerceAtLeast(1e-5)
//                )
//
//                if(t)
//            }
//
//            if (segNum < profile.functions.size - 1) segNum++
//        }
//        return profile.upperBound()
//    }

    fun reset(){
        profile = profile
    }
}