package com.exoticdevs.AppClasses;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mac on 3/23/16.
 */
public class Movie implements Parcelable{

    private int id;
    private long dbMovieID;
    private String poster;
    private String title;
    private String release_date;
    private String overview;
    private double vote_average;
    private String sort;

    public Movie() {
    }

    public Movie(long dbMovieID, int movie_id, String movie_poster, String title, String release_date,
                 String overview, double vote_average, String sort) {
        this.dbMovieID = dbMovieID;
        this.id = movie_id;
        this.poster = movie_poster;
        this.title = title;
        this.release_date = release_date;
        this.overview = overview;
        this.vote_average = vote_average;
        this.sort = sort;
    }

    public String getSort() {
        return sort;
    }

    public long getDbMovieID() {
        return dbMovieID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMovie_poster() {
        return poster;
    }

    public void setMovie_poster(String movie_poster) {
        this.poster = movie_poster;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getVote_average() {
        return vote_average;
    }

    public void setVote_average(double vote_average) {
        this.vote_average = vote_average;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(dbMovieID);
        dest.writeInt(id);
        dest.writeString(poster);
        dest.writeString(title);
        dest.writeString(release_date);
        dest.writeString(overview);
        dest.writeDouble(vote_average);
        dest.writeString(sort);
    }

    protected Movie(Parcel in) {
        dbMovieID = in.readLong();
        id = in.readInt();
        poster = in.readString();
        title = in.readString();
        release_date = in.readString();
        overview = in.readString();
        vote_average = in.readDouble();
        sort = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
