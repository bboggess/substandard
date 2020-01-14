package com.example.substandard.ui.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.substandard.database.SubsonicLibraryRepository;

public class ArtistViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final SubsonicLibraryRepository repository;

    public ArtistViewModelFactory(SubsonicLibraryRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ArtistViewModel(repository);
    }
}
