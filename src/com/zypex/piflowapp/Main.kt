package com.zypex.piflowapp

import com.zypex.piflow.DriveConfig
import com.zypex.piflow.profile.*
import com.zypex.piflowapp.Graphics.FunctionRenderer
import com.zypex.piflowapp.Graphics.RenderedFunction
import com.zypex.piflowapp.Input.Mouse
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap
import javafx.stage.Stage
import javafx.util.Duration
import utils.math.BoundedFunction
import utils.math.SingleBoundedFunction
import utils.math.Vector
import kotlin.math.pow

class Main : Application() {
    private var gc: GraphicsContext? = null
    private val initialHeight = 600.0
    private val initialWidth = 600.0
    private val config = DriveConfig(1.0, 10.0, 1.0)

    override fun start(primaryStage: Stage) {
        primaryStage.title = "wait does this actually work?"
        val canvas = Canvas(initialWidth, initialHeight)
        val root = Group(canvas)
        gc = canvas.graphicsContext2D
        val mainLoop = Timeline()
        mainLoop.cycleCount = Timeline.INDEFINITE
        val kf = KeyFrame(Duration.seconds(1.0 / 60), { gc ?: onUpdate() })

        canvas.onMousePressed = EventHandler {
            if (it.isPrimaryButtonDown) Mouse.leftButton = true
            if (it.isSecondaryButtonDown) Mouse.rightButton = true
        }
        canvas.onMouseReleased = EventHandler {
            if (it.isPrimaryButtonDown) Mouse.leftButton = false
            if (it.isSecondaryButtonDown) Mouse.rightButton = false
        }
        canvas.onMouseMoved = EventHandler { e: MouseEvent ->
            Mouse.position.x = e.x
            Mouse.position.y = e.y
        }
        canvas.onMouseDragged = EventHandler { e: MouseEvent ->
            Mouse.position.x = e.x
            Mouse.position.y = e.y
        }
        canvas.onMouseClicked = EventHandler { e: MouseEvent ->
            when (e.button) {
                MouseButton.PRIMARY -> Mouse.callLeftClick()
                MouseButton.SECONDARY -> Mouse.callRightClick()
            }
        }
        mainLoop.keyFrames.add(kf)
        mainLoop.play()
        primaryStage.scene = Scene(root, initialWidth, initialHeight)
        primaryStage.show()
    }

    private var renderer = FunctionRenderer(0.0, 0.0, 600.0, 600.0)
    private var profile: BoundedFunction<Derivatives<Double>> =
        SingleBoundedFunction({ Derivatives(0.0, 0.0, 0.0, 0.0) }, 0.0, 0.0)
    private var points: MutableList<Vector> = ArrayList()
    private var startTime: Double = System.nanoTime() / 1.0e9 + 3.0
    private var runTime: Double = 0.0

    var arcs: List<Arc> = ArrayList()


    override fun init() {
        renderer.minX = -1.0
        renderer.maxX = 14.0
        renderer.minY = -5.0
        renderer.maxY = 15.0
        renderer.resolution = 1.0


        linearInit()
//        interpolationInit()
    }

    private fun interpolationInit() {
        points.add(Vector(-5.0, -14.0))
        points.add(Vector.Polar(10.0, 0.1 * Math.PI) + points[points.size - 1])
        points.add(Vector.Polar(10.0, 0.9 * Math.PI) + points[points.size - 1])
        points.add(Vector.Polar(10.0, 0.2 * Math.PI) + points[points.size - 1])
        points.add(Vector.Polar(10.0, 1 * Math.PI) + points[points.size - 1])
        points.add(Vector.Polar(5.0, 0.3 * Math.PI) + points[points.size - 1])
        points.add(Vector.Polar(5.0, 0.9 * Math.PI) + points[points.size - 1])
        points.add(Vector.Polar(10.0, 0.1 * Math.PI) + points[points.size - 1])
        for (i in 1 until points.size - 1) {
            val start = points[i - 1]
            val middle = points[i]
            val end = points[i + 1]
            val turn = createTurn(start, middle, end, config, config.maxVelocity)
            //            renderer.functions.add(new RenderedFunction(turn.lowerBound, turn.upperBound)
//                    .attachX(t -> turn.get(t).position.x)
//                    .attachY(t -> turn.get(t).position.y)
//                    .setColor(Color.BLUE)
//                    .setSize(2)
//            );
        }
        arcs = createInterpolation(points, config, config.maxVelocity)
        for (arc in arcs) {
            renderer.functions.add(RenderedFunction(arc.lowerBound(), arc.upperBound())
                .attachX { t: Double -> arc(t).position.x }
                .attachY { t: Double -> arc(t).position.y }
                .setSize(2.0)
                .setColor(Color.GREEN)
            )
        }
    }

    private fun onUpdate() {
        val gc: GraphicsContext = gc ?: return
        gc.clearRect(0.0, 0.0, gc.canvas.width, gc.canvas.height)
        gc.lineCap = StrokeLineCap.ROUND

        runTime = System.nanoTime() / 1.0e9 - startTime


//        interpolation()
        linear()
    }

    private fun linearInit() {

        val displacement = 10.0

        val start = System.nanoTime()
        profile = createDisplacement(displacement, 0.0, 0.0, 50.0, config)
        val end = System.nanoTime()

        println("Profile generation finished in " + ((end - start) / 1.0e6) + " milliseconds")

        renderer.functions.add(RenderedFunction(profile.lowerBound(), profile.upperBound())
            .attachX { it }
            .attachY { profile(it).velocity }
//            .attachG { (profile(it).jerk / 2).coerceIn(0.0..1.0)}
//            .attachR { (-profile(it).jerk / 2).coerceIn(0.0..1.0)}
//            .attachSize { (abs(profile(it).acceleration)).coerceIn(1.0..5.0) }
            .setColor(Color.BLACK)
            .setSize(3.0)
        )

        renderer.functions.add(RenderedFunction(profile.lowerBound(), profile.upperBound())
            .attachX { it }
            .attachY { profile(it).acceleration }
            .setColor(Color.RED)
            .setSize(3.0)
        )

        renderer.functions.add(RenderedFunction(profile.lowerBound(), profile.upperBound())
            .attachX { it }
            .attachY { profile(it).jerk }
            .setColor(Color.BLUE)
            .setSize(3.0)
        )

        renderer.functions.add(RenderedFunction(profile.lowerBound(), profile.upperBound())
            .attachX { it }
            .attachY { profile(it).position }
            .setColor(Color.GREEN)
            .setSize(3.0)
        )

        renderer.functions.add(RenderedFunction(profile.lowerBound(), profile.upperBound())
            .attachX { it }
            .setY(displacement)
            .setColor(Color.GREY)
            .setSize(3.0)
        )
    }

    //    Linear linearProfile = ProfileBuilder.
    private fun linear() {
        val gc: GraphicsContext = gc ?: return

        renderer.render(gc)

        gc.fill = Color.BLUE


        renderer.renderInFrame({
            gc.fillRect(14.5, 0.5 + profile.bounded(runTime).position, 0.5, 0.5)
        }, gc)


    }

    private fun interpolation() {
        val gc: GraphicsContext = gc ?: return

        val mousePos = renderer.toFrame(Mouse.position)
        val closest = arcs[0](arcs[0].getT(mousePos)).position
        renderer.renderInFrame({ gc: GraphicsContext ->
            gc.lineWidth = 0.1
            gc.beginPath()
            gc.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) gc.lineTo(points[i].x, points[i].y)
            gc.stroke()
            gc.closePath()
            gc.fill = Color.BLACK
//                        for(Arc a : arcs) gc.fillOval(a.center.x - 0.2, a.center.y - 0.2, 0.4, 0.4);
            gc.lineWidth = 0.3
            gc.fill = Color.RED
            //            gc.fillOval(mousePos.x - 0.2, mousePos.y - 0.2, 0.4, 0.4);
            gc.fillOval(closest.x - 0.2, closest.y - 0.2, 0.4, 0.4)
        }, gc)

        renderer.render(gc)
    }

}

fun main(args: Array<String>) {
    Application.launch(*args)
}

