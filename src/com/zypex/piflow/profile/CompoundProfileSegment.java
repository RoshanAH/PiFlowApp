package com.zypex.piflow.profile;

import utils.math.Vector;


import java.util.List;

public class CompoundProfileSegment extends ProfileSegment {

    List<ProfileSegment> segments;

    @Override
    public double upperBound() {
        double highest = segments.get(0).lowerBound();

        for(int i = 1; i < segments.size(); i++){
            final double current = segments.get(i).lowerBound();
            if(current > highest) highest = current;
        }

        return highest;
    }

    @Override
    public double lowerBound() {
        double lowest = segments.get(0).lowerBound();

        for(int i = 1; i < segments.size(); i++){
            final double current = segments.get(i).lowerBound();
            if(current < lowest) lowest = current;
        }

        return lowest;
    }

    @Override
    public double getLength() {
        double total = 0;
        for (ProfileSegment s : segments) total += s.getLength();
        return total;
    }

    @Override
    public CompoundProfileSegment offset(double offset) throws ClassCastException {
        CompoundProfileSegment out = new CompoundProfileSegment();
        for(ProfileSegment segment : segments){
            out.add(segment.offset(offset));
        }

        return out;
    }

    public void add(ProfileSegment segment) {
        if(segment instanceof CompoundProfileSegment){
            for(ProfileSegment sub : ((CompoundProfileSegment) segment).segments) add(sub);
        }

        segments.add(segment);
    }

    public void append(ProfileSegment segment){
        if(segment instanceof CompoundProfileSegment){
            for(ProfileSegment sub : ((CompoundProfileSegment) segment).segments) append(sub);
            return;
        }

        add(segment.offset(upperBound()));
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
