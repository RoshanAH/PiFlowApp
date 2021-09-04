package com.zypex.piflow.profile;

import com.zypex.piflow.DrivetrainConfig;
import com.zypex.piflow.path.Path;
import utils.math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileBuilder {

    private final DrivetrainConfig config;

    private PiecewiseFunction<Derivatives<Vector>> profile = new PiecewiseFunction<>();
    private PiecewiseFunction<Derivatives<Double>> headingProfile = new PiecewiseFunction<>();

    private static Linear CreateLinear(Vector start, Vector end, double startSpeed, double endSpeed, DrivetrainConfig config) {

        return null;

    }

    private static Linear CreateDisplacement(Vector start, Vector end, double startSpeed, double endSpeed, double maxSpeed, DrivetrainConfig config) {

        return null;
    }

    public static Arc CreateTurn(Vector start, Vector middle, Vector end, DrivetrainConfig config, double speed) {

        final Vector center = FindTurnCenter(start, middle, end, config, speed);

        final Vector deltaStart = start.clone().subtract(middle);
        final Vector deltaEnd = end.clone().subtract(middle);

        final double tStart = (deltaStart.dot(center) - deltaStart.dot(middle)) / deltaStart.dot(deltaStart);
        final double tEnd = (deltaEnd.dot(center) - deltaEnd.dot(middle)) / deltaEnd.dot(deltaEnd);

        final double dir1 = deltaStart.clone().scale(tStart).add(middle).subtract(center).getTheta();
        final double dir2 = deltaEnd.clone().scale(tEnd).add(middle).subtract(center).getTheta();

        return CreateArc(center, dir1, dir2, (int) Math.signum(FindAngleDifference(deltaStart.getTheta(), deltaEnd.getTheta())), config, speed);

    }


    private static Arc CreateArc(Vector center, double initialTheta, double finalTheta, int dir, DrivetrainConfig config, double speed) {

        final int tSign = Integer.signum(dir);
        final double pi2 = 2 * Math.PI;

        final double iMod = (initialTheta % pi2 + pi2) % pi2;
        final double fMod = (finalTheta % pi2 + pi2) % pi2;

        final double diff = ((tSign * (fMod - iMod)) % pi2 + pi2) % pi2;

        final double coeff = config.maxJerk < config.maxAcceleration ? Math.sqrt(config.maxJerk / speed) : config.maxAcceleration / speed;

        final double r = speed / coeff;

        SingleBoundedFunction<Derivatives<Vector>> function = new SingleBoundedFunction<>(t -> new Derivatives<>(
                new Vector(
                        speed / coeff * Math.cos(tSign * coeff * t + initialTheta) + center.x,
                        speed / coeff * Math.sin(tSign * coeff * t + initialTheta) + center.y
                ),
                new Vector(
                        -tSign * speed * Math.sin(tSign * coeff * t + initialTheta),
                        tSign * speed * Math.cos(tSign * coeff * t + initialTheta)
                ),
                new Vector(
                        -speed * coeff * Math.cos(tSign * coeff * t + initialTheta),
                        -speed * coeff * Math.sin(tSign * coeff * t + initialTheta)
                ),
                new Vector(
                        tSign * speed * coeff * coeff * Math.sin(tSign * coeff * t + initialTheta),
                        -tSign * speed * coeff * coeff * Math.cos(tSign * coeff * t + finalTheta)
                )
        ), 0, Math.abs(diff) / speed);

        return new Arc(function, coeff, speed, diff, tSign, center);
    }

    public static List<Arc> CreateInterpolation(List<Vector> points, DrivetrainConfig config, double speed) {
        if (points.size() < 4) throw new IllegalArgumentException("An interpolation must have at least 2 turns");

        List<Solution> solutions = new ArrayList<>();

        final double r = FindTurningRadius(config, speed);

        for (int i = 0; i < points.size() - 2; i++) {
            final Vector start = points.get(i);
            final Vector middle = points.get(i + 1);
            final Vector end = points.get(i + 2);
            final Vector initial = ProfileBuilder.FindTurnCenter(start, middle, end, config, config.maxVelocity);

            final Vector slope;

            if (i == 0) slope = middle.subtract(start).normalize();
            else if (i == points.size() - 3) slope = middle.subtract(end).normalize();
            else slope = start.subtract(middle).normalize().add(end.subtract(middle).normalize()).normalize().scale(-1);

            if (i >= 1) {
                ;
                final Solution last = solutions.get(i - 1);
                final Vector scaledSlope = last.slope.scale(1d / (i));

                final double a = scaledSlope.subtract(slope).square();
                final double b = 2 * last.initial.subtract(initial).dot(scaledSlope.subtract(slope));
                final double c = last.initial.subtract(initial).square() - 4 * r * r;

                final double det = Math.sqrt(b * b - 4 * a * c);
                final double t1 = (-b - det) / (2 * a);
                final double t2 = (-b + det) / (2 * a);

                final double t;
                if (Math.abs(t1) < Math.abs(t2)) t = t1;
                else t = t2;

                solutions.add(new Solution(initial, slope, t));
            } else {
                solutions.add(new Solution(initial, slope, 0));
            }
        }

        List<Vector> centers = new ArrayList<>();
        centers.add(solutions.get(solutions.size() - 1).getPoint());

        for (int i = solutions.size() - 2; i >= 0; i--) {
            Vector next = centers.get(0);
            Solution current = solutions.get(i);

            final double a = current.slope.square();
            final double b = 2 * current.initial.subtract(next).dot(current.slope);
            final double c = current.initial.subtract(next).square() - 4 * r * r;

            final double determinate = Math.sqrt(b * b - 4 * a * c);
            final double t1 = (-b - determinate) / (2 * a);
            final double t2 = (-b + determinate) / (2 * a);

            final double t;
            if (Math.abs(t1) < Math.abs(t2)) t = t1;
            else t = t2;

            centers.add(0, current.get(t));
        }

        List<Arc> arcs = new ArrayList<>();

        {
            final Vector delta = points.get(1).subtract(points.get(0));
            final Vector center = centers.get(0);
            final double t = (delta.dot(center) - delta.dot(points.get(0))) / delta.dot(delta);

            final double dir1 = delta.scale(t).add(points.get(0)).subtract(center).getTheta();
            final double dir2 = centers.get(1).subtract(center).getTheta();

            final Vector deltaStart = points.get(0).subtract(points.get(1));
            final Vector deltaEnd = points.get(2).subtract(points.get(1));

            arcs.add(CreateArc(center, dir1, dir2, (int) Math.signum(FindAngleDifference(deltaStart.getTheta(), deltaEnd.getTheta())), config, speed));
        }

        for (int i = 1; i < centers.size() - 1; i++) {
            final Vector last = centers.get(i - 1);
            final Vector current = centers.get(i);
            final Vector next = centers.get(i + 1);


            final double dir1 = last.subtract(current).getTheta();
            final double dir2 = next.subtract(current).getTheta();

            arcs.add(CreateArc(current, dir1, dir2, -arcs.get(i - 1).dir, config, speed));
        }

        {
            final Vector delta = points.get(points.size() - 2).subtract(points.get(points.size() - 1));
            final Vector center = centers.get(centers.size() - 1);
            final double t = (delta.dot(center) - delta.dot(points.get(points.size() - 2))) / delta.dot(delta);

            final double dir1 = centers.get(centers.size() - 2).subtract(centers.get(centers.size() - 1)).getTheta();
            final double dir2 = delta.scale(t).add(points.get(points.size() - 2)).subtract(center).getTheta();

            arcs.add(CreateArc(center, dir1, dir2, -arcs.get(arcs.size() - 1).dir, config, speed));
        }

        return arcs;

    }

    public static Vector FindTurnCenter(Vector start, Vector middle, Vector end, DrivetrainConfig config, double speed) {

        Vector dir1 = start.clone().subtract(middle).normalize();
        Vector dir2 = end.clone().subtract(middle).normalize();

        final double r = FindTurningRadius(config, speed);
        final double t = (dir2.x * dir2.x + dir2.y * dir2.y + dir1.x * dir2.x + dir1.y * dir2.y) / (dir2.x * dir1.y - dir1.x * dir2.y) * r;

        final double x = dir1.x * t + dir1.y * r;
        final double y = dir1.y * t - dir1.x * r;

        Vector[] solutions = {
                new Vector(x, y),
                new Vector(-y, x),
                new Vector(y, -x),
                new Vector(-x, -y),
        };

        Vector closest = solutions[0];
        Vector average = dir1.clone().add(dir2).scale(0.5);
        double closestDist = closest.dist(average);

        for (int i = 1; i < solutions.length; i++) {
            Vector point = solutions[i];
            double dist = point.dist(average);
            if (dist < closestDist) {
                closest = point;
                closestDist = dist;
            }
        }

        return closest.clone().add(middle);
    }

    public static double FindAngleDifference(double angle2, double angle1) {

        angle2 = (angle2 % (2 * Math.PI) + 2 * Math.PI) % (2 * Math.PI);
        angle1 = (angle1 % (2 * Math.PI) + 2 * Math.PI) % (2 * Math.PI);

        final double rawDiff = angle2 - angle1;

        if (Math.abs(rawDiff) < Math.PI) {
            return rawDiff;
        } else {
            return rawDiff - Math.signum(rawDiff) * 2 * Math.PI;
        }
    }

    public static BoundedFunction<Derivatives<Double>> CreateVelocityChange(double initialVel, double finalVel, DrivetrainConfig config) {
        PiecewiseFunction<Derivatives<Double>> out = new PiecewiseFunction<>();
        final double deltaVel = finalVel - initialVel;

        final double maxAccel = Math.signum(deltaVel) * Math.sqrt(config.maxJerk * Math.abs(deltaVel));

        if (Math.abs(maxAccel) <= config.maxAcceleration) {
            out.addFunction(t -> new Derivatives<>(
                    config.maxJerk * Math.pow(t, 3) / 6 * Math.signum(deltaVel),
                    initialVel + config.maxJerk * Math.pow(t, 2) / 2 * Math.signum(deltaVel), // Increasing position
                    config.maxJerk * t * Math.signum(deltaVel), // Increasing vel
                    config.maxJerk * Math.signum(deltaVel) // Increasing accel
            ), 0, maxAccel / config.maxJerk);

            final Derivatives<Double> endPoint1 = out.get(out.functions.get(0).upperBound());

            out.appendFunction(t -> new Derivatives<>(
                    endPoint1.position +
                            endPoint1.velocity * t +
                            endPoint1.acceleration * Math.pow(t, 2) / 2 +
                            config.maxJerk * Math.pow(t, 3) / 6 * Math.signum(-deltaVel),

                    endPoint1.velocity +
                            endPoint1.acceleration * t +
                            config.maxJerk * Math.pow(t, 2) / 2 * Math.signum(-deltaVel), // Increasing position

                    endPoint1.acceleration +
                            config.maxJerk * t * Math.signum(-deltaVel), // Decreasing vel

                    config.maxJerk * Math.signum(-deltaVel) // Decreasing accel
            ), 0, maxAccel / config.maxJerk);
        } else {

            out.addFunction(t -> new Derivatives<>(
                    config.maxJerk * Math.pow(t, 3) / 6 * Math.signum(deltaVel),
                    initialVel + config.maxJerk * Math.pow(t, 2) / 2 * Math.signum(deltaVel), // Increasing position
                    config.maxJerk * t * Math.signum(deltaVel), // Increasing vel
                    config.maxJerk * Math.signum(deltaVel) // Increasing accel
            ), 0, config.maxAcceleration / config.maxJerk);

            final Derivatives<Double> endPoint1 = out.get(out.functions.get(0).upperBound());

            out.appendFunction(t -> new Derivatives<>(
                    endPoint1.position +
                            endPoint1.velocity * t +
                            config.maxAcceleration * Math.pow(t, 2) / 2 * Math.signum(deltaVel),

                    endPoint1.velocity +
                            config.maxAcceleration * t * Math.signum(deltaVel), // Increasing position

                    config.maxAcceleration * Math.signum(deltaVel), // Increasing Vel
                    0d // Constant accel
            ), 0, deltaVel / config.maxAcceleration - config.maxAcceleration / config.maxJerk);

            final Derivatives<Double> endPoint2 = out.get(out.functions.get(1).upperBound());

            out.appendFunction(t -> new Derivatives<>(
                    endPoint2.position +
                            endPoint2.velocity * t +
                            endPoint2.acceleration * Math.pow(t, 2) / 2 +
                            config.maxJerk * Math.pow(t, 3) / 6 * Math.signum(-deltaVel),

                    endPoint2.velocity +
                            endPoint2.acceleration * t +
                            config.maxJerk * Math.pow(t, 2) / 2 * Math.signum(-deltaVel), // Increasing position

                    endPoint2.acceleration +
                            config.maxJerk * t * Math.signum(-deltaVel), // Decreasing vel

                    config.maxJerk * Math.signum(-deltaVel) // Decreasing accel
            ), 0, config.maxAcceleration / config.maxJerk);
        }

        return out;
    }

    public static double FindTurningRadius(DrivetrainConfig config, double speed) {
        final double accel = config.maxAcceleration;
        final double jerk = config.maxJerk;

        final double coeff = jerk < accel ? Math.sqrt(jerk / speed) : accel / speed;

        return speed / coeff;
    }

    public ProfileBuilder(DrivetrainConfig config, Path path) {
        this.config = config;

        path = null;
    }

    public Profile build() {
        return new Profile(profile, headingProfile);
    }

    private static class Solution implements Function<Vector> {
        Vector initial;
        Vector slope;
        double t;

        Solution(Vector initial, Vector slope, double t) {
            this.initial = initial;
            this.slope = slope;
            this.t = t;
        }

        Vector getPoint() {
            return slope.scale(t).add(initial);
        }

        public String toString() {
            return getPoint().toString();
        }

        @Override
        public Vector get(double t) {
            return initial.add(slope.scale(t));
        }
    }

}
