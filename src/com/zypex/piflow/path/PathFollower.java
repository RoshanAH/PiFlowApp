package com.zypex.piflow.path;

import com.zypex.piflow.DrivePowers;
import com.zypex.piflow.DrivetrainConfig;
import com.zypex.piflow.path.Path;
import com.zypex.piflow.profile.Profile;
import com.zypex.piflow.profile.ProfileBuilder;

public class PathFollower {

    private final Profile profile;
    private final Path path;
    private final DrivetrainConfig config;
    public int pathSection;

    public PathFollower(DrivetrainConfig config, Path path){
        this.config = config;
        this.path = path;
        profile = new ProfileBuilder(config, path).build();
    }

    public DrivePowers getPowers(double x, double y){
        return null;
    }

}
