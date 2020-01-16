package com.example.substandard.database.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface SongDao extends BaseDao<Song> {
    @Query("SELECT * FROM songs ORDER BY id")
    LiveData<List<Song>> loadAll();

    @Query("SELECT * FROM songs WHERE id = :id")
    Single<Song> loadSongById(String id);

    @Query("SELECT * FROM songs WHERE album_id = :albumId")
    LiveData<List<Song>> loadSongsFromAlbumId(String albumId);

    @Query("SELECT suffix FROM songs WHERE id = :id")
    LiveData<String> loadSuffix(String id);

    @Query("SELECT offline FROM songs WHERE id = :id")
    LiveData<Boolean> isOffline(String id);

    @Query("UPDATE songs SET offline = :availableOffline WHERE id = :id")
    void setAvailableOffline(String id, boolean availableOffline);

    @Query("SELECT artist_name FROM songs WHERE id = :id")
    LiveData<String> getArtistName(String id);
}
