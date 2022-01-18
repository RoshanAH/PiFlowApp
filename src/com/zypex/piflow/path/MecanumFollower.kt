package com.zypex.piflow.path

import com.zypex.piflow.DrivePowers
import com.zypex.piflow.DriveConfig
import com.zypex.piflow.profile.Profile

class MecanumFollower(val config: DriveConfig, val path: Path, val profile: Profile) {
    var pathSection = 0


    fun getPowers(x: Double, y: Double): DrivePowers = TODO()
}