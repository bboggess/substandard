package com.example.substandard.player.server;

import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.example.substandard.database.LocalMusicLibrary;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends MediaBrowserServiceCompat {
    private MediaSessionCompat mediaSession;
    private MediaPlayerHolder holder;
    private MediaSessionCallback callback;

    private PlaybackState.Builder playbackStateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;

    @Override
    public void onCreate() {
        mediaSession = new MediaSessionCompat(this, MusicService.class.getSimpleName());
        setSessionToken(mediaSession.getSessionToken());
        holder = new MediaPlayerHolder(this);
        callback = new MediaSessionCallback();
        mediaSession.setCallback(callback);

        // These builders will be used over and over, so recommended that we cache
        playbackStateBuilder = new PlaybackState.Builder();
        metadataBuilder = new MediaMetadataCompat.Builder();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }

    @Override
    public void onDestroy() {
        holder.release();
        mediaSession.release();
    }

    /**
     * Defines behavior for media controls. These requests could come from app UI, the lock
     * screen, or even hardware such as pause button on headphones. They are then sent to
     * the actual player.
     */
    public class MediaSessionCallback extends MediaSessionCompat.Callback {
        private List<MediaSessionCompat.QueueItem> playlist = new ArrayList<>();
        private int nowPlaying = -1;
        private MediaMetadataCompat loadedMedia;

        @Override
        public void onPlay() {
            if (playlist.isEmpty()) {
                return;
            }

            if (null == loadedMedia) {
                loadMedia();
            }

            holder.playFromMedia(loadedMedia);
        }

        @Override
        public void onStop() {
            holder.stop();
            mediaSession.setActive(false);
        }

        @Override
        public void onPause() {
            holder.pause();
        }

        @Override
        public void onSkipToNext() {
            nowPlaying = (nowPlaying + 1) % playlist.size();
            loadedMedia = null;
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            nowPlaying = (nowPlaying - 1) % playlist.size();
            loadedMedia = null;
            onPlay();
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            MediaSessionCompat.QueueItem track = new MediaSessionCompat.QueueItem(description,
                    description.hashCode());
            playlist.add(track);
            nowPlaying = (nowPlaying < 0) ? 0 : nowPlaying;
            mediaSession.setQueue(playlist);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            MediaSessionCompat.QueueItem track = new MediaSessionCompat.QueueItem(description,
                    description.hashCode());
            playlist.remove(track);
            nowPlaying = (playlist.isEmpty()) ? -1 : nowPlaying;
            mediaSession.setQueue(playlist);
        }

        @Override
        public void onSeekTo(long pos) {
            holder.seekTo(pos);
        }

        private void loadMedia() {
            MediaSessionCompat.QueueItem toPlay = playlist.get(nowPlaying);
            String mediaId = toPlay.getDescription().getMediaId();
            LocalMusicLibrary library = LocalMusicLibrary.getInstance(MusicService.this);
            loadedMedia = library.getMetadata(mediaId, metadataBuilder);
            mediaSession.setMetadata(loadedMedia);
        }
    }
}
