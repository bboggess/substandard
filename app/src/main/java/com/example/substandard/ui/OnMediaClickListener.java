package com.example.substandard.ui;

import com.example.substandard.database.data.AlbumAndAllSongs;
import com.example.substandard.database.data.Song;

/**
 * Listener interface for an Activity that can handle clicking on artists, albums, and
 * songs. These all must be handled differently, no doubt, and so we need three methods.
 */
public interface OnMediaClickListener {
    // The second argument is needed because the UI should play an entire
    // album when any song is clicked
    void onSongClick(Song song, AlbumAndAllSongs album);
}
