package com.zypex.piflow.profile

class ProfileOverconstrainedException(
    val dist: Double,
    val deltaVel: Double,
    val minDisplacement: Double,
    val minVeloChange: Double,
) : IllegalArgumentException("Distance $dist is too small to change $deltaVel in velocity. " +
"Distance must either be $minDisplacement or the change in velocity must be $minVeloChange")