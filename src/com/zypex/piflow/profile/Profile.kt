package com.zypex.piflow.profile

import utils.math.PiecewiseFunction
import utils.math.Vector

class Profile(val profile: PiecewiseFunction<Derivatives<Vector>>, val headingProfile: PiecewiseFunction<Derivatives<Double>>) {
    fun getPos(t: Double): Derivatives<Vector> = profile(t)
}