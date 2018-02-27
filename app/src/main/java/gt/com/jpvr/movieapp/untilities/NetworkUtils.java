package gt.com.jpvr.movieapp.untilities;

import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Juan Pablo Villegas on 2/24/2018.
 * Utility class to handle network requests.
 */

public class NetworkUtils {
    private static final String BASE_URL = "http://api.themoviedb.org";
    private static final String MOVIE_PATH = "/3/movie";

    private static final String IMAGE_BASE_URL = "http://image.tmdb.org";
    private static final String IMAGE_PATH = "/t/p";

    private final static String API_KEY_PARAM = "api_key";
    private static final String apiKey = "YOUR_API_KEY";

    /**
     *  Defines the two possible criteria that the app handles.
     */
    public enum SortCriteria {
        POPULAR("popular"), TOP_RATED("top_rated");

        private final String value;
        SortCriteria(String s) {
            value = s;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     *  Defines the sizes of the images to download.
     *  Only using one (w185) right now, but I will add more later to test in different views.
     */
    public enum ImageSize {
        W185("/w185");

        private final String value;

        ImageSize(String s) {
            value = s;
        }
    }

    /**
     * Builds the URL used to fetch a list of movies form The Movie DB sorted according to
     * the specified criteria.
     *
     * @param criteria The criteria to sort the movies. Either "popular" or "top_rated".
     * @return The URL to use to query the movie db server for a list of movies.
     */
    public static URL buildMoviesUrl(String criteria) {

        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .path(MOVIE_PATH)
                .appendPath(criteria)
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Builds the URL used to fetch an image of a movie poster from The Movie DB.
     *
     * @param size The size of the image.
     * @return The URL to use to query the movie db server for the image.
     * @see ImageSize
     */
    public static URL buildImageURL(ImageSize size, String id) {
        Uri builtUri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                .path(IMAGE_PATH + size.value + id)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}
