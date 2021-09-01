package com.zypex.piflow;

public class DrivetrainConfig{

    public double maxVelocity;
    public double maxAcceleration;
    public double maxJerk;

    public double turnSpeed = 1;

    public PIDFConstants velocityPID;

    public DrivetrainConfig(double maxVelocity, double maxAcceleration, double maxJerk) {
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
        this.maxJerk = maxJerk;
    }
}
