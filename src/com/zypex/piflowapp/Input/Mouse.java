package com.zypex.piflowapp.Input;

import utils.math.Vector;

import java.util.ArrayList;
import java.util.List;

public class Mouse {
    public static Vector position = new Vector(0, 0);
    public static boolean leftButton = false;
    public static boolean rightButton = false;

    private static List<Runnable> leftClickEvents = new ArrayList<>();
    private static List<Runnable> rightClickEvents = new ArrayList<>();

    public static void callLeftClick(){
        for(Runnable r : leftClickEvents) r.run();
    }
    public static void callRightClick(){
        for(Runnable r : rightClickEvents) r.run();
    }

    public static void addOnLeftClick(Runnable runnable){
        leftClickEvents.add(runnable);
    }
    public static void addOnRightClick(Runnable runnable){
        rightClickEvents.add(runnable);
    }

}
