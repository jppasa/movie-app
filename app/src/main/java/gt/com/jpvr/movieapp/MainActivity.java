package gt.com.jpvr.movieapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import gt.com.jpvr.movieapp.adapters.MovieAdapter;
import gt.com.jpvr.movieapp.data.MovieContract;
import gt.com.jpvr.movieapp.databinding.ActivityMainBinding;
import gt.com.jpvr.movieapp.models.Movie;
import gt.com.jpvr.movieapp.utilities.MoviesJsonUtils;
import gt.com.jpvr.movieapp.utilities.NetworkUtils;
import gt.com.jpvr.movieapp.utilities.NetworkUtils.SortCriteria;

public class MainActivity extends AppCompatActivity implements
        MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String ARGS_CRITERIA = "criteria";
    private static final String ARGS_CRITERIA_NAME = "name";
    private static final int MOVIE_LOADER_ID = 1;
    private static final int REQUEST_CODE_MOVIE = 1;

    private MovieAdapter mMovieAdapter;
    private SortCriteria mCurrentCriteria;

    ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mCurrentCriteria = SortCriteria.POPULAR;
        if (savedInstanceState != null) {
            String criteria = savedInstanceState.getString(ARGS_CRITERIA);
            mCurrentCriteria = SortCriteria.valueOf(criteria);
        }

        mMovieAdapter = new MovieAdapter(this, new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(Movie movie) {
                launchDetailView(movie);
            }
        });

        int columnCount = 2;
        if (getScreenOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
            columnCount = 4;
        }

        mBinding.rvMovies.setLayoutManager(new GridLayoutManager(this, columnCount));
        mBinding.rvMovies.setAdapter(mMovieAdapter);

        mBinding.swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.accent));
        mBinding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMovies(true);
            }
        });

        loadMovies(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(ARGS_CRITERIA, mCurrentCriteria.name());
    }

    public int getScreenOrientation() {
        Display screenOrientation = getWindowManager().getDefaultDisplay();
        int orientation;

        if (screenOrientation.getWidth() == screenOrientation.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (screenOrientation.getWidth() < screenOrientation.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        Spinner criteriaSpinner = (Spinner) menu.findItem(R.id.action_sort).getActionView();
        criteriaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SortCriteria newCriteria = SortCriteria.POPULAR;

                switch (position) {
                    case 1:
                        newCriteria = SortCriteria.TOP_RATED;
                        break;
                    case 2:
                        newCriteria = SortCriteria.FAVORITES;
                        break;
                }

                if (!newCriteria.equals(mCurrentCriteria)) {
                    mCurrentCriteria = newCriteria;

                    loadMovies(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        int selection = 0;
        switch (mCurrentCriteria) {
            case TOP_RATED:
                selection = 1;
                break;
            case FAVORITES:
                selection = 2;
                break;
        }

        criteriaSpinner.setSelection(selection);

        return true;
    }

    /**
     * Show progress bar and run AsyncTask to fetch movies from server with the sorting criteria
     * the user has selected.
     */
    private void loadMovies(boolean forceReload) {
        showMovies();

        Bundle bundleForLoader = new Bundle();
        bundleForLoader.putString(ARGS_CRITERIA, mCurrentCriteria.toString());
        bundleForLoader.putString(ARGS_CRITERIA_NAME, mCurrentCriteria.name());

        if (forceReload) {
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, bundleForLoader, MainActivity.this);
        } else {
            getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, bundleForLoader, MainActivity.this);
        }
    }

    /**
     * Hide error message and show recyclerView.
     */
    private void showMovies() {
        mBinding.tvErrorMessage.setVisibility(View.INVISIBLE);
        mBinding.rvMovies.setVisibility(View.VISIBLE);
    }

    /**
     * Show error message and hide recyclerView.
     */
    private void showErrorMessage() {
        mBinding.tvErrorMessage.setVisibility(View.VISIBLE);
        mBinding.rvMovies.setVisibility(View.GONE);
    }

    @Override
    public void onClick(Movie movie) {
        launchDetailView(movie);
    }

    /**
     * Launches DetailActivity passing the {@code movie} as an extra.
     *
     * @param movie The Movie object to show at the DetailActivity
     */
    private void launchDetailView(Movie movie) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_MOVIE, movie);
        startActivityForResult(intent, REQUEST_CODE_MOVIE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_MOVIE) {
            if (resultCode == RESULT_OK && mCurrentCriteria == SortCriteria.FAVORITES) {
                loadMovies(true);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Loader of a movie list from The Movie DB according to a {@code SortCriteria}.
     */
    @SuppressWarnings("unchecked")
    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, final Bundle args) {

        return new AsyncTaskLoader<List<Movie>>(this) {
            List<Movie> mMovies = null;

            @Override
            protected void onStartLoading() {
                if (mMovies != null) {
                    deliverResult(mMovies);
                } else {
                    mBinding.swipeRefreshLayout.setRefreshing(true);
                    forceLoad();
                }
            }

            @Override
            public List<Movie> loadInBackground() {
                String criteriaStr = args.getString(ARGS_CRITERIA);
                SortCriteria criteria = SortCriteria.valueOf(args.getString(ARGS_CRITERIA_NAME));

                if (criteria == SortCriteria.POPULAR || criteria == SortCriteria.TOP_RATED) {
                    URL moviesUrl = NetworkUtils.buildMoviesUrl(criteriaStr);

                    try {
                        String response = NetworkUtils.getResponseFromHttpUrl(moviesUrl);
                        return MoviesJsonUtils.getMoviesFromJson(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    return queryFavorites();
                }
            }

            public void deliverResult(List<Movie> data) {
                mMovies = data;
                super.deliverResult(data);
            }
        };
    }

    /**
     * Queries the content provider for all the movies marked as favorites.
     * @return a List of Movie objects that are stored in the db.
     */
    private List<Movie> queryFavorites() {
        try {
            Cursor cursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null,null);

            if (cursor != null && cursor.moveToFirst()) {
                LinkedList<Movie> list = new LinkedList<>();

                int serverIdIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SERVER_ID);
                int hasVideoIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_HAS_VIDEO);
                int voteCountIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_COUNT);
                int voteAverageIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
                int titleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
                int popularityIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POPULARITY);
                int posterPathIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
                int originalLanguageIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE);
                int originalTitleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE);
                int overviewIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW);
                int releaseDateIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);

                while (!cursor.isAfterLast()) {
                    Movie movie = new Movie(cursor.getLong(serverIdIndex));

                    movie.setHasVideo(cursor.getInt(hasVideoIndex) == 1);
                    movie.setVoteCount(cursor.getInt(voteCountIndex));
                    movie.setVoteAverage(cursor.getDouble(voteAverageIndex));
                    movie.setTitle(cursor.getString(titleIndex));
                    movie.setPopularity(cursor.getDouble(popularityIndex));
                    movie.setPosterPath(cursor.getString(posterPathIndex));
                    movie.setOriginalLanguage(cursor.getString(originalLanguageIndex));
                    movie.setOriginalTitle(cursor.getString(originalTitleIndex));
                    movie.setOverview(cursor.getString(overviewIndex));
                    movie.setReleaseDate(cursor.getString(releaseDateIndex));

                    list.add(movie);
                    cursor.moveToNext();
                }

                cursor.close();
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        mBinding.swipeRefreshLayout.setRefreshing(false);

        if (data == null || data.isEmpty()) {
            showErrorMessage();
        } else {
            showMovies();
            mMovieAdapter.setMovieData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) { }
}
