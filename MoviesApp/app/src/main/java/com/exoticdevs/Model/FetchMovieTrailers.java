package com.exoticdevs.Model;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.exoticdevs.Data.MoviesContract;
import com.exoticdevs.moviesapp.BuildConfig;

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
public class FetchMovieTrailers  extends AsyncTask<String, Void, Void>  {

    private final Context mContext;
    private long mDbMovieID;

    private final String LOG_TAG = FetchMovieTrailers.class.getSimpleName();

    public FetchMovieTrailers(Context context, long dbMovieID ) {
        mContext = context;
        mDbMovieID = dbMovieID;
    }

    /**
     * Take the String representing the complete movie's trailers in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     *constructor takes the JSON string and converts it
     * into an Object hierarchy.
     */
    private void getMovieTrailersDataFromJson(String moviesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MT_LIST = "results";
        final String MT_ID = "id";
        final String MT_KEY = "key";
        final String MT_NAME = "name";
        final String MT_SIZE = "size";
        final String MT_TYPE = "type";

        try{
        JSONObject trailersJson = new JSONObject(moviesJsonStr);
        JSONArray trailersArray = trailersJson.getJSONArray(MT_LIST);

        // clear movie trailers at certain movieID to add its new trailers
        String[] selectionArgs = new String[]{String.valueOf(mDbMovieID)};
        String where = MoviesContract.TrailerEntry.TABLE_NAME+
                "." + MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY + " = ? ";

        int rowId =  mContext.getContentResolver().delete(MoviesContract.TrailerEntry.CONTENT_URI,
                where, selectionArgs);
        Log.v(LOG_TAG, "deleted " + rowId);

        // Insert the new Trailers information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(trailersArray.length());

        for(int i = 0; i < trailersArray.length(); i++) {

            String trailerId;
            String key;
            String name;
            int size;
            String type;

            // Get the JSON object representing the movie's trailer
            JSONObject trailerObj = trailersArray.getJSONObject(i);

            trailerId = trailerObj.getString(MT_ID);
            key = trailerObj.getString(MT_KEY);
            name = trailerObj.getString(MT_NAME);
            size = trailerObj.getInt(MT_SIZE);
            type = trailerObj.getString(MT_TYPE);

            ContentValues contentValue = new ContentValues();
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_KEY, mDbMovieID);
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_TRAILER_ID, trailerId);
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_KEY, key);
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_NAME, name);
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_SIZE, size);
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_TYPE, type);

            cVVector.add(contentValue);
        }

        int inserted = 0;
        // add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MoviesContract.TrailerEntry.CONTENT_URI, cvArray);
        }

        Log.d(LOG_TAG, "FetchTrailers Complete. " + inserted + " Inserted");
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

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String trailersJsonStr = null;

        try {
            // Construct the URL for the themoviedb query
            // Possible parameters are avaiable at themoviedb's API page, at
            // http://docs.themoviedb.apiary.io/#reference/movies/movieidvideos
            final String FORECAST_BASE_URL =
                    "http://api.themoviedb.org/3/movie/" + params[0] + "/videos";;
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
            trailersJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the trailers data, there's no point in attempting
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
             getMovieTrailersDataFromJson(trailersJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the trailers.
        return null;
    }
}

