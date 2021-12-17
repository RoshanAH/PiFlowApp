package com.zypex.piflow.path

import utils.math.Vector

class WayPoint(x: Double, y: Double) : PathElement {
    var speed: Double? = null
    var heading: Double? = null
    var rotation: Double? = null
    var position: Vector? = null

    constructor(pos: Vector) : this(pos.x, pos.y)
}