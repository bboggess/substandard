package com.example.substandard.ui.mediaplayer;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.ui.OnMediaClickListener;
import com.example.substandard.ui.ViewHolderItemClickListener;
import com.example.substandard.ui.model.MediaPlayerViewModel;
import com.example.substandard.ui.model.MediaPlayerViewModelFactory;

/**
 * Fragment which displays and allows editing of the currently playing playlist.
 */
public class PlaylistFragment extends Fragment
        implements ViewHolderItemClickListener<MediaSessionCompat.QueueItem> {

    private MediaPlayerViewModel viewModel;
    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;
    private OnMediaClickListener mediaListener;

    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.media_player_playlist, container, false);
        setupRecyclerView(rootView);
        setupViewModel();
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnMediaClickListener) {
            mediaListener = (OnMediaClickListener) context;
        } else {
            throw new RuntimeException(context.toString() +
                    " must implement onMediaClickListener");
        }
    }

    private void setupRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.rv_playlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PlaylistAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        MediaPlayerViewModelFactory factory = new MediaPlayerViewModelFactory();
        viewModel = new ViewModelProvider(getActivity(), factory).get(MediaPlayerViewModel.class);
        viewModel.getPlaylist().observe(getViewLifecycleOwner(), playlist -> {
            adapter.setQueue(playlist);
        });
        viewModel.getCurrentSong().observe(getViewLifecycleOwner(), song -> {
            adapter.setCurrentTrack(song);
        });
    }

    @Override
    public void onItemClick(MediaSessionCompat.QueueItem obj) {
        mediaListener.onSongClick(obj.getDescription().getMediaId());
    }
}
