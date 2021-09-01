package com.zypex.piflow.profile;

import utils.math.PiecewiseFunction;
import utils.math.Vector;

public class Profile {

    public final PiecewiseFunction<Derivatives<Vector>> profile;
    public final PiecewiseFunction<Derivatives<Double>> headingProfile;

    Profile(PiecewiseFunction<Derivatives<Vector>> profile, PiecewiseFunction<Derivatives<Double>> headingProfile){
        this.profile = profile;
        this.headingProfile = headingProfile;
    }

    public Derivatives<Vector> getPos(double t){
        return profile.get(t);
    }

}
