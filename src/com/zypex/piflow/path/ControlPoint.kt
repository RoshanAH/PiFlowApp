package piflow.path

import com.zypex.piflow.math.Vector

class ControlPoint(pos: Vector) : PathElement {
    constructor(x: Double, y: Double) : this(Vector(x, y))
}