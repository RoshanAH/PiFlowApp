package com.zypex.piflow.profile

import com.zypex.piflow.math.PiecewiseFunction
import com.zypex.piflow.math.Vector


class Profile(val profile: PiecewiseFunction<Derivatives<Vector>>, val headingProfile: PiecewiseFunction<Derivatives<Double>>) {
//    val elements: List<PathElement>
    fun getPos(t: Double): Derivatives<Vector> = profile(t)
}