package com.example.substandard.ui.main;

import android.os.Bundle;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.substandard.R;
import com.example.substandard.database.data.AlbumAndAllSongs;
import com.example.substandard.database.data.Song;
import com.example.substandard.player.client.BaseMediaBrowserAdapter;
import com.example.substandard.ui.OnMediaClickListener;
import com.example.substandard.ui.mediaplayer.MediaPlayerLayout;
import com.example.substandard.ui.model.MediaPlayerViewModel;
import com.example.substandard.ui.model.MediaPlayerViewModelFactory;
import com.example.substandard.utility.MediaMetadataUtils;
import com.google.android.material.navigation.NavigationView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MainActivity extends AppCompatActivity implements
        OnMediaClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout drawerLayout;
    private MediaPlayerPanelListener slidingPanelListener;
    private SlidingUpPanelLayout mediaPlayerSlidingPanel;
    private MediaPlayerLayout mediaPlayerLayout;

    private NavHostFragment navHost;
    private NavController navController;
    private Toolbar toolbar;

    private BaseMediaBrowserAdapter mediaBrowserAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        navHost = NavHostFragment.create(R.navigation.nav_graph);
        // create and show the start screen
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, navHost);

        setUpNavigationDrawer();

        // Initialize view member variables
        mediaPlayerLayout = findViewById(R.id.media_player_layout);
        mediaPlayerSlidingPanel = findViewById(R.id.sliding_panel_media_player);

        mediaBrowserAdapter = new MainActivityAudioBrowser();
        mediaPlayerLayout.setMediaBrowser(mediaBrowserAdapter);

        setupViewModel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserAdapter.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        mediaBrowserAdapter.onStop();
        mediaPlayerLayout.disconnectController();
        mediaPlayerSlidingPanel.removePanelSlideListener(slidingPanelListener);
    }

    private void setupPlayerControlView() {
        // Setup sliding panel
        slidingPanelListener = new MediaPlayerPanelListener();
        mediaPlayerSlidingPanel.addPanelSlideListener(slidingPanelListener);
    }


    private void setupViewModel() {
        MediaPlayerViewModelFactory factory = new MediaPlayerViewModelFactory();
        MediaPlayerViewModel viewModel = new ViewModelProvider(this, factory).get(MediaPlayerViewModel.class);
        mediaPlayerLayout.setViewModel(viewModel);
    }

    /**
     * Helper method to configure the navigation drawer to open on swipe.
     */
    private void setUpNavigationDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navController = Navigation.findNavController(this, R.id.fragment_container);

        NavigationView navView = findViewById(R.id.nav_drawer);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph())
                        .setDrawerLayout(drawerLayout)
                        .build();

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {

        });

        NavigationUI.setupWithNavController(navView, navController);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    /*
     * If we press back while the drawer is open, close the drawer instead of the app.
     */
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if(mediaPlayerSlidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mediaPlayerSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }

    @Override
    public void onSongClick(Song clickedSong, AlbumAndAllSongs albumAndAllSongs) {
        MediaControllerCompat.TransportControls transportControls = mediaBrowserAdapter.getTransportControl();
        queueAlbum(albumAndAllSongs);
        transportControls.skipToQueueItem(Long.parseLong(clickedSong.getId()));
        mediaPlayerSlidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    /**
     * Replaces the current play queue with the given album
     * @param albumAndAllSongs
     */
    private void queueAlbum(AlbumAndAllSongs albumAndAllSongs) {
        mediaBrowserAdapter.clearQueue();
        for (Song song : albumAndAllSongs.getSongs()) {
            MediaDescriptionCompat description = MediaMetadataUtils.convertSongToMediaDescription(song);
            mediaBrowserAdapter.addQueueItem(description);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
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
            mediaPlayerLayout.setController();
        }

    }
}