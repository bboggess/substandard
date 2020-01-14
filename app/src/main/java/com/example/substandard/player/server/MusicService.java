package com.example.substandard.player.server;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends Service {
    private MediaSessionCompat mediaSession;
    private MediaPlayerHolder holder;
    private MediaSessionCallback callback;

    @Override
    public void onCreate() {
        mediaSession = new MediaSessionCompat(this, MusicService.class.getSimpleName());
        holder = new MediaPlayerHolder(this);
        callback = new MediaSessionCallback();
        mediaSession.setCallback(callback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        holder.release();
        mediaSession.release();
    }

    /**
     * Defines behavior for media controls
     */
    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        private List<MediaSessionCompat.QueueItem> playlist = new ArrayList<>();

        @Override
        public void onPlay() {
            super.onPlay();
        }

        @Override
        public void onStop() {
            super.onStop();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            MediaSessionCompat.QueueItem track = new MediaSessionCompat.QueueItem(description,
                    description.hashCode());
            playlist.add(track);
            mediaSession.setQueue(playlist);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            MediaSessionCompat.QueueItem track = new MediaSessionCompat.QueueItem(description,
                    description.hashCode());
            playlist.remove(track);
            mediaSession.setQueue(playlist);
        }
    }
}
