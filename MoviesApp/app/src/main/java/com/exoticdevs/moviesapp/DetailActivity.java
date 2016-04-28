package com.exoticdevs.moviesapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.exoticdevs.Data.MoviesContract;
import com.exoticdevs.Fragments.DetailFragment;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private final String LOG_TAG = DetailActivity.class.getName();
    private static final int DETAIL_LOADER = 0;
    private DetailFragment mDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDetailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentDetail);

        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }
        Uri data = intent.getData();
        Log.v(LOG_TAG, "detail uri: " + data);

        // create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                DetailActivity.this,
                data,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) { return; }

        String mMovieTitle = data.getString(data.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE));
        Log.v(LOG_TAG, "mMovieTitle from uri " + mMovieTitle);


        mDetailFragment.updateData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
