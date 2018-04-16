package gt.com.jpvr.movieapp;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;
import java.util.Locale;

import gt.com.jpvr.movieapp.data.MovieContract;
import gt.com.jpvr.movieapp.databinding.ActivityDetailBinding;
import gt.com.jpvr.movieapp.models.Movie;
import gt.com.jpvr.movieapp.models.Review;
import gt.com.jpvr.movieapp.models.Video;
import gt.com.jpvr.movieapp.utilities.MoviesJsonUtils;
import gt.com.jpvr.movieapp.utilities.NetworkUtils;
import gt.com.jpvr.movieapp.utilities.NetworkUtils.ImageSize;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "extra_movie";

    private static final String ARGS_ID = "id";
    private static final String ARGS_FAVORITE = "favorite";

    private static final int VIDEOS_LOADER_ID = 1;
    private static final int REVIEWS_LOADER_ID = 2;

    private Movie mMovie;
    private boolean isFavorite = false;

    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        if (savedInstanceState != null) {
            isFavorite = savedInstanceState.getBoolean(ARGS_FAVORITE);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_MOVIE)) {
            mMovie = extras.getParcelable(EXTRA_MOVIE);
            populate();
        } else {
            finish();
        }

        Bundle args = new Bundle();
        args.putString(ARGS_ID, String.valueOf(mMovie.getId()));

        Uri uri = MovieContract.MovieEntry.buildMovieUriFromId(mMovie.getId());
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            isFavorite = cursor.getCount() > 0;
            cursor.close();
        }

        getSupportLoaderManager().initLoader(VIDEOS_LOADER_ID, args, videosLoaderCallbacks);
        getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, args, reviewsLoaderCallbacks);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(ARGS_FAVORITE, isFavorite);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);

        MenuItem item = menu.findItem(R.id.action_favorite);

        if (isFavorite) {
            item.setIcon(R.drawable.ic_favorite_white_24dp);
        } else {
            item.setIcon(R.drawable.ic_favorite_border_white_24dp);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                toggleFavorite(item);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleFavorite(MenuItem item) {
        if (isFavorite) {
            removeFromFavorites(item);
        } else {
            addToFavorites(item);
        }
    }

    private void removeFromFavorites(MenuItem item) {
        Uri uri = MovieContract.MovieEntry.buildMovieUriFromId(mMovie.getId());

        int deleted = getContentResolver().delete(uri, null, null);

        if (deleted > 0) {
            Snackbar.make(mDetailBinding.ivMoviePoster, R.string.removed_from_favorites, Snackbar.LENGTH_LONG).show();
            item.setIcon(R.drawable.ic_favorite_border_white_24dp);

            isFavorite = false;
        }
    }

    private void addToFavorites(MenuItem item) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(MovieContract.MovieEntry.COLUMN_SERVER_ID, mMovie.getId());
        contentValues.put(MovieContract.MovieEntry.COLUMN_HAS_VIDEO, mMovie.hasVideo()? 1 : 0);
        contentValues.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT, mMovie.getVoteCount());
        contentValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, mMovie.getVoteAverage());
        contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, mMovie.getTitle());
        contentValues.put(MovieContract.MovieEntry.COLUMN_POPULARITY, mMovie.getPopularity());
        contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, mMovie.getPosterPath());
        contentValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, mMovie.getOriginalLanguage());
        contentValues.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, mMovie.getOriginalTitle());
        contentValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
        contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());

        Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

        if(uri != null) {
            Snackbar.make(mDetailBinding.ivMoviePoster, R.string.added_to_favorites, Snackbar.LENGTH_LONG).show();
            item.setIcon(R.drawable.ic_favorite_white_24dp);

            isFavorite = true;
        }
    }

//    @SuppressLint("StaticFieldLeak")
//    @Override
//    public Loader<List<Video>> onCreateLoader(int id, final Bundle args) {
//        return new AsyncTaskLoader<List<Video>>(this) {
//            List<Video> mVideos = null;
//
//            @Override
//            protected void onStartLoading() {
//                if (mVideos != null) {
//                    deliverResult(mVideos);
//                } else {
//                    mDetailBinding.progressBar.setVisibility(View.VISIBLE);
//                    forceLoad();
//                }
//            }
//
//            @Override
//            public List<Video> loadInBackground() {
//                String id = args.getString(ARGS_ID);
//                URL moviesUrl = NetworkUtils.buildVideosURL(id);
//
//                try {
//                    String response = NetworkUtils.getResponseFromHttpUrl(moviesUrl);
//                    return MoviesJsonUtils.getVideosFromJson(response);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//
//            @Override
//            public void deliverResult(List<Video> data) {
//                mVideos = data;
//                super.deliverResult(data);
//            }
//        };
//    }
//
//    @Override
//    public void onLoadFinished(Loader<List<Video>> loader, List<Video> data) {
//        mDetailBinding.progressBar.setVisibility(View.GONE);
//
//        if (data == null || data.isEmpty()) {
//            mDetailBinding.trailersLayout.setVisibility(View.GONE);
//        } else {
//            mDetailBinding.trailersLayout.setVisibility(View.VISIBLE);
//
//            for (Video video : data) {
//                populateVideoLayout(video);
//            }
//        }
//    }

    private void populateVideoLayout(final Video video) {
        View videoView = View.inflate(this, R.layout.video_item_layout, null);

        TextView name = videoView.findViewById(R.id.tvVideoName);
        name.setText(video.getName());

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                watchYoutubeVideo(DetailActivity.this, video.getKey());
            }
        });

        mDetailBinding.trailersLayout.addView(videoView);
    }

    private void populateReviewLayout(final Review review) {
        View itemView = View.inflate(this, R.layout.review_item_layout, null);

        TextView author = itemView.findViewById(R.id.tvAuthor);
        TextView content = itemView.findViewById(R.id.tvContent);

        author.setText(review.getAuthor());
        content.setText(review.getContent());

        mDetailBinding.reviewsLayout.addView(itemView);
    }

    public static void watchYoutubeVideo(Context context, String id){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + id));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }

//    @Override
//    public void onLoaderReset(Loader<List<Video>> loader) { }

//    public void OnClickSeeReviews(View view) {
//        Intent intent = new Intent(this, ReviewsActivity.class);
//        intent.putExtra(EXTRA_MOVIE, mMovie);
//        startActivity(intent);
//    }

    private LoaderCallbacks<List<Video>> videosLoaderCallbacks = new LoaderCallbacks<List<Video>>() {
        @SuppressLint("StaticFieldLeak")
        @Override
        public Loader<List<Video>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<List<Video>>(DetailActivity.this) {
                List<Video> mVideos = null;

                @Override
                protected void onStartLoading() {
                    if (mVideos != null) {
                        deliverResult(mVideos);
                    } else {
                        mDetailBinding.videosProgressBar.setVisibility(View.VISIBLE);
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
            mDetailBinding.videosProgressBar.setVisibility(View.GONE);

            if (data == null || data.isEmpty()) {
                mDetailBinding.trailersLayout.setVisibility(View.GONE);
            } else {
                mDetailBinding.trailersLayout.setVisibility(View.VISIBLE);

                for (Video video : data) {
                    populateVideoLayout(video);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Video>> loader) { }
    };


    private LoaderCallbacks<List<Review>> reviewsLoaderCallbacks = new LoaderCallbacks<List<Review>>() {
        @SuppressLint("StaticFieldLeak")
        @Override
        public Loader<List<Review>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<List<Review>>(DetailActivity.this) {
                List<Review> mReviews = null;

                @Override
                protected void onStartLoading() {
                    if (mReviews != null) {
                        deliverResult(mReviews);
                    } else {
                        mDetailBinding.reviewsProgressBar.setVisibility(View.VISIBLE);
                        forceLoad();
                    }
                }

                @Override
                public List<Review> loadInBackground() {
                    String id = args.getString(ARGS_ID);

                    URL reviewsURL = NetworkUtils.buildReviewsURL(id);

                    try {
                        String response = NetworkUtils.getResponseFromHttpUrl(reviewsURL);
                        return MoviesJsonUtils.getReviewsFromJson(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                public void deliverResult(List<Review> data) {
                    mReviews = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<List<Review>> loader, List<Review> data) {
            mDetailBinding.reviewsProgressBar.setVisibility(View.GONE);

            if (data == null || data.isEmpty()) {
                mDetailBinding.reviewsLayout.setVisibility(View.GONE);
            } else {
                mDetailBinding.reviewsLayout.setVisibility(View.VISIBLE);

                for (Review review : data) {
                    populateReviewLayout(review);
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<List<Review>> loader) { }
    };
}
