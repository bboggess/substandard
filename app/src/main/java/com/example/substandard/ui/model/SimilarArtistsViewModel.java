package com.example.substandard.ui.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.Artist;

import java.util.List;

public class SimilarArtistsViewModel extends ViewModel {
    private final LiveData<List<Artist>> similarArtists;
    private final SubsonicLibraryRepository repository;

    SimilarArtistsViewModel(String artistId, SubsonicLibraryRepository repository) {
        this.repository = repository;
        this.similarArtists = repository.getSimilarArtists(artistId);
    }

    public LiveData<List<Artist>> getSimilarArtists() { return similarArtists; }
}
