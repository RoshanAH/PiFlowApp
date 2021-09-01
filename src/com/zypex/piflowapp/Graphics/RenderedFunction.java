package com.zypex.piflowapp.Graphics;

import javafx.scene.paint.Color;
import utils.math.Function;

import javax.swing.*;

public class RenderedFunction {

    //    All the available dimensions for the renderer
    public Function<Double> x = t -> 0d;
    public Function<Double> y = t -> 0d;
    public Function<Double> r = t -> 0d;
    public Function<Double> g = t -> 0d;
    public Function<Double> b = t -> 0d;
    public Function<Double> size = t -> 0.5;

    public double minT;
    public double maxT;

    public RenderedFunction(double minT, double maxT){
        this.minT = minT;
        this.maxT = maxT;
    }


    //    Set the dimensions
    public RenderedFunction setX(double x) {
        this.x = t -> x;
        return this;
    }

    public RenderedFunction setY(double y) {
        this.y = t -> y;
        return this;
    }

    public RenderedFunction setR(double r) {
        this.r = t -> r;
        return this;
    }

    public RenderedFunction setG(double g) {
        this.g = t -> g;
        return this;
    }

    public RenderedFunction setB(double b) {
        this.b = t -> b;
        return this;
    }

    public RenderedFunction setColor(Color color){
        r = t -> color.getRed();
        g = t -> color.getGreen();
        b = t -> color.getBlue();
        return this;
    }

    public RenderedFunction setSize(double size) {
        this.size = t -> size;
        return this;
    }

    public RenderedFunction attachX(Function<Double> x) {
        this.x = x;
        return this;
    }

    public RenderedFunction attachY(Function<Double> y) {
        this.y = y;
        return this;
    }

    public RenderedFunction attachR(Function<Double> r) {
        this.r = r;
        return this;
    }

    public RenderedFunction attachG(Function<Double> g) {
        this.g = g;
        return this;
    }

    public RenderedFunction attachB(Function<Double> b) {
        this.b = b;
        return this;
    }

    public RenderedFunction attachSize(Function<Double> size) {
        this.size = size;
        return this;
    }
}
