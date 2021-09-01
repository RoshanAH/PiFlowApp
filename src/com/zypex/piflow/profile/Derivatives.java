package com.zypex.piflow.profile;

public class Derivatives<T> {
    public T position;
    public T velocity;
    public T acceleration;
    public T jerk;

    public Derivatives(T position, T velocity, T acceleration, T jerk) {
        this.position = position;
        this.velocity = velocity;
        this.acceleration = acceleration;
        this.jerk = jerk;
    }
}
