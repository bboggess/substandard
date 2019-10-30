package com.example.substandard.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

/**
 * DAO class for database of artists on the server
 */
@Dao
public interface ArtistDao {
    @Query("SELECT * FROM artists_server ORDER BY name")
    List<Artist> loadAll();

    @Delete
    void delete(Artist artist);
}
