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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.substandard.R;
import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.AlbumAndAllSongs;
import com.example.substandard.database.data.Song;
import com.example.substandard.ui.OnMediaClickListener;
import com.example.substandard.ui.ViewHolderItemClickListener;
import com.example.substandard.ui.model.SongListViewModel;
import com.example.substandard.ui.model.SongListViewModelFactory;
import com.example.substandard.utility.InjectorUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SongListFragment extends Fragment implements ViewHolderItemClickListener<Song> {
    private static final String TAG = SongListFragment.class.getSimpleName();

    private SongListViewModel songsViewModel;

    private RecyclerView recyclerView;
    private SongAdapter adapter;
    private ProgressBar progressBar;

    private OnMediaClickListener clickListener;

    private String albumId;
    private AlbumAndAllSongs album;

    public static final String EXTRA_ALBUM_ID_KEY = "albumId";

    public static SongListFragment newInstance(String id) {
        SongListFragment fragment = new SongListFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_ALBUM_ID_KEY, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.song_list_fragment, container, false);
        progressBar = rootView.findViewById(R.id.song_view_pb);
        FloatingActionButton fab = rootView.findViewById(R.id.play_album_fab);
        // play starting from the first song...hacky?
        fab.setOnClickListener(view -> onItemClick(album.getSongs().get(0)));

        this.albumId = SongListFragmentArgs.fromBundle(getArguments()).getAlbumId();
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

    private void setupRecyclerView(View rootView) {
        recyclerView = rootView.findViewById(R.id.rv_song_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new SongAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);
    }

    // This is necessary because I haven't found a good way to share with calling fragment
    private void loadImageToViewModel(Album album) {
        SubsonicLibraryRepository repository = InjectorUtils.provideLibraryRepository(getContext());
        songsViewModel.setCoverArt(repository.getCoverArt(album, getContext()));
        songsViewModel.getCoverArt().observe(getViewLifecycleOwner(), bitmap -> {
            adapter.setCoverArt(bitmap);
        });
    }

    private void setupSongsViewModel() {
        Log.d(TAG, "setting up song view model ");
        progressBar.setVisibility(View.VISIBLE);

        // TODO setup so whole nav graph is able to share a ViewModel
        SongListViewModelFactory factory = InjectorUtils.provideSongListViewModelFactory(getContext(), albumId);
        songsViewModel = new ViewModelProvider(this, factory).get(SongListViewModel.class);
        songsViewModel.getAlbum().observe(getViewLifecycleOwner(), album -> {
                Log.d(TAG, "fetched albums: " + album.toString());
                this.album = album;
                adapter.setSongs(album.getSongs());
                progressBar.setVisibility(View.INVISIBLE);
                // I really shouldn't have to go through loading this again...
                loadImageToViewModel(album.getAlbum());
            });
    }

    @Override
    public void onItemClick(Song obj) {
        Log.d(TAG, "clicked on song: " + obj.getTitle());
        clickListener.onLoadAlbum(album);
        clickListener.onSongClick(obj.getId());
    }
}
