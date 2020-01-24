package com.example.substandard.database.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class AlbumAndAllSongs {
    @Embedded
    private Album album;

    @Relation(parentColumn = "id", entityColumn = "album_id")
    private List<Song> songs;

    public AlbumAndAllSongs(Album album, List<Song> songs) {
        this.album = album;
        this.songs = songs;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
