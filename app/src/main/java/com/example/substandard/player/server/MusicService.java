package com.example.substandard.player.server;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import com.example.substandard.AppExecutors;
import com.example.substandard.database.LocalMusicLibrary;
import com.example.substandard.database.data.Song;
import com.example.substandard.player.AudioNotificationUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Background service which controls the media player and facilitates communication between
 * the app and a media session.
 */
public class MusicService extends MediaBrowserServiceCompat {
    private static final String TAG = MusicService.class.getSimpleName();

    private MediaSessionCompat mediaSession;
    private PlayerAdapter player;
    private MediaSessionCallback callback;

    // These get used repeatedly, and Android recommends caching them
    private PlaybackState.Builder playbackStateBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeMediaSession();
        registerActionMediaButtons();
        playbackStateBuilder = new PlaybackState.Builder();
        metadataBuilder = new MediaMetadataCompat.Builder();
    }

    private void initializeMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mediaSession = new MediaSessionCompat(getApplicationContext(),
                MusicService.class.getSimpleName(), mediaButtonReceiver, null);
        callback = new MediaSessionCallback();
        mediaSession.setCallback(callback);
        setSessionToken(mediaSession.getSessionToken());
        player = new MediaPlayerHolder(this, new MediaPlayerListener());
    }

    private void registerActionMediaButtons() {
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mediaSession.setMediaButtonReceiver(pendingIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    //TODO
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        LocalMusicLibrary library = LocalMusicLibrary.getInstance(this);
        return new BrowserRoot(library.getRoot(), null);
    }

    @Override
    // TODO
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public void onDestroy() {
        player.release();
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
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            Log.d(TAG, "playing media: " + mediaId);
            if (null == loadedMedia) {
                loadMediaFromId(mediaId);
            }
        }

        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay called");
            if (playlist.isEmpty()) {
                return;
            }

            if (null == loadedMedia) {
                loadMedia();
            }

            player.playFromMedia(loadedMedia);
        }

        @Override
        public void onStop() {
            Log.d(TAG, "onStop called");
            player.stop();
            mediaSession.setActive(false);
        }

        @Override
        public void onPause() {
            Log.d(TAG, "onPause called");
            player.pause();
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
            Log.d(TAG, "adding to play queue");
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
            player.seekTo(pos);
        }

        private void loadMedia() {
            Log.d(TAG, "loading media");
            MediaSessionCompat.QueueItem toPlay = playlist.get(nowPlaying);
            String mediaId = toPlay.getDescription().getMediaId();
        }

        private void loadMediaFromId(String id) {
            Log.d(TAG, "loading media: " + id);
            SongPlayRequest request = new SongPlayRequest(id);
            LocalMusicLibrary.getInstance(MusicService.this).processSongRequest(request);
        }

        void setLoadedMedia(MediaMetadataCompat loadedMedia) {
            this.loadedMedia = loadedMedia;
        }

        void onReadyToPlayLoadedMedia() {
            player.playFromMedia(loadedMedia);

        }

    }

    /**
     * Used to load metadata when playing a song. Pass to LocalMusicLibrary.loadMetadata(), and
     * it will play when loading is finished.
     */
    public class SongPlayRequest implements LocalMusicLibrary.SongLoadRequest {
        private String songId;

        @Override
        public void onSubscribe(Disposable d) {
            Log.d(TAG, "request subscribed");
        }

        @Override
        public void onSuccess(Song song) {
            Log.d(TAG, "loaded song: " + song.getTitle());
            MediaMetadataCompat metadata = convertSongToMediaMetadata(song);
            mediaSession.setMetadata(metadata);
            callback.setLoadedMedia(metadata);

            if (!mediaSession.isActive()) {
                mediaSession.setActive(true);
            }

            // EOPlayer gets angry about being used on a background thread
            // so for now let's do this until I come up with a more permanent fix
            AppExecutors.getInstance().mainThread().execute(
                    () -> callback.onReadyToPlayLoadedMedia() );
        }

        private MediaMetadataCompat convertSongToMediaMetadata(Song song) {
            // TODO Song needs a more convenient way of keeping track of album name
            return metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.getId())
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtistName())
                    .putString(MediaMetadataCompat.METADATA_KEY_GENRE, song.getGenre())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                    .putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, song.getTrackNum())
                    .build();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        public String getSongId() {
            return songId;
        }

        public SongPlayRequest(String songId) {
            this.songId = songId;
        }

    }

    public class MediaPlayerListener implements PlaybackStateListener {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            mediaSession.setPlaybackState(playbackState);

            switch (playbackState.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                case PlaybackStateCompat.STATE_PAUSED:
                    showNotification(playbackState);
                    break;
                default:
                    removeNotification();
                    break;
            }
        }

        private int notificationId;

        void showNotification(PlaybackStateCompat state) {
            Notification notification = AudioNotificationUtils
                    .buildNotification(MusicService.this, mediaSession, state)
                    .build();
            notificationId = AudioNotificationUtils.createNotificationId();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MusicService.this);
            notificationManager.notify(notificationId, notification);
        }

        private void removeNotification() {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MusicService.this);
            notificationManager.cancel(notificationId);
        }
    }
}
