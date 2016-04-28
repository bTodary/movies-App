package com.exoticdevs.Data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by mac on 4/25/16.
 */
public class MoviesProvider extends ContentProvider{

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    static final int MOVIES = 100;
    static final int FAV = 101;
    static final int MOVIES_WITH_SORT = 102;
    static final int TRAILER = 103;
    static final int TRAILER_WITH_ID = 104;
    static final int REVIEW = 105;
    static final int REVIEW_WITH_ID = 106;

    static final int SINGLE_MOVIE = 107;
    static final int SINGLE_FAV = 108;

    static final int MOVIES_WITH_SORT_AND_ID = 109;


    //movies.sort = ?
    private static final String sSortSettingSelection =
            MoviesContract.MovieEntry.TABLE_NAME+
                    "." + MoviesContract.MovieEntry.COLUMN_SORT + " = ? ";

    //movies.sort = ? AND _ID >= ?
    private static final String sSortSettingWithIdSelection =
            MoviesContract.MovieEntry.TABLE_NAME+
                    "." + MoviesContract.MovieEntry.COLUMN_SORT + " = ? AND " +
                    MoviesContract.MovieEntry._ID + " >= ? ";

    //favorite._ID = ?
    private static final String sFavSelection =
            MoviesContract.FavEntry.TABLE_NAME+
                    "." + MoviesContract.FavEntry._ID + " = ? ";

    //movie._ID = ?
    private static final String sMovieSelection =
            MoviesContract.MovieEntry.TABLE_NAME+
                    "." + MoviesContract.MovieEntry._ID + " = ? ";

    //trailer.movieId = ?
    private static final String sTrailerIdSelection =
            MoviesContract.TrailerEntry.TABLE_NAME+
                    "." + MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

    //review.movieId = ?
    private static final String sReviewIdSelection =
            MoviesContract.ReviewEntry.TABLE_NAME+
                    "." + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

    private Cursor getMoviesBySortSetting(Uri uri, String[] projection, String sortOrder) {

        String sortSetting = MoviesContract.MovieEntry.getLastPathFromUri(uri);
        String selection = sSortSettingSelection;
        String[] selectionArgs = new String[]{sortSetting};

        return mOpenHelper.getReadableDatabase().query(MoviesContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrailersById(Uri uri, String[] projection, String sortOrder) {

        String id = MoviesContract.TrailerEntry.getIdFromUri(uri);

        String selection = sTrailerIdSelection;
        String[] selectionArgs = new String[]{id};

        return mOpenHelper.getReadableDatabase().query(MoviesContract.TrailerEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getReviewsById(Uri uri, String[] projection, String sortOrder) {

        String id = MoviesContract.ReviewEntry.getIdFromUri(uri);

        String selection = sReviewIdSelection;
        String[] selectionArgs = new String[]{id};

        return mOpenHelper.getReadableDatabase().query(MoviesContract.ReviewEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieByMovieId(Uri uri, String[] projection, String sortOrder) {

        String id = MoviesContract.MovieEntry.getLastPathFromUri(uri);

        String selection = sMovieSelection;
        String[] selectionArgs = new String[]{id};

        return mOpenHelper.getReadableDatabase().query(
                MoviesContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getFavByMovieId(Uri uri, String[] projection, String sortOrder) {

        String id = MoviesContract.FavEntry.getIdFromUri(uri);

        String selection = sFavSelection;
        String[] selectionArgs = new String[]{id};

        return mOpenHelper.getReadableDatabase().query(
                MoviesContract.FavEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieByMovieSortAndId(Uri uri, String[] projection, String sortOrder) {

        String sort = MoviesContract.MovieEntry.getLastPathFromUri(uri);
        long id = MoviesContract.MovieEntry.getIdFromUri(uri);

        String selection = sSortSettingWithIdSelection;
        String[] selectionArgs = new String[]{sort, Long.toString(id)};

        return mOpenHelper.getReadableDatabase().query(
                MoviesContract.MovieEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.PATH_FAVORITE, FAV);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/*", MOVIES_WITH_SORT);
        matcher.addURI(authority, MoviesContract.PATH_TRAILER, TRAILER);
        matcher.addURI(authority, MoviesContract.PATH_TRAILER + "/#", TRAILER_WITH_ID);
        matcher.addURI(authority, MoviesContract.PATH_REVIEW, REVIEW);
        matcher.addURI(authority, MoviesContract.PATH_REVIEW + "/#", REVIEW_WITH_ID);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/#", SINGLE_MOVIE);
        matcher.addURI(authority, MoviesContract.PATH_FAVORITE + "/#", SINGLE_FAV);

        matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/*/#", MOVIES_WITH_SORT_AND_ID);


        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        try {
            switch (sUriMatcher.match(uri)) {

                case MOVIES_WITH_SORT_AND_ID:
                {
                    retCursor = getMovieByMovieSortAndId(uri, projection, sortOrder);
                    break;
                }
                case SINGLE_MOVIE: {

                    retCursor = getMovieByMovieId(uri, projection, sortOrder);
                    break;
                }
                case SINGLE_FAV: {

                    retCursor = getFavByMovieId(uri, projection, sortOrder);
                    break;
                }
                case FAV: {
                    retCursor = mOpenHelper.getReadableDatabase().query(MoviesContract.FavEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    break;
                }
                case MOVIES: {
                    Log.v("providerBosy", "query MOVIES");

                    retCursor = mOpenHelper.getReadableDatabase().query(MoviesContract.MovieEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    break;
                }
                case MOVIES_WITH_SORT: {
                    retCursor = getMoviesBySortSetting(uri, projection, sortOrder);
                    break;
                }
                case TRAILER: {
                    retCursor = mOpenHelper.getReadableDatabase().query(MoviesContract.TrailerEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    break;
                }
                case TRAILER_WITH_ID: {
                    retCursor = getTrailersById(uri, projection, sortOrder);
                    break;
                }
                case REVIEW: {
                    retCursor = mOpenHelper.getReadableDatabase().query(MoviesContract.ReviewEntry.TABLE_NAME,
                            projection,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sortOrder);
                    break;
                }
                case REVIEW_WITH_ID: {
                    retCursor = getReviewsById(uri, projection, sortOrder);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);

            // to avoid IllegalStateException: attempt to re-open an already-closed in rare cases
        }catch (IllegalStateException e){
            e.printStackTrace();
            return  null;
        }
        return retCursor;
        }


    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case FAV: // returns multiple items so it gets CONTENT_TYPE which is a dir
                return MoviesContract.FavEntry.CONTENT_TYPE;
            case MOVIES:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case MOVIES_WITH_SORT:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case TRAILER:
                return MoviesContract.TrailerEntry.CONTENT_TYPE;
            case TRAILER_WITH_ID:
                return MoviesContract.TrailerEntry.CONTENT_TYPE;
            case REVIEW:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW_WITH_ID:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            case SINGLE_MOVIE:  //  can only return a single row so it gets CONTENT_ITEM_TYPE which is a single item
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case SINGLE_FAV:
                return MoviesContract.FavEntry.CONTENT_ITEM_TYPE;
            case MOVIES_WITH_SORT_AND_ID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FAV: {
                long _id = db.insert(MoviesContract.FavEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.FavEntry.buildFavUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MOVIES: {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER: {
                long _id = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.TrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEW: {
                long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.ReviewEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case FAV:
                rowsDeleted = db.delete(
                        MoviesContract.FavEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIES:
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER:
                rowsDeleted = db.delete(
                        MoviesContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(
                        MoviesContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows, notifyChange only in case at least one row deleted
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);
            int rowsUpdated;

            switch (match) {
                case FAV:
                    rowsUpdated = db.update(MoviesContract.FavEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                case MOVIES:
                    rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                case TRAILER:
                    rowsUpdated = db.update(MoviesContract.TrailerEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                case REVIEW:
                    rowsUpdated = db.update(MoviesContract.ReviewEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            // notifyChange only in case at least one row updated
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
        }

    // insert multi rows once
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FAV:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.FavEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case MOVIES:
                db.beginTransaction();
                int returnMoviesCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnMoviesCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnMoviesCount;
            case TRAILER:
                db.beginTransaction();
                int returnTrailersCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnTrailersCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnTrailersCount;
            case REVIEW:
                db.beginTransaction();
                int returnReviewsCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnReviewsCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnReviewsCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
