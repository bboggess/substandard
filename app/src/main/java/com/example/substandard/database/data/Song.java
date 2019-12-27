package com.example.substandard.database.data;

public class Song {
    private int id;
    private String title;
    private int artistId;
    private int albumId;
    private String genre;
    private long duration;
    private int trackNum;

    public Song(int id, String title, int artistId, int albumId, String genre, long duration, int trackNum) {
        this.id = id;
        this.title = title;
        this.artistId = artistId;
        this.albumId = albumId;
        this.genre = genre;
        this.duration = duration;
        this.trackNum = trackNum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
}