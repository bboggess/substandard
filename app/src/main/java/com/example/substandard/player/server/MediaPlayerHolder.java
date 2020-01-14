package com.example.substandard.player.server;

import android.content.Context;
import android.media.MediaPlayer;

public class MediaPlayerHolder implements PlayerAdapter {
    private final Context context;
    private MediaPlayer player;

    public MediaPlayerHolder(Context context) {
        this.context = context.getApplicationContext();
    }

    public void initializeMediaPlayer() {
        if (null == player) {
            player = new MediaPlayer();
        }
    }

    @Override
    public void play() {
        if (null != player && !player.isPlaying()) {
            player.start();
        }
    }

    @Override
    public void pause() {
        if (null != player && player.isPlaying()) {
            player.pause();
        }
    }

    @Override
    public void stop() {
        if (null != player && player.isPlaying()) {
            player.stop();
        }
    }

    @Override
    public void release() {
        if (null != player) {
            player.release();
            player = null;
        }
    }

    @Override
    public void seekTo(int position) {
        if (null != player && player.isPlaying()) {
            player.seekTo(position);
        }
    }

    @Override
    public boolean isPlaying() {
        if (null != player) {
            return player.isPlaying();
        }

        return false;
    }

    @Override
    public void reset() {
        if (null != player) {
            player.reset();
        }
    }
}
