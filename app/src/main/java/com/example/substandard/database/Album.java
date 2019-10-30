package com.example.substandard.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "albums_server")
public class Album {
    @PrimaryKey
    private int id;
    private String name;
    @ColumnInfo(name = "cover_art")
    private String coverArt;
    @ColumnInfo(name = "song_count")
    private int songCount;
    private String created;
    private String duration;
    @ColumnInfo(name = "album_artist")
    private Artist albumArtist;

    public Album(int id, String name, String coverArt, int songCount, String created, String duration, Artist albumArtist) {
        this.id = id;
        this.name = name;
        this.coverArt = coverArt;
        this.songCount = songCount;
        this.created = created;
        this.duration = duration;
        this.albumArtist = albumArtist;
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

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Artist getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(Artist albumArtist) {
        this.albumArtist = albumArtist;
    }

    /**
     * Get String representation, which is the album name.
     * @return String representation of the album.
     */
    @Override
    public String toString() {
        return name;
    }
}
