package com.example.substandard.player.server;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * Allows UI components to control the {@link MediaPlayerHolder} without exposing anything
 * we don't want to expose. I'm actually questioning what role this is playing.
 */
public interface PlayerAdapter {

    void play();

    void pause();

    void stop();

    void seekTo(long position);

    boolean isPlaying();

    void release();

    void playFromMedia(MediaMetadataCompat metadata);

    void setVolume(float volume);

    void cacheMedia(MediaSessionCompat.QueueItem metadata);
}
