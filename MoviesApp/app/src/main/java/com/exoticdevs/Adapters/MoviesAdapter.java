package com.exoticdevs.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.exoticdevs.AppClasses.Movie;
import com.exoticdevs.moviesapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mac on 3/23/16.
 */
public class MoviesAdapter extends ArrayAdapter<Movie> {

    private static final String LOG_TAG = MoviesAdapter.class.getSimpleName();

    Context context;

    /* @param context The current context. Used to inflate the layout file.
     * @param Movie A List of movies objects to display in a list
    */
    public MoviesAdapter(Activity context, List<Movie> Movies) {
        // Here, we initialize the ArrayAdapter's internal storage for the context and the list.
        // the second argument is used when the ArrayAdapter is populating a single ImageView.
        // Because this is a custom adapter for an ImageView, the adapter is not
        // going to use this second argument, so it can be any value. Here, we used 0.
        super(context, 0, Movies);
        this.context = context;
    }

    /**
     * @param position    The AdapterView position that is requesting a view
     * @param convertView The recycled view to populate.
     * @param parent The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the AndroidFlavor object from the ArrayAdapter at the appropriate position
        Movie movieItem = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_movie, parent, false);
        }

        ImageView movie_image = (ImageView) convertView.findViewById(R.id.movie_image);
        String movieThumbnail = movieItem.getMovie_poster();
        Picasso.with(context).load("http://image.tmdb.org/t/p/w185" + movieThumbnail).into(movie_image);

        return convertView;
    }
}
