package com.exoticdevs.Model;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.exoticdevs.Adapters.ReviewsAdapter;
import com.exoticdevs.AppClasses.Review;
import com.exoticdevs.Data.MoviesDbManager;
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
import java.util.ArrayList;

/**
 * Created by mac on 4/13/16.
 */
public class FetchMovieReviews extends AsyncTask<Integer, Void, ArrayList<Review>> {

    private final Context mContext;
    private ReviewsAdapter mReviewsAdapter;
    private long mDbMovieID;
    private MoviesDbManager moviesDbManager;

    private final String LOG_TAG = FetchMovieReviews.class.getSimpleName();

    public FetchMovieReviews(Context context, ReviewsAdapter reviewsAdapter, long dbMovieID ) {
        mContext = context;
        mReviewsAdapter = reviewsAdapter;
        mDbMovieID = dbMovieID;
    }


    /**
     * Take the String representing the complete movie's reviews in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     *constructor takes the JSON string and converts it
     * into an Object hierarchy.
     */
    private ArrayList<Review> getMovieReviewsDataFromJson(String moviesJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MR_LIST = "results";
        final String MR_ID = "id";
        final String MR_AUTHOR = "author";
        final String MR_CONTENT = "content";
        final String MR_URL = "url";

        JSONObject trailersJson = new JSONObject(moviesJsonStr);
        JSONArray trailersArray = trailersJson.getJSONArray(MR_LIST);

        ArrayList<Review> resultStrs = new ArrayList<>();
        moviesDbManager = new MoviesDbManager(mContext);

        // clear movie's reviews to add new reviews
        moviesDbManager.clearReviewTableAtMovieId(mDbMovieID);
        Log.v(LOG_TAG, "ReviewsCount before " + moviesDbManager.getMovieReviewsCountAtMovieID(mDbMovieID));

        for(int i = 0; i < trailersArray.length(); i++) {

            String reviewId;
            String author;
            String content;
            String url;

            // Get the JSON object representing the movie's trailer
            JSONObject trailerObj = trailersArray.getJSONObject(i);

            reviewId = trailerObj.getString(MR_ID);
            author = trailerObj.getString(MR_AUTHOR);
            content = trailerObj.getString(MR_CONTENT);
            url = trailerObj.getString(MR_URL);

            resultStrs.add(new Review(mDbMovieID, reviewId, author,content, url));
            long mDbReviewID =  moviesDbManager.insertMovieReviews(mDbMovieID, reviewId, author, content, url);
        }
        Log.v(LOG_TAG, "ReviewsCount after " + moviesDbManager.getMovieReviewsCountAtMovieID(mDbMovieID));
        return resultStrs;
    }

    @Override
    protected ArrayList<Review> doInBackground(Integer... params) {

        // If there's no movie id, there's nothing to look up.  Verify size of params.
        if (params.length == 0) {
            return null;
        }

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
            return getMovieReviewsDataFromJson(reviewsJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the reviews.
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Review> result) {

        if (result != null && mReviewsAdapter != null) {
            mReviewsAdapter.clear();

            if(result.size() == 0){
                FragmentManager fragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();
                DetailFragment detailFragment = (DetailFragment) fragmentManager.findFragmentById(R.id.fragmentDetail);
                if(null != detailFragment) {
                    detailFragment.noReviewsAvailable();
                }
                return;
            }

            for(Review review : result) {
                mReviewsAdapter.add(review);
            }
        }
    }
}

