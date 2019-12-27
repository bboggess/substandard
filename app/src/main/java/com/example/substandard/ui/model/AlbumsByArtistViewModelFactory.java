package com.example.substandard.ui.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.substandard.database.SubsonicArtistRepository;
import com.example.substandard.database.data.Artist;

public class AlbumsByArtistViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private SubsonicArtistRepository repository;
    private Artist artist;

    public AlbumsByArtistViewModelFactory(SubsonicArtistRepository repository, Artist artist) {
        this.repository = repository;
        this.artist = artist;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AlbumsByArtistViewModel(artist, repository);
    }
}
