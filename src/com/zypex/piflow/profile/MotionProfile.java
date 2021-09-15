package com.zypex.piflow.profile;

import utils.math.BoundedFunction;
import utils.math.Vector;

import java.util.ArrayList;
import java.util.List;

public class MotionProfile extends ProfileSegment {

    List<ProfileSegment> segments;

    @Override
    public double upperBound() {
        double highest = segments.get(0).lowerBound();
        ProfileSegment highestSegment;

        for(int i = 1; i < segments.size(); i++){
            final ProfileSegment currentSegment = segments.get(i);
            final double currentHighest = currentSegment.lowerBound();

            if(currentHighest > highest){
                highestSegment = currentSegment;
                highest = currentHighest;
            }
        }

        return highest; ;

    }

    @Override
    public double lowerBound() {
        return 0;
    }

    @Override
    public double getLength() {
        double total = 0;
        for (ProfileSegment s : segments) total += s.getLength();
        return total;
    }

    @Override
    public MotionProfile offset(double offset) throws ClassCastException {
        return null;
    }

    @Override
    public double getT(Vector pos) {

        double t = segments.get(0).getT(pos);
        Vector closest = segments.get(0).get(t).position;

        for(int i = 1; i < segments.size(); i++){
            final ProfileSegment segment = segments.get(i);
            final double currentT = segment.getT(pos);
            final Vector currentPos = segment.get(currentT).position;

            if(currentPos.subtract(pos).getMagnitude() < closest.subtract(pos).getMagnitude()){
                t = currentT;
                closest = currentPos;
            }
        }

        return t;
    }

    @Override
    public Derivatives<Vector> get(double input) {
        return null;
    }
}
