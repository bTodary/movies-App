package com.exoticdevs.AppClasses;

/**
 * Created by mac on 4/13/16.
 */
public class Trailer{

    private long dbMovieID;
    private String trailerId;
    private String key;
    private String name;
    private int size;
    private String type;

    public Trailer(long dbMovieID, String trailerId, String key, String name, int size, String type) {
        this.trailerId = trailerId;
        this.key = key;
        this.name = name;
        this.size = size;
        this.type = type;
        this.dbMovieID = dbMovieID;
    }

    public long getDbMovieID() {
        return dbMovieID;
    }

    public String getTrailerId() {
        return trailerId;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public String getType() {
        return type;
    }
}
