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
public interface SongDao {
    @Query("SELECT * FROM songs ORDER BY id")
    LiveData<List<Song>> loadAll();

    @Delete
    void delete(Song song);

    @Insert
    void insert(Song song);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Song> Songs);

    @Update
    void update(List<Song> songs);

    @Query("SELECT * FROM songs WHERE id = :id")
    LiveData<Song> loadSongById(String id);

    @Query("SELECT * FROM songs where album_id = :albumId")
    LiveData<List<Song>> loadSongsFromAlbumId(String albumId);
}
