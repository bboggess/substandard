package com.example.substandard.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Song;
import com.example.substandard.ui.OnMediaClickListener;
import com.example.substandard.ui.model.SongListViewModel;
import com.example.substandard.ui.model.SongListViewModelFactory;
import com.example.substandard.utility.InjectorUtils;

import java.util.List;

public class SongListFragment extends Fragment implements ViewHolderItemClickListener<Song> {
    private static final String TAG = SongListFragment.class.getSimpleName();

    private SongListViewModel songsViewModel;

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private ProgressBar progressBar;

    private OnMediaClickListener clickListener;

    private Album album;

    void setAlbum(Album album) {
        this.album = album;
    }

    public static SongListFragment newInstance() {
        return new SongListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.song_list_fragment, container, false);
        progressBar = rootView.findViewById(R.id.song_view_pb);

        setupRecyclerView(rootView);
        setupSongsViewModel();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMediaClickListener) {
            clickListener = (OnMediaClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMediaClickListener");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void setupRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.rv_song_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new SongAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSongsViewModel() {
        Log.d(TAG, "setting up song view model ");
        progressBar.setVisibility(View.VISIBLE);

        SongListViewModelFactory factory = InjectorUtils
                .provideSongListViewModelFactory(getContext(), album.getId());
        songsViewModel = new ViewModelProvider(this, factory)
                .get(SongListViewModel.class);
        songsViewModel.getSongs().observe(this, new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                Log.d(TAG, "fetched albums: " + songs.toString());
                adapter.setSongs(songs);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onItemClick(Song obj) {
        Log.d(TAG, "clicked on song: " + obj.getTitle());
        clickListener.onSongClick(obj);
    }
}
