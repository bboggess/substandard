package com.example.substandard.database;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.substandard.AppExecutors;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.ArtistDao;
import com.example.substandard.database.data.Song;
import com.example.substandard.database.network.SubsonicNetworkDataSource;

import java.util.List;

/**
 * This class connects the Artists database with other components of the app. The only way
 * to request network data from UI code is through the instance of this class.
 * Uses singleton instantiation pattern.
 */
public class SubsonicArtistRepository {
    private final static String TAG = SubsonicArtistRepository.class.getSimpleName();

    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static SubsonicArtistRepository repositoryInstance;

    // Actually knows how to make calls to server
    private final SubsonicNetworkDataSource dataSource;
    // Knows how to read from/write to the database
    private final ArtistDao artistDao;
    // Only used to schedule database operations off of main thread
    private final AppExecutors executors;

    // This is done poorly. Supposed to check whether we've filled the database from the
    // server yet, but tries to reload whenever the app starts. I guess this isn't terrible?
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

    /**
     *
     * @param artistId id of artist for search
     * @return list of similar artists (note: currently only those present in the library)
     */
    public LiveData<List<Artist>> getSimilarArtists(int artistId) {
        Log.d(TAG, "getting similar artists");
        return dataSource.fetchSimilarArtists(artistId);
    }

    /**
     * Get cover art from the server
     * @param album Album whose cover is requested
     * @return LiveData which will be updated with image once loaded
     */
    public LiveData<Bitmap> getCoverArt(Album album) {
        Log.d(TAG, "getting cover art");
        return dataSource.fetchCoverArt(album);
    }

    /**
     * Get all songs from an album on the server
     * @param album Album to find songs from
     * @return list of all songs, wrapped in LiveData updated once processing finishes.
     */
    public LiveData<List<Song>> getAlbum(Album album) {
        Log.d(TAG, "getting songs for: " + album.getName());
        return dataSource.fetchAlbum(album);
    }

    public LiveData<List<Song>> getAlbum(int albumId) {
        return dataSource.fetchAlbum(albumId);
    }
}
