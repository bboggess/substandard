package com.example.substandard.player.server;

import android.support.v4.media.session.PlaybackStateCompat;

public interface PlaybackStateListener {
    void onPlaybackStateChanged(PlaybackStateCompat playbackState);
}
