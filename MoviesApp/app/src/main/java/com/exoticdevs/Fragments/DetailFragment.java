package com.exoticdevs.Fragments;

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

import com.exoticdevs.Adapters.ReviewsAdapter;
import com.exoticdevs.Adapters.TrailersAdapter;
import com.exoticdevs.AppClasses.Review;
import com.exoticdevs.Data.MoviesContract;
import com.exoticdevs.Model.FetchMovieTrailers;
import com.exoticdevs.Util.ConnectionDetector;
import com.exoticdevs.Util.FragmentData;
import com.exoticdevs.Util.PicassoCache;
import com.exoticdevs.moviesapp.R;

import java.util.ArrayList;

// observer
public class DetailFragment extends Fragment implements FragmentData, LoaderManager.LoaderCallbacks<Cursor> {

    private String LOG_TAG = DetailFragment.class.getName();
    private ImageView mPoster;
    private String mMovieId;
    private long mDbMovieID;
    private TextView mReleasedDate ,mOverview, mVoteAverage;
    private FloatingActionButton mFavCheck;
    private RecyclerView mRvTrailers;
    private TrailersAdapter mTrailersAdapter;
    public ListView mReviews_list;
    private ReviewsAdapter mReviewsAdapter;
    private String mMoviePoster, mMovieTitle, mMovieReleaseDate, mMovieOverview;
    private double mMovieVoteAverage;
    private boolean mIsConnected;
    private LinearLayout mTrailers_view;
    private boolean mIsMarked;
    private ArrayList<Review> mArrReviews;

    private static final String MOVIES_SHARE_HASHTAG = " #PopMovies";
    private ShareActionProvider mShareActionProvider;
    private String mMovieShare;
    private static final int TRAILERS_LOADER = 0;


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        View header = inflater.inflate(R.layout.movie_detail_header, mReviews_list,false);

        mPoster = (ImageView)header.findViewById(R.id.poster);
        mReleasedDate = (TextView)header.findViewById(R.id.releasedDate);
        mOverview = (TextView)header.findViewById(R.id.overview);
        mVoteAverage = (TextView)header.findViewById(R.id.voteAverage);
        mFavCheck = (FloatingActionButton)header.findViewById(R.id.fav_click);
        mRvTrailers = (RecyclerView) header.findViewById(R.id.rvTrailers);
        mTrailers_view = (LinearLayout) header.findViewById(R.id.Trailers_view);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        mReviews_list = (ListView) rootView.findViewById(R.id.reviews_list);
        mReviews_list.addHeaderView(header);

        mReviewsAdapter = new ReviewsAdapter(getActivity(), new ArrayList<Review>());
        mReviews_list.setAdapter(mReviewsAdapter);


        mTrailersAdapter = new TrailersAdapter(getActivity(), new TrailersAdapter.TrailersAdapterOnClickHandler() {
            @Override
            public void onClick(Cursor cursor, TrailersAdapter.ViewHolder vh) {
               if(cursor != null){

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,"Watch out " + mMovieShare + MOVIES_SHARE_HASHTAG);
        return shareIntent;
    }

    private void bindMovieReviews() {
        if (mIsConnected) {
      //      fetchReviewsFromServer();
        } else {
        //    fetchReviewsFromDataBase();
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(mTrailersAdapter);
    }

    private void bindMovieTrailers() {
        mIsConnected = ConnectionDetector.isConnectingToInternet(getActivity());
        mTrailers_view.setVisibility(View.VISIBLE);

        if (mIsConnected) {
           fetchTrailersFromServer();
        } else {
            getLoaderManager().initLoader(TRAILERS_LOADER, null, this);
        }
    }

    public void noTrailersAvailable(){
        mTrailers_view.setVisibility(View.GONE);
        Snackbar snackbar = Snackbar
                .make(getView(), getActivity().getResources().getString(R.string.noTrailersAvailbe), Snackbar.LENGTH_LONG);
        snackbar.show();
        mMovieShare = null;
    }

    public void noReviewsAvailable() {
        Snackbar snackbar = Snackbar
                .make(getView(), getActivity().getResources().getString(R.string.noReviewsAvailable), Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void updateFaveTable(){
        // clear movie table at certain sort to add new data of same sort which is returned from the server
        final String[] selectionArgs = new String[]{mMovieId};
        final String where = MoviesContract.FavEntry.TABLE_NAME+
                "." + MoviesContract.FavEntry.COLUMN_MOVIE_ID + " = ? ";

        if (!mIsMarked) {

            ContentValues contentValue = new ContentValues();
            contentValue.put(MoviesContract.FavEntry.COLUMN_MOVIE_ID, mMovieId);
            contentValue.put(MoviesContract.FavEntry.COLUMN_MOVIE_KEY, mDbMovieID);
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

                Snackbar snackbar = Snackbar
                        .make(getView(), getActivity().getResources().getString(R.string.marked), Snackbar.LENGTH_SHORT)
                        .setAction(getActivity().getResources().getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                int rowDeleted =  getActivity().getContentResolver().delete(MoviesContract.FavEntry.CONTENT_URI,
                                        where, selectionArgs);
                                Log.v(LOG_TAG, "deleted " + rowDeleted + " id= " + mMovieId);

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
            int rowDeleted =  getActivity().getContentResolver().delete(MoviesContract.FavEntry.CONTENT_URI,
                    where, selectionArgs);
            Log.v(LOG_TAG, "deleted " + rowDeleted + " id= " + mMovieId);

            if (rowDeleted != -1) {
                Snackbar snackbar = Snackbar
                        .make(getView(), getActivity().getResources().getString(R.string.removed), Snackbar.LENGTH_SHORT);
                snackbar.show();
                mIsMarked = false;
                updateFavorite();
            }
        }
    }

    private void updateFavorite(){
        if(mIsMarked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFavCheck.setImageDrawable(getResources().getDrawable(R.drawable.select, null));
            } else {
                mFavCheck.setImageDrawable(getResources().getDrawable(R.drawable.select));
            }
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFavCheck.setImageDrawable(getResources().getDrawable(R.drawable.unselect, null));
            } else {
                mFavCheck.setImageDrawable(getResources().getDrawable(R.drawable.unselect));
            }
        }
    }

    @Override
    public void updateData(Cursor movieDetailCursor) {

        if(movieDetailCursor != null) {
            mDbMovieID = movieDetailCursor.getLong(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry._ID));

            Log.v(LOG_TAG, "mDbMovieID " +  mDbMovieID);

            mMovieId = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_MOVIE_ID));

            mMoviePoster = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER));
            mMovieTitle = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE));
            mMovieReleaseDate = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE));
            mMovieOverview = movieDetailCursor.getString(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_OVERVIEW));
            mMovieVoteAverage = movieDetailCursor.getDouble(movieDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE));

            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mMovieTitle);

            PicassoCache.getPicassoInstance(getActivity())
                    .load("http://image.tmdb.org/t/p/w185" + mMoviePoster)
                    .into(mPoster);

            mReleasedDate.setText(mMovieReleaseDate);
            mOverview.setText(mMovieOverview);
            mVoteAverage.setText(mMovieVoteAverage + "/10");

            Uri favUri = MoviesContract.FavEntry.CONTENT_URI;
            Cursor cursor = getActivity().getContentResolver().query(
                    favUri,
                    null,
                    null,
                    null,
                    null
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String favId = cursor.getString(cursor.getColumnIndex(MoviesContract.FavEntry.COLUMN_MOVIE_ID));

                    if (favId.equals(mMovieId)) {
                        mIsMarked = true;
                        break;
                    }else{
                        mIsMarked = false;
                    }
                }
            }

            updateFavorite();
            bindMovieTrailers();
            bindMovieReviews();
        }
    }

    private void fetchTrailersFromServer(){
        try {
            FetchMovieTrailers fetchMovieTrailers = new FetchMovieTrailers(getActivity(), mDbMovieID);
            fetchMovieTrailers.execute(mMovieId);

            getLoaderManager().initLoader(TRAILERS_LOADER, null, this);
        }catch (Exception i){
            i.printStackTrace();
        }
    }

//    private void fetchReviewsFromServer(){
//        try {
//            FetchMovieReviews fetchMovieReviews = new FetchMovieReviews(getActivity(), mReviewsAdapter, mDbMovieID);
//            fetchMovieReviews.execute(mMovieId);
//
//        }catch (Exception i){
//            i.printStackTrace();
//        }
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case TRAILERS_LOADER:
               Uri trailersUri = MoviesContract.TrailerEntry.buildTrailerUri(mDbMovieID);
                return new CursorLoader(getActivity(),
                        trailersUri, null,null,null,null);
            default:
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()){
            case TRAILERS_LOADER:
                if (!(data.moveToFirst()) || data.getCount() ==0){
                    //cursor is empty
                    noTrailersAvailable();
                }else{
                    mTrailersAdapter.swapCursor(data);
                    data.moveToPosition(0);
                    String trailerKey = data.getString(data.getColumnIndex(MoviesContract.TrailerEntry.COLUMN_KEY));
                    mMovieShare = "http://www.youtube.com/watch?v=" + trailerKey;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()){
            case TRAILERS_LOADER:
                mTrailersAdapter.swapCursor(null);
                break;
            default:
                break;
        }
    }

//    private void fetchReviewsFromDataBase(){
//        mReviewsAdapter.clear();
//        mArrReviews = mMoviesDbManager.getReviewsAtMovieID(mDbMovieID);
//
//        Log.v(LOG_TAG, "arrReviews " + mArrReviews.size());
//
//        if(mArrReviews != null && mArrReviews.size() == 0){
//            noReviewsAvailable();
//            return;
//        }
//
//        mReviewsAdapter = new ReviewsAdapter(getActivity(),  mArrReviews);
//        mReviews_list.setAdapter(mReviewsAdapter);
//    }
}
