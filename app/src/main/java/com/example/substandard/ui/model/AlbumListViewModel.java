package com.example.substandard.ui.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.AlbumAndArtist;

import java.util.List;

public class AlbumListViewModel extends ViewModel {
    private static final String TAG = AlbumListViewModel.class.getSimpleName();

    private final LiveData<List<AlbumAndArtist>> albums;

    AlbumListViewModel(SubsonicLibraryRepository repository, List<String> ids) {
        Log.d(TAG, "loading album list view model: " + ids);
        this.albums = repository.getAlbumsWithArtist(ids);
    }

    public LiveData<List<AlbumAndArtist>> getAlbums() {
        return albums;
    }
}
