package com.example.substandard.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
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
import com.example.substandard.ui.OnMediaClickListener;
import com.example.substandard.ui.settings.SettingsActivity;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements
        OnMediaClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        FragmentManager.OnBackStackChangedListener {

    private DrawerLayout mDrawerLayout;

    private BaseMediaBrowserAdapter mediaBrowserAdapter;
    private boolean isPlaying;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, new ArtistsFragment(), "Artists");
        ft.commit();

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        setUpNavigationDrawer();
        setupNavigationClickListener();

        mediaBrowserAdapter = new BaseMediaBrowserAdapter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowserAdapter.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaBrowserAdapter.onStop();
    }

    /**
     * Helper method to configure the navigation drawer to open on swipe.
     */
    private void setUpNavigationDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
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
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
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
        // TODO play a song
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
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

            default:
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

}