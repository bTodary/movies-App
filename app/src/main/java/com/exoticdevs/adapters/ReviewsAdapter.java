package com.exoticdevs.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.exoticdevs.data.MoviesContract;
import com.exoticdevs.moviesapp.R;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.exoticdevs.moviesapp.R.id.author_txt;
import static com.exoticdevs.moviesapp.R.id.content_txt;
import static com.exoticdevs.moviesapp.R.id.more_click;

/**
 * Created by mac on 4/13/16.
 */
public class ReviewsAdapter extends CursorAdapter {

    private static final String LOG_TAG = ReviewsAdapter.class.getSimpleName();

    private Context mContext;

    class ViewHolder  extends RecyclerView.ViewHolder{
        @Bind(author_txt) TextView authorTxt;
        @Bind(content_txt) TextView contentTxt;
        @Bind(more_click) Button moreClick;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public ReviewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.review_item_movie, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.moreClick.setVisibility(View.VISIBLE);

        String reviewAuthor =
                cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_AUTHOR));

        String reviewContent =
                cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_CONTENT));

        final String reviewUrl =
                cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_URL));

        holder.authorTxt.setText(reviewAuthor);
        holder.contentTxt.setText(reviewContent);

        holder.contentTxt.post(new Runnable() {
            @Override
            public void run() {
                int lineCnt = holder.contentTxt.getLineCount();
                if (lineCnt < 3) {
                    holder.moreClick.setVisibility(View.GONE);
                }
            }
        });

        holder.moreClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(reviewUrl));
                mContext.startActivity(i);
            }
        });
    }
}
