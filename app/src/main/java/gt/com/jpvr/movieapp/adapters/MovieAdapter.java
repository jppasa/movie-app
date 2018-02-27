package gt.com.jpvr.movieapp.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

import gt.com.jpvr.movieapp.R;
import gt.com.jpvr.movieapp.models.Movie;
import gt.com.jpvr.movieapp.untilities.NetworkUtils;

/**
 * Created by Juan Pablo Villegas on 2/24/2018.
 * Adapter for main list of movies.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private final Context context;
    private List<Movie> mMovies;
    private final MovieAdapterOnClickHandler mClickHandler;

    public MovieAdapter(Context context, MovieAdapterOnClickHandler clickHandler) {
        this.context = context;
        this.mClickHandler = clickHandler;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item_layout, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {
        Movie movie = mMovies.get(position);

        holder.mMovieTitle.setText(movie.getTitle());

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            URL posterUrl = NetworkUtils.buildImageURL(NetworkUtils.ImageSize.W185, movie.getPosterPath());

            Picasso.with(context)
                    .load(posterUrl.toString())
                    .into(holder.mPosterImage);
        } else {
            holder.mPosterImage.setImageResource(R.drawable.no_poster);
        }
    }

    @Override
    public int getItemCount() {
        if (mMovies == null) return 0;
        return mMovies.size();
    }

    public void setMovieData(List<Movie> movieData) {
        mMovies = movieData;
        notifyDataSetChanged();
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView mMovieTitle;
        private final ImageView mPosterImage;

        MovieAdapterViewHolder(View itemView) {
            super(itemView);

            mMovieTitle = (TextView) itemView.findViewById(R.id.tv_movie_title);
            mPosterImage = (ImageView) itemView.findViewById(R.id.iv_movie_poster);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movie movie = mMovies.get(adapterPosition);
            mClickHandler.onClick(movie);
        }
    }

    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie);
    }
}
