package com.example.substandard.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Room database class for the database of artists on the server.
 */
@Database(version = 1, entities = {Artist.class})
public abstract class ArtistDatabase extends RoomDatabase {
}
