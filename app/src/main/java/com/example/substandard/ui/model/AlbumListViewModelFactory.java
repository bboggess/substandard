package com.example.substandard.ui.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.substandard.database.SubsonicLibraryRepository;

import java.util.List;

public class AlbumListViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private SubsonicLibraryRepository repository;
    private List<String> ids;

    public AlbumListViewModelFactory(SubsonicLibraryRepository repository, List<String> ids) {
        this.repository = repository;
        this.ids = ids;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AlbumListViewModel(repository, ids);
    }
}
