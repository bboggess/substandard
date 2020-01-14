package com.example.substandard.ui.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.Artist;

import java.util.List;

public class ArtistViewModel extends ViewModel {

    private final LiveData<List<Artist>> artists;
    private final SubsonicLibraryRepository repository;

    ArtistViewModel(SubsonicLibraryRepository repository) {
        this.repository = repository;
        artists = repository.getArtists();
    }

    public LiveData<List<Artist>> getArtists() {
        return artists;
    }
}
