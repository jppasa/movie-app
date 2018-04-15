package gt.com.jpvr.movieapp;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;
import java.util.Locale;

import gt.com.jpvr.movieapp.models.Movie;
import gt.com.jpvr.movieapp.models.Video;
import gt.com.jpvr.movieapp.utilities.MoviesJsonUtils;
import gt.com.jpvr.movieapp.utilities.NetworkUtils;
import gt.com.jpvr.movieapp.utilities.NetworkUtils.ImageSize;
import gt.com.jpvr.movieapp.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Video>> {

    public static final String EXTRA_MOVIE = "extra_movie";
    private static final String ARGS_ID = "id";
    private static final int VIDEO_LOADER_ID = 1;
    private Movie mMovie;

    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_detail);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_MOVIE)) {
            mMovie = extras.getParcelable(EXTRA_MOVIE);
            populate();
        } else {
            finish();
        }

        Bundle args = new Bundle();
        args.putString(ARGS_ID, String.valueOf(mMovie.getId()));

        getSupportLoaderManager().initLoader(VIDEO_LOADER_ID, args, this);
    }

    /**
     * Populates all the views with the movie information in {@code mMovie} field.
     */
    private void populate() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(mMovie.getTitle());
        }

        if (mMovie.getPosterPath() != null && !mMovie.getPosterPath().isEmpty()) {
            URL posterPath = NetworkUtils.buildImageURL(ImageSize.W185, mMovie.getPosterPath());

            Picasso.with(this)
                    .load(posterPath.toString())
                    .into(mDetailBinding.ivMoviePoster);
        } else {
            mDetailBinding.ivMoviePoster.setImageResource(R.drawable.no_poster);
        }

        mDetailBinding.tvReleaseDate.setText(mMovie.getReleaseDate());
        mDetailBinding.tvUserRating.setText(String.format(Locale.US, "%.1f / 10", mMovie.getVoteAverage()));
        mDetailBinding.ratingBar.setRating((float) mMovie.getVoteAverage());

        String originalTitleStr = mMovie.getOriginalTitle() + " (" + mMovie.getOriginalLanguage().toUpperCase() + ")";

        mDetailBinding.tvOriginalTitleLanguage.setText(originalTitleStr);
        mDetailBinding.tvOverview.setText(mMovie.getOverview());
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<List<Video>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<Video>>(this) {
            List<Video> mVideos = null;

            @Override
            protected void onStartLoading() {
                if (mVideos != null) {
                    deliverResult(mVideos);
                } else {
                    mDetailBinding.progressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public List<Video> loadInBackground() {
                String id = args.getString(ARGS_ID);
                URL moviesUrl = NetworkUtils.buildVideosURL(id);

                try {
                    String response = NetworkUtils.getResponseFromHttpUrl(moviesUrl);
                    return MoviesJsonUtils.getVideosFromJson(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(List<Video> data) {
                mVideos = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Video>> loader, List<Video> data) {
        mDetailBinding.progressBar.setVisibility(View.GONE);

        if (data == null || data.isEmpty()) {
            mDetailBinding.trailersLayout.setVisibility(View.GONE);
        } else {
            mDetailBinding.trailersLayout.setVisibility(View.VISIBLE);

            for (Video video : data) {
                populateVideoLayout(video);
            }
        }
    }

    private void populateVideoLayout(Video video) {
        View videoView = View.inflate(this, R.layout.video_item_layout, null);

        TextView name = videoView.findViewById(R.id.tvVideoName);

        name.setText(video.getName());

        mDetailBinding.trailersLayout.addView(videoView);
    }

    @Override
    public void onLoaderReset(Loader<List<Video>> loader) { }
}
