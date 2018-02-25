package gt.com.jpvr.movieapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
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
import gt.com.jpvr.movieapp.untilities.MoviesJsonUtils;
import gt.com.jpvr.movieapp.untilities.NetworkUtils;
import gt.com.jpvr.movieapp.untilities.NetworkUtils.SortCriteria;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private static final int COLUMN_COUNT = 2;

    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView mMoviesRecyclerView;
    TextView mErrorMessage;

    MovieAdapter mMovieAdapter;
    SortCriteria mCurrentCriteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrentCriteria = SortCriteria.POPULAR;

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.srl_movies);
        mMoviesRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        mErrorMessage = (TextView) findViewById(R.id.tv_error_message);

        mMovieAdapter = new MovieAdapter(this);
        mMovieAdapter.setClickHandler(this);

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

    /**
     * Show progress bar and run AsyncTask to fetch movies from server with the sorting criteria
     * the user has selected.
     */
    private void loadMovies() {
        showMovies();

        new FetchMovieList().execute(mCurrentCriteria.toString());
    }

    /**
     * Show progress bar and run AsyncTask to fetch movies from server with the sorting criteria
     * the user has selected.
     */
    private void showMovies() {
        mErrorMessage.setVisibility(View.INVISIBLE);
        mMoviesRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mErrorMessage.setVisibility(View.VISIBLE);
        mMoviesRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(Movie movie) {
        launchDetailView(movie);
    }

    private void launchDetailView(Movie movie) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_MOVIE, movie);
        startActivity(intent);
    }

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
