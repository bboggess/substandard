package com.example.substandard.database.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "albums",
    foreignKeys = {
        @ForeignKey(entity = Artist.class,
            parentColumns = "id",
            childColumns = "artist_id")},
    indices = @Index(value = "artist_id"))
public class Album {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    @Ignore
    private int songCount;
    @Ignore
    private Date created;
    @Ignore
    private int duration;
    @ColumnInfo(name = "artist_id")
    private String artistId;
    private String coverArt;

    public Album(String id, String name, String artistId, String coverArt) {
        this.id = id;
        this.name = name;
        this.artistId = artistId;
        this.coverArt = coverArt;
    }

    public Album(String id, String name, int songCount, Date created, int duration, String artistId, String coverArt) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
        this.created = created;
        this.duration = duration;
        this.artistId = artistId;
        this.coverArt = coverArt;
    }

    /**
     *
     * @return Unique identifier for the album. Matches the ID stored by Subsonic server
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
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
    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
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

    /**
     *
     * @return key for obtaining cover art from server
     */
    public String getCoverArt() {
        return coverArt;
    }

    public void setCoverArt(String coverArt) {
        this.coverArt = coverArt;
    }
}
