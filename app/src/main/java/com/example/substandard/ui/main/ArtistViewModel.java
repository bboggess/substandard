package com.example.substandard.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicArtistRepository;
import com.example.substandard.database.data.Artist;

import java.util.List;

public class ArtistViewModel extends ViewModel {

    private final LiveData<List<Artist>> artists;
    private final SubsonicArtistRepository repository;

    ArtistViewModel(SubsonicArtistRepository repository) {
        this.repository = repository;
        artists = repository.getArtists();
    }

    public LiveData<List<Artist>> getArtists() {
        return artists;
    }
}
