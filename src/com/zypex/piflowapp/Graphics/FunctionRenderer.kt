package com.zypex.piflowapp.Graphics

import javafx.scene.canvas.GraphicsContext
import javafx.scene.shape.StrokeLineCap
import java.lang.NullPointerException
import javafx.scene.paint.Color
import com.zypex.piflow.math.*
import kotlin.math.pow
import kotlin.math.sqrt

class FunctionRenderer(var canvasX: Double, var canvasY: Double, var canvasW: Double, var canvasH: Double) {
    var resolution = 5.0 // In pixels
    var derivativePrecision = 0.001
    var functions: ArrayList<RenderedFunction> = ArrayList()
    var minX = 0.0
    var maxX = 0.0
    var minY = 0.0
    var maxY = 0.0

    fun render(gc: GraphicsContext) {
        gc.save()
        gc.translate(canvasX + canvasW / 2, canvasY + canvasH / 2)
        gc.scale(1.0, -1.0)
        gc.lineCap = StrokeLineCap.ROUND
        val xToCanvas = canvasW / (maxX - minX)
        val yToCanvas = canvasH / (maxY - minY)
        val graphCenter = Vector((minX + maxX) / 2, (minY + maxY) / 2)
        for (f in functions) {
            gc.beginPath()
            try {
                gc.moveTo((f.x(f.minT) - graphCenter.x) * xToCanvas, (f.y(f.minT) - graphCenter.y) * yToCanvas)
                var lastDerivative = 0.0
                var t = f.minT
                while (t <= f.maxT) {
                    val tOfX = f.x(t)
                    val tOfY = f.y(t)
                    val tOfR = f.r(t)
                    val tOfG = f.g(t)
                    val tOfB = f.b(t)
                    val tOfsize = f.size(t)

                    var pixelDistDerivative: Double
                    try {
                        val h = derivativePrecision
                        val xDerivative = (f.x(t + h) - tOfX) / h
                        val yDerivative = (f.y(t + h) - tOfY) / h
                        pixelDistDerivative = sqrt((xDerivative * xToCanvas).pow(2.0) + (yDerivative * yToCanvas).pow(2.0))
                        lastDerivative = pixelDistDerivative
                    } catch (e: InputOutOfDomainException) {
                        pixelDistDerivative = lastDerivative
                    }
                    gc.beginPath()
                    if (tOfX in minX..maxX && tOfY in minY..maxY) {
                        gc.stroke = Color.color(tOfR, tOfG, tOfB)
                        gc.lineWidth = tOfsize
                        gc.lineTo((tOfX - graphCenter.x) * xToCanvas, (tOfY - graphCenter.y) * yToCanvas)
                    } else {
                        gc.moveTo((tOfX - graphCenter.x) * xToCanvas, (tOfY - graphCenter.y) * yToCanvas)
                    }
                    gc.stroke()
                    gc.closePath()
                    t += resolution / pixelDistDerivative
                }
            } catch (ignored: NullPointerException) {
            }
        }
        gc.restore()
    }

    fun toCanvas(x: Double, y: Double): Vector {
        val xToCanvas = canvasW / (maxX - minX)
        val yToCanvas = canvasH / (maxY - minY)
        val graphCenter = Vector((minX + maxX) / 2, (minY + maxY) / 2)
//        return Vector((x - graphCenter.x) * xToCanvas + canvasX + canvasW / 2, (graphCenter.y - y) * yToCanvas + canvasY + canvasH / 2)
        return Vector((x - graphCenter.x) * xToCanvas + canvasX + canvasW / 2, (-y + graphCenter.y) * yToCanvas + canvasH / 2 + canvasY)
    }

    fun toFrame(x: Double, y: Double): Vector {
        val canvasToX = (maxX - minX) / canvasW
        val canvasToY = (maxY - minY) / canvasH
        val graphCenter = Vector((minX + maxX) / 2, (minY + maxY) / 2)
        return Vector((x - canvasX - canvasW / 2) * canvasToX + graphCenter.x, (canvasH / 2 + canvasY - y) * canvasToY + graphCenter.y)
    }

    fun toCanvas(v: Vector): Vector {
        return toCanvas(v.x, v.y)
    }

    fun toFrame(v: Vector): Vector {
        return toFrame(v.x, v.y)
    }

    fun renderInFrame(gc: GraphicsContext, render: (GraphicsContext) -> Unit) {
        gc.save()
        val xToCanvas = canvasW / (maxX - minX)
        val yToCanvas = canvasH / (maxY - minY)
        val graphCenter = Vector((minX + maxX) / 2, (minY + maxY) / 2)
        gc.translate(canvasX + canvasW / 2 - graphCenter.x * xToCanvas, canvasY + canvasH / 2 + graphCenter.y * yToCanvas)
        gc.scale(xToCanvas, -yToCanvas)
        render(gc)
        gc.restore()
    }
}