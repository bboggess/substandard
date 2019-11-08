package com.example.substandard.database.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "albums_server",
    foreignKeys = {
        @ForeignKey(entity = Artist.class,
            parentColumns = "id",
            childColumns = "artist_id")})
public class Album {
    @PrimaryKey
    private int id;
    private String name;
    @ColumnInfo(name = "song_count")
    private int songCount;
    private Date created;
    private int duration;
    @ColumnInfo(name = "artist_id")
    private int artistId;

    public Album(int id, String name, int songCount, Date created, int duration, int artistId) {
        this.id = id;
        this.name = name;
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
     * @return The date the album was added to the library
     */
    // TODO replace this by a Date object with necessary TypeConverter
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     *
     * @return Length of the album in seconds
     */
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
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
