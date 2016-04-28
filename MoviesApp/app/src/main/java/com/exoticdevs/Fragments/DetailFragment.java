package com.exoticdevs.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.exoticdevs.AppClasses.Movie;
import com.exoticdevs.moviesapp.R;
import com.squareup.picasso.Picasso;


public class DetailFragment extends Fragment {

    String LOG_TAG = DetailFragment.class.getName();
    ImageView poster;
    TextView title ,releasedDate ,overview, voteAverage;
    public static final String ARG_MOVIE = "MovieDetail";

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        setMovieValues(new Movie());
    }

    public static DetailFragment newInstance(Movie movie) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_MOVIE, movie);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);
        return fragment;
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        return rootView;
    }
    
    public void setMovieValues(Movie movieDetail){

        Intent intent = getActivity().getIntent();
        Bundle bundle = getArguments();

        if (bundle != null && bundle.containsKey(ARG_MOVIE)) {
            movieDetail = bundle.getParcelable(ARG_MOVIE);
            Log.v(LOG_TAG, "detail_title " + movieDetail.getTitle());
        }

        if (intent != null && intent.hasExtra(ARG_MOVIE)) {
            movieDetail = intent.getParcelableExtra(ARG_MOVIE);
        }
        int movieId = movieDetail.getId();
        String moviePoster = movieDetail.getMovie_poster();
        String movieTitle = movieDetail.getTitle();
        String movieReleaseDate = movieDetail.getRelease_date();
        String movieOverview = movieDetail.getOverview();
        double movieVoteAverage = movieDetail.getVote_average();

        poster = (ImageView) getView().findViewById(R.id.poster);
        title = (TextView) getView().findViewById(R.id.title);
        releasedDate = (TextView) getView().findViewById(R.id.releasedDate);
        overview = (TextView) getView().findViewById(R.id.overview);
        voteAverage = (TextView) getView().findViewById(R.id.voteAverage);

        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185" + moviePoster).into(poster);
        title.setText(movieTitle);
        releasedDate.setText(movieReleaseDate);
        overview.setText(movieOverview);
        voteAverage.setText(movieVoteAverage + "/10");
    }
}
