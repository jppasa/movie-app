package gt.com.jpvr.movieapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import gt.com.jpvr.movieapp.data.MovieContract.MovieEntry;
/**
 * Created by Juan Pablo Villegas on 12-Apr-18.
 * 
 */

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
                "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                        MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MovieEntry.COLUMN_SERVER_ID + " INTEGER NOT NULL, " +
                        MovieEntry.COLUMN_HAS_VIDEO + " INTEGER NOT NULL DEFAULT 0," +
                        MovieEntry.COLUMN_VOTE_COUNT + " INTEGER, " +
                        MovieEntry.COLUMN_VOTE_AVERAGE + " INTEGER, " +
                        MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        MovieEntry.COLUMN_POPULARITY + " REAL, " +
                        MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
                        MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT, " +
                        MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT, " +
                        MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
                        MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " +

                        " UNIQUE (" + MovieEntry.COLUMN_SERVER_ID + ") ON CONFLICT REPLACE);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
