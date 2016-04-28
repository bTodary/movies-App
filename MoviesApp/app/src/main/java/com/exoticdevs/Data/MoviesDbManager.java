package com.exoticdevs.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.exoticdevs.AppClasses.Movie;
import com.exoticdevs.AppClasses.Review;
import com.exoticdevs.AppClasses.Trailer;
import com.exoticdevs.Data.MoviesContract.FavEntry;
import com.exoticdevs.Data.MoviesContract.MovieEntry;
import com.exoticdevs.Data.MoviesContract.ReviewEntry;
import com.exoticdevs.Data.MoviesContract.TrailerEntry;

import java.util.ArrayList;

/**
 * Created by mac on 4/14/16.
 */
public class MoviesDbManager {

    private final String LOG_TAG = MoviesDbManager.class.getSimpleName();

    private MoviesDbHelper mDbHelper;
    private SQLiteDatabase mDatabase;

    public MoviesDbManager(Context context) {
        mDbHelper = new MoviesDbHelper(context);
    }

    public MoviesDbManager open_db() throws SQLException {
        mDatabase = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    public int bulkInsertMovie(ContentValues[] values){
        int returnCount = 0;
        try {
            open_db();
            for (ContentValues value : values) {
                long _id = mDatabase.insert(MovieEntry.TABLE_NAME, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
        }finally {
            close();
        }
        return returnCount;
    }
    public long insertMovie(int movieId, String poster, String title, String release_date, String overview,
                            double vote_average, String sort) {
            long id = -1;
            try {
                open_db();
                ContentValues contentValue = new ContentValues();
                contentValue.put(MovieEntry.COLUMN_MOVIE_ID, movieId);
                contentValue.put(MovieEntry.COLUMN_POSTER, poster);
                contentValue.put(MovieEntry.COLUMN_TITLE, title);
                contentValue.put(MovieEntry.COLUMN_RELEASE_DATE, release_date);
                contentValue.put(MovieEntry.COLUMN_OVERVIEW, overview);
                contentValue.put(MovieEntry.COLUMN_VOTE_AVERAGE, vote_average);
                contentValue.put(MovieEntry.COLUMN_SORT, sort);

                id = mDatabase.insert(MovieEntry.TABLE_NAME, null, contentValue);
            }finally {
                close();
            }
            return id;
        }

    public int clearMovieTableAtSort(String sort) {
        int rowId = -1;
            int length = getMoviesCount();
        try {
            open_db();
            for (int i = 0; i < length; i++) {
                rowId = mDatabase.delete(MovieEntry.TABLE_NAME, MovieEntry.COLUMN_SORT + " = '" + sort + "'", null);
            }
        }finally {
            close();
        }
        return rowId;
        }

    public int getMoviesCount() {
            try{
                open_db();
                String countQuery = "SELECT  * FROM " + MovieEntry.TABLE_NAME;
                Cursor cursor = mDatabase.rawQuery(countQuery, null);
                if(cursor==null)
                    return 0;
                return cursor.getCount();
            }finally {
                close();
            }
        }

    public ArrayList<Movie> getMovies() {
            ArrayList<Movie> moviesArrayList = new ArrayList<>();
            try {
                open_db();
                String countQuery = "SELECT  * FROM " + MovieEntry.TABLE_NAME;
                Cursor cursor = mDatabase.rawQuery(countQuery, null);

                if (cursor != null) {  // looping through all rows and adding to list

                    while (cursor.moveToNext()) {
                        long dbMovieID = cursor.getLong(cursor.getColumnIndex(MovieEntry._ID));
                        int id = cursor.getInt(cursor.getColumnIndex((MovieEntry.COLUMN_MOVIE_ID)));
                        String poster = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER));
                        String title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
                        String release_date = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
                        String overview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
                        double vote_average = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));
                        String sort = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_SORT));

                        moviesArrayList.add(new Movie(dbMovieID, id, poster, title, release_date, overview, vote_average, sort));
                    }
                }
            }finally {
                close();
            }
            return moviesArrayList;
        }

    public ArrayList<Movie> getMoviesAtSort(String sort)
    {
        ArrayList<Movie> moviesArrayList = new ArrayList<>();
        try{
            open_db();
        String countQuery = "SELECT * FROM " + MovieEntry.TABLE_NAME + " WHERE "
                + MovieEntry.COLUMN_SORT + " = '" + sort + "'";
        Cursor cursor = mDatabase.rawQuery(countQuery, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long dbMovieID = cursor.getLong(cursor.getColumnIndex(MovieEntry._ID));
                int id = cursor.getInt(cursor.getColumnIndex((MovieEntry.COLUMN_MOVIE_ID)));
                String poster = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER));
                String title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
                String release_date = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
                String overview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
                double vote_average = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));
                String movieSort = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_SORT));

                moviesArrayList.add(new Movie(dbMovieID, id, poster, title, release_date, overview, vote_average, movieSort));
            }
         }
        }finally {
            close();
        }
        return moviesArrayList;
    }

    // movieId is the id returned from insertMovie() not the id returned from API
    public long insertMovieTrailers(long movieId, String trailerId, String key, String name, int size, String type) {
        long id = -1;
        try {
            open_db();
            ContentValues contentValue = new ContentValues();
            contentValue.put(TrailerEntry.COLUMN_MOVIE_KEY, movieId);
            contentValue.put(TrailerEntry.COLUMN_TRAILER_ID, trailerId);
            contentValue.put(TrailerEntry.COLUMN_KEY, key);
            contentValue.put(TrailerEntry.COLUMN_NAME, name);
            contentValue.put(TrailerEntry.COLUMN_SIZE, size);
            contentValue.put(TrailerEntry.COLUMN_TYPE, type);

            id = mDatabase.insert(TrailerEntry.TABLE_NAME, null, contentValue);
        }finally {
            close();
        }
        return id;
    }

    public void clearTrailerTableAtMovieId(long dbMovieId) {
        int rowId = -1;
        int length = getTrailersCount();
        try {
            open_db();
            for (int i = 0; i < length; i++) {
                rowId = mDatabase.delete(TrailerEntry.TABLE_NAME, TrailerEntry.COLUMN_MOVIE_KEY + " = '" + dbMovieId + "'", null);
            }
        }finally {
            close();
        }
    }

    public ArrayList<Trailer> getTrailersAtMovieID(long dbMovieId) {
        ArrayList<Trailer> movieTrailersArrayList = new ArrayList<>();
        try {
            open_db();
            String countQuery = "SELECT  * FROM " + TrailerEntry.TABLE_NAME + " WHERE "
                    + TrailerEntry.COLUMN_MOVIE_KEY + " = '" + dbMovieId + "'";
            Cursor cursor = mDatabase.rawQuery(countQuery, null);

            if (cursor != null) {  // looping through all rows and adding to list

                while (cursor.moveToNext()) {
                    long dbMovieID = cursor.getLong(cursor.getColumnIndex(TrailerEntry.COLUMN_MOVIE_KEY));
                    String trailerId = cursor.getString(cursor.getColumnIndex((TrailerEntry.COLUMN_TRAILER_ID)));
                    String key = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_KEY));
                    String name = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_NAME));
                    int size = cursor.getInt(cursor.getColumnIndex(TrailerEntry.COLUMN_SIZE));
                    String type = cursor.getString(cursor.getColumnIndex(TrailerEntry.COLUMN_TYPE));

                    movieTrailersArrayList.add(new Trailer(dbMovieID, trailerId, key, name, size, type));
                }
            }
        }finally {
            close();
        }
        return movieTrailersArrayList;
    }

    public int getMovieTrailersCountAtMovieID(long dbMovieId) {
        try{
            open_db();
            String countQuery = "SELECT  * FROM " + TrailerEntry.TABLE_NAME + " WHERE "
                    + TrailerEntry.COLUMN_MOVIE_KEY + " = '" + dbMovieId + "'";
            Cursor cursor = mDatabase.rawQuery(countQuery, null);
            if(cursor==null)
                return 0;
            return cursor.getCount();
        }finally {
            close();
        }
    }

    // movieId is the id returned from insertMovie() not the id returned from API
    public long insertMovieReviews(long movieId, String reviewId, String author, String content, String url) {
        long id = -1;
        try {
            open_db();
            ContentValues contentValue = new ContentValues();
            contentValue.put(ReviewEntry.COLUMN_MOVIE_KEY, movieId);
            contentValue.put(ReviewEntry.COLUMN_REVIEW_ID, reviewId);
            contentValue.put(ReviewEntry.COLUMN_AUTHOR, author);
            contentValue.put(ReviewEntry.COLUMN_CONTENT, content);
            contentValue.put(ReviewEntry.COLUMN_URL, url);

            id = mDatabase.insert(ReviewEntry.TABLE_NAME, null, contentValue);
        }finally {
            close();
        }
        return id;
    }

    public void clearReviewTableAtMovieId(long dbMovieId) {
        int rowId = -1;
        int length = getReviewsCount();
        try {
            open_db();
            for (int i = 0; i < length; i++) {
                rowId = mDatabase.delete(ReviewEntry.TABLE_NAME, ReviewEntry.COLUMN_MOVIE_KEY + " = '" + dbMovieId + "'", null);
            }
        }finally {
            close();
        }
    }

    public ArrayList<Review> getReviewsAtMovieID(long dbMovieId) {
        ArrayList<Review> movieReviewsArrayList = new ArrayList<>();
        try {
            open_db();
            String countQuery = "SELECT  * FROM " + ReviewEntry.TABLE_NAME + " WHERE "
                    + ReviewEntry.COLUMN_MOVIE_KEY + " = '" + dbMovieId + "'";
            Cursor cursor = mDatabase.rawQuery(countQuery, null);

            if (cursor != null) {

                while (cursor.moveToNext()) {
                    long dbMovieID = cursor.getLong(cursor.getColumnIndex(ReviewEntry.COLUMN_MOVIE_KEY));
                    String reviewId = cursor.getString(cursor.getColumnIndex((ReviewEntry.COLUMN_REVIEW_ID)));
                    String author = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_AUTHOR));
                    String content = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_CONTENT));
                    String url = cursor.getString(cursor.getColumnIndex(ReviewEntry.COLUMN_URL));

                    movieReviewsArrayList.add(new Review(dbMovieID, reviewId, author, content, url));
                }
            }
        }finally {
            close();
        }
        return movieReviewsArrayList;
    }

    public int getMovieReviewsCountAtMovieID(long dbMovieId) {
        try{
            open_db();
            String countQuery = "SELECT  * FROM " + ReviewEntry.TABLE_NAME + " WHERE "
                    + ReviewEntry.COLUMN_MOVIE_KEY + " = '" + dbMovieId + "'";
            Cursor cursor = mDatabase.rawQuery(countQuery, null);
            if(cursor==null)
                return 0;
            return cursor.getCount();
        }finally {
            close();
        }
    }


    public long insertMovieFav(int movieId, long dbMovieID, String poster, String title, String release_date,
                               String overview, double vote_average) {
        long id = -1;
        try {
            open_db();
            ContentValues contentValue = new ContentValues();
            contentValue.put(FavEntry.COLUMN_MOVIE_ID, movieId);
            contentValue.put(FavEntry.COLUMN_MOVIE_KEY, dbMovieID);
            contentValue.put(FavEntry.COLUMN_POSTER, poster);
            contentValue.put(FavEntry.COLUMN_TITLE, title);
            contentValue.put(FavEntry.COLUMN_RELEASE_DATE, release_date);
            contentValue.put(FavEntry.COLUMN_OVERVIEW, overview);
            contentValue.put(FavEntry.COLUMN_VOTE_AVERAGE, vote_average);

            id = mDatabase.insert(FavEntry.TABLE_NAME, null, contentValue);
        }finally {
            close();
        }
        return id;
    }

    public ArrayList<Movie> getMoviesFav() {
        ArrayList<Movie> moviesFavArrayList = new ArrayList<>();
        try {
            open_db();
            String countQuery = "SELECT  * FROM " + FavEntry.TABLE_NAME;
            Cursor cursor = mDatabase.rawQuery(countQuery, null);

            if (cursor != null) {

                while (cursor.moveToNext()) {
                    long dbMovieID = cursor.getLong(cursor.getColumnIndex(FavEntry.COLUMN_MOVIE_KEY));
                    int movieId = cursor.getInt(cursor.getColumnIndex((FavEntry.COLUMN_MOVIE_ID)));
                    String poster = cursor.getString(cursor.getColumnIndex(FavEntry.COLUMN_POSTER));
                    String title = cursor.getString(cursor.getColumnIndex(FavEntry.COLUMN_TITLE));
                    String release_date = cursor.getString(cursor.getColumnIndex(FavEntry.COLUMN_RELEASE_DATE));
                    String overview = cursor.getString(cursor.getColumnIndex(FavEntry.COLUMN_OVERVIEW));
                    double vote_average = cursor.getDouble(cursor.getColumnIndex(FavEntry.COLUMN_VOTE_AVERAGE));

                    moviesFavArrayList.add(new Movie(dbMovieID, movieId, poster, title, release_date, overview, vote_average, ""));
                }
            }
        }finally {
            close();
        }
        return moviesFavArrayList;
    }

    public int getMoviesFavCount() {
        try{
            open_db();
            String countQuery = "SELECT  * FROM " + FavEntry.TABLE_NAME;
            Cursor cursor = mDatabase.rawQuery(countQuery, null);
            if(cursor==null)
                return 0;
            return cursor.getCount();
        }finally {
            close();
        }
    }

    public int clearMovieFavAtID(int movieApiId) {
        int rowId = -1;
        int length = getMoviesCount();
        try {
            open_db();
            for (int i = 0; i < length; i++) {
                rowId = mDatabase.delete(FavEntry.TABLE_NAME, FavEntry.COLUMN_MOVIE_ID + " = '" + movieApiId + "'", null);
            }
        }finally {
            close();
        }
        return rowId;
    }

    public boolean isMovieMarkedAsFav(int movieApiId)
    {
        boolean isMarked = false;
        try{
            open_db();
            String countQuery = "SELECT  * FROM " + FavEntry.TABLE_NAME + " WHERE "
                    + FavEntry.COLUMN_MOVIE_ID + " = '" + movieApiId + "'";
            Cursor cursor = mDatabase.rawQuery(countQuery, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex((FavEntry.COLUMN_MOVIE_ID)));
                    if(id == movieApiId){
                        return true;
                    }
                }
            }
        }finally {
            close();
        }
        return isMarked;
    }

    //    public ArrayList<Movie> getMoviesAtFav(int isFav)
//    {
//        ArrayList<Movie> moviesArrayList = new ArrayList<>();
//        try{
//            open_db();
//            String countQuery = "SELECT  * FROM " + MovieEntry.TABLE_NAME + " WHERE "
//                    + MovieEntry.COLUMN_FAV + " = '" + isFav + "'";
//            Cursor cursor = mDatabase.rawQuery(countQuery, null);
//
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    long dbMovieID = cursor.getLong(cursor.getColumnIndex(MovieEntry._ID));
//                    int id = cursor.getInt(cursor.getColumnIndex((MovieEntry.COLUMN_MOVIE_ID)));
//                    String poster = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER));
//                    String title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
//                    String release_date = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
//                    String overview = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW));
//                    double vote_average = cursor.getDouble(cursor.getColumnIndex(MovieEntry.COLUMN_VOTE_AVERAGE));
//                    String movieSort = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_SORT));
//
//                    moviesArrayList.add(new Movie(dbMovieID, id, poster, title, release_date, overview, vote_average, movieSort));
//                }
//            }
//        }finally {
//            close();
//        }
//        return moviesArrayList;
//    }


//    public int updateMovieData(long id, int isFav) {
//        int rowId = -1;
//        try {
//            open_db();
//            ContentValues values = new ContentValues();
//            values.put(MovieEntry.COLUMN_FAV, isFav);
//            rowId = mDatabase.update(MovieEntry.TABLE_NAME, values, MovieEntry._ID + " = '" + id + "'", null);
//        }finally {
//            close();
//        }
//        return rowId;
//    }

    public int getTrailersCount() {
        try{
            open_db();
        String countQuery = "SELECT  * FROM " + TrailerEntry.TABLE_NAME;
        Cursor cursor = mDatabase.rawQuery(countQuery, null);
        if(cursor==null)
            return 0;
        return cursor.getCount();
        }finally {
            close();
        }
    }

    public int getReviewsCount() {
        try{
            open_db();
            String countQuery = "SELECT  * FROM " + ReviewEntry.TABLE_NAME;
            Cursor cursor = mDatabase.rawQuery(countQuery, null);
            if(cursor==null)
                return 0;
            return cursor.getCount();
        }finally {
            close();
        }
    }
}
