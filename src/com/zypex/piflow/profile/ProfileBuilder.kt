package com.zypex.piflow.profile

import com.zypex.piflow.DriveConfig
import com.zypex.piflow.MotionConstraints
import com.zypex.piflow.path.Path
import utils.math.*
import kotlin.math.*

typealias VecProfile = PiecewiseFunction<Derivatives<Vector>>
typealias DoubleProfile = PiecewiseFunction<Derivatives<Double>>

fun buildProfile(path: Path, config: DriveConfig): Profile = TODO()

internal class Solution(val initial: Vector, val slope: Vector, private val t: Double) : (Double) -> Vector {
    val point: Vector
        get() = slope.scale(t).add(initial)

    override fun toString(): String {
        return point.toString()
    }

    override fun invoke(t: Double): Vector {
        return initial.add(slope.scale(t))
    }
}


fun createLinear(
    start: Vector, dir: Vector, startSpeed: Double, endSpeed: Double, config: MotionConstraints
): CompoundProfileSegment {
    val out = CompoundProfileSegment()
    val magProfile = createVelocityChange(startSpeed, endSpeed, config)
    for (function in magProfile.functions) {
        out.add(createLinear(function as SingleBoundedFunction<Derivatives<Double>>, start, dir))
    }
    return out
}

fun createLinear(profile: SingleBoundedFunction<Derivatives<Double>>, start: Vector, dir: Vector): Linear {
    val function = SingleBoundedFunction({
        Derivatives(
            start.add(dir.normalize().scale(profile(it).position)),
            dir.normalize().scale(profile(it).velocity),
            dir.normalize().scale(profile(it).acceleration),
            dir.normalize().scale(profile(it).jerk)
        )
    }, profile.lowerBound(), profile.upperBound())
    return Linear(function)
}

fun createDisplacement(
    dist: Double,
    initialSpeed: Double,
    finalSpeed: Double,
    maxSpeed: Double,
    config: MotionConstraints
): DoubleProfile {
    val finalMaxSpeed = maxSpeed.coerceAtMost(config.maxVelocity)

    val out = DoubleProfile()

    val maxIntermediate: Double =
        createVelocityChange(initialSpeed, finalMaxSpeed, config).upper.position + createVelocityChange(
            finalMaxSpeed, finalSpeed, config
        ).upper.position

    if (maxIntermediate < dist) {
        val speedUp = createVelocityChange(initialSpeed, finalMaxSpeed, config)
        val relativeSlowDown = createVelocityChange(finalMaxSpeed, finalSpeed, config)
        val makeUpDist = dist - (speedUp.upper.position + relativeSlowDown.upper.position)

        val maintain =
            SingleBoundedFunction(
                {
                    Derivatives(
                        finalMaxSpeed * it + speedUp.upper.position, finalMaxSpeed, 0.0, 0.0
                    )
                }, 0.0, makeUpDist / finalMaxSpeed
            )

        out.appendFunction(speedUp)
        out.appendFunction(maintain)
        out.appendFunction(createVelocityChange(finalMaxSpeed, finalSpeed, config, maintain.upper.position))
    } else {
        val intermediateSpeed: Double
        val maxChange = 2 * config.maxAcceleration.pow(2) / config.maxJerk
        val lowestSpeed = min(abs(initialSpeed), abs(finalSpeed))
        val highestSpeed = max(abs(initialSpeed), abs(finalSpeed))

        val minDisplacement = createVelocityChange(initialSpeed, finalSpeed, config).upper.position

//        This is the max amount of distance that could be traveled with acceleration never constant
        val doubleAccelDisplacement =
            createVelocityChange(lowestSpeed, lowestSpeed + maxChange, config).upper.position +
                    createVelocityChange(lowestSpeed + maxChange, highestSpeed, config).upper.position
//        This is the max amount of distance that could be traveled with acceleration never constant at only one side of the curve
        val singleAccelDisplacement =
            createVelocityChange(lowestSpeed, highestSpeed + maxChange, config).upper.position + createVelocityChange(
                highestSpeed + maxChange,
                highestSpeed,
                config
            ).upper.position

        if (dist < minDisplacement) {
            throw ProfileOverconstrainedException("Distance $dist is too small to change from velocities $initialSpeed to $finalSpeed. Distance must either be $minDisplacement or the change in velocity must be lower") // TODO actually solve for the change in velocity here
        } else if (dist < doubleAccelDisplacement && lowestSpeed + maxChange > highestSpeed) {
            intermediateSpeed = newtonMethodSolve(
                dist, SingleBoundedFunction({
                    createVelocityChange(initialSpeed, it, config).upper.position +
                            createVelocityChange(it, finalSpeed, config).upper.position
                }, highestSpeed - lowestSpeed, lowestSpeed + maxChange)
            )
        } else if (dist < singleAccelDisplacement) {
            intermediateSpeed = newtonMethodSolve(
                dist, SingleBoundedFunction({
                    createVelocityChange(initialSpeed, it, config).upper.position +
                            createVelocityChange(it, finalSpeed, config).upper.position
                }, lowestSpeed + maxChange, highestSpeed + maxChange)
            )
        } else {
            intermediateSpeed = newtonMethodSolve(
                dist, SingleBoundedFunction({
                    createVelocityChange(initialSpeed, it, config).upper.position +
                            createVelocityChange(it, finalSpeed, config).upper.position
                }, highestSpeed + maxChange, config.maxVelocity)
            )
        }

        val speedUp = createVelocityChange(initialSpeed, intermediateSpeed, config)
        val slowDown = createVelocityChange(intermediateSpeed, finalSpeed, config, speedUp.upper.position)
        out.appendFunction(speedUp)
        out.appendFunction(slowDown)
    }
    return out
}

fun createDisplacement(
    start: Vector, end: Vector, initialSpeed: Double, finalSpeed: Double, maxSpeed: Double, config: MotionConstraints
): CompoundProfileSegment {
    TODO() /* Call the overloaded function, break it down, and reattach it along the line*/
}

fun createVelocityChange(
    initialVel: Double, finalVel: Double, config: MotionConstraints, initialPos: Double = 0.0
): PiecewiseFunction<Derivatives<Double>> {

    val deltaVel = finalVel - initialVel
    val maxAccel = sign(deltaVel) * sqrt(config.maxJerk * abs(deltaVel))
    return if (abs(maxAccel) <= config.maxAcceleration) {
        val accelerate: BoundedFunction<Derivatives<Double>> = SingleBoundedFunction({
            Derivatives(
                initialPos +
                        initialVel * it +
                        config.maxJerk * it.pow(3.0) / 6 * sign(deltaVel),

                initialVel +
                        config.maxJerk * it.pow(2.0) / 2 * sign(deltaVel),  // Increasing position

                config.maxJerk * it * sign(deltaVel), // Increasing vel

                config.maxJerk * sign(deltaVel) // Increasing accel
            )
        }, 0.0, abs(maxAccel) / config.maxJerk)
        val endPoint1 = accelerate.upper
        val decelerate: BoundedFunction<Derivatives<Double>> = SingleBoundedFunction({
            Derivatives(
                endPoint1.position +
                        endPoint1.velocity * it +
                        endPoint1.acceleration * it.pow(2.0) / 2 +
                        config.maxJerk * it.pow(3.0) / 6 * sign(-deltaVel),

                endPoint1.velocity +
                        endPoint1.acceleration * it +
                        config.maxJerk * it.pow(2.0) / 2 * sign(-deltaVel),  // Increasing position

                endPoint1.acceleration +
                        config.maxJerk * it * sign(-deltaVel),  // Decreasing vel

                config.maxJerk * sign(-deltaVel) // Decreasing accel
            )
        }, 0.0, abs(maxAccel) / config.maxJerk)
        createAppended(accelerate, decelerate)
    } else {
        val accelerate: BoundedFunction<Derivatives<Double>> = SingleBoundedFunction({
            Derivatives(
                initialPos + initialVel * it +
                        config.maxJerk * it.pow(3.0) / 6 * sign(deltaVel),

                initialVel +
                        config.maxJerk * it.pow(2.0) / 2 * sign(deltaVel),  // Increasing position

                config.maxJerk * it * sign(deltaVel),  // Increasing vel

                config.maxJerk * sign(deltaVel) // Increasing accel
            )
        }, 0.0, config.maxAcceleration / config.maxJerk)
        val endPoint1 = accelerate.upper
        val maintain: BoundedFunction<Derivatives<Double>> = SingleBoundedFunction({
            Derivatives(
                endPoint1.position +
                        endPoint1.velocity * it +
                        config.maxAcceleration * it.pow(2.0) / 2 * sign(deltaVel),

                endPoint1.velocity +
                        config.maxAcceleration * it * sign(deltaVel),  // Increasing position

                config.maxAcceleration * sign(deltaVel),  // Increasing Vel
                0.0 // Constant accel
            )
        }, 0.0, abs(deltaVel / config.maxAcceleration) - config.maxAcceleration / config.maxJerk)
        val endPoint2 = maintain.upper
        val decelerate: BoundedFunction<Derivatives<Double>> = SingleBoundedFunction({
            Derivatives(
                endPoint2.position +
                        endPoint2.velocity * it +
                        endPoint2.acceleration * it.pow(2.0) / 2 +
                        config.maxJerk * it.pow(3.0) / 6 * sign(-deltaVel),

                endPoint2.velocity +
                        endPoint2.acceleration * it +
                        config.maxJerk * it.pow(2.0) / 2 * sign(-deltaVel),  // Increasing position

                endPoint2.acceleration +
                        config.maxJerk * it * sign(-deltaVel),  // Decreasing vel

                config.maxJerk * sign(-deltaVel) // Decreasing accel
            )
        }, 0.0, config.maxAcceleration / config.maxJerk)
        createAppended(accelerate, maintain, decelerate)
    }
}

fun createTurn(start: Vector, middle: Vector, end: Vector, config: DriveConfig, speed: Double): Arc {
    val center = findTurnCenter(start, middle, end, config, speed)
    val deltaStart = start.clone().subtract(middle)
    val deltaEnd = end.clone().subtract(middle)
    val tStart = (deltaStart.dot(center) - deltaStart.dot(middle)) / deltaStart.dot(deltaStart)
    val tEnd = (deltaEnd.dot(center) - deltaEnd.dot(middle)) / deltaEnd.dot(deltaEnd)
    val dir1 = deltaStart.clone().scale(tStart).add(middle).subtract(center).theta
    val dir2 = deltaEnd.clone().scale(tEnd).add(middle).subtract(center).theta
    return createArc(
        center, dir1, dir2, sign(findAngleDifference(deltaStart.theta, deltaEnd.theta)).toInt(), config, speed
    )
}

private fun createArc(
    center: Vector, initialTheta: Double, finalTheta: Double, dir: Int, config: DriveConfig, speed: Double
): Arc {
    val tSign = Integer.signum(dir)
    val pi2 = 2 * Math.PI
    val iMod = (initialTheta % pi2 + pi2) % pi2
    val fMod = (finalTheta % pi2 + pi2) % pi2
    val diff = (tSign * (fMod - iMod) % pi2 + pi2) % pi2
    val coeff =
        if (config.maxJerk < config.maxAcceleration) sqrt(config.maxJerk / speed) else config.maxAcceleration / speed

    val function = SingleBoundedFunction({
        Derivatives(
            Vector(
                speed / coeff * cos(tSign * coeff * it + initialTheta) + center.x,
                speed / coeff * sin(tSign * coeff * it + initialTheta) + center.y
            ), Vector(
                -tSign * speed * sin(tSign * coeff * it + initialTheta),
                tSign * speed * cos(tSign * coeff * it + initialTheta)
            ), Vector(
                -speed * coeff * cos(tSign * coeff * it + initialTheta),
                -speed * coeff * sin(tSign * coeff * it + initialTheta)
            ), Vector(
                tSign * speed * coeff * coeff * sin(tSign * coeff * it + initialTheta),
                -tSign * speed * coeff * coeff * cos(tSign * coeff * it + finalTheta)
            )
        )
    }, 0.0, abs(diff) / coeff)
    return Arc(function, coeff, speed, diff, tSign, center)
}

fun createInterpolation(points: List<Vector>, config: DriveConfig, speed: Double): List<Arc> {
    require(points.size >= 4) { "An interpolation must have at least 2 turns" }
    val solutions: MutableList<Solution> = ArrayList()
    val r = findTurningRadius(config, speed)
    for (i in 0 until points.size - 2) {
        val start = points[i]
        val middle = points[i + 1]
        val end = points[i + 2]
        val initial = findTurnCenter(start, middle, end, config, config.maxVelocity)
        val slope: Vector = if (i == 0) middle.subtract(start).normalize()
        else if (i == points.size - 3) middle.subtract(end).normalize()
        else start.subtract(middle).normalize().add(end.subtract(middle).normalize()).normalize().scale(-1.0)

        if (i >= 1) {
            val last = solutions[i - 1]
            val scaledSlope = last.slope.scale(1.0 / i)

            val (t1: Double, t2: Double) = quadraticSolve(
                scaledSlope.subtract(slope).square(),
                2 * last.initial.subtract(initial).dot(scaledSlope.subtract(slope)),
                last.initial.subtract(initial).square() - 4 * r * r
            )

            val t: Double = if (abs(t1) < abs(t2)) t1 else t2
            solutions.add(Solution(initial, slope, t))
        } else {
            solutions.add(Solution(initial, slope, 0.0))
        }
    }
    val centers: MutableList<Vector> = ArrayList()
    centers.add(solutions[solutions.size - 1].point)
    for (i in solutions.size - 2 downTo 0) {
        val next = centers[0]
        val current = solutions[i]
        val a = current.slope.square()
        val b = 2 * current.initial.subtract(next).dot(current.slope)
        val c = current.initial.subtract(next).square() - 4 * r * r
        val determinate = sqrt(b * b - 4 * a * c)
        val t1 = (-b - determinate) / (2 * a)
        val t2 = (-b + determinate) / (2 * a)
        val t: Double = if (abs(t1) < abs(t2)) t1 else t2
        centers.add(0, current(t))
    }
    val arcs: MutableList<Arc> = ArrayList()
    run {
        val delta = points[1].subtract(points[0])
        val center = centers[0]
        val t = (delta.dot(center) - delta.dot(points[0])) / delta.dot(delta)
        val dir1 = delta.scale(t).add(points[0]).subtract(center).theta
        val dir2 = centers[1].subtract(center).theta
        val deltaStart = points[0].subtract(points[1])
        val deltaEnd = points[2].subtract(points[1])
        arcs.add(
            createArc(
                center, dir1, dir2, sign(findAngleDifference(deltaStart.theta, deltaEnd.theta)).toInt(), config, speed
            )
        )
    }
    for (i in 1 until centers.size - 1) {
        val last = centers[i - 1]
        val current = centers[i]
        val next = centers[i + 1]
        val dir1 = last.subtract(current).theta
        val dir2 = next.subtract(current).theta
        arcs.add(createArc(current, dir1, dir2, -arcs[i - 1].dir, config, speed))
    }
    run {
        val delta = points[points.size - 2].subtract(points[points.size - 1])
        val center = centers[centers.size - 1]
        val t = (delta.dot(center) - delta.dot(points[points.size - 2])) / delta.dot(delta)
        val dir1 = centers[centers.size - 2].subtract(centers[centers.size - 1]).theta
        val dir2 = delta.scale(t).add(points[points.size - 2]).subtract(center).theta
        arcs.add(createArc(center, dir1, dir2, -arcs[arcs.size - 1].dir, config, speed))
    }
    return arcs
}

fun findTurnCenter(start: Vector, middle: Vector, end: Vector, config: DriveConfig, speed: Double): Vector {
    val dir1 = start.clone().subtract(middle).normalize()
    val dir2 = end.clone().subtract(middle).normalize()
    val r = findTurningRadius(config, speed)
    val t =
        (dir2.x * dir2.x + dir2.y * dir2.y + dir1.x * dir2.x + dir1.y * dir2.y) / (dir2.x * dir1.y - dir1.x * dir2.y) * r
    val x = dir1.x * t + dir1.y * r
    val y = dir1.y * t - dir1.x * r
    val solutions = arrayOf(
        Vector(x, y), Vector(-y, x), Vector(y, -x), Vector(-x, -y)
    )
    var closest = solutions[0]
    val average = dir1.clone().add(dir2).scale(0.5)
    var closestDist = closest.dist(average)
    for (i in 1 until solutions.size) {
        val point = solutions[i]
        val dist = point.dist(average)
        if (dist < closestDist) {
            closest = point
            closestDist = dist
        }
    }
    return closest.clone().add(middle)
}

fun findAngleDifference(angle2: Double, angle1: Double): Double {
    val rawDiff =
        (angle2 % (2 * Math.PI) + 2 * Math.PI) % (2 * Math.PI) - (angle1 % (2 * Math.PI) + 2 * Math.PI) % (2 * Math.PI)
    return if (abs(rawDiff) < Math.PI) rawDiff
    else rawDiff - sign(rawDiff) * 2 * Math.PI
}

fun findTurningRadius(config: DriveConfig, speed: Double): Double {
    val accel = config.maxAcceleration
    val jerk = config.maxJerk
    val coeff = if (jerk < accel) sqrt(jerk / speed) else accel / speed
    return speed / coeff
}

fun quadraticSolve(a: Double, b: Double, c: Double): Pair<Double, Double> {
    val det: Double = sqrt(b.pow(2) - 4 * a * c)
    return Pair((-b + det) / (2 * a), (-b - det) / (2 * a))
}

fun newtonMethodSolve(
    output: Double, function: BoundedFunction<Double>, derivative: BoundedFunction<Double>, precision: Double = 1e-13
): Double {

    val initialGuess = (function.upperBound() + function.lowerBound()) / 2
    var lastGuess: Double
    var guess = initialGuess
    var error = -1.0
    while (error < 0 || error > precision) {
        lastGuess = guess
        guess += (output - function(guess)) / derivative(guess)
        error = abs(guess - lastGuess)
    }
    return guess
}

fun newtonMethodSolve(
    output: Double, function: BoundedFunction<Double>, precision: Double = 1e-14, dx: Double = 1e-14
): Double {
    val initialGuess = (function.upperBound() + function.lowerBound()) / 2
    var guess = initialGuess
    var error = -1.0


    while (error < 0 || error > precision) {
        val out = function(guess)

        // dx is scaled by the output of the function in order to compensate for double rounding
        val derivative = (function.bounded(guess + dx / 2 * out) - function.bounded(guess - dx / 2 * out)) / (dx * out)
        val delta = (output - out) / derivative

        guess += delta
        guess = guess.coerceIn(function.lowerBound()..function.upperBound())
        error = abs(output - out)
    }
    return guess
}

