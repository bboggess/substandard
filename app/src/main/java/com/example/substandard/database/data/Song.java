package com.example.substandard.database.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "songs",
        foreignKeys = {
                @ForeignKey(entity = Artist.class,
                        parentColumns = "id",
                        childColumns = "artist_id"),
                @ForeignKey(entity = Album.class,
                    parentColumns = "id",
                    childColumns = "album_id")})
public class Song {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    @ColumnInfo(name="artist_id")
    private String artistId;
    @ColumnInfo(name="album_id")
    private String albumId;
    private String genre;
    @Ignore
    private long duration;
    @Ignore
    private int trackNum;
    private boolean offline;

    public Song(String id, String title, String artistId, String albumId, String genre, boolean offline) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.albumId = albumId;
        this.genre = genre;
        this.offline = offline;
    }

    public Song(String id, String title, String artistId, String albumId, String genre, long duration, int trackNum) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.albumId = albumId;
        this.genre = genre;
        this.duration = duration;
        this.trackNum = trackNum;
        offline = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getTrackNum() {
        return trackNum;
    }

    public void setTrackNum(int trackNum) {
        this.trackNum = trackNum;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}