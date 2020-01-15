package com.example.substandard.player.server;

import android.support.v4.media.MediaMetadataCompat;

/**
 * Allows UI components to control the {@link MediaPlayerHolder} without exposing anything
 * we don't want to expose.
 */
public interface PlayerAdapter {

    void play();

    void pause();

    void stop();

    void seekTo(long position);

    boolean isPlaying();

    void reset();

    void release();

    void playFromMedia(MediaMetadataCompat metadata);

    void setVolume(float volume);
}
