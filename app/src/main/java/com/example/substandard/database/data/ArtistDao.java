package com.example.substandard.database.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

/**
 * DAO class for database of artists on the server
 */
@Dao
public interface ArtistDao extends BaseDao<Artist> {
    @Query("SELECT * FROM artists ORDER BY name")
    LiveData<List<Artist>> loadAll();

    @Query("SELECT name FROM artists WHERE id = :id")
    String getArtistName(String id);

    @Query("SELECT * FROM artists WHERE id = :id")
    LiveData<Artist> loadArtistById(String id);
}
