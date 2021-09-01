package com.zypex.piflowapp.Graphics;

import javafx.css.Size;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import utils.math.Vector;

import java.util.ArrayList;
import java.util.List;

public class Graph {

    private final List<Vector> datapoints = new ArrayList<>();
    public Color color = Color.RED;
    public DataMode dataMode = DataMode.LINEAR;
    public SizeMode sizeMode = SizeMode.RELATIVE;

    public void drawSection(GraphicsContext gc){
        final double w = gc.getCanvas().getWidth();
        final double h = gc.getCanvas().getHeight();
    }

    public void addDatapoint(double x, double y){
        for(int i = 0; i < datapoints.size(); i++){
            Vector point = datapoints.get(i);
            if(point.x < x){
                datapoints.add(i, new Vector(x, y));
            }
        }
    }

    public void addDataPoint(Vector point){
        addDatapoint(point.x, point.y);
    }

    public enum DataMode{
        SCATTER,
        LINEAR,
        SQUARE
    }

    public enum SizeMode{
        ABSOLUTE,
        RELATIVE
    }
}
