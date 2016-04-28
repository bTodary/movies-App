package com.exoticdevs.Model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.exoticdevs.Data.MoviesContract;
import com.exoticdevs.Fragments.DetailFragment;
import com.exoticdevs.moviesapp.BuildConfig;
import com.exoticdevs.moviesapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by mac on 4/13/16.
 */
public class FetchMovieReviews extends AsyncTask<String, Void, Void> {

    private final Context mContext;
    private long mApiMovieID;

    private final String LOG_TAG = FetchMovieReviews.class.getSimpleName();

    public FetchMovieReviews(Context context) {
        mContext = context;
    }


    /**
     * Take the String representing the complete movie's reviews in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     *constructor takes the JSON string and converts it
     * into an Object hierarchy.
     */
    private void getMovieReviewsDataFromJson(String moviesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MR_LIST = "results";
        final String MR_ID = "id";
        final String MR_AUTHOR = "author";
        final String MR_CONTENT = "content";
        final String MR_URL = "url";

        try{

        JSONObject reviewsJson = new JSONObject(moviesJsonStr);
        JSONArray reviewsArray = reviewsJson.getJSONArray(MR_LIST);

            if(reviewsArray.length() == 0){
                FragmentManager fragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();
                DetailFragment detailFragment = (DetailFragment) fragmentManager.findFragmentById(R.id.fragmentDetail);
                if(detailFragment != null) {
                    detailFragment.noReviewsAvailable();
                }
                return;
            }

            // clear movie reviews at certain movieID to add its new reviews
            String where = MoviesContract.ReviewEntry.TABLE_NAME+
                    "." + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

            String[] selectionArgs = new String[]{String.valueOf(mApiMovieID)};

            int rowId =  mContext.getContentResolver().delete(MoviesContract.ReviewEntry.CONTENT_URI,
                    where, selectionArgs);
            Log.v(LOG_TAG, "deleted " + rowId + "where id = " + mApiMovieID);

            // Insert the new reviews information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(reviewsArray.length());

        for(int i = 0; i < reviewsArray.length(); i++) {

            String reviewId;
            String author;
            String content;
            String url;

            // Get the JSON object representing the movie's review
            JSONObject reviewObj = reviewsArray.getJSONObject(i);

            reviewId = reviewObj.getString(MR_ID);
            author = reviewObj.getString(MR_AUTHOR);
            content = reviewObj.getString(MR_CONTENT);
            url = reviewObj.getString(MR_URL);

            ContentValues contentValue = new ContentValues();
            contentValue.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, mApiMovieID);
            contentValue.put(MoviesContract.ReviewEntry.COLUMN_REVIEW_ID, reviewId);
            contentValue.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, author);
            contentValue.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, content);
            contentValue.put(MoviesContract.ReviewEntry.COLUMN_URL, url);

            cVVector.add(contentValue);
        }
            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                inserted = mContext.getContentResolver().bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchReviews Complete. " + inserted + " Inserted");
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(String... params) {

        // If there's no movie id, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }

        mApiMovieID = Long.parseLong(params[0]);

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String reviewsJsonStr = null;

        try {
            // Construct the URL for the themoviedb query
            // Possible parameters are avaiable at themoviedb's API page, at
            // http://docs.themoviedb.apiary.io/#reference/movies/movieidreviews
            final String FORECAST_BASE_URL =
                    "http://api.themoviedb.org/3/movie/" + params[0] + "/reviews";;
            final String APIKEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            reviewsJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the reviews data, there's no point in attempting
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            getMovieReviewsDataFromJson(reviewsJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the reviews.
        return null;
    }
}

