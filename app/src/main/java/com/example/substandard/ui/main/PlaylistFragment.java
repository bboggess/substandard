package com.example.substandard.ui.main;

import android.os.Bundle;
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
import com.example.substandard.ui.model.PlaylistViewModel;
import com.example.substandard.ui.model.PlaylistViewModelFactory;

public class PlaylistFragment extends Fragment {

    private PlaylistViewModel viewModel;
    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;

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

    private void setupRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.rv_playlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PlaylistAdapter(getContext());
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        PlaylistViewModelFactory factory = new PlaylistViewModelFactory();
        viewModel = new ViewModelProvider(getActivity(), factory).get(PlaylistViewModel.class);
        viewModel.getPlaylist().observe(getViewLifecycleOwner(), playlist -> {
            adapter.setQueue(playlist);
        });
        viewModel.getCurrentSong().observe(getViewLifecycleOwner(), song -> {
            adapter.setCurrentTrack(song);
        });
    }
}
