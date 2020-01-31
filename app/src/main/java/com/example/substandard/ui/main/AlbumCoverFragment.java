package com.example.substandard.ui.main;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.substandard.R;
import com.example.substandard.ui.model.MediaPlayerViewModel;
import com.example.substandard.ui.model.MediaPlayerViewModelFactory;

/**
 * A Fragment which merely holds an album cover, dead center
 */
public class AlbumCoverFragment extends Fragment {

    private MediaPlayerViewModel viewModel;
    private ImageView coverArtView;

    public static AlbumCoverFragment newInstance() {
        Bundle args = new Bundle();
        AlbumCoverFragment fragment = new AlbumCoverFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album_cover, container, false);
        coverArtView = view.findViewById(R.id.album_art_large);
        setupViewModel();
        return view;
    }

    private void setupViewModel() {
        MediaPlayerViewModelFactory factory = new MediaPlayerViewModelFactory();
        viewModel = new ViewModelProvider(getActivity(), factory).get(MediaPlayerViewModel.class);

        // Subscribe to updates on now playing album art, and update the album cover
        viewModel.getAlbumArt().observe(getActivity(), image -> {
            if (null == image) {
                Drawable transparentDrawable = getContext().getDrawable(android.R.color.transparent);
                coverArtView.setImageDrawable(transparentDrawable);
            } else {
                coverArtView.setImageBitmap(image);
            }
        });
    }
}
