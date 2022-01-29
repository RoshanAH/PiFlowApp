package com.zypex.piflow.path
import utils.math.*

class ControlPoint(pos: Vector) : PathElement{
    constructor(x: Double, y: Double) : this(Vector(x, y))
}