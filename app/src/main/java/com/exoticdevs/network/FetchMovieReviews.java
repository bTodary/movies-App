package com.exoticdevs.network;

import android.content.ContentValues;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.exoticdevs.data.MoviesContract;
import com.exoticdevs.fragments.DetailFragment;
import com.exoticdevs.helper.MoviesApi;
import com.exoticdevs.model.Review;
import com.exoticdevs.model.Reviews;
import com.exoticdevs.moviesapp.R;

import java.util.ArrayList;
import java.util.Vector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mac on 4/13/16.
 */
public class FetchMovieReviews{

    private final Context mContext;
    private long mApiMovieID;

    private final String LOG_TAG = FetchMovieReviews.class.getSimpleName();

    public FetchMovieReviews(Context context, String movieId) {
        mContext = context;
        mApiMovieID = Long.valueOf(movieId);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/movie/" + movieId + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MoviesApi moviesApi = retrofit.create(MoviesApi.class);

        Call<Reviews> call = moviesApi.getReviews();
        call.enqueue(new Callback<Reviews>() {
            @Override
            public void onResponse(Call<Reviews> call, Response<Reviews> response) {
                if (response.isSuccess()) {
                    ArrayList<Review> reviews = response.body().getReviews();
                    getMovieReviewsDataFromJson(reviews);
                } else {
                    FragmentManager fragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();
                    DetailFragment detailFragment = (DetailFragment) fragmentManager.findFragmentById(R.id.fragmentDetail);
                    if (detailFragment != null) {
                        detailFragment.noReviewsAvailable();
                    }
                    Log.e("Error Code", String.valueOf(response.code()));
                    Log.e("Error Body", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<Reviews> call, Throwable t) {
                Toast.makeText(mContext, "Failed, keep trying", Toast.LENGTH_SHORT).show();
            }
        });

        Log.v("connectionRequest", call.request().url().toString());
    }


    /**
     * Take the String representing the complete movie's reviews in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p>
     * constructor takes the JSON string and converts it
     * into an Object hierarchy.
     */
    private void getMovieReviewsDataFromJson(ArrayList<Review> reviews) {

        // clear movie reviews at certain movieID to add its new reviews
        String where = MoviesContract.ReviewEntry.TABLE_NAME +
                "." + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";

        String[] selectionArgs = new String[]{String.valueOf(mApiMovieID)};

        int rowId = mContext.getContentResolver().delete(MoviesContract.ReviewEntry.CONTENT_URI,
                where, selectionArgs);
        Log.v(LOG_TAG, "deleted " + rowId + "where id = " + mApiMovieID);

        // Insert the new reviews information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(reviews.size());

        for (int i = 0; i < reviews.size(); i++) {

            String reviewId;
            String author;
            String content;
            String url;

            // Get the JSON object representing the movie's review
            Review reviewObj = reviews.get(i);

            reviewId = reviewObj.getId();
            author = reviewObj.getAuthor();
            content = reviewObj.getContent();
            url = reviewObj.getUrl();

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
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, cvArray);
        }

        Log.d(LOG_TAG, "FetchReviews Complete. " + inserted + " Inserted");
    }
}