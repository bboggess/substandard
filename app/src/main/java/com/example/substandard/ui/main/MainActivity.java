package com.example.substandard.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.substandard.R;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.Song;
import com.example.substandard.player.client.BaseMediaBrowserAdapter;
import com.example.substandard.service.LibraryRefreshIntentService;
import com.example.substandard.ui.OnMediaClickListener;
import com.example.substandard.ui.settings.SettingsActivity;
import com.google.android.material.navigation.NavigationView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity implements
        OnMediaClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout drawerLayout;
    private ImageView playButton;
    private SlidingUpPanelLayout mediaPlayerSlidingPanel;
    private MediaPlayerPanelListener slidingPanelListener;
    private ConstraintLayout miniMediaControllerLayout;
    private FrameLayout mediaPlayerLayout;

    private TextView songTitleView;
    private TextView artistNameView;

    private BaseMediaBrowserAdapter mediaBrowserAdapter;
    private int mediaState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create and show the start screen, which is currently the list of artists
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, new ArtistsFragment(), "Artists");
        ft.commit();

        // set up navigation drawer
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        setUpNavigationDrawer();
        setupNavigationClickListener();

        // Initialize view member variables
        songTitleView = findViewById(R.id.song_name_view);
        artistNameView = findViewById(R.id.artist_name_view);
        mediaPlayerSlidingPanel = findViewById(R.id.sliding_panel_media_player);
        playButton = findViewById(R.id.play_button);
        miniMediaControllerLayout = findViewById(R.id.mini_media_control_view);
        mediaPlayerLayout = findViewById(R.id.media_view);

        mediaBrowserAdapter = new MainActivityAudioBrowser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserAdapter.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaBrowserAdapter.onStop();
        mediaPlayerSlidingPanel.removePanelSlideListener(slidingPanelListener);
    }

    private void setupPlayerControlView(MediaControllerCompat controller) {
        // Setup sliding panel
        slidingPanelListener = new MediaPlayerPanelListener();
        mediaPlayerSlidingPanel.addPanelSlideListener(slidingPanelListener);

        // Populate mini player
        playButton.setOnClickListener((l) -> {
                if (mediaState == PlaybackStateCompat.STATE_PLAYING) {
                    mediaBrowserAdapter.getTransportControl().pause();
                } else if (mediaState == PlaybackStateCompat.STATE_PAUSED){
                    mediaBrowserAdapter.getTransportControl().play();
                }
            });
    }

    /**
     * Helper method to configure the navigation drawer to open on swipe.
     */
    private void setUpNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Helper method to add click functionality to navigation drawer.
     */
    private void setupNavigationClickListener() {
        NavigationView navigationView = findViewById(R.id.nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /*
     * If we press back while the drawer is open, close the drawer instead of the app.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onArtistClick(Artist artist) {
        ArtistViewFragment fragment = new ArtistViewFragment();
        fragment.setArtist(artist);
        swapFragments(fragment);
    }

    @Override
    public void onAlbumClick(Album album) {
        SongListFragment fragment = SongListFragment.newInstance();
        fragment.setAlbum(album);
        swapFragments(fragment);
    }

    @Override
    public void onSongClick(Song song) {
        // TODO add entire album to queue (this should be easy?)
        MediaControllerCompat.TransportControls transportControls = mediaBrowserAdapter.getTransportControl();
        transportControls.playFromMediaId(song.getId(), null);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The interface consists of a main Activity containing a primary Fragment. This method swaps
     * in a new primary Fragment, while adding the old Fragment to the back stack.
     * @param newFragment
     */
    void swapFragments(Fragment newFragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, newFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.item_rescan:
                Intent rescanIntent = new Intent(this, LibraryRefreshIntentService.class);
                startService(rescanIntent);
                break;
            default:
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setPlayPauseButton() {
        if (mediaState == PlaybackStateCompat.STATE_PLAYING) {
            playButton.setImageDrawable(getDrawable(R.drawable.exo_controls_pause));
        } else {
            playButton.setImageDrawable(getDrawable(R.drawable.exo_controls_play));
        }
    }

    /**
     * Logic for transitioning from mini to full media player on sliding up
     */
    private class MediaPlayerPanelListener implements SlidingUpPanelLayout.PanelSlideListener {
        private static final float OFFSET_THRESHOLD = 0.3f;

        @Override
        public void onPanelSlide(View panel, float slideOffset) {
            ActionBar actionBar = getSupportActionBar();

            if (slideOffset > OFFSET_THRESHOLD) {
                if (actionBar.isShowing()) {
                    actionBar.hide();
                }
            } else {
                if (!actionBar.isShowing()) {
                    actionBar.show();
                }
            }

            // make mini player fade out as you slide, otherwise looks awkward
            miniMediaControllerLayout.setAlpha(1 - slideOffset);
        }

        @Override
        public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            switch (newState) {
                case EXPANDED:
                    miniMediaControllerLayout.setVisibility(View.GONE);
                    break;
                case COLLAPSED:
                    miniMediaControllerLayout.setVisibility(View.VISIBLE);
                    break;
                case HIDDEN:
                case DRAGGING:
                    break;
            }
        }
    }

    /**
     * Implements listeners for updating UI on various media session events
     */
    private class MainActivityAudioBrowser extends BaseMediaBrowserAdapter {
        public MainActivityAudioBrowser() {
            super(MainActivity.this);
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            // I think that null gets passed when nothing is playing
            if (null == state) {
                mediaState = PlaybackStateCompat.STATE_NONE;
            } else {
                mediaState = state.getState();
            }
            setPlayPauseButton();
        }

        @Override
        public void onConnected(MediaControllerCompat controller) {
            setupPlayerControlView(controller);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (null != metadata) {
                songTitleView.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
                artistNameView.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            } else {
                songTitleView.setText("");
                artistNameView.setText("");
            }
        }
    }
}