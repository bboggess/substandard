package com.example.substandard.player.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.example.substandard.R;
import com.example.substandard.database.LocalMusicLibrary;
import com.example.substandard.service.download.DownloadRepository;
import com.example.substandard.service.download.DownloadTracker;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.net.MalformedURLException;
import java.net.URL;


/**
 * Exposes the necessary functionality of the MediaPlayer to rest of app.
 */
public class MediaPlayerHolder implements PlayerAdapter, AudioManager.OnAudioFocusChangeListener {
    private final static String TAG = MediaPlayerHolder.class.getSimpleName();

    private final Context context;
    private SimpleExoPlayer player;

    private PlaybackStateListener listener;

    private int playbackState;

    private DownloadTracker downloadTracker;

    // Android recommends caching this, as it is used constantly
    private PlaybackStateCompat.Builder builder;

    // Listens for change in headphone state
    private BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isPlaying()) {
                pause();
            }
        }
    };

    public MediaPlayerHolder(Context context, PlaybackStateListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.builder = new PlaybackStateCompat.Builder();
        this.downloadTracker = new DownloadTracker(context);
    }

    /**
     * Every file that is played needs a new MediaPlayer, and once a track is stopped
     * all resources are released. Thus it is very possible that the player is null, so
     * use this before loading media.
     */
    private void initializeMediaPlayer() {
        if (null == player) {
            Log.d(TAG, "instantiating media player");
            player = new SimpleExoPlayer.Builder(context).build();
        }
    }

    @Override
    public void playFromMedia(MediaMetadataCompat metadata) {
        String id = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        LocalMusicLibrary library = LocalMusicLibrary.getInstance(context);
        boolean isOffline = downloadTracker.isDownloaded(id);
        try {
            playFromUrl(library.getStream(id), isOffline);
        } catch (MalformedURLException e) {
            Log.d(TAG, "unable to download media: "
                    + metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            e.printStackTrace();
        }
    }

    private void playFromUrl(URL url, boolean offline) {
        playFromUri(Uri.parse(url.toString()), offline);
    }

    private void playFromUri(Uri uri, boolean isOffline) {
        Log.d(TAG, "playFromUrl: " + uri.toString());

        initializeMediaPlayer();

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getString(R.string.app_name)));
        if (isOffline) {
            Cache audioCache = DownloadRepository.getInstance(context).getAudioDownloadCache();
            dataSourceFactory = new CacheDataSourceFactory(audioCache, dataSourceFactory);
        }

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);
        player.prepare(mediaSource);
        Log.d(TAG, "set data source");

        play();
    }

    @Override
    public void cacheMedia(MediaSessionCompat.QueueItem metadata) {
        String id = metadata.getDescription().getMediaId();
        if (downloadTracker.isDownloaded(id)) {
            return;
        }

        LocalMusicLibrary library = LocalMusicLibrary.getInstance(context);
        try {
            downloadTracker.requestDownload(id, library.getStream(id));
        } catch (MalformedURLException e){
            Log.d(TAG, "cacheMedia: malformed URL " + id);
            e.printStackTrace();
        }
    }

    @Override
    public void play() {
        Log.d(TAG, "player starting");
        // TODO figure out what happens when player.isPlaying() is true
        if (null != player) {
            if (getAudioFocus()) {
                registerNoisyReceiver();
                player.setPlayWhenReady(true);
                setPlaybackState(PlaybackStateCompat.STATE_PLAYING);
            }
        }
    }

    @Override
    public void pause() {
        if (null != player && player.isPlaying()) {
            player.setPlayWhenReady(false);
            setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
        }
    }

    @Override
    public void stop() {
        Log.d(TAG, "stopping player");
        if (null != player && player.isPlaying()) {
            player.stop();
            removeAudioFocus();
            unregisterNoisyReceiver();
            setPlaybackState(PlaybackStateCompat.STATE_STOPPED);
        }
    }

    @Override
    public void release() {
        Log.d(TAG, "releasing player");
        if (null != player) {
            player.release();
            player = null;
        }
    }

    @Override
    public void seekTo(long position) {
        if (null != player && player.isPlaying()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                player.seekTo(position);
            }
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
    public void setVolume(float volume) {
        if (null != player) {
            player.setVolume(volume);
        }
    }

    /**
     * Update the saved playback state and notify listener
     * @param newState
     */
    private void setPlaybackState(@PlaybackStateCompat.State int newState) {
        playbackState = newState;
        long trackPosition = (null == player) ? 0 : player.getCurrentPosition();
        builder.setState(newState, trackPosition, 1.0f);
        listener.onPlaybackStateChanged(builder.build());
    }

    /**
     * Registers headphone listener
     */
    private void registerNoisyReceiver() {
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        context.registerReceiver(noisyReceiver, filter);
    }

    private void unregisterNoisyReceiver() {
        context.unregisterReceiver(noisyReceiver);
    }



    // For some reason removing the audio focus requires passing in the same request you
    // originally sent in, so we have to save this.
    private AudioFocusRequest audioFocusRequest;

    /**
     * As a courtesy, make sure to remove the focus when the music is done
     */
    private void removeAudioFocus() {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && null != audioFocusRequest) {
            manager.abandonAudioFocusRequest(audioFocusRequest);
            audioFocusRequest = null;
        } else {
            manager.abandonAudioFocus(this);
        }
    }
    /**
     * Requests audio focus from the system.
     * @return true if the request was granted
     */
    private boolean getAudioFocus() {
        Log.d(TAG, "asking for audio focus");
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int result;

        // The former approach only works in version 26, and the latter is deprecated
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attributes)
                    .setOnAudioFocusChangeListener(this)
                    .build();
            result = manager.requestAudioFocus(audioFocusRequest);
        } else {
            result = manager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private static final float MEDIA_VOLUME_DUCK = 0.3f;
    private static final float MEDIA_VOLUME_GAIN = 1.0f;

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                if (isPlaying()) {
                    stop();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPlaying()) {
                    pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                setVolume(MEDIA_VOLUME_DUCK);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                play();
                setVolume(MEDIA_VOLUME_GAIN);
                break;

        }
    }
}
