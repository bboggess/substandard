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
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager.widget.ViewPager;

import com.example.substandard.R;
import com.example.substandard.database.data.ArtistAndAllAlbums;
import com.example.substandard.ui.model.ArtistDetailViewModel;
import com.example.substandard.ui.model.ArtistDetailViewModelFactory;
import com.example.substandard.utility.InjectorUtils;
import com.google.android.material.tabs.TabLayout;

public class ArtistViewFragment extends AbstractArtistViewFragment {
    private static final String TAG = ArtistViewFragment.class.getSimpleName();

    private View rootView;

    private ArtistDetailViewModel artistDetailViewModel;

    /**
     * Helper method to set up ViewPager and tabs
     */
    private void setupViewPager() {
        TabLayout tabLayout = rootView.findViewById(R.id.tab_layout_artist_view);
        ViewPager viewPager = rootView.findViewById(R.id.view_pager_artist_view);

        Log.d(TAG, "creating PageAdapter for: " + getArtist().getName());
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager(),
                tabLayout.getTabCount(), getArtistAndAllAlbums());
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
        String artistId = ArtistViewFragmentArgs.fromBundle(getArguments()).getArtistId();
        setUpArtistDetailViewModel(artistId);
        return rootView;
    }

    private void setUpArtistDetailViewModel(String artistId) {
        ArtistDetailViewModelFactory factory = InjectorUtils.provideArtistDetailViewModelFactory(getContext(), artistId);
        artistDetailViewModel = new ViewModelProvider(this, factory).get(ArtistDetailViewModel.class);
        artistDetailViewModel.getArtist().observe(this, (artistAndAlbums) -> {
            setArtist(artistAndAlbums);
            setupViewPager();
        });
    }


    /**
     * Adapter for tab pager. Handles populating tabs, handling titles, etc.
     */
    public class PagerAdapter extends FragmentStatePagerAdapter {

        private int numTabs;
        private ArtistAndAllAlbums artist;


        public PagerAdapter(FragmentManager fragmentManager, int numTabs, ArtistAndAllAlbums artist) {
            super(fragmentManager);
            Log.d(TAG, "PagerAdapter constructor: " + artist.getArtist().getName());
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
                    return fragment;
                case 1:
                    SimilarArtistsFragment artistsFragment = new SimilarArtistsFragment();
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

        public ArtistAndAllAlbums getArtist() {
            return artist;
        }

        @Override
        public int getCount() {
            return numTabs;
        }
    }
}
