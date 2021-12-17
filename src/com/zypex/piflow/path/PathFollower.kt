package com.zypex.piflow.path

import com.zypex.piflow.DrivePowers
import com.zypex.piflow.DrivetrainConfig
import com.zypex.piflow.profile.Profile
import com.zypex.piflow.profile.ProfileBuilder

class PathFollower(private val config: DrivetrainConfig, private val path: Path) {
    private val profile: Profile?
    var pathSection = 0

    init {
        profile = ProfileBuilder(config, path).build()
    }

    fun getPowers(x: Double, y: Double): DrivePowers? {
        return null
    }
}