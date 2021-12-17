package com.zypex.piflow

import com.zypex.piflow.PIDFConstants

class DrivetrainConfig(var maxVelocity: Double, var maxAcceleration: Double, var maxJerk: Double) {
    var turnSpeed = 1.0
    var velocityPID: PIDFConstants? = null
}