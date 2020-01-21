package com.example.substandard.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.substandard.R;
import com.example.substandard.player.client.BaseMediaBrowserAdapter;

/**
 * Layout containing the media player used in the main activity. All logic for updating the UI
 * to reflect currently playing music, or sending commands to the music service, is contained
 * here.
 */
public class MediaPlayerLayout extends LinearLayout {

    private ImageButton playButton;
    private ImageButton mediaPlayerPlayButton;
    private ConstraintLayout miniMediaControllerLayout;
    private ConstraintLayout mediaPlayerHeader;

    private TextView songTitleView;
    private TextView mediaPlayerSongTileView;
    private TextView artistNameView;
    private TextView mediaPlayerArtistNameView;
    private TextView trackProgress;
    private TextView trackLength;

    private ImageView albumArtViewSmall;
    private ImageView albumArtViewLarge;

    private MediaControllerCompat controller;
    private MediaControllerCompat.Callback controllerCallback;

    private BaseMediaBrowserAdapter mediaBrowserAdapter;
    private int mediaState;

    public MediaPlayerLayout(Context context) {
        super(context);
        initializeViews();
    }

    public MediaPlayerLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initializeViews();
    }

    public MediaPlayerLayout(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        initializeViews();
    }

    public void setController(MediaControllerCompat controller) {
        if (null != controller) {
            controllerCallback = new MediaPlayerControllerCallback();
            controller.registerCallback(controllerCallback);
        }

        this.controller = controller;
    }

    public void setMediaBrowser(BaseMediaBrowserAdapter browser) {
        this.mediaBrowserAdapter = browser;
    }

    public void disconnectController() {
        if (null != controller) {
            controller.unregisterCallback(controllerCallback);
            controller = null;
            controllerCallback = null;
        }
    }

    /**
     * Loads all views (God there are so many) and sets onClickListeners
     */
    private void initializeViews() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.media_player, this, true);
        songTitleView = view.findViewById(R.id.song_name_view);
        artistNameView = view.findViewById(R.id.artist_name_view);
        mediaPlayerSongTileView = view.findViewById(R.id.song_name_view_media_player);
        mediaPlayerArtistNameView = view.findViewById(R.id.artist_name_view_media_player);
        playButton = view.findViewById(R.id.play_button);
        mediaPlayerPlayButton = view.findViewById(R.id.media_player_play_pause);
        miniMediaControllerLayout = view.findViewById(R.id.mini_media_control_view);
        trackLength = view.findViewById(R.id.current_time);
        trackProgress = view.findViewById(R.id.track_length);
        mediaPlayerHeader = view.findViewById(R.id.media_player_header);
        albumArtViewSmall = view.findViewById(R.id.album_art_view);
        albumArtViewLarge = view.findViewById(R.id.album_art_large);


        playButton.setOnClickListener((l) -> {
            if (mediaState == PlaybackStateCompat.STATE_PLAYING) {
                mediaBrowserAdapter.getTransportControl().pause();
            } else if (mediaState == PlaybackStateCompat.STATE_PAUSED){
                mediaBrowserAdapter.getTransportControl().play();
            }
        });
    }

    /**
     * Listens for changes in playback and updates the UI accordingly
     */
    private class MediaPlayerControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (null != metadata) {
                // Add metadata to UI
                String songTitle = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
                String artistName = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
                Bitmap albumArtBitmap = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);
                songTitleView.setText(songTitle);
                artistNameView.setText(artistName);
                mediaPlayerSongTileView.setText(songTitle);
                mediaPlayerArtistNameView.setText(artistName);
                albumArtViewSmall.setImageBitmap(albumArtBitmap);
                BitmapDrawable albumArtDrawable = new BitmapDrawable(getResources(), albumArtBitmap);
                albumArtViewLarge.setBackground(albumArtDrawable);

            } else {
                // Erase metadata from UI -- nothing is playing!
                songTitleView.setText("");
                artistNameView.setText("");
                mediaPlayerSongTileView.setText("");
                mediaPlayerArtistNameView.setText("");
                trackLength.setText(R.string.track_time_default);
                trackProgress.setText(R.string.track_time_default);
                Drawable transparentDrawable = getContext().getDrawable(android.R.color.transparent);
                albumArtViewSmall.setImageDrawable(transparentDrawable);
                albumArtViewLarge.setImageDrawable(transparentDrawable);
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (null == state) {
                mediaState = PlaybackStateCompat.STATE_NONE;
            } else {
                mediaState = state.getState();
            }
            setPlayPauseButton();
        }
    }

    public void onExpanded() {
        miniMediaControllerLayout.setVisibility(View.GONE);
        mediaPlayerHeader.setVisibility(View.VISIBLE);
    }

    public void onCollapsed() {
        mediaPlayerHeader.setVisibility(View.INVISIBLE);
        miniMediaControllerLayout.setVisibility(View.VISIBLE);
    }

    void setMiniPlayerAlpha(float alpha) {
        miniMediaControllerLayout.setAlpha(alpha);
    }

    private void setPlayPauseButton() {
        if (mediaState == PlaybackStateCompat.STATE_PLAYING) {
            playButton.setImageDrawable(getContext().getDrawable(R.drawable.exo_controls_pause));
            mediaPlayerPlayButton.setImageDrawable(getContext().getDrawable(R.drawable.exo_controls_pause));
        } else {
            playButton.setImageDrawable(getContext().getDrawable(R.drawable.exo_controls_play));
            mediaPlayerPlayButton.setImageDrawable(getContext().getDrawable(R.drawable.exo_controls_play));
        }
    }
}
