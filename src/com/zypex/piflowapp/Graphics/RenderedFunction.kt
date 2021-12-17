package com.zypex.piflowapp.Graphics

import javafx.scene.paint.Color

class RenderedFunction(var minT: Double, var maxT: Double) {
    //    All the available dimensions for the renderer
    var x: (Double) -> Double = { 0.0 }
    var y: (Double) -> Double = { 0.0 }
    var r: (Double) -> Double = { 0.0 }
    var g: (Double) -> Double = { 0.0 }
    var b: (Double) -> Double = { 0.0 }
    var size: (Double) -> Double = { 0.5 }

    //    Set the dimensions
    fun setX(x: Double): RenderedFunction {
        this.x = { x }
        return this
    }

    fun setY(y: Double): RenderedFunction {
        this.y = { y }
        return this
    }

    fun setR(r: Double): RenderedFunction {
        this.r = { r }
        return this
    }

    fun setG(g: Double): RenderedFunction {
        this.g = { g }
        return this
    }

    fun setB(b: Double): RenderedFunction {
        this.b = { b }
        return this
    }

    fun setColor(color: Color): RenderedFunction {
        r = { color.red }
        g = { color.green }
        b = { color.blue }
        return this
    }

    fun setSize(size: Double): RenderedFunction {
        this.size = { size }
        return this
    }

    fun attachX(x: (Double) -> Double): RenderedFunction {
        this.x = x
        return this
    }

    fun attachY(y: (Double) -> Double): RenderedFunction {
        this.y = y
        return this
    }

    fun attachR(r: (Double) -> Double): RenderedFunction {
        this.r = r
        return this
    }

    fun attachG(g: (Double) -> Double): RenderedFunction {
        this.g = g
        return this
    }

    fun attachB(b: (Double) -> Double): RenderedFunction {
        this.b = b
        return this
    }

    fun attachSize(size: (Double) -> Double): RenderedFunction {
        this.size = size
        return this
    }
}