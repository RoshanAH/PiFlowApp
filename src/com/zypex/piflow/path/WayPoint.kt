package com.zypex.piflow.path

import com.zypex.piflow.math.Vector
import piflow.path.PathElement

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