package com.example.substandard.ui.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.Artist;

public class ArtistDetailViewModel extends ViewModel {
    private final LiveData<Artist> artist;

    ArtistDetailViewModel(String artistId, SubsonicLibraryRepository repository) {
        artist = repository.getArtistById(artistId);
    }

    public LiveData<Artist> getArtist() { return artist; }
}
