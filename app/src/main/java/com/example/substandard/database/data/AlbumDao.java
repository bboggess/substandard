package com.example.substandard.database.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY name")
    LiveData<List<Album>> loadAll();

    @Delete
    void delete(Album album);

    @Insert
    void insert(Album album);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Album> albums);

    @Update
    void update(List<Album> albums);

    @Query("SELECT * FROM albums WHERE id = :id")
    LiveData<List<Album>> loadAlbumById(String id);

    @Query("SELECT * FROM albums WHERE artist_id = :artistId")
    LiveData<List<Album>> loadAlbumsByArtistId(String artistId);
}
