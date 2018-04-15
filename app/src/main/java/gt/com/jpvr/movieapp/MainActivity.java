package gt.com.jpvr.movieapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.net.URL;
import java.util.List;

import gt.com.jpvr.movieapp.adapters.MovieAdapter;
import gt.com.jpvr.movieapp.models.Movie;
import gt.com.jpvr.movieapp.utilities.MoviesJsonUtils;
import gt.com.jpvr.movieapp.utilities.NetworkUtils;
import gt.com.jpvr.movieapp.utilities.NetworkUtils.SortCriteria;

public class MainActivity extends AppCompatActivity implements
        MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final int COLUMN_COUNT = 2;
    private static final String ARGS_CRITERIA = "criteria";
    private static final int MOVIE_LOADER_ID = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mMoviesRecyclerView;
    private TextView mErrorMessage;

    private MovieAdapter mMovieAdapter;
    private SortCriteria mCurrentCriteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentCriteria = SortCriteria.POPULAR;

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_movies);
        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mErrorMessage = (TextView) findViewById(R.id.tv_error_message);

        mMovieAdapter = new MovieAdapter(this, new MovieAdapter.MovieAdapterOnClickHandler() {
            @Override
            public void onClick(Movie movie) {
                launchDetailView(movie);
            }
        });

        mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(this, COLUMN_COUNT));
        mMoviesRecyclerView.setAdapter(mMovieAdapter);

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.accent));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMovies();
            }
        });

        loadMovies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        Spinner criteriaSpinner = (Spinner) menu.findItem(R.id.action_sort).getActionView();
        criteriaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SortCriteria newCriteria = SortCriteria.POPULAR;
                if (position == 1) {
                    newCriteria = SortCriteria.TOP_RATED;
                }

                if (!newCriteria.equals(mCurrentCriteria)) {
                    mCurrentCriteria = newCriteria;

                    loadMovies();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        return true;
    }

    private void invalidateData() {
        mMovieAdapter.setMovieData(null);
    }

    /**
     * Show progress bar and run AsyncTask to fetch movies from server with the sorting criteria
     * the user has selected.
     */
    private void loadMovies() {
        showMovies();

        Bundle bundleForLoader = new Bundle();
        bundleForLoader.putString(ARGS_CRITERIA, mCurrentCriteria.toString());

//        invalidateData();
//        getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, bundleForLoader, MainActivity.this);
        getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, bundleForLoader, MainActivity.this);
    }

    /**
     * Hide error message and show recyclerView.
     */
    private void showMovies() {
        mErrorMessage.setVisibility(View.INVISIBLE);
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Show error message and hide recyclerView.
     */
    private void showErrorMessage() {
        mErrorMessage.setVisibility(View.VISIBLE);
        mMoviesRecyclerView.setVisibility(View.GONE);
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
        startActivity(intent);
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
                    mSwipeRefreshLayout.setRefreshing(true);
                    forceLoad();
                }
            }


            @Override
            public List<Movie> loadInBackground() {
                String criteria = args.getString(ARGS_CRITERIA);
                URL moviesUrl = NetworkUtils.buildMoviesUrl(criteria);

                try {
                    String response = NetworkUtils.getResponseFromHttpUrl(moviesUrl);
                    return MoviesJsonUtils.getMoviesFromJson(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(List<Movie> data) {
                mMovies = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        mSwipeRefreshLayout.setRefreshing(false);

        if (data == null || data.isEmpty()) {
            showErrorMessage();
        } else {
            showMovies();
            mMovieAdapter.setMovieData(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) { }

    /**
     * AsyncTask to fetch a list of movies according to the criteria selected by the user.
     */
    public class FetchMovieList extends AsyncTask<String, Void, List<Movie>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSwipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected List<Movie> doInBackground(String... params) {
            if (params.length == 0) return null;

            String criteria = params[0];
            URL moviesUrl = NetworkUtils.buildMoviesUrl(criteria);

            try {
                String response = NetworkUtils.getResponseFromHttpUrl(moviesUrl);
                return MoviesJsonUtils.getMoviesFromJson(response);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            mSwipeRefreshLayout.setRefreshing(false);

            if (movies == null || movies.isEmpty()) {
                showErrorMessage();
            } else {
                showMovies();
                mMovieAdapter.setMovieData(movies);
            }

            super.onPostExecute(movies);
        }
    }
}
