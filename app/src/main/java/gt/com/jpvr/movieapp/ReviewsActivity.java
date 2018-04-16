package gt.com.jpvr.movieapp;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;

import java.net.URL;
import java.util.List;

import gt.com.jpvr.movieapp.adapters.ReviewsAdapter;
import gt.com.jpvr.movieapp.databinding.ActivityReviewsBinding;
import gt.com.jpvr.movieapp.models.Movie;
import gt.com.jpvr.movieapp.models.Review;
import gt.com.jpvr.movieapp.utilities.MoviesJsonUtils;
import gt.com.jpvr.movieapp.utilities.NetworkUtils;

import static gt.com.jpvr.movieapp.DetailActivity.EXTRA_MOVIE;

public class ReviewsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Review>> {

    private static final String ARGS_ID = "id";
    private static final int REVIEWS_LOADER_ID = 1;
    private ActivityReviewsBinding mBinding;
    private Movie mMovie;

    private ReviewsAdapter mReviewsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_reviews);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_MOVIE)) {
            mMovie = extras.getParcelable(EXTRA_MOVIE);
            populate();
        } else {
            finish();
        }

        mReviewsAdapter = new ReviewsAdapter();

        mBinding.rvReviews.setLayoutManager(new LinearLayoutManager(this));
        mBinding.rvReviews.setAdapter(mReviewsAdapter);

        mBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadReviews(true);
            }
        });

        loadReviews(false);
    }

    /**
     * Populates views of this activity. In this case only the action bar.
     */
    private void populate() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.reviews));
            actionBar.setSubtitle(mMovie.getTitle());

            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Hide error message and show recyclerView.
     */
    private void showReviews() {
        mBinding.tvErrorMessage.setVisibility(View.INVISIBLE);
        mBinding.rvReviews.setVisibility(View.VISIBLE);
    }

    /**
     * Show progress bar and run AsyncTask to fetch reviews from server from the current movie.
     */
    private void loadReviews(boolean forceReload) {
        showReviews();

        Bundle bundleForLoader = new Bundle();
        bundleForLoader.putLong(ARGS_ID, mMovie.getId());

        if (forceReload) {
            getSupportLoaderManager().restartLoader(REVIEWS_LOADER_ID, bundleForLoader, this);
        } else {
            getSupportLoaderManager().initLoader(REVIEWS_LOADER_ID, bundleForLoader, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Show error message and hide recyclerView.
     */
    private void showErrorMessage() {
        mBinding.tvErrorMessage.setVisibility(View.VISIBLE);
        mBinding.rvReviews.setVisibility(View.GONE);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<List<Review>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<Review>>(this) {
            List<Review> mReviews = null;

            @Override
            protected void onStartLoading() {
                if (mReviews != null) {
                    deliverResult(mReviews);
                } else {
                    mBinding.swipeRefreshLayout.setRefreshing(true);
                    forceLoad();
                }
            }

            @Override
            public List<Review> loadInBackground() {
                long id = args.getLong(ARGS_ID);

                URL reviewsURL = NetworkUtils.buildReviewsURL(String.valueOf(id));

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
        mBinding.swipeRefreshLayout.setRefreshing(false);

        if (data == null || data.isEmpty()) {
            showErrorMessage();
        } else {
            showReviews();
            mReviewsAdapter.setMovieData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Review>> loader) { }
}
