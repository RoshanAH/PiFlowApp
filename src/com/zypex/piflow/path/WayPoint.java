package com.zypex.piflow.path;

import utils.math.Vector;

public class WayPoint implements PathElement{
    public Double speed = null;
    public Double heading = null;
    public Double rotation = null;
    public Vector position;

    public WayPoint(double x, double y){

    }
    public WayPoint(Vector pos){
        this(pos.x, pos.y);
    }

}
