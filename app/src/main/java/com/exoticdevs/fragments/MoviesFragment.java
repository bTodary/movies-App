package com.exoticdevs.fragments;

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
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.exoticdevs.adapters.MoviesAdapter;
import com.exoticdevs.data.MoviesContract;
import com.exoticdevs.helper.ConnectionDetector;
import com.exoticdevs.helper.DeviceUtils;
import com.exoticdevs.helper.FragmentData;
import com.exoticdevs.helper.PreCachingGridLayoutManager;
import com.exoticdevs.model.FetchMovies;
import com.exoticdevs.moviesapp.DetailActivity;
import com.exoticdevs.moviesapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MoviesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    String LOG_TAG = MoviesFragment.class.getName();
    @Bind(R.id.movies_grid) RecyclerView mMovies_rv;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    private boolean mIsConnected;
    private static boolean mTwoPane = true;
    private FragmentData iFragmentData;
    public static final String ARG_MOVIE_SORT = "sort";
    private String mSort;
    private MoviesAdapter mMoviesAdapter;
    private static final int MOVIES_LOADER = 0;
    private static final int FAV_LOADER = 1;

    public MoviesFragment() {
    }

    public static MoviesFragment newInstance(String sort) {
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

        if (mTwoPane) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mSort = prefs.getString(getActivity().getString(R.string.pref_sort_key),
                    getActivity().getString(R.string.pref_sort_default));
        } else {
            mSort = getArguments().getString(ARG_MOVIE_SORT);
        }


        if (mSort.equals(getActivity().getString(R.string.pref_sort_favorite))) {
            mProgressBar.setVisibility(View.GONE);
            initLoader(FAV_LOADER, null, this, getLoaderManager());

        } else {
            if (mIsConnected) {
                mProgressBar.setVisibility(View.VISIBLE);
                fetchMoviesFromServer();

            } else {
                mProgressBar.setVisibility(View.GONE);

                if (getView() == null) return;
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
        ButterKnife.bind(this, rootView);

        mMoviesAdapter = new MoviesAdapter(getActivity(), null);

        PreCachingGridLayoutManager glm = new PreCachingGridLayoutManager(getActivity(), numberOfColumns());
        glm.setExtraLayoutSpace(DeviceUtils.getScreenHeight(getActivity()));
        mMovies_rv.setLayoutManager(glm);

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

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }

    private void fetchMoviesFromServer() {
        new FetchMovies(getActivity(), mSort);
    }

    public void setFragData(FragmentData iFragmentData) {
        this.iFragmentData = iFragmentData;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        Uri moviesUri = null;

        switch (loaderId) {
            case MOVIES_LOADER:
                moviesUri = MoviesContract.MovieEntry.buildMovieWithSortUri(mSort);
                break;
            case FAV_LOADER:
                moviesUri = MoviesContract.FavEntry.CONTENT_URI;
                break;
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
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

        if (mTwoPane) {
            if (cursor == null) return;
            cursor.moveToFirst();
            if (cursor.moveToFirst() || cursor.getCount() >= 1) {
                cursor.moveToPosition(0);
                iFragmentData.updateData(cursor, mSort);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mMoviesAdapter.swapCursor(null);
    }

    public void onSortChanged() {
        updateMovies();
    }

    public static <T> void initLoader(final int loaderId, final Bundle args, final LoaderManager.LoaderCallbacks<T> callbacks,
                                      final LoaderManager loaderManager) {
        final Loader<T> loader = loaderManager.getLoader(loaderId);
        if (loader != null) {
            loaderManager.restartLoader(loaderId, args, callbacks);
        } else {
            loaderManager.initLoader(loaderId, args, callbacks);
        }
    }
}
