package com.example.substandard.database.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * DAO class for database of artists on the server
 */
@Dao
public interface ArtistDao {
    @Query("SELECT * FROM artists ORDER BY name")
    LiveData<List<Artist>> loadAll();

    @Delete
    void delete(Artist artist);

    @Insert
    void insert(Artist artist);

    @Insert
    void insertAll(List<Artist> artists);

    @Update
    void update(List<Artist> artists);

    @Query("SELECT * FROM artists WHERE id = :id")
    LiveData<Artist> loadArtistById(int id);
}
