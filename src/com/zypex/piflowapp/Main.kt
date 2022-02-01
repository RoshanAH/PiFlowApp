package com.zypex.piflowapp


import com.zypex.piflow.DriveConfig
import com.zypex.piflow.PIDFConstants
import com.zypex.piflow.ProfileFollower
import com.zypex.piflow.profile.*
import com.zypex.piflowapp.Graphics.FunctionRenderer
import com.zypex.piflowapp.Graphics.RenderedFunction
import com.zypex.piflowapp.Input.*
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.shape.StrokeLineCap
import javafx.stage.Stage
import javafx.util.Duration
import utils.math.SingleBoundedFunction
import utils.math.Vector

class Main : Application() {
    private lateinit var gc: GraphicsContext
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
        val kf = KeyFrame(Duration.seconds(1.0 / 60), { onUpdate() })

        configure(canvas)

        mainLoop.keyFrames.add(kf)
        mainLoop.play()
        primaryStage.scene = Scene(root, initialWidth, initialHeight)
        primaryStage.show()
    }

    private var renderer = FunctionRenderer(0.0, 0.0, 600.0, 600.0)
    private var profile: DoubleProfile =
        DoubleProfile(
            SingleBoundedFunction({ Derivatives(0.0, 0.0, 0.0, 0.0) }, 0.0, 0.0)
        )
    private var points: MutableList<Vector> = mutableListOf()
    private var startTime: Double = System.nanoTime() / 1.0e9 + 3.0
    private var runTime: Double = 0.0

    private var follower: ProfileFollower = ProfileFollower(
        PIDFConstants(0.1, 0.0, 0.0, 0.0),
        {
            renderer.toFrame(mouse).y
        },
        { println(getT()) }
    )

    private var getT = { follower.t }

    private var arcs: List<Arc> = ArrayList()


    override fun init() {
        renderer.minX = -1.0
        renderer.maxX = 24.0
        renderer.minY = -5.0
        renderer.maxY = 20.0
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

        follower.profile = profile

        keyboard.addPressEvent(KeyCode.SPACE) {
            follower.profile = profile
        }

        renderer.functions.add(RenderedFunction(profile.lowerBound(), profile.upperBound())
            .attachX { it }
            .attachY { profile(it).velocity }
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
        val gc: GraphicsContext = gc

        renderer.render(gc)

        gc.stroke = Color.BLUE
        gc.lineWidth = 0.3

        follower.update()

        renderer.renderInFrame(gc) {
            val mousePos = renderer.toFrame(mouse)
            val profilePos = Vector(follower.t, follower.profile(follower.t).position)
            gc.strokeLine(profilePos.x, profilePos.y, profilePos.x, profilePos.y)
            gc.strokeLine(mousePos.x, mousePos.y, mousePos.x, mousePos.y)

            gc.strokeLine(0.0, 0.0, 0.0, 0.0)
        }
    }

    private fun interpolation() {
        val gc: GraphicsContext = gc

        val mousePos = renderer.toFrame(mouse)
        val closest = arcs[0](arcs[0].getT(mousePos)).position

//        renderer.renderInFrame({ gc: GraphicsContext ->
//            gc.lineWidth = 0.1
//            gc.beginPath()
//            gc.moveTo(points[0].x, points[0].y)
//            for (i in 1 until points.size) gc.lineTo(points[i].x, points[i].y)
//            gc.stroke()
//            gc.closePath()
//            gc.fill = Color.BLACK
////                        for(Arc a : arcs) gc.fillOval(a.center.x - 0.2, a.center.y - 0.2, 0.4, 0.4);
//            gc.lineWidth = 0.3
//            gc.fill = Color.RED
//            //            gc.fillOval(mousePos.x - 0.2, mousePos.y - 0.2, 0.4, 0.4);
//            gc.fillOval(closest.x - 0.2, closest.y - 0.2, 0.4, 0.4)
//        }, gc)

        renderer.render(gc)
    }

}

fun main(args: Array<String>) {
    Application.launch(*args)
}

