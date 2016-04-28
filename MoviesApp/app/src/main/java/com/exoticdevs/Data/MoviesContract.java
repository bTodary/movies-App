package com.exoticdevs.Data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by mac on 4/14/16.
 */
public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "com.exoticdevs.moviesapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";
    public static final String PATH_TRAILER = "trailer";
    public static final String PATH_REVIEW = "review";
    public static final String PATH_FAVORITE = "favorite";

    /*
    Inner class that defines the table contents of the movie table
 */
    public static final class MovieEntry implements BaseColumns {

        public static final String TABLE_NAME = "movie";
        // movie id as returned by API
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_SORT = "sort";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieWithSortUri(String sort) {
            return CONTENT_URI.buildUpon().appendPath(sort).build();
        }

        public static String getLastPathFromUri(Uri uri) {
            // 1 is the index to return the element at the specified argument.
            return uri.getPathSegments().get(1);
        }

        public static Uri buildMovieAtSortWithId(String sort, long id) {
            return CONTENT_URI.buildUpon().appendPath(sort)
                    .appendPath(Long.toString(id)).build();
        }

        public static long getIdFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }
    }

    /* Inner class that defines the table trailers of the movie table */
    public static final class TrailerEntry implements BaseColumns {

        public static final String TABLE_NAME = "trailer";

        // movie id as returned by API and also the foreign key into the movie table.
        public static final String COLUMN_MOVIE_ID = "movieId";

        // trailer id as returned by API
        public static final String COLUMN_TRAILER_ID = "trailer_id";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SIZE = "size";
        public static final String COLUMN_TYPE = "type";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIdFromUri(Uri uri) {
            // 1 is the index to return the element at the specified sort.
            return uri.getPathSegments().get(1);
        }

    }

    /* Inner class that defines the table reviews of the movie table */
    public static final class ReviewEntry implements BaseColumns {

        public static final String TABLE_NAME = "review";

        // movie id as returned by API
        public static final String COLUMN_MOVIE_ID = "movieId";

        // review id as returned by API
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_URL = "url";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIdFromUri(Uri uri) {
            // 1 is the index to return the element at the specified sort.
            return uri.getPathSegments().get(1);
        }
    }

    /* Inner class that defines the table favorite of the movie table */
    public static final class FavEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorite";

        // movie id as returned by API
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        public static Uri buildFavUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIdFromUri(Uri uri) {
            // 1 is the index to return the element at the specified sort.
            return uri.getPathSegments().get(1);
        }
    }
}

