package com.zypex.piflow

class DriveConfig(
    maxVelocity: Double,
    maxAcceleration: Double,
    maxJerk: Double,
    val pidf: PIDFConstants,
    val turnPIDF: PIDFConstants
) : MotionConstraints(maxVelocity, maxAcceleration, maxJerk)