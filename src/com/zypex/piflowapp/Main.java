package com.zypex.piflowapp;

import com.zypex.piflow.DrivetrainConfig;
import com.zypex.piflow.profile.Arc;
import com.zypex.piflow.profile.Derivatives;
import com.zypex.piflow.profile.ProfileBuilder;
import com.zypex.piflowapp.Graphics.FunctionRenderer;
import com.zypex.piflowapp.Graphics.RenderedFunction;
import com.zypex.piflowapp.Input.Mouse;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.util.Duration;
import utils.math.BoundedFunction;
import utils.math.Function;
import utils.math.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main extends Application {

    private GraphicsContext gc;
    private final double initialHeight = 600;
    private final double initialWidth = 600;

    private DrivetrainConfig config = new DrivetrainConfig(3, 2, 3);

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Hello World");

        Canvas canvas = new Canvas(initialWidth, initialHeight);
        Group root = new Group(canvas);

        gc = canvas.getGraphicsContext2D();

        Timeline mainLoop = new Timeline();
        mainLoop.setCycleCount(Timeline.INDEFINITE);

        KeyFrame kf = new KeyFrame(Duration.seconds(1d / 60),
                (ActionEvent event) -> {
                    onUpdate();
                }
        );
        canvas.setOnMousePressed(e ->{
            if(e.isPrimaryButtonDown()) Mouse.leftButton = true;
            if(e.isSecondaryButtonDown()) Mouse.rightButton = true;
        });

        canvas.setOnMouseReleased(e ->{
            if(!e.isPrimaryButtonDown()) Mouse.leftButton = false;
            if(!e.isSecondaryButtonDown()) Mouse.rightButton = false;
        });

        canvas.setOnMouseMoved(e ->{
            Mouse.position.x = e.getX();
            Mouse.position.y = e.getY();
        });

        canvas.setOnMouseDragged(e ->{
            Mouse.position.x = e.getX();
            Mouse.position.y = e.getY();
        });

        canvas.setOnMouseClicked(e ->{
            switch (e.getButton()){
                case PRIMARY -> Mouse.callLeftClick();
                case SECONDARY -> Mouse.callRightClick();
            }
        });

        mainLoop.getKeyFrames().add(kf);
        mainLoop.play();

        primaryStage.setScene(new Scene(root, initialWidth, initialHeight));
        primaryStage.show();
    }

    FunctionRenderer renderer = new FunctionRenderer(0, 0, 600, 600);
    BoundedFunction<Derivatives<Double>> profile = ProfileBuilder.CreateVelocityChange(0, 5, config);
    List<Vector> points = new ArrayList<>();
    List<Vector> centers = new ArrayList<>();
    List<Arc> arcs = new ArrayList<>();

    public void init() {

        renderer.minX = -15;
        renderer.maxX = 15;
        renderer.minY = -15;
        renderer.maxY = 15;
        renderer.resolution = 1;

        points.add(new Vector(-5, -14));
        points.add(Vector.Polar(10, 0.1 * Math.PI).add(points.get(points.size() - 1)));
        points.add(Vector.Polar(10, 0.9 * Math.PI).add(points.get(points.size() - 1)));
        points.add(Vector.Polar(10, 0.2 * Math.PI).add(points.get(points.size() - 1)));
        points.add(Vector.Polar(10, 1 * Math.PI).add(points.get(points.size() - 1)));
        points.add(Vector.Polar(5, 0.3 * Math.PI).add(points.get(points.size() - 1)));
        points.add(Vector.Polar(5, 0.9 * Math.PI).add(points.get(points.size() - 1)));
        points.add(Vector.Polar(10, 0.1 * Math.PI).add(points.get(points.size() - 1)));

        for(int i = 1; i < points.size() - 1; i++){
            final Vector start = points.get(i - 1);
            final Vector middle = points.get(i);
            final Vector end = points.get(i + 1);

            final Arc turn = ProfileBuilder.CreateTurn(start, middle, end, config, config.maxVelocity);
//            renderer.functions.add(new RenderedFunction(turn.lowerBound, turn.upperBound)
//                    .attachX(t -> turn.get(t).position.x)
//                    .attachY(t -> turn.get(t).position.y)
//                    .setColor(Color.BLUE)
//                    .setSize(2)
//            );
        }

        arcs = ProfileBuilder.CreateInterpolation(points, config, config.maxVelocity);

        for(Arc arc : arcs){
            renderer.functions.add(new RenderedFunction(arc.lowerBound(), arc.upperBound())
                    .attachX(t -> arc.get(t).position.x)
                    .attachY(t -> arc.get(t).position.y)
                    .setSize(2)
                    .setColor(Color.GREEN)
            );
        }

//        renderer.functions.add(new RenderedFunction(profile.lowerBound(), profile.upperBound())
//                .attachX(t -> t)
//                .attachY(t -> profile.get(t).position)
//                .setSize(2)
//                .setColor(Color.BLUE)
//        );

        centers = calcCenters();

//        System.out.println(centers);

    }

    private List<Vector> calcCenters(){
        List<Function<Vector>> axes = new ArrayList<>();

        for(int i = 0; i < points.size() - 2; i++){
            final Vector start = points.get(i);
            final Vector middle = points.get(i + 1);
            final Vector end = points.get(i + 2);
            final Vector turnCenter = ProfileBuilder.FindTurnCenter(start, middle, end, config, config.maxVelocity);

            if(i == 0) axes.add(t -> middle.subtract(start).normalize().scale(t).add(turnCenter));
            else if (i == points.size() - 3) axes.add(t -> middle.subtract(end).normalize().scale(t).add(turnCenter));
            else axes.add(t -> start.subtract(middle).normalize().add(end.subtract(middle).normalize()).normalize().scale(-t).add(turnCenter));
        }

        final double r = ProfileBuilder.FindTurningRadius(config, config.maxVelocity);

        List<Vector> centers = new ArrayList<>();
        {
            final Vector initial = axes.get(0).get(0d);
            final Vector slope = axes.get(0).get(1d).subtract(initial);

            final Vector initial2 = axes.get(1).get(0d);
            final Vector slope2 = axes.get(1).get(1d).subtract(initial2).scale(1d / (axes.size() - 1));

            final double a = slope2.subtract(slope).square();
            final double b = 2 * initial2.subtract(initial).dot(slope2.subtract(slope));
            final double c = initial2.subtract(initial).square() - 4 * r * r;

            final double t = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);

            centers.add(initial.add(slope.scale(t)));
            centers.add(initial2.add(slope2.scale(t)));

//            System.out.println(initial.add(slope.scale(t)).subtract(initial2.add(slope2.scale(t))).getMagnitude() / r);
        }

        for(int i = 2; i < axes.size(); i++){
            final Vector initial = axes.get(i).get(0d);
            final Vector slope = axes.get(i).get(1d).subtract(initial);
            final Vector last = centers.get(i - 1);

            final double a = slope.square();
            final double b = 2 * initial.subtract(last).dot(slope);
            final double c = initial.subtract(last).square() - 4 * r * r;

            final double determinate = Math.sqrt(b * b - 4 * a * c);
            final double t1 = (-b - determinate) / (2 * a);
            final double t2 = (-b + determinate) / (2 * a);

            final Function<Vector> axis = axes.get(i);
            final Vector center;

            if(axis.get(t1).subtract(initial).getMagnitude() < axis.get(t2).subtract(initial).getMagnitude()){
                center = axis.get(t1);
            }else{
                center = axis.get(t2);
            }

//            System.out.println(center.subtract(centers.get(centers.size() - 1)).getMagnitude() / r);
            centers.add(center);
        }

        return centers;
    }

    private void onUpdate() {

        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        gc.setLineCap(StrokeLineCap.ROUND);

        final Vector mousePos = renderer.toFrame(Mouse.position);
        final Vector closest = arcs.get(0).get(arcs.get(0).getT(mousePos)).position;

        renderer.renderInFrame(gc -> {
            gc.setLineWidth(0.1);

            gc.beginPath();
            gc.moveTo(points.get(0).x, points.get(0).y);
            for(int i = 1; i < points.size(); i++) gc.lineTo(points.get(i).x, points.get(i).y);
            gc.stroke();
            gc.closePath();


            gc.setFill(Color.BLACK);
//            for(Arc a : arcs) gc.fillOval(a.center.x - 0.2, a.center.y - 0.2, 0.4, 0.4);


            gc.setLineWidth(0.3);
            gc.setFill(Color.RED);
//            gc.fillOval(mousePos.x - 0.2, mousePos.y - 0.2, 0.4, 0.4);
            gc.fillOval(closest.x - 0.2, closest.y - 0.2, 0.4, 0.4);

        }, gc);
        renderer.render(gc);

//        System.out.println(mousePos);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
