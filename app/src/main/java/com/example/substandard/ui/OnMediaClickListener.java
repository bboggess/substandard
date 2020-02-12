package com.example.substandard.ui;

import com.example.substandard.database.data.AlbumAndAllSongs;

/**
 * Listener interface for an Activity that can handle clicking on artists, albums, and
 * songs. These all must be handled differently, no doubt, and so we need three methods.
 */
// TODO redesign more intelligently. Methods such as playQueueItem, addToQueue, etc
public interface OnMediaClickListener {
    void onSongClick(String songId);
    void onLoadAlbum(AlbumAndAllSongs album);
}
