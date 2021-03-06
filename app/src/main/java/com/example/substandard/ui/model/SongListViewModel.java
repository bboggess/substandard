package com.example.substandard.ui.model;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicLibraryRepository;
import com.example.substandard.database.data.AlbumAndAllSongs;

public class SongListViewModel extends ViewModel {
    public static final String TAG = SongListViewModel.class.getSimpleName();

    private final LiveData<AlbumAndAllSongs> songs;
    private LiveData<Bitmap> coverArt;

    SongListViewModel(String albumId, SubsonicLibraryRepository repository) {
        Log.d(TAG, "Created ViewModel");
        this.songs = repository.getAlbum(albumId);
    }

    public LiveData<Bitmap> getCoverArt() {
        return coverArt;
    }

    public void setCoverArt(LiveData<Bitmap> coverArt) {
        this.coverArt = coverArt;
    }

    public LiveData<AlbumAndAllSongs> getAlbum() {
        return songs;
    }
}
