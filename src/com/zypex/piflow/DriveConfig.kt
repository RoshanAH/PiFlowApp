package com.zypex.piflow

class DriveConfig(maxVelocity: Double, maxAcceleration: Double, maxJerk: Double) : MotionConstraints(maxVelocity, maxAcceleration, maxJerk) {
    var turnSpeed = 1.0
    var velocityPID: PIDFConstants = PIDFConstants(0.0, 0.0, 0.0, 0.0)
}