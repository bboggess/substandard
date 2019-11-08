package com.example.substandard.database;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.substandard.AppExecutors;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.ArtistDao;
import com.example.substandard.database.network.SubsonicNetworkDataSource;

import java.util.List;

/**
 * This class class connects the Artists database with other components of the app.
 * Uses singleton instantiation pattern.
 */
public class SubsonicArtistRepository {
    private final static String TAG = SubsonicArtistRepository.class.getSimpleName();

    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static SubsonicArtistRepository repositoryInstance;

    private final SubsonicNetworkDataSource dataSource;
    private final ArtistDao artistDao;
    private final AppExecutors executors;

    // This is done poorly. Supposed to check whether we've filled the database from the
    // server yet, but definitely doesn't.
    private boolean isInitialized;

    private SubsonicArtistRepository(final ArtistDao artistDao,
                                     final SubsonicNetworkDataSource dataSource,
                                     final AppExecutors executors) {
        this.artistDao = artistDao;
        this.dataSource = dataSource;
        this.executors = executors;
        isInitialized = false;

        // sets live data in the data source to update database when changed
        dataSource.getArtists().observeForever(new Observer<List<Artist>>() {
            @Override
            public void onChanged(final List<Artist> artists) {
                Log.d(TAG, "Artist database updated");
                executors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        artistDao.update(artists);
                    }
                });
            }
        });
    }

    public static SubsonicArtistRepository getInstance(ArtistDao artistDao,
                                                       SubsonicNetworkDataSource dataSource,
                                                       AppExecutors executors) {
        if (repositoryInstance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "created new repository instance");
                repositoryInstance = new SubsonicArtistRepository(artistDao, dataSource, executors);
            }
        }

        Log.d(TAG, "repository instance accessed");
        return repositoryInstance;
    }

    /**
     * Fetches artists from the server if hasn't been done (at least, it's supposed to do this)
     */
    private synchronized void initializeData() {
        if (isInitialized) {
            return;
        }

        isInitialized = true;

        Log.d(TAG, "database initialized");
        dataSource.fetchArtists();
    }

    /**
     * Grabs all artists from server, updating database if need be.
     */
    public synchronized void refreshLibrary() {
        Log.d(TAG, "refreshing library");
        dataSource.fetchArtists();
    }

    /**
     *
     * @return list of Artists in the database, ordered by name
     */
    public LiveData<List<Artist>> getArtists() {
        Log.d(TAG, "obtaining artists from DAO");
        initializeData();
        return artistDao.loadAll();
    }

    /**
     * Lookup artist by id column
     * @param id id of the artist we want
     * @return artist from the database
     */
    public LiveData<Artist> getArtistById(int id) {
        return artistDao.loadArtistById(id);
    }

    /**
     * Fetches all albums by the artist from the server
     * @param artist the artist whose albums we want
     * @return a list of all albums from the given artist
     */
    public LiveData<List<Album>> getAlbumsByArtist(Artist artist) {
        return dataSource.fetchAlbumsForArtist(artist);
    }
}
