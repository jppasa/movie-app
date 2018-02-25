package gt.com.jpvr.movieapp;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.Locale;

import gt.com.jpvr.movieapp.models.Movie;
import gt.com.jpvr.movieapp.untilities.NetworkUtils;
import gt.com.jpvr.movieapp.untilities.NetworkUtils.ImageSize;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE = "extra_movie";
    private Movie mMovie;

    private ImageView mPosterImage;
    private TextView mReleaseDateTextView;
    private TextView mUserRatingTextView;
    private TextView mOverviewTextView;
    private TextView mOriginalTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_MOVIE)) {
            mMovie = extras.getParcelable(EXTRA_MOVIE);

            mPosterImage = (ImageView) findViewById(R.id.iv_detail_movie_poster);
            mReleaseDateTextView = (TextView) findViewById(R.id.tv_release_date);
            mUserRatingTextView = (TextView) findViewById(R.id.tv_user_rating);
            mOverviewTextView = (TextView) findViewById(R.id.tv_overview);
            mOriginalTitle = (TextView) findViewById(R.id.tv_original_title_language);

            populate();
        } else {
            finish();
        }
    }

    private void populate() {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(mMovie.getTitle());
        }

        if (mMovie.getPosterPath() != null) {
            URL posterPath = NetworkUtils.buildImageURL(ImageSize.W185, mMovie.getPosterPath());

            Picasso.with(this)
                    .load(posterPath.toString())
                    .into(mPosterImage);
        }

        mReleaseDateTextView.setText(mMovie.getReleaseDate());
        mUserRatingTextView.setText(String.format(Locale.US, "%.1f / 10", mMovie.getVoteAverage()));

        String originalTitleStr = mMovie.getOriginalTitle() + " (" + mMovie.getOriginalLanguage().toUpperCase() + ")";

        mOriginalTitle.setText(originalTitleStr);
        mOverviewTextView.setText(mMovie.getOverview());
    }
}
