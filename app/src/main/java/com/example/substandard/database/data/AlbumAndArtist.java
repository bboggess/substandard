package com.example.substandard.database.data;

import androidx.room.Embedded;
import androidx.room.Relation;

public class AlbumAndArtist {
    @Embedded
    private Artist artist;

    @Relation(parentColumn = "id", entityColumn = "artist_id")
    private Album album;

    public AlbumAndArtist(Album album, Artist artist) {
        this.album = album;
        this.artist = artist;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }
}
