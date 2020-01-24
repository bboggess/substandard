package com.example.substandard.ui.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.ArtistAndAllAlbums;

public class ArtistDetailViewModel extends ViewModel {
    private final LiveData<ArtistAndAllAlbums> artist;

    ArtistDetailViewModel(String artistId, SubsonicLibraryRepository repository) {
        artist = repository.getArtistAndAllAlbums(artistId);
    }

    public LiveData<ArtistAndAllAlbums> getArtist() { return artist; }
}
