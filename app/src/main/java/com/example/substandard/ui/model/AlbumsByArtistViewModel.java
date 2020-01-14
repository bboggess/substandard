package com.example.substandard.ui.model;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;

import java.util.List;

public class AlbumsByArtistViewModel extends ViewModel {
    private static final String TAG = AlbumsByArtistViewModel.class.getSimpleName();

    private final LiveData<List<Album>> albums;

    AlbumsByArtistViewModel(Artist artist, SubsonicLibraryRepository repository) {
        this.albums = repository.getAlbumsByArtist(artist);
        Log.d(TAG, "setting up ViewModel for: " + artist.getName());
    }

    public LiveData<List<Album>> getAlbums() {
        return albums;
    }
}
