package com.zypex.piflow.path

import utils.math.Vector

class WayPoint() : PathElement {
    var speed: Double? = null
    var heading: Double? = null
    var rotation: Double? = null
    var velocity: Vector? = null

    private var _t: Double? = null
    var t: Double?
        get() = _t
        internal set(d){
            _t = d
        }

    val initialized: Boolean
        get() = t == null

    var isStopPoint: Boolean = false


}