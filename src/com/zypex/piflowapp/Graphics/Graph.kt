package com.zypex.piflowapp.Graphics

import javafx.scene.canvas.GraphicsContext

import javafx.scene.paint.Color
import com.zypex.piflow.math.*
import java.util.ArrayList

class Graph {
    private val datapoints: MutableList<Vector> = ArrayList()
    var color = Color.RED
    var dataMode = DataMode.LINEAR
    var sizeMode = SizeMode.RELATIVE
    fun drawSection(gc: GraphicsContext) {
        val w = gc.canvas.width
        val h = gc.canvas.height
    }

    fun addDatapoint(x: Double, y: Double) {
        for (i in datapoints.indices) {
            val point = datapoints[i]
            if (point.x < x) {
                datapoints.add(i, Vector(x, y))
            }
        }
    }

    fun addDataPoint(point: Vector) {
        addDatapoint(point.x, point.y)
    }

    enum class DataMode {
        SCATTER, LINEAR, SQUARE
    }

    enum class SizeMode {
        ABSOLUTE, RELATIVE
    }
}