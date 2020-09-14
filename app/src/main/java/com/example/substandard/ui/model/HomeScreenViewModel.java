package com.example.substandard.ui.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicLibraryRepository;

import java.util.List;

public class HomeScreenViewModel extends ViewModel {

    private final LiveData<List<String>> newAlbums;
    private final LiveData<List<String>> recentAlbums;
    private final LiveData<List<String>> favAlbums;
    private final LiveData<List<String>> randomAlbums;

    public HomeScreenViewModel(SubsonicLibraryRepository repository) {
        newAlbums = repository.getNewestAlbums();
        recentAlbums = repository.getRecentAlbums();
        favAlbums = repository.getFavoriteAlbums();
        randomAlbums = repository.getRandomAlbums();
    }

    public LiveData<List<String>> getNewAlbums() {
        return newAlbums;
    }

    public LiveData<List<String>> getRecentAlbums() {
        return recentAlbums;
    }

    public LiveData<List<String>> getFavoriteAlbums() {
        return favAlbums;
    }

    public LiveData<List<String>> getRandomAlbums() {
        return randomAlbums;
    }
}
