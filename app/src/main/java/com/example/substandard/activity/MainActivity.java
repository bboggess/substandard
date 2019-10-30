package com.example.substandard.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.substandard.R;
import com.example.substandard.fragment.MainFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements
        MainFragment.OnFragmentInteractionListener,
        NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout mDrawerLayout;
    // TODO delete test TextView
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpNavigationDrawer();
        setupNavigationClickListener();
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

        // Testing -- delete
        textView = findViewById(R.id.tv_main);
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

    // What does this do???
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    // Should I be launching Settings as an Activity or a Fragment?
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.item_settings:
                Intent launchSettingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(launchSettingsActivity);
                break;

            default:
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}