package com.exoticdevs.model;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.exoticdevs.data.MoviesContract;
import com.exoticdevs.helper.MoviesApi;

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
public class FetchMovies {

    private final Context mContext;
    private String mSort;

    private final String LOG_TAG = FetchMovies.class.getSimpleName();

    public FetchMovies(Context context, String sort) {
        mContext = context;
        mSort = sort;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/movie/" + mSort + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        MoviesApi moviesApi = retrofit.create(MoviesApi.class);

        Call<Movies> call = moviesApi.getMovies();
        call.enqueue(new Callback<Movies>() {
            @Override
            public void onResponse(Call<Movies> call, Response<Movies> response) {
                if (response.isSuccess()) {
                    ArrayList<Movie> movies = response.body().getMovies();
                    getMoviesDataFromJson(movies);
                } else {
                    Log.e("Error Code", String.valueOf(response.code()));
                    Log.e("Error Body", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<Movies> call, Throwable t) {
                Toast.makeText(mContext, "Failed, keep trying", Toast.LENGTH_SHORT).show();
            }
        });

        Log.v("connectionRequest", call.request().url().toString());
    }

    private void getMoviesDataFromJson(ArrayList<Movie> movies) {

        if (movies == null || movies.size() == 0) return;
        // clear movie table at certain sort to add new data which is returned from the server
        String where = MoviesContract.MovieEntry.TABLE_NAME +
                "." + MoviesContract.MovieEntry.COLUMN_SORT + " = ? ";

        String[] selectionArgs = new String[]{mSort};

        int rowId = mContext.getContentResolver().delete(MoviesContract.MovieEntry.CONTENT_URI,
                where, selectionArgs);
        Log.v(LOG_TAG, "deleted " + rowId + " " + mSort);

        // Insert the new movies information into the database
        Vector<ContentValues> cVVector = new Vector<>(movies.size());

        for (int i = 0; i < movies.size(); i++) {

            int movieId;
            String moviePoster;
            String title;
            String release_date;
            String overview;
            double vote_average;

            // Get the JSON object representing the movie
            Movie movieObj = movies.get(i);

            movieId = movieObj.getId();
            moviePoster = movieObj.getPoster_path();
            title = movieObj.getTitle();
            release_date = movieObj.getRelease_date();
            overview = movieObj.getOverview();
            vote_average = movieObj.getVote_average();

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
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, cvArray);
        }

        Log.d(LOG_TAG, "FetchMovies Complete. " + inserted + " Inserted" + " " + mSort);
    }
}