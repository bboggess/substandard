package com.example.substandard.ui.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.substandard.database.SubsonicLibraryRepository;

public class ArtistDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private SubsonicLibraryRepository repository;
    private String artistId;

    public ArtistDetailViewModelFactory(SubsonicLibraryRepository repository, String artistId) {
        this.repository = repository;
        this.artistId = artistId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ArtistDetailViewModel(artistId, repository);
    }
}
