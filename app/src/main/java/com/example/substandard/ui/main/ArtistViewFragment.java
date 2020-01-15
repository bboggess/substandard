package com.example.substandard.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.substandard.R;
import com.example.substandard.database.data.Artist;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

public class ArtistViewFragment extends AbstractArtistViewFragment {
    private static final String TAG = ArtistViewFragment.class.getSimpleName();

    private View rootView;

    // For tabs that let us switch between views
    private TabLayout tabLayout;
    private TabItem tabAlbums;
    private TabItem tabSimilarArtists;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    /**
     * Helper method to set up ViewPager and tabs
     */
    private void setupViewPager() {
        tabLayout = rootView.findViewById(R.id.tab_layout_artist_view);
        tabAlbums = rootView.findViewById(R.id.tab_item_albums);
        tabSimilarArtists = rootView.findViewById(R.id.tab_item_similar);
        viewPager = rootView.findViewById(R.id.view_pager_artist_view);

        Log.d(TAG, "creating PageAdapter for: " + getArtist().getName());
        pagerAdapter = new PagerAdapter(getChildFragmentManager(),
                tabLayout.getTabCount(), getArtist());
        viewPager.setAdapter(pagerAdapter);
        Log.d(TAG, "populated ViewPager with: " + pagerAdapter.getArtist());
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_artist_view, container, false);
        setupViewPager();
        return rootView;
    }


    /**
     * Adapter for tab pager. Handles populating tabs, handling titles, etc.
     */
    public class PagerAdapter extends FragmentStatePagerAdapter {

        private int numTabs;
        private Artist artist;


        public PagerAdapter(FragmentManager fragmentManager, int numTabs, Artist artist) {
            super(fragmentManager);
            Log.d(TAG, "PagerAdapter constructor: " + artist.getName());
            this.numTabs = numTabs;
            this.artist = artist;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "PagerAdapter getItem at position " + position);
            switch (position) {
                case 0:
                    AlbumsByArtistFragment fragment = new AlbumsByArtistFragment();
                    fragment.setArtist(artist);
                    return fragment;
                case 1:
                    SimilarArtistsFragment artistsFragment = new SimilarArtistsFragment();
                    artistsFragment.setArtist(artist);
                    return artistsFragment;
                default:
                    return null;
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.albums);
                case 1:
                    return getString(R.string.similar_artists);
                default:
                    return null;
            }
        }

        public Artist getArtist() {
            return artist;
        }

        @Override
        public int getCount() {
            return numTabs;
        }
    }
}
