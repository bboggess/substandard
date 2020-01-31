package com.example.substandard.ui.main;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.substandard.R;
import com.example.substandard.player.client.BaseMediaBrowserAdapter;
import com.example.substandard.ui.model.MediaPlayerViewModel;

import java.util.List;

/**
 * Layout containing the media player used in the main activity. All logic for updating the UI
 * to reflect currently playing music, or sending commands to the music service, is contained
 * here.
 */
public class MediaPlayerLayout extends LinearLayout {

    private ImageButton playButtonSmall;
    private ImageButton mediaPlayerPlayButton;
    private ImageButton playlistButton;
    private ConstraintLayout miniMediaControllerLayout;
    private ConstraintLayout mediaPlayerHeader;

    private TextView songTitleView;
    private TextView mediaPlayerSongTileView;
    private TextView artistNameView;
    private TextView mediaPlayerArtistNameView;
    private TextView trackProgress;
    private TextView trackLength;

    private ImageView albumArtViewSmall;

    private MediaPlayerViewModel viewModel;

    // Save this so we can unregister on cleanup
    private MediaControllerCompat.Callback controllerCallback;

    private BaseMediaBrowserAdapter mediaBrowserAdapter;
    private int mediaState;

    private boolean isPlaylistVisible;

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

    public void setController() {
        if (null != mediaBrowserAdapter) {
            controllerCallback = new MediaPlayerControllerCallback();
            mediaBrowserAdapter.registerCallback(controllerCallback);
        }
    }

    public void setMediaBrowser(BaseMediaBrowserAdapter browser) {
        this.mediaBrowserAdapter = browser;
    }

    public void disconnectController() {
        if (null != mediaBrowserAdapter) {
            mediaBrowserAdapter.unregisterCallback(controllerCallback);
        }
    }

    public void setViewModel(MediaPlayerViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Loads all views (God there are so many) and sets onClickListeners. Obviously some
     * of this should be further delegated to new classes encapsulating the various pieces
     * of the UI.
     */
    private void initializeViews() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.media_player, this, true);
        songTitleView = view.findViewById(R.id.song_name_view);
        artistNameView = view.findViewById(R.id.artist_name_view);
        mediaPlayerSongTileView = view.findViewById(R.id.song_name_view_media_player);
        mediaPlayerArtistNameView = view.findViewById(R.id.artist_name_view_media_player);
        playButtonSmall = view.findViewById(R.id.play_button);
        mediaPlayerPlayButton = view.findViewById(R.id.media_player_play_pause);
        miniMediaControllerLayout = view.findViewById(R.id.mini_media_control_view);
        trackLength = view.findViewById(R.id.current_time);
        trackProgress = view.findViewById(R.id.track_length);
        mediaPlayerHeader = view.findViewById(R.id.media_player_header);
        albumArtViewSmall = view.findViewById(R.id.album_art_view);
        playlistButton = view.findViewById(R.id.playlist_button);

        setupButtonListeners(view);

        // hacky
        isPlaylistVisible = true;
        swapPlaylistFragment();
    }

    /**
     * Sets click listeners for all UI buttons
     * @param view the root view, so we can find buttons by ID
     */
    private void setupButtonListeners(View view) {
        // Don't need to keep references to these as they are never updated
        ImageButton prevTrackButton = view.findViewById(R.id.media_player_prev);
        ImageButton nextTrackButton = view.findViewById(R.id.media_player_next);

        OnClickListener playPauseListener = (v) -> {
            if (mediaState == PlaybackStateCompat.STATE_PLAYING) {
                mediaBrowserAdapter.getTransportControl().pause();
            } else if (mediaState == PlaybackStateCompat.STATE_PAUSED){
                mediaBrowserAdapter.getTransportControl().play();
            }
        };
        OnClickListener prevTrackListener = (v) ->
                mediaBrowserAdapter.getTransportControl().skipToPrevious();
        OnClickListener nextTrackListener = (v) ->
                mediaBrowserAdapter.getTransportControl().skipToNext();

        playButtonSmall.setOnClickListener(playPauseListener);
        mediaPlayerPlayButton.setOnClickListener(playPauseListener);
        prevTrackButton.setOnClickListener(prevTrackListener);
        nextTrackButton.setOnClickListener(nextTrackListener);

        playlistButton.setOnClickListener(v -> {
            swapPlaylistFragment();
        });
    }

    private void swapPlaylistFragment() {
        // Need the activity to get the fragment manager. Don't think there's a better way?
        Context context = getContext();
        if (!(context instanceof MainActivity)) {
            return;
        }
        MainActivity mainActivity = (MainActivity) context;

        FragmentManager manager = mainActivity.getSupportFragmentManager();
        Fragment fragment;
        if (isPlaylistVisible) {
            fragment = AlbumCoverFragment.newInstance();
            isPlaylistVisible = false;
        } else {
            fragment = PlaylistFragment.newInstance();
            isPlaylistVisible = true;
        }
        manager.beginTransaction()
                .replace(R.id.media_player_fragment_holder, fragment)
                .commit();
    }

    /**
     * Listens for changes in playback and updates the UI accordingly
     */
    // This method is an absolute mess. Move everything into a ViewModel, please
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
                viewModel.setAlbumArt(albumArtBitmap);
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
                viewModel.setAlbumArt(null);
            }
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            // Update ViewModel
            super.onQueueChanged(queue);
            viewModel.setPlaylist(queue);
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

    /**
     * Call when the media player is given focus
     */
    public void onExpanded() {
        miniMediaControllerLayout.setVisibility(View.GONE);
        mediaPlayerHeader.setVisibility(View.VISIBLE);
    }

    /**
     * Call when the media player is closed
     */
    public void onCollapsed() {
        mediaPlayerHeader.setVisibility(View.INVISIBLE);
        miniMediaControllerLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the alpha value for the mini controller. Useful for fading out as you slide up.
     * @param alpha
     */
    void setMiniPlayerAlpha(float alpha) {
        miniMediaControllerLayout.setAlpha(alpha);
    }

    private void setPlayPauseButton() {
        if (mediaState == PlaybackStateCompat.STATE_PLAYING) {
            playButtonSmall.setImageDrawable(getContext().getDrawable(R.drawable.exo_controls_pause));
            mediaPlayerPlayButton.setImageDrawable(getContext().getDrawable(R.drawable.exo_controls_pause));
        } else {
            playButtonSmall.setImageDrawable(getContext().getDrawable(R.drawable.exo_controls_play));
            mediaPlayerPlayButton.setImageDrawable(getContext().getDrawable(R.drawable.exo_controls_play));
        }
    }
}
