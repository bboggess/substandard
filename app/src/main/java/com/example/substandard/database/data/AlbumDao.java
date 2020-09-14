package com.example.substandard.database.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface AlbumDao extends BaseDao<Album> {
    @Query("SELECT * FROM albums ORDER BY name")
    LiveData<List<Album>> loadAll();

    @Query("SELECT * FROM albums WHERE id = :id")
    LiveData<List<Album>> loadAlbumById(String id);

    @Transaction
    @Query("SELECT * FROM albums WHERE id IN (:ids)")
    LiveData<List<Album>> loadAlbumsById(List<String> ids);

    @Query("SELECT * FROM albums WHERE artist_id = :artistId")
    LiveData<List<Album>> loadAlbumsByArtistId(String artistId);

    @Query("SELECT name FROM albums WHERE id = :id")
    String loadAlbumName(String id);

    @Transaction
    @Query("SELECT * FROM albums WHERE id = :id")
    LiveData<AlbumAndAllSongs> getAlbumWithAllSongs(String id);

    @Transaction
    @Query("SELECT * FROM albums INNER JOIN artists ON albums.artist_id = artists.id WHERE albums.id = :id")
    LiveData<AlbumAndArtist> getAlbumWithArtist(String id);

    @Query("SELECT * FROM albums INNER JOIN artists ON  albums.artist_id = artists.id WHERE albums.id IN (:ids)")
    LiveData<List<AlbumAndArtist>> getAlbumsWithArtist(List<String> ids);
}
