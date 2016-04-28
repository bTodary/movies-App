package com.exoticdevs.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.exoticdevs.Adapters.MoviesAdapter;
import com.exoticdevs.AppClasses.Movie;
import com.exoticdevs.moviesapp.BuildConfig;
import com.exoticdevs.moviesapp.DetailActivity;
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


public class MoviesFragment extends Fragment {

    // how to apply savedInstanceState to avoid calling service if no connection available???

    String LOG_TAG = MoviesFragment.class.getName();
    public static final String ARG_MOVIE = "MovieDetail";

    MoviesAdapter mMoviesAdapter;
    GridView mMovies_grid;
    ProgressBar mProgressBar;

    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Invoking updateMovies() onStart and not onCreate to update the UI automatically after sort preference in settings changed
    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        mProgressBar.setVisibility(View.VISIBLE);
        FetchMoviesTask fetchMovies = new FetchMoviesTask();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));

        fetchMovies.execute(sort);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        // Get a reference to the GridView, and attach this adapter to it.
        mMovies_grid = (GridView) rootView.findViewById(R.id.movies_grid);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        mMoviesAdapter = new MoviesAdapter(getActivity(), new ArrayList<Movie>());
       // mMovies_grid.setAdapter(mMoviesAdapter);

        mMovies_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (getActivity().findViewById(R.id.detail_container) == null) {
                    // DisplayFragment (DetailFragment) is not in the layout (handset layout),
                    // so start DetailActivity
                    // and pass it the info about the selected item
                    Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                    detailIntent.putExtra(ARG_MOVIE, mMoviesAdapter.getItem(position));
                    startActivity(detailIntent);
                } else {
                    // DisplayFragment (DetailFragment) is in the layout (tablet layout),
                    // so tell the fragment to update
                    // Update the keys.

                    DetailFragment detailFragment = new DetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(ARG_MOVIE, mMoviesAdapter.getItem(position));
                    detailFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                                    .add(R.id.detail_container, detailFragment)
                                    .commit();

                   // DetailFragment.newInstance(mMoviesAdapter.getItem(position));
                }
            }
        });

        return rootView;
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /**
         * Take the String representing the complete movies in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private ArrayList<Movie> getMoviesDataFromJson(String moviesJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_LIST = "results";
            final String TMDB_ID = "id";
            final String TMDB_PATH = "poster_path";
            final String TMDB_TITLE = "title";
            final String TMDB_RELEASE_DATE = "release_date";
            final String TMDB_OVERVIEW = "overview";
            final String TMDB_VOTE_AVERAGE = "vote_average";

            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_LIST);

            ArrayList<Movie> resultStrs = new ArrayList<>();

            for(int i = 0; i < moviesArray.length(); i++) {

                int movieId;
                String moviePoster;
                String title;
                String release_date;
                String overview;
                double vote_average;


                // Get the JSON object representing the movie
                JSONObject movieObj = moviesArray.getJSONObject(i);

                movieId = movieObj.getInt(TMDB_ID);
                moviePoster = movieObj.getString(TMDB_PATH);
                title = movieObj.getString(TMDB_TITLE);
                release_date = movieObj.getString(TMDB_RELEASE_DATE);
                overview = movieObj.getString(TMDB_OVERVIEW);
                vote_average = movieObj.getDouble(TMDB_VOTE_AVERAGE);

                resultStrs.add(new Movie(movieId, moviePoster, title, release_date, overview, vote_average));

            }
            return resultStrs;
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                // Construct the URL for the themoviedb query
                // Possible parameters are avaiable at themoviedb's API page, at
                // https://www.themoviedb.org/documentation/api/discover?language=en
                final String FORECAST_BASE_URL =
                        "https://api.themoviedb.org/3/discover/movie?";
                final String QUERY_PARAM = "sort_by";
                final String APIKEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
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
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
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
                return getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the movies.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> result) {
            if (result != null) {
               if(getActivity() != null) {
                   mMoviesAdapter = new MoviesAdapter(getActivity(), result);
                   mMovies_grid.setAdapter(mMoviesAdapter);
                   mProgressBar.setVisibility(View.GONE);

                   // New data is back from the server
               }
            }
        }
    }
}
