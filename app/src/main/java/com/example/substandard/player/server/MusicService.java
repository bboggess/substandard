package com.example.substandard.player.server;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
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
import com.example.substandard.service.CoverArtDownloadIntentService;
import com.example.substandard.service.CoverArtResultReceiver;
import com.example.substandard.utility.MediaMetadataUtils;

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

    private PlaybackStateCompat playbackState;
    // These get used repeatedly, and Android recommends caching them
    private MediaMetadataCompat.Builder metadataBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeMediaSession();
        registerActionMediaButtons();
        metadataBuilder = new MediaMetadataCompat.Builder();
    }

    private void initializeMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mediaSession = new MediaSessionCompat(getApplicationContext(),
                MusicService.class.getSimpleName(), mediaButtonReceiver, null);
        callback = new MediaSessionCallback();
        mediaSession.setCallback(callback);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);
        setSessionToken(mediaSession.getSessionToken());
        player = new MediaPlayerHolder(this, new MediaPlayerListener());

        // I really don't want this to be null
        playbackState = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 0)
                .build();
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
            loadMediaFromId(mediaId);
        }

        @Override
        public void onPlay() {
            Log.d(TAG, "onPlay called");
            if (playlist.isEmpty() || nowPlaying < 0) {
                return;
            }

            if (null == loadedMedia) {
                loadNowPlaying();
            }

//            player.playFromMedia(loadedMedia);
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
            skipToPlaylistPosition((nowPlaying + 1) % playlist.size());
        }

        @Override
        public void onSkipToPrevious() {
            skipToPlaylistPosition((nowPlaying - 1) % playlist.size());
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            Log.d(TAG, "adding to play queue");
            onAddQueueItem(description, playlist.size());
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description, int index) {
            MediaSessionCompat.QueueItem track = new MediaSessionCompat.QueueItem(description,
                    Long.parseLong(description.getMediaId()));
            playlist.add(index, track);
            nowPlaying = (nowPlaying < 0) ? 0 : nowPlaying;
            mediaSession.setQueue(playlist);
        }

        @Override
        public void onSkipToQueueItem(long id) {
            for (MediaSessionCompat.QueueItem item : playlist) {
                if (item.getQueueId() == id) {
                    skipToPlaylistPosition(playlist.indexOf(item));
                    return;
                }
            }

            Log.d(TAG, "onSkipToQueueItem: item not found");
        }

        /**
         * Plays the QueueItem in a given spot in the queue
         * @param pos
         */
        private void skipToPlaylistPosition(int pos) {
            nowPlaying = pos;
            loadedMedia = null;
            onPlay();
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            MediaSessionCompat.QueueItem track = new MediaSessionCompat.QueueItem(description,
                    Long.parseLong(description.getMediaId()));
            playlist.remove(track);
            nowPlaying = (playlist.isEmpty()) ? -1 : nowPlaying;
            mediaSession.setQueue(playlist);
        }

        @Override
        public void onSeekTo(long pos) {
            player.seekTo(pos);
        }

        private void loadMediaFromId(String id) {
            Log.d(TAG, "loading media: " + id);
            SongPlayRequest request = new SongPlayRequest(id);
            LocalMusicLibrary.getInstance(MusicService.this).processSongRequest(request);
        }

        void setLoadedMedia(MediaMetadataCompat loadedMedia) {
            this.loadedMedia = loadedMedia;
        }

        private void loadNowPlaying() {
            String nowPlayingId = playlist.get(nowPlaying).getDescription().getMediaId();
            loadMediaFromId(nowPlayingId);
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
        private CoverArtResultReceiver coverArtResultReceiver = new CoverArtResultReceiver(new Handler());

        @Override
        public void onSubscribe(Disposable d) {
            Log.d(TAG, "request subscribed");

            // go ahead and load the album art to add to the metadata
            Intent coverArtIntent = new Intent(MusicService.this, CoverArtDownloadIntentService.class);
            coverArtIntent.putExtra(CoverArtDownloadIntentService.IMAGE_PATH_EXTRA_KEY, songId);
            coverArtIntent.putExtra(CoverArtDownloadIntentService.RESULT_RECEIVER_EXTRA_KEY, coverArtResultReceiver);
            startService(coverArtIntent);

            coverArtResultReceiver.setReceiver((resultCode, resultData) -> {
                if (resultCode == CoverArtDownloadIntentService.STATUS_SUCCESS) {
                    Bitmap coverArt = resultData.getParcelable(CoverArtDownloadIntentService.BITMAP_EXTRA_KEY);
                    metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, coverArt);
                    MediaMetadataCompat updated = metadataBuilder.build();
                    updateMetadata(updated);
                }
            });
        }

        @Override
        public void onSuccess(Song song) {
            Log.d(TAG, "loaded song: " + song.getTitle());
            MediaMetadataCompat metadata = MediaMetadataUtils.convertSongToMediaMetadata(song, metadataBuilder);
            updateMetadata(metadata);

            if (!mediaSession.isActive()) {
                mediaSession.setActive(true);
            }

            // EOPlayer gets angry about being used on a background thread
            // so for now let's do this until I come up with a more permanent fix
            AppExecutors.getInstance().mainThread().execute(
                    () -> callback.onReadyToPlayLoadedMedia() );
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

    /**
     * Listens for state changes to the media player to notify the media session
     */
    public class MediaPlayerListener implements PlaybackStateListener {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            MusicService.this.playbackState = playbackState;
            mediaSession.setPlaybackState(playbackState);

            switch (playbackState.getState()) {
                case PlaybackStateCompat.STATE_PLAYING:
                case PlaybackStateCompat.STATE_PAUSED:
                    showNotification();
                    break;
                default:
                    removeNotification();
                    break;
            }
        }

    }

    /**
     * Updates notifications and session metadata, and triggers callbacks.
     * @param metadata
     */
    private void updateMetadata(MediaMetadataCompat metadata) {
        mediaSession.setMetadata(metadata);
        callback.setLoadedMedia(metadata);
        showNotification();
    }


    // keep the same id throughout session's life, so that we update the existing notification
    // rather than creating new ones
    private int notificationId = AudioNotificationUtils.createNotificationId();

    /**
     * Shows/updates notification based on state of the session
     */
    private void showNotification() {
        Notification notification = AudioNotificationUtils
                .buildNotification(MusicService.this, mediaSession, playbackState)
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MusicService.this);
        notificationManager.notify(notificationId, notification);
    }

    private void removeNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MusicService.this);
        notificationManager.cancel(notificationId);
    }
}
