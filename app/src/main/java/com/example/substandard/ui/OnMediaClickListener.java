package com.example.substandard.ui;

import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.Song;

/**
 * Listener interface for an Activity that can handle clicking on artists, albums, and
 * songs. These all must be handled differently, no doubt, and so we need three methods.
 */
public interface OnMediaClickListener {
    void onArtistClick(Artist artist);
    void onAlbumClick(Album album);
    void onSongClick(Song song);
}
