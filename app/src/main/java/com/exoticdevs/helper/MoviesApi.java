package com.exoticdevs.helper;

import com.exoticdevs.model.Movies;
import com.exoticdevs.model.Reviews;
import com.exoticdevs.model.Trailers;
import com.exoticdevs.moviesapp.BuildConfig;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by bassant on 5/27/17.
 */

public interface MoviesApi {

    final String API_KEY = BuildConfig.THE_MOVIE_DATABASE_API_KEY;

    @GET("?api_key=" + API_KEY)
    Call<Movies> getMovies();

    @GET("reviews?api_key=" + API_KEY)
    Call<Reviews> getReviews();

    @GET("videos?api_key=" + API_KEY)
    Call<Trailers> getTrailers();
}
