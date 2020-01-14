package com.example.substandard.player.server;

/**
 * Allows UI components to control the {@link MediaPlayerHolder}.
 */
public interface PlayerAdapter {

    void play();

    void pause();

    void stop();

    void seekTo(int position);

    boolean isPlaying();

    void reset();

    void release();
}
