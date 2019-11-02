package com.example.substandard.database.data;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room database class for the database of artists on the server.
 */
@Database(version = 1, entities = {Artist.class})
public abstract class ArtistDatabase extends RoomDatabase {
    private static final String TAG = ArtistDatabase.class.getSimpleName();

    // For singleton instantiation.
    // All ArtistDatabase objects share the same LOCK Object, so only one call
    // to Room.databaseBuilder can go on at once.
    private static final Object LOCK = new Object();

    private static final String DATABASE_NAME = "artists";

    private static ArtistDatabase databaseInstance;

    public static ArtistDatabase getInstance(Context context) {
        if (databaseInstance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "getInstance: Creating database instance");
                databaseInstance = Room.databaseBuilder(context, ArtistDatabase.class, DATABASE_NAME).build();
            }
        }
        Log.d(TAG, "getInstance: Getting the database instance");
        return databaseInstance;
    }

    public abstract ArtistDao artistDao();
}
