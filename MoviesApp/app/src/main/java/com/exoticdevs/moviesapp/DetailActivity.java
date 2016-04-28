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

import com.exoticdevs.Fragments.DetailFragment;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getName();

    public static final String SORT_ARG = "sort";
    private static final int DETAIL_LOADER = 0;
    private String mSort;
    private DetailFragment mDetailFragment;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDetailFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentDetail);

        mIntent = getIntent();
        if (mIntent == null) {
            return;
        }
        mSort = mIntent.getStringExtra(SORT_ARG);

        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri detailUri = mIntent.getData();

        Log.v(LOG_TAG, "detailUri: " + detailUri);

        // create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                this,
                detailUri,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        if (mDetailFragment != null) {
            mDetailFragment.updateData(data, mSort);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
