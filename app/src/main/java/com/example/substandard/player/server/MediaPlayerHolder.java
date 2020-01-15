package com.example.substandard.player.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import com.example.substandard.database.LocalMusicLibrary;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Exposes the necessary functionality of the MediaPlayer to rest of code.
 */
public class MediaPlayerHolder implements PlayerAdapter, AudioManager.OnAudioFocusChangeListener {
    private final static String TAG = MediaPlayerHolder.class.getSimpleName();

    private final Context context;
    private MediaPlayer player;

    // Listens for change in headphone state
    private BroadcastReceiver noisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isPlaying()) {
                pause();
            }
        }
    };

    public MediaPlayerHolder(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Every file that is played needs a new MediaPlayer, and once a track is stopped
     * all resources are released. Thus it is very possible that the player is null, so
     * use this before loading media.
     */
    public void initializeMediaPlayer() {
        if (null == player) {
            player = new MediaPlayer();
        }
    }

    @Override
    public void playFromMedia(MediaMetadataCompat metadata) {
        String id = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
        LocalMusicLibrary library = LocalMusicLibrary.getInstance(context);
        playFromFile(library.getSongFile(id));
    }

    private void playFromFile(File file) {
        initializeMediaPlayer();

        try {
            player.setDataSource(file.getAbsolutePath());
            player.prepare();
        } catch (IOException e) {
            Log.d(TAG, "playFromFile: failed to read from file " + file);
        }

        play();
    }

    private void playFromUrl(URL url) {
        initializeMediaPlayer();

        try {
            player.setDataSource(url.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                play();
            }
        });
        player.prepareAsync();
    }

    @Override
    public void play() {
        if (null != player && !player.isPlaying()) {
            if (getAudioFocus()) {
                registerNoisyReceiver();
                player.start();
            }
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
            removeAudioFocus();
            unregisterNoisyReceiver();
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
    public void seekTo(long position) {
        if (null != player && player.isPlaying()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                player.seekTo(position, MediaPlayer.SEEK_CLOSEST_SYNC);
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
    public void reset() {
        if (null != player) {
            player.reset();
        }
    }

    @Override
    public void setVolume(float volume) {
        if (null != player) {
            player.setVolume(volume, volume);
        }
    }

    private void registerNoisyReceiver() {
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        context.registerReceiver(noisyReceiver, filter);
    }

    private void unregisterNoisyReceiver() {
        context.unregisterReceiver(noisyReceiver);
    }

    private void removeAudioFocus() {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && null != audioFocusRequest) {
            manager.abandonAudioFocusRequest(audioFocusRequest);
            audioFocusRequest = null;
        } else {
            manager.abandonAudioFocus(this);
        }
    }

    private AudioFocusRequest audioFocusRequest;

    private boolean getAudioFocus() {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int result;

        // The former approach only works in version 26, and the latter becomes deprecated
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
