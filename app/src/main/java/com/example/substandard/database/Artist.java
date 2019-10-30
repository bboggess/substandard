package com.example.substandard.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "artists_server")
public class Artist {

    @PrimaryKey
    private int id;
    private String name;
    @ColumnInfo(name = "cover_art")
    private String coverArt;
    @ColumnInfo(name = "image_url")
    private String imageUrl;
    @ColumnInfo(name = "album_count")
    private int albumCount;

    public Artist(int id, String name, String coverArt, String imageUrl, int albumCount) {
        this.id = id;
        this.name = name;
        this.coverArt = coverArt;
        this.imageUrl = imageUrl;
        this.albumCount = albumCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoverArt() {
        return coverArt;
    }

    public void setCoverArt(String coverArt) {
        this.coverArt = coverArt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getAlbumCount() {
        return albumCount;
    }

    public void setAlbumCount(int albumCount) {
        this.albumCount = albumCount;
    }

    /**
     * Gives String representation of the Artist, namely the artist's name.
     * @return String representation of the Artist
     */
    @Override
    public String toString() {
        return name;
    }
}
