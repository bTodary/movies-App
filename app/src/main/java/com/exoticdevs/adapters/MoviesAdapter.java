package com.exoticdevs.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.exoticdevs.data.MoviesContract;
import com.exoticdevs.helper.PicassoCache;
import com.exoticdevs.moviesapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.exoticdevs.moviesapp.R.id.movie_image;

/**
 * Created by mac on 3/23/16.
 */
public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.ViewHolder> {

    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();

    private Cursor mCursor;
    final private Context mContext;
    final private MoviestAdapterOnClickHandler mClickHandler;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @Bind(movie_image) ImageView mImg;
        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onClick(mCursor, this);
        }
    }

    public static interface MoviestAdapterOnClickHandler {
        void onClick(Cursor cursor, ViewHolder vh);
    }

    public MoviesAdapter(Context mContext, MoviestAdapterOnClickHandler mClickHandler) {
        this.mContext = mContext;
        this.mClickHandler = mClickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item_movie, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        mCursor.moveToPosition(position);
        PicassoCache.getPicassoInstance(mContext)
                .load("http://image.tmdb.org/t/p/w185" + convertCursorRowToUXFormat(mCursor))
                .into(viewHolder.mImg);
    }

    private String convertCursorRowToUXFormat(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(MoviesContract.FavEntry.COLUMN_POSTER));

    }


    @Override
    public int getItemCount() {
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

}
