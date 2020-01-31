package com.example.substandard.ui.model;

import android.graphics.Bitmap;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

/**
 * View model to be used in all components of the media player UI. Note that this means you
 * should attach this view model to the main activity's lifecycle.
 */
public class MediaPlayerViewModel extends ViewModel {

    private final MutableLiveData<List<MediaSessionCompat.QueueItem>> songs;
    private final MutableLiveData<MediaSessionCompat.QueueItem> currentSong;
    private final MutableLiveData<Bitmap> albumArt;

    MediaPlayerViewModel() {
        songs = new MutableLiveData<>();
        currentSong = new MutableLiveData<>();
        albumArt = new MutableLiveData<>();
    }

    public void setCurrentSong(MediaSessionCompat.QueueItem song) {
        currentSong.postValue(song);
    }

    public void setPlaylist(List<MediaSessionCompat.QueueItem> playlist) {
        songs.postValue(playlist);
    }

    public void setAlbumArt(Bitmap albumArt) {
        this.albumArt.postValue(albumArt);
    }

    public LiveData<MediaSessionCompat.QueueItem> getCurrentSong() {
        return currentSong;
    }

    public LiveData<List<MediaSessionCompat.QueueItem>> getPlaylist() {
        return songs;
    }

    public LiveData<Bitmap> getAlbumArt() {
        return albumArt;
    }
}
