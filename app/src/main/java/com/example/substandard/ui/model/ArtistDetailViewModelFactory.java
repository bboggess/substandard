package com.example.substandard.ui.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.substandard.database.SubsonicArtistRepository;

public class ArtistDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private SubsonicArtistRepository repository;
    private int artistId;

    public ArtistDetailViewModelFactory(SubsonicArtistRepository repository, int artistId) {
        this.repository = repository;
        this.artistId = artistId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ArtistDetailViewModel(artistId, repository);
    }
}
