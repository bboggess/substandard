package com.example.substandard.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
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
    private MediaPlayerPanelListener slidingPanelListener;
    private SlidingUpPanelLayout mediaPlayerSlidingPanel;
    private MediaPlayerLayout mediaPlayerLayout;

    private BaseMediaBrowserAdapter mediaBrowserAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.main_toolbar));

        // create and show the start screen, which is currently the list of artists
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, new ArtistsFragment(), "Artists");
        ft.commit();

        // set up navigation drawer
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        setUpNavigationDrawer();
        setupNavigationClickListener();

        // Initialize view member variables
        mediaPlayerLayout = findViewById(R.id.media_player_layout);
        mediaPlayerSlidingPanel = findViewById(R.id.sliding_panel_media_player);

        mediaBrowserAdapter = new MainActivityAudioBrowser();
        mediaPlayerLayout.setMediaBrowser(mediaBrowserAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserAdapter.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        mediaBrowserAdapter.onStop();
        mediaPlayerLayout.disconnectController();
        mediaPlayerSlidingPanel.removePanelSlideListener(slidingPanelListener);
    }

    private void setupPlayerControlView() {
        // Setup sliding panel
        slidingPanelListener = new MediaPlayerPanelListener();
        mediaPlayerSlidingPanel.addPanelSlideListener(slidingPanelListener);
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

    /**
     * Logic for transitioning from mini to full media player on sliding up
     */
    private class MediaPlayerPanelListener implements SlidingUpPanelLayout.PanelSlideListener {
        private static final float OFFSET_THRESHOLD = 0.3f;

        @Override
        public void onPanelSlide(View panel, float slideOffset) {
            // deals with removing the action bar
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
            mediaPlayerLayout.setMiniPlayerAlpha(1 - slideOffset);
        }

        @Override
        public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            switch (newState) {
                case EXPANDED:
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    mediaPlayerLayout.onExpanded();
                    break;
                case COLLAPSED:
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    mediaPlayerLayout.onCollapsed();
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
        public void onConnected(MediaControllerCompat controller) {
            setupPlayerControlView();
            mediaPlayerLayout.setController(controller);
        }

    }
}