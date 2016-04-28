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
public class FetchMovies extends AsyncTask<Void, Void, Void> {

    private final Context mContext;
    private String mSort;

    private final String LOG_TAG = FetchMovies.class.getSimpleName();

    public FetchMovies(Context context, String sort) {
        mContext = context;
        mSort = sort;
    }

    /**
     * Take the String representing the complete movies in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_LIST = "results";
        final String TMDB_ID = "id";
        final String TMDB_PATH = "poster_path";
        final String TMDB_TITLE = "title";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_VOTE_AVERAGE = "vote_average";

        try{
        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_LIST);

        JSONObject movieObj;

        // clear movie table at certain sort to add new data which is returned from the server
            String where = MoviesContract.MovieEntry.TABLE_NAME+
                            "." + MoviesContract.MovieEntry.COLUMN_SORT + " = ? ";

            String[] selectionArgs = new String[]{mSort};

            int rowId =  mContext.getContentResolver().delete(MoviesContract.MovieEntry.CONTENT_URI,
                        where, selectionArgs);
            Log.v(LOG_TAG, "deleted " + rowId + " " + mSort);

            // Insert the new movies information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());

        for(int i = 0; i < moviesArray.length(); i++) {

            int movieId;
            String moviePoster;
            String title;
            String release_date;
            String overview;
            double vote_average;

            // Get the JSON object representing the movie
            movieObj = moviesArray.getJSONObject(i);

            movieId = movieObj.getInt(TMDB_ID);
            moviePoster = movieObj.getString(TMDB_PATH);
            title = movieObj.getString(TMDB_TITLE);
            release_date = movieObj.getString(TMDB_RELEASE_DATE);
            overview = movieObj.getString(TMDB_OVERVIEW);
            vote_average = movieObj.getDouble(TMDB_VOTE_AVERAGE);

            ContentValues contentValue = new ContentValues();
            contentValue.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
            contentValue.put(MoviesContract.MovieEntry.COLUMN_POSTER, moviePoster);
            contentValue.put(MoviesContract.MovieEntry.COLUMN_TITLE, title);
            contentValue.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, release_date);
            contentValue.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);
            contentValue.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, vote_average);
            contentValue.put(MoviesContract.MovieEntry.COLUMN_SORT, mSort);

            cVVector.add(contentValue);
        }

        int inserted = 0;
        // add to database
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, cvArray);
        }

        Log.d(LOG_TAG, "FetchMovies Complete. " + inserted + " Inserted" + " " + mSort);

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        // If there's no Sort, there's nothing to look up.  Verify size of mSort.
        if (mSort.equals(null)) {
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String moviesJsonStr = null;

        try {
            // Construct the URL for the themoviedb query
            // Possible parameters are avaiable at themoviedb's API page, at
            // https://www.themoviedb.org/documentation/api/discover?language=en
            final String FORECAST_BASE_URL =
                    "https://api.themoviedb.org/3/movie/" + mSort + "?";
            final String APIKEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(APIKEY_PARAM, BuildConfig.THE_MOVIE_DATABASE_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, url.toString());

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
            moviesJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attempting
            // to parse it.
            e.printStackTrace();
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
                    e.printStackTrace();
                }
            }
        }

        try {
            getMoviesDataFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }
}
