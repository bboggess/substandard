package com.example.substandard.ui.artistdetail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.substandard.R;
import com.example.substandard.database.data.Artist;
import com.example.substandard.ui.settings.SettingsActivity;
import com.example.substandard.utility.InjectorUtils;
import com.google.android.material.navigation.NavigationView;

/**
 * This activity is launched when the user chooses an Artist from the main activity.
 * Should display things such as list of albums, similar artists, top songs, maybe a
 * bio and picture.
 */
public class ArtistViewActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener,
        AlbumsByArtistFragment.OnFragmentInteractionListener {
    private static final String TAG = ArtistViewActivity.class.getSimpleName();

    public static final String ARTIST_ID_EXTRA = "id";

    private ArtistDetailViewModel artistViewModel;

    private DrawerLayout drawerLayout;

    private void setUpArtistsViewModel(int artistId) {
        ArtistDetailViewModelFactory factory = InjectorUtils.provideArtistDetailViewModelFactory(this, artistId);
        artistViewModel = new ViewModelProvider(this, factory).get(ArtistDetailViewModel.class);
        artistViewModel.getArtist().observe(this, new Observer<Artist>() {
            @Override
            public void onChanged(Artist artist) {
                Log.d(TAG, "got artist info for: " + artist.getName());
                // TODO launch that fragment
                AlbumsByArtistFragment albumsFragment = new AlbumsByArtistFragment();
                albumsFragment.setArtist(artist);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.artist_info_fragment_container, albumsFragment, "Albums");
                transaction.commit();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent callingIntent = getIntent();
        if (!callingIntent.hasExtra(ARTIST_ID_EXTRA)) {
            // TODO some sort of error
        }

        int artistId = callingIntent.getIntExtra(ARTIST_ID_EXTRA, -1);
        setUpNavigationDrawer();
        setupNavigationClickListener();
        setUpArtistsViewModel(artistId);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
//                swapFragments(new SettingsFragment(),"Settings");
                break;

            default:
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
