package gt.com.jpvr.movieapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Juan Pablo Villegas on 12-Apr-18.
 *
 */

public class MovieContract {
    public static final String AUTHORITY = "gt.com.jpvr.movieapp";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // TODO should it be named "favorites"?
    public static final String PATH_FAVORITES = "favorites";

     /* MovieEntry is an inner class that defines the contents of the task table */
    public static final class MovieEntry implements BaseColumns {

        // MovieEntry content URI = base content URI + path
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES)
                .build();

        public static Uri buildMovieUriFromId(long id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(id))
                    .build();
        }

        // Task table and column names
        public static final String TABLE_NAME = "favorites";

        // MovieEntry implements the interface "BaseColumns", it has an automatically produced "_ID"
        // column. "server_id" is added to store the ID in server.
        public static final String COLUMN_SERVER_ID = "server_id";
        public static final String COLUMN_HAS_VIDEO = "video";
        public static final String COLUMN_VOTE_COUNT = "vote_count";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
    }
}
