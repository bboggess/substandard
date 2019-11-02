package com.example.substandard.database.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "albums_server",
    foreignKeys = {
        @ForeignKey(entity = Artist.class,
            parentColumns = "id",
            childColumns = "artist_id")})
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
    @ColumnInfo(name = "artist_id")
    private int artistId;

    public Album(int id, String name, String coverArt, int songCount, String created, String duration, int artistId) {
        this.id = id;
        this.name = name;
        this.coverArt = coverArt;
        this.songCount = songCount;
        this.created = created;
        this.duration = duration;
        this.artistId = artistId;
    }

    /**
     *
     * @return Unique identifier for the album. Matches the ID stored by Subsonic server
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return the name of the album
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return ID that the Subsonic server uses for the cover art image file. You can use this
     * to request the art from the server
     */
    // TODO there's 100% got to be a better way of storing this info. How?
    public String getCoverArt() {
        return coverArt;
    }

    public void setCoverArt(String coverArt) {
        this.coverArt = coverArt;
    }

    /**
     *
     * @return the number of tracks on the album
     */
    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    /**
     *
     * @return The date the album was added to the library, in the format
     * %y%y%y%y-%m%m-%d%dT%h%h:%m%m:%s%s
     * E.g. 2004-11-27T20:23:22
     */
    // TODO replace this by a Date object with necessary TypeConverter
    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    /**
     *
     * @return Length of the album in seconds
     */
    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     *
     * @return ID of the album artist. Useful for joins.
     */
    public int getArtistId() {
        return artistId;
    }

    public void setArtistId(int artistId) {
        this.artistId = artistId;
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
