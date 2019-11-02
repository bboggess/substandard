package com.example.substandard.database.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "artists")
public class Artist {

    @PrimaryKey
    private int id;
    private String name;
//    @ColumnInfo(name = "cover_art")
//    private String coverArt;
//    @ColumnInfo(name = "image_url")
//    private String imageUrl;
    @ColumnInfo(name = "album_count")
    private int albumCount;

    public Artist(int id, String name,  int albumCount) {
        this.id = id;
        this.name = name;
        this.albumCount = albumCount;
    }

    /**
     *
     * @return Unique identifier for each Artist. This matches the ID used by server
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     *
     * @return Just the artist name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return The number of albums this artist owns in the library
     */
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
