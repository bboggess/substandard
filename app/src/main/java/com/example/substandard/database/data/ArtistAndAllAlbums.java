package com.example.substandard.database.data;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class ArtistAndAllAlbums {
    @Embedded
    private Artist artist;

    @Relation(parentColumn = "id", entityColumn = "artist_id")
    private List<Album> albums;

    public ArtistAndAllAlbums(Artist artist, List<Album> albums) {
        this.artist = artist;
        this.albums = albums;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }
}
