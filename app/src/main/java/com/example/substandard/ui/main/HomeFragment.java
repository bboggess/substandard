package com.example.substandard.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.substandard.R;
import com.example.substandard.ui.model.HomeScreenViewModel;
import com.example.substandard.ui.model.HomeScreenViewModelFactory;
import com.example.substandard.utility.InjectorUtils;
import com.example.substandard.utility.SubstandardPreferences;

import java.util.ArrayList;

/**
 * Main {@link android.widget.FrameLayout}, first page seen when user opens the app. Contains
 * quick links to different albums. Otherwise, user is expected to use nav drawer to navigate
 */
public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!SubstandardPreferences.isLoggedIn(getContext())) {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.loginFragment);
        }

        // observe the viewmodel and load fragments as we get info
        HomeScreenViewModelFactory factory = InjectorUtils.provideHomeScreenViewModelFactory(getContext());
        HomeScreenViewModel viewModel = new ViewModelProvider(this, factory).get(HomeScreenViewModel.class);
        viewModel.getRecentAlbums().observe(getViewLifecycleOwner(), idList -> {
            Log.d(TAG, "Loaded IDs: " + idList);
            HomeAlbumListFragment fragment = HomeAlbumListFragment
                                                .newInstance(new ArrayList<>(idList), getString(R.string.recent_albums_label));
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.recent_albums_fragment, fragment).commit();
        });

        viewModel.getFavoriteAlbums().observe(getViewLifecycleOwner(), idList -> {
            Log.d(TAG, "Loaded IDs: " + idList);
            HomeAlbumListFragment fragment = HomeAlbumListFragment
                                                .newInstance(new ArrayList<>(idList), getString(R.string.fav_albums_label));
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.fav_albums_fragment, fragment).commit();
        });

        viewModel.getNewAlbums().observe(getViewLifecycleOwner(), idList -> {
            Log.d(TAG, "Loaded IDs: " + idList);
            HomeAlbumListFragment fragment = HomeAlbumListFragment
                    .newInstance(new ArrayList<>(idList), getString(R.string.new_albums_label));
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.new_albums_fragment, fragment).commit();
        });

        viewModel.getRandomAlbums().observe(getViewLifecycleOwner(), idList -> {
            Log.d(TAG, "Loaded IDs: " + idList);
            HomeAlbumListFragment fragment = HomeAlbumListFragment
                    .newInstance(new ArrayList<>(idList), getString(R.string.random_albums_label));
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.random_albums_fragment, fragment).commit();
        });
    }
}