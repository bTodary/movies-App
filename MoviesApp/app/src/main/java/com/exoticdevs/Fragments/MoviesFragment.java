package com.exoticdevs.Fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.exoticdevs.Adapters.MoviesAdapter;
import com.exoticdevs.Data.MoviesContract;
import com.exoticdevs.Model.FetchMovies;
import com.exoticdevs.Util.ConnectionDetector;
import com.exoticdevs.Util.FragmentData;
import com.exoticdevs.moviesapp.DetailActivity;
import com.exoticdevs.moviesapp.R;


public class MoviesFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>{

    String LOG_TAG = MoviesFragment.class.getName();
    private RecyclerView mMovies_rv;
    private boolean mIsConnected;
    private static boolean mTwoPane = true;
    private ProgressBar mProgressBar;
    private FragmentData iFragmentData;
    public static final String ARG_MOVIE_SORT = "sort";
    private String mSort;
    private MoviesAdapter mMoviesAdapter;
    private static final int MOVIES_LOADER = 0;
    private static final int FAV_LOADER = 1;

    public MoviesFragment() {
    }

    public static final MoviesFragment newInstance(String sort) {
        mTwoPane = false;
        MoviesFragment fr = new MoviesFragment();
        Bundle bdl = new Bundle();
        bdl.putString(ARG_MOVIE_SORT, sort);
        fr.setArguments(bdl);
        return fr;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateMovies();
    }

    private void updateMovies() {
        mIsConnected = ConnectionDetector.isConnectingToInternet(getActivity());

        if(mTwoPane){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mSort = prefs.getString(getActivity().getString(R.string.pref_sort_key),
                    getActivity().getString(R.string.pref_sort_default));
        }else{
            mSort = getArguments().getString(ARG_MOVIE_SORT);
        }

//        Cursor cursor = getActivity().getContentResolver().query(
//                MoviesContract.MovieEntry.CONTENT_URI,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//        Log.v(LOG_TAG, "movies table " + cursor.getCount());
//
//
//        Uri moviesUri = MoviesContract.MovieEntry.buildMovieWithSortUri(mSort);
//
//        Cursor cursor2 = getActivity().getContentResolver().query(
//                moviesUri,
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//
//        Log.v(LOG_TAG,"movies table at sort "  + cursor2.getCount() + " " + mSort);


        if (mSort.equals(getActivity().getString(R.string.pref_sort_favorite))) {
            mProgressBar.setVisibility(View.GONE);
            initLoader(FAV_LOADER, null, this, getLoaderManager());

        } else {
            if (mIsConnected) {
                mProgressBar.setVisibility(View.VISIBLE);
                 fetchMoviesFromServer();

            } else {
                mProgressBar.setVisibility(View.GONE);

                Snackbar snackbar = Snackbar
                        .make(getView(), getActivity().getResources().getString(R.string.couldNotSync), Snackbar.LENGTH_LONG)
                        .setAction(getActivity().getString(R.string.retry), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                updateMovies();
                            }
                        });
                snackbar.show();
            }
            initLoader(MOVIES_LOADER, null, this, getLoaderManager());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movies, container, false);

        // Get a reference to the GridView, and attach this adapter to it.
        mMovies_rv = (RecyclerView) rootView.findViewById(R.id.movies_grid);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        mMoviesAdapter = new MoviesAdapter(getActivity(), null);

        if(mTwoPane) {
            mMovies_rv.setLayoutManager(new GridLayoutManager(mMovies_rv.getContext(), 3));
        }else{
            mMovies_rv.setLayoutManager(new GridLayoutManager(mMovies_rv.getContext(), 2));
        }

        mMoviesAdapter = new MoviesAdapter(getActivity(), new MoviesAdapter.MoviestAdapterOnClickHandler() {
            @Override
            public void onClick(Cursor cursor, MoviesAdapter.ViewHolder vh) {
                if (getActivity().getSupportFragmentManager().findFragmentById(R.id.fragmentDetail) == null) {
                    // DisplayFragment (DetailFragment) is not in the layout (handset layout),
                    // so start DetailActivity
                    // and pass it the info about the selected item

                    Intent detailIntent = new Intent(getActivity(), DetailActivity.class);

                    if (cursor != null) {
                        if (mSort.equals(getActivity().getString(R.string.pref_sort_favorite))) {
                            detailIntent.setData(MoviesContract.FavEntry.buildFavUri(
                                    cursor.getLong(cursor.getColumnIndex(MoviesContract.FavEntry._ID))));

                        } else {

                            detailIntent.setData(MoviesContract.MovieEntry.buildMovieAtSortWithId(mSort,
                                    cursor.getLong(cursor.getColumnIndex(MoviesContract.MovieEntry._ID))));

//                            String id = cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry._ID));
//                            String poster = cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER));
//                            String title = cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE));
//                            String overview = cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_OVERVIEW));
//                            String releaseDate = cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE));
//                            String voteAverage = cursor.getString(cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE));
//
//                            detailIntent.putExtra("id", id);
//                            detailIntent.putExtra("poster", poster);
//                            detailIntent.putExtra("title", title);
//                            detailIntent.putExtra("overview", overview);
//                            detailIntent.putExtra("releaseDate", releaseDate);
//                            detailIntent.putExtra("voteAverage", voteAverage);
//                            detailIntent.putExtra("mTwoPane", false);

                        }
                        detailIntent.putExtra(DetailActivity.SORT_ARG, mSort);
                        startActivity(detailIntent);
                    }
                } else {
                    if (cursor != null) {
                        //  tell the detail fragment to update
                        iFragmentData.updateData(cursor, mSort);
                    }
                }
            }
        });

        mMovies_rv.setAdapter(mMoviesAdapter);

        return rootView;
    }

    private void fetchMoviesFromServer() {
        FetchMovies fetchMovies = new FetchMovies(getActivity(), mSort);
        fetchMovies.execute();
    }

    public void setFragData(FragmentData iFragmentData){
        this.iFragmentData = iFragmentData;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Uri moviesUri = null;

        switch (i){
            case MOVIES_LOADER:
                moviesUri = MoviesContract.MovieEntry.buildMovieWithSortUri(mSort);
                break;
            case FAV_LOADER:
                moviesUri = MoviesContract.FavEntry.CONTENT_URI;
                break;
            default:
                break;
        }
        return new CursorLoader(getActivity(),
                moviesUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        mMoviesAdapter.swapCursor(cursor);

        if(mTwoPane) {
            cursor.moveToFirst();
            if (cursor != null && (cursor.moveToFirst()) || cursor.getCount() >= 1) {
                cursor.moveToPosition(0);
                iFragmentData.updateData(cursor, mSort);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mMoviesAdapter.swapCursor(null);
    }

    public void onSortChanged(){
        updateMovies();
    }

    public static <T> void initLoader(final int loaderId, final Bundle args, final LoaderManager.LoaderCallbacks<T> callbacks,
                                      final LoaderManager loaderManager){
        final Loader<T> loader = loaderManager.getLoader(loaderId);
        if(loader != null){
            loaderManager.restartLoader(loaderId, args, callbacks);
        }else {
            loaderManager.initLoader(loaderId, args, callbacks);
        }
    }
}
