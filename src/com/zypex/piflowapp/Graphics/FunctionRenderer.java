package com.zypex.piflowapp.Graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import utils.math.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionRenderer {

    public double resolution = 5; // In pixels

    public double derivativePrecision = 0.001;

    public List<RenderedFunction> functions = new ArrayList<>();

    public double canvasX;
    public double canvasY;
    public double canvasH;
    public double canvasW;

    public double minX;
    public double maxX;
    public double minY;
    public double maxY;


    public FunctionRenderer(double canvasX, double canvasY, double canvasW, double canvasH) {
        this.canvasX = canvasX;
        this.canvasY = canvasY;
        this.canvasW = canvasW;
        this.canvasH = canvasH;
    }

    public void render(GraphicsContext gc) {
        gc.save();
        gc.translate(canvasX + canvasW / 2, canvasY + canvasH / 2);
        gc.scale(1, -1);
        gc.setLineCap(StrokeLineCap.ROUND);

        final double xToCanvas = canvasW / (maxX - minX);
        final double yToCanvas = canvasH / (maxY - minY);
        final Vector graphCenter = new Vector((minX + maxX) / 2, (minY + maxY) / 2);

        for(RenderedFunction f : functions) {
            gc.beginPath();
            try {
                gc.moveTo((f.x.get(f.minT) - graphCenter.x) * xToCanvas, (f.y.get(f.minT) - graphCenter.y) * yToCanvas);

                for (double t = f.minT; t <= f.maxT; ) {
                    final double tOfX = f.x.get(t);
                    final double tOfY = f.y.get(t);
                    final double tOfR = f.r.get(t);
                    final double tOfG = f.g.get(t);
                    final double tOfB = f.b.get(t);
                    final double tOfsize = f.size.get(t);

                    final double h = derivativePrecision;
                    final double xDerivative = (f.x.get(t + h) - tOfX) / h;
                    final double yDerivative = (f.y.get(t + h) - tOfY) / h;
                    final double pixelDistDerivative = Math.sqrt(Math.pow(xDerivative * xToCanvas, 2) + Math.pow(yDerivative * yToCanvas, 2));

                    gc.beginPath();

                    if (tOfX >= minX && tOfX <= maxX && tOfY >= minY && tOfY <= maxY) {
                        gc.setStroke(Color.color(tOfR, tOfG, tOfB));
                        gc.setLineWidth(tOfsize);
                        gc.lineTo((tOfX - graphCenter.x) * xToCanvas, (tOfY - graphCenter.y) * yToCanvas);
                    } else {
                        gc.moveTo((tOfX - graphCenter.x) * xToCanvas, (tOfY - graphCenter.y) * yToCanvas);
                    }

                    gc.stroke();
                    gc.closePath();

                    t += resolution / pixelDistDerivative;
                }
            } catch (NullPointerException ignored) {}
        }
        gc.restore();
    }

    public Vector toCanvas(double x, double y){

        final double xToCanvas = canvasW / (maxX - minX);
        final double yToCanvas = canvasH / (maxY - minY);
        final Vector graphCenter = new Vector((minX + maxX) / 2, (minY + maxY) / 2);

        return new Vector((x - graphCenter.x) * xToCanvas + canvasX + canvasW / 2, (graphCenter.y - y) * yToCanvas + canvasY + canvasH / 2);
    }

    public Vector toFrame(double x, double y){

        final double canvasToX = (maxX - minX) / canvasW;
        final double canvasToY = (maxY - minY) / canvasH;
        final Vector graphCenter = new Vector((minX + maxX) / 2, (minY + maxY) / 2);

        return new Vector((x - canvasX - canvasW / 2) * canvasToX + graphCenter.x, (canvasH / 2 - canvasY - y) * canvasToY  + graphCenter.y);

    }

    public Vector toCanvas(Vector v){
        return toCanvas(v.x, v.y);
    }

    public Vector toFrame(Vector v){
        return toFrame(v.x, v.y);
    }

    public void renderInFrame(GraphicsAction action, GraphicsContext gc){
        gc.save();

        final double xToCanvas = canvasW / (maxX - minX);
        final double yToCanvas = canvasH / (maxY - minY);
        final Vector graphCenter = new Vector((minX + maxX) / 2, (minY + maxY) / 2);

        gc.translate(canvasX + canvasW / 2 - graphCenter.x * xToCanvas, canvasY + canvasH / 2 - graphCenter.y * yToCanvas);
        gc.scale(xToCanvas, -yToCanvas);
        action.render(gc);

        gc.restore();
    }

//    private double findNearest(double t){
//        List<Function<Double>> functions = Arrays.asList(x, y, r, g, b, size);
//
//        boolean notNull = false;
//
//        double interval = 10;
//        double lastT = t;
//        double lastNull = t;
//
//        while (!notNull) {
//            notNull = true;
//            for (Function<Double> f : functions) {
//                if (f.get(t) == null) {
//                    notNull = false;
//                    break;
//                }
//            }
//            lastT += interval;
//        }
//
//        notNull = false;
//        while(Math.abs(lastT - lastNull) > 0.01) {
//            while (!notNull) {
//                notNull = true;
//                for (Function<Double> f : functions) {
//                    if (f.get(t) == null) {
//                        notNull = false;
//                        break;
//                    }
//                }
//                lastT += interval;
//            }
//        }
//    }
}
