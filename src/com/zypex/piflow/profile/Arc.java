package com.zypex.piflow.profile;

import com.zypex.piflow.DrivetrainConfig;
import utils.math.BoundedFunction;
import utils.math.Function;
import utils.math.SingleBoundedFunction;
import utils.math.Vector;

public class Arc extends SingleProfileSegment {

    public final Vector center;
    public final double radius;
    public final int dir;

//    Change these back to public
    private final double coeff;
    private final double speed;
    public final double diff;

    Arc(SingleBoundedFunction<Derivatives<Vector>> function, double coeff, double speed, double diff, int dir, Vector center) {
        super(function, Math.abs(diff) / coeff);

        this.center = center;
        this.radius = speed / coeff;
        this.dir = dir;
        this.coeff = coeff;
        this.speed = speed;
        this.diff = diff;
    }



//    Arc(Arc arc, double offset){
//        super(t -> arc.function.get(t - offset), offset, Math.abs(arc.diff) / arc.coeff + offset, arc.speed / arc.coeff * Math.abs(arc.diff));
//
//        this.center = arc.center;
//        this.radius = arc.speed / arc.coeff;
//        this.dir = arc.dir;
//        this.coeff = arc.coeff;
//        this.speed = arc.speed;
//        this.diff = arc.diff;
//        this.offset = offset;
//    }

    @Override
    public Arc offset(double offset) {
        return new Arc(function.offset(offset), coeff, speed, diff, dir, center);
    }

    @Override
    public double getT(Vector pos) {
        final Vector direction = pos.subtract(center).normalize();
        final double theta;

        final double iTheta = get(lowerBound()).position.subtract(center).getTheta();
        final double fTheta = get(upperBound()).position.subtract(center).getTheta();

        if(angleDiff(direction.getTheta(), iTheta) + angleDiff(fTheta, direction.getTheta()) == angleDiff(fTheta, iTheta)){
            theta = direction.getTheta();
        }else{

            final double initialDifference = Math.abs(ProfileBuilder.FindAngleDifference(direction.getTheta(), iTheta));
            final double finalDifference = Math.abs(ProfileBuilder.FindAngleDifference(fTheta, direction.getTheta()));

            if(initialDifference < finalDifference){
                theta = iTheta;
            }else{
                theta = fTheta;
            }
        }


//        System.out.println("( " + iTheta + ", " + fTheta + ")");

        return (theta - iTheta) / (dir * coeff) + lowerBound();
    }

    //    Change back to private future roshan
    private double angleDiff(double finalTheta, double initialTheta){
        final double pi2 = 2 * Math.PI;

        final double iMod = (initialTheta % pi2 + pi2) % pi2;
        final double fMod = (finalTheta % pi2 + pi2) % pi2;

        return ((dir * (fMod - iMod)) % pi2 + pi2) % pi2;
    }
}
