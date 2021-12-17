package com.zypex.piflow.profile

import com.zypex.piflow.DrivetrainConfig
import com.zypex.piflow.path.Path
import utils.math.*
import kotlin.math.*

typealias VecProfile = PiecewiseFunction<Derivatives<Vector>>
typealias DoubleProfile = PiecewiseFunction<Derivatives<Double>>

class ProfileBuilder(config: DrivetrainConfig, path: Path?) {
    private val config: DrivetrainConfig
    private val profile = VecProfile()
    private val headingProfile = PiecewiseFunction<Derivatives<Double>>()

    init {
        var path = path
        this.config = config
        path = null
    }

    fun build(): Profile {
        return Profile(profile, headingProfile)
    }

    class Solution(var initial: Vector, var slope: Vector, var t: Double) : (Double) -> Vector {
        val point: Vector
            get() = slope.scale(t).add(initial)

        override fun toString(): String {
            return point.toString()
        }

        override operator fun invoke(t: Double): Vector {
            return initial.add(slope.scale(t))
        }
    }

    companion object {
        private fun createLinear(start: Vector, dir: Vector, startSpeed: Double, endSpeed: Double, config: DrivetrainConfig): CompoundProfileSegment {
            val out = CompoundProfileSegment()
            val magProfile = createVelocityChange(startSpeed, endSpeed, config)
            for (function in magProfile.functions) {
                out.add(createLinear(function as SingleBoundedFunction<Derivatives<Double>>, start, dir))
            }
            return out
        }

        private fun createLinear(profile: SingleBoundedFunction<Derivatives<Double>>, start: Vector, dir: Vector): Linear {
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

        private fun createDisplacement(start: Vector, end: Vector, startSpeed: Double, endSpeed: Double, maxSpeed: Double, config: DrivetrainConfig): CompoundProfileSegment {
            val dist = end.dist(start)
            val dir = end.subtract(start).normalize()
            val finalMaxSpeed = Math.min(maxSpeed, config.maxVelocity)
            val intermediateSpeed = -1.0 // TODO solve for intermediate speed as a function of displacement

//    Steps for solving for intermediate speed as a function of displacement
//    1) Find displacement as a function of intermediate speed
//      1.1) Find for half of a curve
//        1.1.1)
//      1.2) Make one big function based on the starting and ending velocities
//    2) Solve for intermediate speed
            val out = CompoundProfileSegment()
            if (intermediateSpeed > finalMaxSpeed) {
                val speedUp = createLinear(start, dir, startSpeed, finalMaxSpeed, config)
                val temp = createVelocityChange(finalMaxSpeed, endSpeed, config)
                val makeUpDist = dist - (speedUp.upper.position.subtract(speedUp.lower.position).magnitude + temp.upper.position)
                val maintain = createLinear(
                    SingleBoundedFunction({
                        Derivatives (
                            maxSpeed * it,
                            maxSpeed,
                            0.0,
                            0.0
                        )
                    }, 0.0, makeUpDist / maxSpeed),
                    speedUp.upper.position, dir)
                val slowDown = createLinear(maintain.upper.position, dir, maxSpeed, endSpeed, config)
                out.append(speedUp)
                out.append(maintain)
                out.append(slowDown)
            } else {
                val speedUp = createLinear(start, dir, startSpeed, intermediateSpeed, config)
                val slowDown = createLinear(speedUp.upper.position, dir, intermediateSpeed, endSpeed, config)
                out.append(speedUp)
                out.append(slowDown)
            }
            return out
        }

        fun createVelocityChange(initialVel: Double, finalVel: Double, config: DrivetrainConfig): PiecewiseFunction<Derivatives<Double>> {
            val deltaVel = finalVel - initialVel
            val maxAccel = sign(deltaVel) * sqrt(config.maxJerk * abs(deltaVel))
            return if (abs(maxAccel) <= config.maxAcceleration) {
                val accelerate: BoundedFunction<Derivatives<Double>> = SingleBoundedFunction({
                    Derivatives(
                        config.maxJerk * Math.pow(it, 3.0) / 6 * Math.signum(deltaVel),
                        initialVel + config.maxJerk * Math.pow(it, 2.0) / 2 * Math.signum(deltaVel),  // Increasing position
                        config.maxJerk * it * Math.signum(deltaVel),  // Increasing vel
                        config.maxJerk * Math.signum(deltaVel) // Increasing accel
                    )
                }, 0.0, maxAccel / config.maxJerk)
                val endPoint1 = accelerate.upper
                val decelerate: BoundedFunction<Derivatives<Double>> = SingleBoundedFunction({
                    Derivatives(
                        endPoint1.position + endPoint1.velocity * it + endPoint1.acceleration * it.pow(2.0) / 2 + config.maxJerk * it.pow(3.0) / 6 * sign(-deltaVel),
                        endPoint1.velocity + endPoint1.acceleration * it + config.maxJerk * it.pow(2.0) / 2 * sign(-deltaVel),  // Increasing position
                        endPoint1.acceleration + config.maxJerk * it * sign(-deltaVel),  // Decreasing vel
                        config.maxJerk * sign(-deltaVel) // Decreasing accel
                    )
                }, 0.0, maxAccel / config.maxJerk)
                createAppended(accelerate, decelerate)
            } else {
                val accelerate: BoundedFunction<Derivatives<Double>> = SingleBoundedFunction({
                    Derivatives(
                        config.maxJerk * Math.pow(it, 3.0) / 6 * Math.signum(deltaVel),
                        initialVel + config.maxJerk * Math.pow(it, 2.0) / 2 * Math.signum(deltaVel),  // Increasing position
                        config.maxJerk * it * Math.signum(deltaVel),  // Increasing vel
                        config.maxJerk * Math.signum(deltaVel) // Increasing accel
                    )
                }, 0.0, config.maxAcceleration / config.maxJerk)
                val endPoint1 = accelerate.upper
                val maintain: BoundedFunction<Derivatives<Double>> = SingleBoundedFunction({
                    Derivatives(
                        endPoint1.position + endPoint1.velocity * it + config.maxAcceleration * it.pow(2.0) / 2 * sign(deltaVel),
                        endPoint1.velocity +
                                config.maxAcceleration * it * sign(deltaVel),  // Increasing position
                        config.maxAcceleration * sign(deltaVel),  // Increasing Vel
                        0.0 // Constant accel
                    )
                }, 0.0, deltaVel / config.maxAcceleration - config.maxAcceleration / config.maxJerk)
                val endPoint2 = maintain.upper
                val decelerate: BoundedFunction<Derivatives<Double>> = SingleBoundedFunction({
                    Derivatives(
                        endPoint2.position + endPoint2.velocity * it + endPoint2.acceleration * it.pow(2.0) / 2 + config.maxJerk * it.pow(3.0) / 6 * sign(-deltaVel),
                        endPoint2.velocity + endPoint2.acceleration * it + config.maxJerk * it.pow(2.0) / 2 * sign(-deltaVel),  // Increasing position
                        endPoint2.acceleration + config.maxJerk * it * sign(-deltaVel),  // Decreasing vel
                        config.maxJerk * sign(-deltaVel) // Decreasing accel
                    )
                }, 0.0, config.maxAcceleration / config.maxJerk)
                createAppended(accelerate, maintain, decelerate)
            }
        }

        fun createTurn(start: Vector, middle: Vector, end: Vector, config: DrivetrainConfig, speed: Double): Arc {
            val center = findTurnCenter(start, middle, end, config, speed)
            val deltaStart = start.clone().subtract(middle)
            val deltaEnd = end.clone().subtract(middle)
            val tStart = (deltaStart.dot(center) - deltaStart.dot(middle)) / deltaStart.dot(deltaStart)
            val tEnd = (deltaEnd.dot(center) - deltaEnd.dot(middle)) / deltaEnd.dot(deltaEnd)
            val dir1 = deltaStart.clone().scale(tStart).add(middle).subtract(center).theta
            val dir2 = deltaEnd.clone().scale(tEnd).add(middle).subtract(center).theta
            return createArc(center, dir1, dir2, sign(findAngleDifference(deltaStart.theta, deltaEnd.theta)).toInt(), config, speed)
        }

        private fun createArc(center: Vector, initialTheta: Double, finalTheta: Double, dir: Int, config: DrivetrainConfig, speed: Double): Arc {
            val tSign = Integer.signum(dir)
            val pi2 = 2 * Math.PI
            val iMod = (initialTheta % pi2 + pi2) % pi2
            val fMod = (finalTheta % pi2 + pi2) % pi2
            val diff = (tSign * (fMod - iMod) % pi2 + pi2) % pi2
            val coeff = if (config.maxJerk < config.maxAcceleration) Math.sqrt(config.maxJerk / speed) else config.maxAcceleration / speed
            val r = speed / coeff
            val function = SingleBoundedFunction({
                Derivatives(
                    Vector(
                        speed / coeff * cos(tSign * coeff * it + initialTheta) + center.x,
                        speed / coeff * sin(tSign * coeff * it + initialTheta) + center.y
                    ),
                    Vector(
                        -tSign * speed * sin(tSign * coeff * it + initialTheta),
                        tSign * speed * cos(tSign * coeff * it + initialTheta)
                    ),
                    Vector(
                        -speed * coeff * cos(tSign * coeff * it + initialTheta),
                        -speed * coeff * sin(tSign * coeff * it + initialTheta)
                    ),
                    Vector(
                        tSign * speed * coeff * coeff * sin(tSign * coeff * it + initialTheta),
                        -tSign * speed * coeff * coeff * cos(tSign * coeff * it + finalTheta)
                    )
                )
            }, 0.0, abs(diff) / coeff)
            return Arc(function, coeff, speed, diff, tSign, center)
        }

        fun createInterpolation(points: List<Vector>, config: DrivetrainConfig, speed: Double): List<Arc> {
            require(points.size >= 4) { "An interpolation must have at least 2 turns" }
            val solutions: MutableList<ProfileBuilder.Solution> = ArrayList()
            val r = findTurningRadius(config, speed)
            for (i in 0 until points.size - 2) {
                val start = points[i]
                val middle = points[i + 1]
                val end = points[i + 2]
                val initial = findTurnCenter(start, middle, end, config, config.maxVelocity)
                val slope: Vector =
                    if (i == 0) middle.subtract(start).normalize()
                    else if (i == points.size - 3) middle.subtract(end).normalize()
                    else start.subtract(middle).normalize().add(end.subtract(middle).normalize()).normalize().scale(-1.0)

                if (i >= 1) {
                    val last = solutions[i - 1]
                    val scaledSlope = last.slope.scale(1.0 / i)
                    val a = scaledSlope.subtract(slope).square()
                    val b = 2 * last.initial.subtract(initial).dot(scaledSlope.subtract(slope))
                    val c = last.initial.subtract(initial).square() - 4 * r * r
                    val det = sqrt(b * b - 4 * a * c)
                    val t1 = (-b - det) / (2 * a)
                    val t2 = (-b + det) / (2 * a)
                    val t: Double = if (abs(t1) < abs(t2)) t1 else t2
                    solutions.add(ProfileBuilder.Solution(initial, slope, t))
                } else {
                    solutions.add(ProfileBuilder.Solution(initial, slope, 0.0))
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
                arcs.add(createArc(center, dir1, dir2, sign(findAngleDifference(deltaStart.theta, deltaEnd.theta)).toInt(), config, speed))
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

        fun findTurnCenter(start: Vector, middle: Vector, end: Vector, config: DrivetrainConfig, speed: Double): Vector {
            val dir1 = start.clone().subtract(middle).normalize()
            val dir2 = end.clone().subtract(middle).normalize()
            val r = findTurningRadius(config, speed)
            val t = (dir2.x * dir2.x + dir2.y * dir2.y + dir1.x * dir2.x + dir1.y * dir2.y) / (dir2.x * dir1.y - dir1.x * dir2.y) * r
            val x = dir1.x * t + dir1.y * r
            val y = dir1.y * t - dir1.x * r
            val solutions = arrayOf(
                Vector(x, y),
                Vector(-y, x),
                Vector(y, -x),
                Vector(-x, -y))
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
            val rawDiff = (angle2 % (2 * Math.PI) + 2 * Math.PI) % (2 * Math.PI) - (angle1 % (2 * Math.PI) + 2 * Math.PI) % (2 * Math.PI)
            return if (abs(rawDiff) < Math.PI) rawDiff
            else rawDiff - sign(rawDiff) * 2 * Math.PI
        }

        fun findTurningRadius(config: DrivetrainConfig, speed: Double): Double {
            val accel = config.maxAcceleration
            val jerk = config.maxJerk
            val coeff = if (jerk < accel) sqrt(jerk / speed) else accel / speed
            return speed / coeff
        }
    }

}