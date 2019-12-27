package com.example.substandard.ui.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.substandard.database.SubsonicArtistRepository;

public class SongListViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private SubsonicArtistRepository repository;
    private int albumId;

    public SongListViewModelFactory(SubsonicArtistRepository repository, int albumId) {
        this.repository = repository;
        this.albumId = albumId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SongListViewModel(albumId, repository);
    }
}