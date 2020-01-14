package com.example.substandard.ui.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.Song;

import java.util.List;

public class SongListViewModel extends ViewModel {
    public static final String TAG = SongListViewModel.class.getSimpleName();

    private final LiveData<List<Song>> songs;

    SongListViewModel(String albumId, SubsonicLibraryRepository repository) {
        Log.d(TAG, "Created ViewModel");
        this.songs = repository.getAlbum(albumId);
    }

    public LiveData<List<Song>> getSongs() {
        return songs;
    }
}
