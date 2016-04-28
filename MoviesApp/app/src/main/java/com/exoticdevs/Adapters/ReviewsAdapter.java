package com.exoticdevs.Adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.exoticdevs.Data.MoviesContract;
import com.exoticdevs.moviesapp.R;

/**
 * Created by mac on 4/13/16.
 */
public class ReviewsAdapter extends CursorAdapter {

    private static final String LOG_TAG = ReviewsAdapter.class.getSimpleName();

    private Context mContext;

    private class ViewHolder {
        TextView author_txt, content_txt;
        Button more_click;
    }

    public ReviewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.review_item_movie, parent, false);
        ViewHolder holder = new ViewHolder();

        holder.author_txt = (TextView) view.findViewById(R.id.author_txt);
        holder.content_txt = (TextView) view.findViewById(R.id.content_txt);
        holder.more_click = (Button) view.findViewById(R.id.more_click);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.more_click.setVisibility(View.VISIBLE);

        String reviewAuthor =
                cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_AUTHOR));

        String reviewContent =
                cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_CONTENT));

        final String reviewUrl =
                cursor.getString(cursor.getColumnIndex(MoviesContract.ReviewEntry.COLUMN_URL));

        holder.author_txt.setText(reviewAuthor);
        holder.content_txt.setText(reviewContent);

        holder.content_txt.post(new Runnable() {
            @Override
            public void run() {
                int lineCnt = holder.content_txt.getLineCount();
                if (lineCnt < 3) {
                    holder.more_click.setVisibility(View.GONE);
                }
            }
        });

        holder.more_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(reviewUrl));
                mContext.startActivity(i);
            }
        });
    }
}
