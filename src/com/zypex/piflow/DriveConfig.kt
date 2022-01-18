package com.zypex.piflow

class DriveConfig(var maxVelocity: Double, var maxAcceleration: Double, var maxJerk: Double) {
    var turnSpeed = 1.0
    var velocityPID: PIDFConstants? = null
}