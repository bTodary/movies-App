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
import com.exoticdevs.model.Trailer;
import com.exoticdevs.model.Trailers;
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
public class FetchMovieTrailers {

    private final Context mContext;
    private long mApiMovieID;

    private final String LOG_TAG = FetchMovieTrailers.class.getSimpleName();

    public FetchMovieTrailers(Context context, String movieId) {
        mContext = context;

        mApiMovieID = Long.valueOf(movieId);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/movie/" + movieId + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MoviesApi moviesApi = retrofit.create(MoviesApi.class);

        Call<Trailers> call = moviesApi.getTrailers();
        call.enqueue(new Callback<Trailers>() {
            @Override
            public void onResponse(Call<Trailers> call, Response<Trailers> response) {
                if (response.isSuccess()) {
                    ArrayList<Trailer> trailers = response.body().getTrailers();
                    getMovieTrailersDataFromJson(trailers);
                } else {
                    FragmentManager fragmentManager = ((FragmentActivity) mContext).getSupportFragmentManager();
                    DetailFragment detailFragment = (DetailFragment) fragmentManager.findFragmentById(R.id.fragmentDetail);
                    if(detailFragment != null) {
                        detailFragment.noTrailersAvailable();
                    }
                    Log.e("Error Code", String.valueOf(response.code()));
                    Log.e("Error Body", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<Trailers> call, Throwable t) {
                Toast.makeText(mContext, "Failed, keep trying", Toast.LENGTH_SHORT).show();
            }
        });

        Log.v("connectionRequest", call.request().url().toString());
    }

    /**
     * Take the String representing the complete movie's trailers in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p>
     * constructor takes the JSON string and converts it
     * into an Object hierarchy.
     */
    private void getMovieTrailersDataFromJson(ArrayList<Trailer> trailers) {

        // clear movie trailers at certain movieID to add its new trailers
        String where = MoviesContract.TrailerEntry.TABLE_NAME +
                "." + MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

        String[] selectionArgs = new String[]{String.valueOf(mApiMovieID)};

        int rowId = mContext.getContentResolver().delete(MoviesContract.TrailerEntry.CONTENT_URI,
                where, selectionArgs);
        Log.v(LOG_TAG, "deleted " + rowId + "where id = " + mApiMovieID);

        // Insert the new Trailers information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(trailers.size());

        for (int i = 0; i < trailers.size(); i++) {

            String trailerId;
            String key;
            String name;
            int size;
            String type;

            // Get the JSON object representing the movie's trailer
            Trailer trailerObj = trailers.get(i);

            trailerId = trailerObj.getMT_ID();
            key = trailerObj.getMT_KEY();
            name = trailerObj.getMT_NAME();
            size = trailerObj.getMT_SIZE();
            type = trailerObj.getMT_TYPE();

            ContentValues contentValue = new ContentValues();
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_ID, mApiMovieID);
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_TRAILER_ID, trailerId);
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_KEY, key);
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_NAME, name);
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_SIZE, size);
            contentValue.put(MoviesContract.TrailerEntry.COLUMN_TYPE, type);

            cVVector.add(contentValue);
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MoviesContract.TrailerEntry.CONTENT_URI, cvArray);
        }

        Log.d(LOG_TAG, "FetchTrailers Complete. " + inserted + " Inserted");
    }
}

