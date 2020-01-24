package com.example.substandard.database.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "songs",
        indices = @Index(value = "album_id"),
        foreignKeys = {
                @ForeignKey(entity = Album.class,
                    parentColumns = "id",
                    childColumns = "album_id")})
/**
 * Note that there is a real problem present in Subsonic when it comes to tracks with features,
 * or those appearing on comps, splits, etc. The artist given will not technically match any
 * in the library (it is only keeping track of *album artists*), and so when the artist of a track
 * does not match an album artist, no artistName is returned. Thus I have decided to remove the
 * foreign key for artist_id and just keep track of the artist name for each song.
 *
 * Annoying, but whatever.
 */
public class Song {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    @ColumnInfo(name = "artist_name")
    private String artistName;
    @ColumnInfo(name="album_id")
    private String albumId;
    private String genre;
    private long duration;
    private int trackNum;
    private String suffix;
    private boolean offline;

    @Ignore
    public Song(String id, String title, String artistName, String albumId, String genre, String suffix, int trackNum, int duration, boolean offline) {
        this.id = id;
        this.title = title;
        this.artistName = artistName;
        this.albumId = albumId;
        this.genre = genre;
        this.suffix = suffix;
        this.trackNum = trackNum;
        this.duration = duration;
        this.offline = offline;
    }

    public Song(String id, String title, String artistName, String albumId, String genre, long duration, String suffix, int trackNum) {
        this.id = id;
        this.title = title;
        this.artistName = artistName;
        this.albumId = albumId;
        this.genre = genre;
        this.duration = duration;
        this.trackNum = trackNum;
        this.suffix = suffix;
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

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
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

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}