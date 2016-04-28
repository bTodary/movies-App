package com.exoticdevs.AppClasses;

/**
 * Created by mac on 4/13/16.
 */
public class Review {

    private long dbMovieID;
    private String id;
    private String author;
    private String content;
    private String url;

    public Review(long dbMovieID, String id, String author, String content, String url) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.url = url;
        this.dbMovieID = dbMovieID;
    }

    public long getDbMovieID() {
        return dbMovieID;
    }

    public String getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getUrl() {
        return url;
    }
}
