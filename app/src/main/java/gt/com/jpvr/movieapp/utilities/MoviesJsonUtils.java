package gt.com.jpvr.movieapp.utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import gt.com.jpvr.movieapp.models.Movie;
import gt.com.jpvr.movieapp.models.Review;
import gt.com.jpvr.movieapp.models.Video;

/**
 * Created by Juan Pablo Villegas on 2/24/2018.
 * Utility class to handle JSON operations with Movie objects.
 */

public class MoviesJsonUtils {

    /**
     * Parses JSON from a the movies db and returns an list of {@code Movies}.
     *
     * @param moviesJsonStr JSON response from server
     * @return List of Movie objects
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static List<Movie> getMoviesFromJson(String moviesJsonStr) throws JSONException {
        final String MDB_RESULTS = "results";

        List<Movie> parsedMovies = new LinkedList<>();

        JSONObject moviesJson = new JSONObject(moviesJsonStr);

        if (moviesJson.has(MDB_RESULTS)) {
            JSONArray moviesArray = moviesJson.getJSONArray(MDB_RESULTS);

            for (int i = 0; i < moviesArray.length(); i++) {
                Movie movie = parseSingleMovie(moviesArray.getJSONObject(i));

                /* If movie was invalid (no id) continue with the next one */
                if (movie != null) {
                    parsedMovies.add(movie);
                }
            }
        }

        return parsedMovies;
    }

    /**
     * Parses JSON from a the movies db and returns an list of {@code Movies}.
     *
     * @param movieJson JSONObject that contains the movie data
     * @return Movie object describing the parsed movie
     */
    private static Movie parseSingleMovie(JSONObject movieJson) {
        final String MDB_ID = "id";
        final String MDB_VIDEO = "video";
        final String MDB_VOTE_COUNT = "vote_count";
        final String MDB_VOTE_AVG = "vote_average";
        final String MDB_TITLE = "title";
        final String MDB_POPULARITY = "popularity";
        final String MDB_POSTER_PATH = "poster_path";
        final String MDB_ORIGINAL_LANG = "original_language";
        final String MDB_ORIGINAL_TITLE = "original_title";
        final String MDB_OVERVIEW = "overview";
        final String MDB_RELEASE_DATE = "release_date";

        if (movieJson.has(MDB_ID)) {
            /* Movie should have at least an id to be valid */
            Movie movie = new Movie(movieJson.optInt(MDB_ID));

            if (movieJson.has(MDB_VIDEO)) {
                movie.setHasVideo(movieJson.optBoolean(MDB_VIDEO, false));
            }

            if (movieJson.has(MDB_VOTE_COUNT)) {
                movie.setVoteCount(movieJson.optInt(MDB_VOTE_COUNT));
            }

            if (movieJson.has(MDB_VOTE_AVG)) {
                movie.setVoteAverage(movieJson.optDouble(MDB_VOTE_AVG));
            }

            if (movieJson.has(MDB_TITLE)) {
                movie.setTitle(movieJson.optString(MDB_TITLE));
            }

            if (movieJson.has(MDB_POPULARITY)) {
                movie.setPopularity(movieJson.optDouble(MDB_POPULARITY));
            }

            if (movieJson.has(MDB_POSTER_PATH)) {
                movie.setPosterPath(movieJson.optString(MDB_POSTER_PATH));
            }

            if (movieJson.has(MDB_ORIGINAL_LANG)) {
                movie.setOriginalLanguage(movieJson.optString(MDB_ORIGINAL_LANG));
            }

            if (movieJson.has(MDB_ORIGINAL_TITLE)) {
                movie.setOriginalTitle(movieJson.optString(MDB_ORIGINAL_TITLE));
            }

            if (movieJson.has(MDB_OVERVIEW)) {
                movie.setOverview(movieJson.optString(MDB_OVERVIEW));
            }

            if (movieJson.has(MDB_RELEASE_DATE)) {
                movie.setReleaseDate(movieJson.optString(MDB_RELEASE_DATE));
            }

            return movie;
        }

        return null;
    }

    /**
     * Parses JSON from a the movies db and returns an list of {@code Video}s.
     *
     * @param videosJsonStr JSON response from server
     * @return List of Video objects
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static List<Video> getVideosFromJson(String videosJsonStr) throws JSONException {
        final String RESULTS = "results";

        List<Video> parseVideos = new LinkedList<>();

        JSONObject videosJson = new JSONObject(videosJsonStr);

        if (videosJson.has(RESULTS)) {
            JSONArray videosArray = videosJson.getJSONArray(RESULTS);

            for (int i = 0; i < videosArray.length(); i++) {
                Video video = parseSingleVideo(videosArray.getJSONObject(i));

                /* If video was invalid (no id) continue with the next one */
                if (video != null) {
                    parseVideos.add(video);
                }
            }
        }

        return parseVideos;
    }

    /**
     * Parses JSON from a the movies db and returns an list of {@code Video}s.
     *
     * @param videoJson JSONObject that contains the video data
     * @return Video object describing the parsed video
     */
    private static Video parseSingleVideo(JSONObject videoJson) {
        final String ID = "id";
        final String KEY = "key";
        final String NAME = "name";
        final String SITE = "site";
        final String SIZE = "size";
        final String TYPE = "type";

        if (videoJson.has(ID)) {
            /* Video should have at least an id to be valid */
            Video video = new Video(videoJson.optInt(ID));

            video.setKey(videoJson.optString(KEY));
            video.setName(videoJson.optString(NAME));
            video.setSite(videoJson.optString(SITE));
            video.setSize(videoJson.optString(SIZE));
            video.setType(videoJson.optString(TYPE));

            return video;
        }

        return null;
    }

    /**
     * Parses JSON from a the movies db and returns an list of {@code Review}s.
     *
     * @param reviewsJsonStr JSON response from server
     * @return List of Review objects
     *
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static List<Review> getReviewsFromJson(String reviewsJsonStr) throws JSONException {
        final String RESULTS = "results";

        List<Review> reviews = new LinkedList<>();

        JSONObject reviewsJson = new JSONObject(reviewsJsonStr);

        if (reviewsJson.has(RESULTS)) {
            JSONArray reviewsArray = reviewsJson.getJSONArray(RESULTS);

            for (int i = 0; i < reviewsArray.length(); i++) {
                Review review = parseSingleReview(reviewsArray.getJSONObject(i));

                /* If video was invalid (no id) continue with the next one */
                if (review != null) {
                    reviews.add(review);
                }
            }
        }

        return reviews;
    }

    /**
     * Parses JSON from a the movies db and returns an list of {@code Review}s.
     *
     * @param reviewJson JSONObject that contains the reviews
     * @return Review object describing the parsed review
     */
    private static Review parseSingleReview(JSONObject reviewJson) {
        final String ID = "id";
        final String AUTHOR = "author";
        final String CONTENT = "content";

        if (reviewJson.has(ID)) {
            /* Review should have at least an id to be valid */
            Review review = new Review(reviewJson.optInt(ID));

            review.setAuthor(reviewJson.optString(AUTHOR));
            review.setContent(reviewJson.optString(CONTENT));

            return review;
        }

        return null;
    }


}
