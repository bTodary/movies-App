package com.exoticdevs.fragments;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.exoticdevs.adapters.ReviewsAdapter;
import com.exoticdevs.adapters.TrailersAdapter;
import com.exoticdevs.data.MoviesContract;
import com.exoticdevs.helper.ConnectionDetector;
import com.exoticdevs.helper.FragmentData;
import com.exoticdevs.helper.PicassoCache;
import com.exoticdevs.network.FetchMovieReviews;
import com.exoticdevs.network.FetchMovieTrailers;
import com.exoticdevs.moviesapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;

// observer
public class DetailFragment extends Fragment implements FragmentData, LoaderManager.LoaderCallbacks<Cursor> {

    private String LOG_TAG = DetailFragment.class.getName();

    @Bind(R.id.poster) ImageView mPoster;
    @Bind(R.id.releasedDate) TextView mReleasedDate;
    @Bind(R.id.overview) TextView mOverview;
    @Bind(R.id.voteAverage) TextView mVoteAverage;
    @Bind(R.id.fav_click) FloatingActionButton mFavCheck;
    @Bind(R.id.rvTrailers) RecyclerView mRvTrailers;
    @Bind(R.id.reviews_list) ListView mReviews_list;
    @Bind(R.id.Trailers_view) LinearLayout mTrailers_view;
    @Bind(R.id.toolbar)Toolbar mToolbar;

    String mMovieId;
    private ReviewsAdapter mReviewsAdapter;
    private String mMoviePoster, mMovieTitle, mMovieReleaseDate, mMovieOverview;
    private double mMovieVoteAverage;
    private boolean mIsConnected, mGotFirstTrailer;
    private TrailersAdapter mTrailersAdapter;

    private boolean mIsMarked;

    private ShareActionProvider mShareActionProvider;
    private String mMovieShare;
    private Intent mIntent;

    private static final String MOVIES_SHARE_HASHTAG = " #PopMovies";
    private static final int TRAILERS_LOADER = 0;
    private static final int REVIEWS_LOADER = 1;
    private static final int FAV_CHECK_LOADER = 2;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        View header = inflater.inflate(R.layout.movie_detail_header, mReviews_list, false);

        mPoster = (ImageView) header.findViewById(R.id.poster);
        mReleasedDate = (TextView) header.findViewById(R.id.releasedDate);
        mOverview = (TextView) header.findViewById(R.id.overview);
        mVoteAverage = (TextView) header.findViewById(R.id.voteAverage);
        mFavCheck = (FloatingActionButton) header.findViewById(R.id.fav_click);
        mRvTrailers = (RecyclerView) header.findViewById(R.id.rvTrailers);
        mTrailers_view = (LinearLayout) header.findViewById(R.id.Trailers_view);

        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        mReviews_list = (ListView) rootView.findViewById(R.id.reviews_list);
        mReviews_list.addHeaderView(header);

        ButterKnife.bind(this, rootView);

        mReviewsAdapter = new ReviewsAdapter(getActivity(), null, 0);
        mReviews_list.setAdapter(mReviewsAdapter);

        mTrailersAdapter = new TrailersAdapter(getActivity(), new TrailersAdapter.TrailersAdapterOnClickHandler() {
            @Override
            public void onClick(Cursor cursor, TrailersAdapter.ViewHolder vh) {
                if (cursor != null) {
                    String trailerKey = cursor.getString(cursor.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_KEY));
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://www.youtube.com/watch?v=" + trailerKey));
                    startActivity(intent);
                }
            }
        });

        setupRecyclerView(mRvTrailers);

        mFavCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFaveTable();
            }
        });

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        if (mMovieShare != null) {
            inflater.inflate(R.menu.detail, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);
            // Get the provider and hold onto it to set/change the share intent.
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        } else {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        }

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Watch out " + mMovieShare + MOVIES_SHARE_HASHTAG);
        return shareIntent;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(mTrailersAdapter);
    }

    private void bindMovieTrailers() {
        mTrailers_view.setVisibility(View.VISIBLE);

        if (mIsConnected) {
            fetchTrailersFromServer();
        }
        mGotFirstTrailer = false;
        initLoader(TRAILERS_LOADER, null, this, getLoaderManager());
    }

    private void bindMovieReviews() {
        if (mIsConnected) {
            fetchReviewsFromServer();
        }
        initLoader(REVIEWS_LOADER, null, this, getLoaderManager());
    }

    public void noTrailersAvailable() {
        if (getView() == null) return;

        // update UI
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTrailers_view.setVisibility(View.GONE);
                Snackbar snackbar = Snackbar
                        .make(getView(), getActivity().getResources().getString(R.string.noTrailersAvailbe), Snackbar.LENGTH_SHORT);
                snackbar.show();
                mMovieShare = null;
            }
        });
    }

    public void noReviewsAvailable() {
        if (getView() == null) return;

        Snackbar snackbar = Snackbar
                .make(getView(), getActivity().getResources().getString(R.string.noReviewsAvailable), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void updateFaveTable() {
        // clear movie table at certain sort to add new data of same sort which is returned from the server
        final String[] selectionArgs = new String[]{mMovieId};
        final String where = MoviesContract.FavEntry.TABLE_NAME +
                "." + MoviesContract.FavEntry.COLUMN_MOVIE_ID + " = ? ";

        if (!mIsMarked) {

            ContentValues contentValue = new ContentValues();
            contentValue.put(MoviesContract.FavEntry.COLUMN_MOVIE_ID, mMovieId);
            contentValue.put(MoviesContract.FavEntry.COLUMN_POSTER, mMoviePoster);
            contentValue.put(MoviesContract.FavEntry.COLUMN_TITLE, mMovieTitle);
            contentValue.put(MoviesContract.FavEntry.COLUMN_RELEASE_DATE, mMovieReleaseDate);
            contentValue.put(MoviesContract.FavEntry.COLUMN_OVERVIEW, mMovieOverview);
            contentValue.put(MoviesContract.FavEntry.COLUMN_VOTE_AVERAGE, mMovieVoteAverage);

            Uri favUri = getActivity().getContentResolver().insert(MoviesContract.FavEntry.CONTENT_URI, contentValue);
            long favRowId = ContentUris.parseId(favUri);

            if (favRowId != -1) {
                mIsMarked = true;
                updateFavorite();

                if (getView() == null) return;

                Snackbar snackbar = Snackbar
                        .make(getView(), getActivity().getResources().getString(R.string.marked), Snackbar.LENGTH_SHORT)
                        .setAction(getActivity().getResources().getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                int rowDeleted = getActivity().getContentResolver().delete(MoviesContract.FavEntry.CONTENT_URI,
                                        where, selectionArgs);

                                if (rowDeleted != -1) {
                                    Snackbar snackbar2 = Snackbar
                                            .make(getView(), getActivity().getResources().getString(R.string.removed), Snackbar.LENGTH_SHORT);
                                    snackbar2.show();
                                    mIsMarked = false;
                                    updateFavorite();
                                }
                            }
                        });
                snackbar.show();
            }
        } else {
            int rowDeleted = getActivity().getContentResolver().delete(MoviesContract.FavEntry.CONTENT_URI,
                    where, selectionArgs);
            Log.v(LOG_TAG, "deleted " + rowDeleted + " id= " + mMovieId);

            if (rowDeleted != -1) {
                if (getView() == null) return;
                Snackbar snackbar = Snackbar
                        .make(getView(), getActivity().getResources().getString(R.string.removed), Snackbar.LENGTH_SHORT);
                snackbar.show();
                mIsMarked = false;
                updateFavorite();
            }
        }
    }

    private void updateFavorite() {
        if (mIsMarked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFavCheck.setImageDrawable(getResources().getDrawable(R.drawable.select, null));
            } else {
                mFavCheck.setImageDrawable(getResources().getDrawable(R.drawable.select));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFavCheck.setImageDrawable(getResources().getDrawable(R.drawable.unselect, null));
            } else {
                mFavCheck.setImageDrawable(getResources().getDrawable(R.drawable.unselect));
            }
        }
    }

    @Override
    public void updateData(Cursor movieDetailCursor, String sort) {
        if (movieDetailCursor != null) {

            if (sort.equals(getString(R.string.pref_sort_favorite))) {

                mMovieId = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.FavEntry.COLUMN_MOVIE_ID));
                Log.v(LOG_TAG, "mApiMovieID " + mMovieId);

                mMoviePoster = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.FavEntry.COLUMN_POSTER));
                mMovieTitle = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.FavEntry.COLUMN_TITLE));
                mMovieReleaseDate = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.FavEntry.COLUMN_RELEASE_DATE));
                mMovieOverview = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.FavEntry.COLUMN_OVERVIEW));
                mMovieVoteAverage = movieDetailCursor.getDouble(movieDetailCursor.getColumnIndex(MoviesContract.FavEntry.COLUMN_VOTE_AVERAGE));

            } else {
                mMovieId = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_MOVIE_ID));
                Log.v(LOG_TAG, "mApiMovieID " + mMovieId);

                mMoviePoster = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER));
                mMovieTitle = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE));
                mMovieReleaseDate = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE));
                mMovieOverview = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_OVERVIEW));
                mMovieVoteAverage = movieDetailCursor.getDouble(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE));
            }

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mMovieTitle);

            PicassoCache.getPicassoInstance(getActivity())
                    .load("http://image.tmdb.org/t/p/w185" + mMoviePoster)
                    .into(mPoster);

            mReleasedDate.setText(mMovieReleaseDate);
            mOverview.setText(mMovieOverview);
            mVoteAverage.setText(mMovieVoteAverage + "/10");

        }
        // reset favorite mark
        mIsMarked = false;
        updateFavorite();

        // check is selected movie marked as favorite or not
        initLoader(FAV_CHECK_LOADER, null, this, getLoaderManager());

        mIsConnected = ConnectionDetector.isConnectingToInternet(getActivity());

        bindMovieTrailers();
        bindMovieReviews();
    }

    private void fetchTrailersFromServer() {
       new FetchMovieTrailers(getActivity(), mMovieId);
    }

    private void fetchReviewsFromServer() {
        FetchMovieReviews fetchMovieReviews = new FetchMovieReviews(getActivity(), mMovieId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case TRAILERS_LOADER:
                Uri trailersUri = MoviesContract.TrailerEntry.buildTrailerUri(Long.parseLong(mMovieId));

                Log.v(LOG_TAG, "trailersUri: " + trailersUri);

                return new CursorLoader(getActivity(),
                        trailersUri, null, null, null, null);

            case REVIEWS_LOADER:
                Uri reviewsUri = MoviesContract.ReviewEntry.buildReviewUri(Long.parseLong(mMovieId));

                Log.v(LOG_TAG, "reviewsUri: " + reviewsUri);

                return new CursorLoader(getActivity(),
                        reviewsUri, null, null, null, null);

            case FAV_CHECK_LOADER:

                Uri favUri = MoviesContract.FavEntry.CONTENT_URI;

                Log.v(LOG_TAG, "favUri: " + favUri);

                return new CursorLoader(getActivity(),
                        favUri, null, null, null, null);
            default:
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case TRAILERS_LOADER:
                if (data != null) {
                    mTrailersAdapter.swapCursor(data);

                    if (!mGotFirstTrailer && data.moveToFirst()) {
                        // get first trailer to be shared
                        data.moveToFirst();

                        String trailerKey = data.getString(data.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_KEY));
                        mMovieShare = "http://www.youtube.com/watch?v=" + trailerKey;

                        getActivity().invalidateOptionsMenu();

                        mGotFirstTrailer = true;
                    }
                }

                break;
            case REVIEWS_LOADER:

                if (data != null) {
                    mReviewsAdapter.swapCursor(data);
                }
                break;

            case FAV_CHECK_LOADER:
                if (data != null) {
                    while (data.moveToNext()) {
                        String favId = data.getString(data.getColumnIndex(MoviesContract.FavEntry.COLUMN_MOVIE_ID));

                        if (favId.equals(mMovieId)) {
                            mIsMarked = true;
                            break;
                        } else {
                            mIsMarked = false;
                        }
                    }
                    updateFavorite();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {

            case TRAILERS_LOADER:
                mTrailersAdapter.swapCursor(null);
                break;

            case REVIEWS_LOADER:
                mReviewsAdapter.swapCursor(null);
                break;

            case FAV_CHECK_LOADER:
                break;

            default:
                break;
        }
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
