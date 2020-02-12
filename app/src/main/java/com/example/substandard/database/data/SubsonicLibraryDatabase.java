package com.example.substandard.database.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.substandard.AppExecutors;
import com.example.substandard.database.network.subsonic.SubsonicNetworkDataSource;

/**
 * This database holds tables for artists, albums, and songs in the library.
 *
 * Follows a singleton instantiation pattern. Use getInstance to access the
 * database object.
 */
@Database(entities = {Album.class, Artist.class, Song.class},
        version = 1)
public abstract class SubsonicLibraryDatabase extends RoomDatabase {
    private static final String TAG = SubsonicLibraryDatabase.class.getSimpleName();

    private final static Object LOCK = new Object();
    private static SubsonicLibraryDatabase databaseInstance;

    public static final String DATABASE_NAME = "librarydb";

    public static SubsonicLibraryDatabase getInstance(final Context context) {
        if (null == databaseInstance) {
            synchronized (LOCK) {
                Log.d(TAG, "getInstance: creating database instance");
                databaseInstance = Room.databaseBuilder(context, SubsonicLibraryDatabase.class, DATABASE_NAME)
                        // Adds a callback to initialize database when table is created
                        .addCallback(new Callback() {
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                AppExecutors executors = AppExecutors.getInstance();
                                SubsonicNetworkDataSource.getInstance(context, executors)
                                        .initializeLibrary();
                            }
                        })
                        .build();
            }
        }
        Log.d(TAG, "getInstance: database accessed");
        return databaseInstance;
    }

    public abstract AlbumDao albumDao();
    public abstract SongDao songDao();
    public abstract ArtistDao artistDao();
}
