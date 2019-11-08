package com.example.substandard.ui.artistdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.substandard.database.SubsonicArtistRepository;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;

import java.util.List;

class AlbumsByArtistViewModel extends ViewModel {
    private final LiveData<List<Album>> albums;
    private final SubsonicArtistRepository repository;

    AlbumsByArtistViewModel(Artist artist, SubsonicArtistRepository repository) {
        this.repository = repository;
        this.albums = repository.getAlbumsByArtist(artist);
    }

    LiveData<List<Album>> getAlbums() { return albums; }

}
