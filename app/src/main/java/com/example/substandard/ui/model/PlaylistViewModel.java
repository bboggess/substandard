package com.example.substandard.ui.model;

import android.support.v4.media.session.MediaSessionCompat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class PlaylistViewModel extends ViewModel {

    private MutableLiveData<List<MediaSessionCompat.QueueItem>> songs;
    private final MutableLiveData<MediaSessionCompat.QueueItem> currentSong;

    PlaylistViewModel() {
        songs = new MutableLiveData<>();
        currentSong = new MutableLiveData<>();
    }

    public void setCurrentSong(MediaSessionCompat.QueueItem song) {
        currentSong.postValue(song);
    }

    public void setPlaylist(List<MediaSessionCompat.QueueItem> playlist) {
        songs.postValue(playlist);
    }

    public LiveData<MediaSessionCompat.QueueItem> getCurrentSong() {
        return currentSong;
    }

    public LiveData<List<MediaSessionCompat.QueueItem>> getPlaylist() {
        return songs;
    }
}
