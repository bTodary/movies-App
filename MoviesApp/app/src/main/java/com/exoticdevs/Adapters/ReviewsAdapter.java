package com.exoticdevs.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.exoticdevs.AppClasses.Review;
import com.exoticdevs.moviesapp.R;

import java.util.List;

/**
 * Created by mac on 4/13/16.
 */
public class ReviewsAdapter extends ArrayAdapter<Review> {

    private static final String LOG_TAG = ReviewsAdapter.class.getSimpleName();

    Context mContext;
    ViewHolder mHolder;

    public ReviewsAdapter(Context context, List<Review> reviews) {
        super(context, 0, reviews);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Review reviewItem = getItem(position);

        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_item_movie, parent, false);

            mHolder.author_txt = (TextView) convertView.findViewById(R.id.author_txt);
            mHolder.content_txt = (TextView) convertView.findViewById(R.id.content_txt);
            mHolder.more_click = (Button) convertView.findViewById(R.id.more_click);
            convertView.setTag(mHolder);
        }else{
            mHolder = (ViewHolder) convertView.getTag();
        }

        mHolder.more_click.setVisibility(View.VISIBLE);

        mHolder.author_txt.setText(reviewItem.getAuthor());
        mHolder.content_txt.setText(reviewItem.getContent());

        mHolder.content_txt.post(new Runnable() {
            @Override
            public void run() {
                int lineCnt = mHolder.content_txt.getLineCount();
                if (lineCnt < 3) {
                    mHolder.more_click.setVisibility(View.GONE);
                }
            }
        });

        mHolder.more_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = reviewItem.getUrl();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                mContext.startActivity(i);
            }
        });

        return convertView;
    }

    private class ViewHolder {
        TextView author_txt, content_txt;
        Button more_click;
    }
}
