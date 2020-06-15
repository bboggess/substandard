package com.example.substandard.database;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.substandard.AppExecutors;
import com.example.substandard.R;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.AlbumAndAllSongs;
import com.example.substandard.database.data.AlbumDao;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.ArtistAndAllAlbums;
import com.example.substandard.database.data.ArtistDao;
import com.example.substandard.database.data.Song;
import com.example.substandard.database.data.SongDao;
import com.example.substandard.database.network.subsonic.SubsonicNetworkDataSource;
import com.example.substandard.service.CoverArtDownloadIntentService;
import com.example.substandard.service.CoverArtResultReceiver;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import io.reactivex.Single;

/**
 * This class connects the Artists database with other components of the app. The only way
 * to request network data from UI code is through the instance of this class.
 * Uses singleton instantiation pattern.
 */
public class SubsonicLibraryRepository {
    private final static String TAG = SubsonicLibraryRepository.class.getSimpleName();

    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static SubsonicLibraryRepository repositoryInstance;

    // Actually knows how to make calls to server
    private final SubsonicNetworkDataSource dataSource;
    // Knows how to read from/write to the database
    private final ArtistDao artistDao;
    private final AlbumDao albumDao;
    private final SongDao songDao;
    // Only used to schedule database operations off of main thread
    private final AppExecutors executors;

    private SubsonicLibraryRepository(final ArtistDao artistDao,
                                      final AlbumDao albumDao,
                                      final SongDao songDao,
                                      final SubsonicNetworkDataSource dataSource,
                                      final AppExecutors executors) {
        this.artistDao = artistDao;
        this.albumDao = albumDao;
        this.songDao = songDao;
        this.dataSource = dataSource;
        this.executors = executors;

        // sets live data in the data source to update database when changed
        dataSource.getArtists().observeForever(new Observer<List<Artist>>() {
            @Override
            public void onChanged(final List<Artist> artists) {
                Log.d(TAG, "Artist database updated");
                executors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        artistDao.insertAll(artists);
                        Log.d(TAG, "artists written to DAO");
                    }
                });
            }
        });

        dataSource.getAlbums().observeForever(new Observer<List<Album>>() {
            @Override
            public void onChanged(final List<Album> albums) {
                Log.d(TAG, "Album database updated");
                executors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        albumDao.insertAll(albums);
                    }
                });
            }
        });

        dataSource.getSongs().observeForever(new Observer<List<Song>>() {
            @Override
            public void onChanged(final List<Song> songs) {
                Log.d(TAG, "Song database updated");
                executors.diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        for (Song song : songs) {
                            Log.d(TAG, "song id: " + song.getId()
                                + ", album id: " + song.getAlbumId()
                                + ", artist: " + song.getArtistName());
                        }
                        songDao.insertAll(songs);
                    }
                });
            }
        });
    }

    // TODO create an "lastUpdated" member to keep track of when last update was
    // TODO change update to only download things added AFTER last update
    public static SubsonicLibraryRepository getInstance(ArtistDao artistDao,
                                                        AlbumDao albumDao,
                                                        SongDao songDao,
                                                        SubsonicNetworkDataSource dataSource,
                                                        AppExecutors executors) {
        if (repositoryInstance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "created new repository instance");
                repositoryInstance = new SubsonicLibraryRepository(artistDao,
                        albumDao, songDao, dataSource, executors);
            }
        }

        Log.d(TAG, "repository instance accessed");
        return repositoryInstance;
    }

    /**
     * Grabs all artists from server, updating database if need be.
     */
    public synchronized void refreshLibrary() {
        Log.d(TAG, "refreshing library");
        // I suppose this technically triggers everything else
        dataSource.initializeLibrary();
    }

    /**
     *
     * @return list of Artists in the database, ordered by name
     */
    public LiveData<List<Artist>> getArtists() {
        Log.d(TAG, "obtaining artists from DAO");
        return artistDao.loadAll();
    }

    public LiveData<List<Album>> getAlbums() {
        Log.d(TAG, "obtaining albums from DAO");
        return albumDao.loadAll();
    }

    public LiveData<List<Song>> getSongs() {
        Log.d(TAG, "obtaining songs from DAO");
        return songDao.loadAll();
    }

    /**
     * Lookup artist by id column
     * @param id id of the artist we want
     * @return artist from the database
     */
    public LiveData<Artist> getArtistById(String id) {
        return artistDao.loadArtistById(id);
    }

    /**
     * Fetches all albums by the artist from the server
     * @param artist the artist whose albums we want
     * @return a list of all albums from the given artist
     */
    public LiveData<List<Album>> getAlbumsByArtist(Artist artist) {
        return albumDao.loadAlbumsByArtistId(artist.getId());
    }

    /**
     *
     * @param artistId id of artist for search
     * @return list of similar artists (note: currently only those present in the library)
     */
    public LiveData<List<Artist>> getSimilarArtists(String artistId) {
        Log.d(TAG, "getting similar artists");
        return dataSource.fetchSimilarArtists(artistId);
    }

    /**
     * Get cover art from the server
     * @param album Album whose cover is requested
     * @return LiveData which will be updated with image once loaded
     */
    public LiveData<Bitmap> getCoverArt(Album album, Context context) {
        Log.d(TAG, "getting cover art");
        final MutableLiveData<Bitmap> image = new MutableLiveData<>();
        Intent coverArtIntent = new Intent(context, CoverArtDownloadIntentService.class);
        coverArtIntent.putExtra(CoverArtDownloadIntentService.IMAGE_PATH_EXTRA_KEY, album.getCoverArt());
        CoverArtResultReceiver resultReceiver = new CoverArtResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == CoverArtDownloadIntentService.STATUS_SUCCESS) {
                    Bitmap coverArt = resultData.getParcelable(CoverArtDownloadIntentService.BITMAP_EXTRA_KEY);
                    image.postValue(coverArt);
                } else {
                    // TODO stock image or something
                }
            }
        };
        coverArtIntent.putExtra(CoverArtDownloadIntentService.RESULT_RECEIVER_EXTRA_KEY, resultReceiver);
        context.startService(coverArtIntent);
        return image;
    }

    public LiveData<Bitmap> getArtistImage(Artist artist, Context context) {
        final MutableLiveData<Bitmap> image = new MutableLiveData<>();
        String path = context.getExternalFilesDir(null).getAbsolutePath() + "/artist_images/" + artist.getId() + ".png";
        File imageFile = new File(path);
        if (imageFile.exists()) {
            Log.d(TAG, "loading image file: " + path);
            image.postValue(BitmapFactory.decodeFile(path));
        } else {
            Log.d(TAG, "image not found");
            image.postValue(BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.artist_not_found));
        }
        return image;
    }

    /**
     * Get all songs from an album on the server
     * @param album Album to find songs from
     * @return list of all songs, wrapped in LiveData updated once processing finishes.
     */
    public LiveData<AlbumAndAllSongs> getAlbum(Album album) {
        Log.d(TAG, "getting songs for: " + album.getName());
        return getAlbum(album.getId());
    }

    public LiveData<AlbumAndAllSongs> getAlbum(String albumId) {
        return albumDao.getAlbumWithAllSongs(albumId);
    }

    public LiveData<ArtistAndAllAlbums> getArtistAndAllAlbums(String artistId) {
        return artistDao.loadArtistAndAllAlbums(artistId);
    }

    public LiveData<String> getSongSuffix(String id) {
        return songDao.loadSuffix(id);
    }

    public LiveData<Boolean> isSongAvailableOffline(String id) {
        return songDao.isOffline(id);
    }

    /**
     * Download song and write to given File
     * @param id
     * @param toWrite
     */
    public void downloadSong(String id, File toWrite) {
        dataSource.writeTrackToFile(id, toWrite);
        songDao.setAvailableOffline(id, true);
    }

    /**
     *
     * @param id
     * @return URL from which to stream
     */
    public URL streamSong(String id) throws MalformedURLException {
        return dataSource.getStreamUrl(id);
    }

    /**
     * Loads a song as an Observable
     * @param id
     * @return
     */
    public Single<Song> getSong(String id) {
        return songDao.loadSongById(id);
    }

    // TODO replace return type in DAO with Single<String>
    public String getAlbumName(String id) {
        return albumDao.loadAlbumName(id);
    }
}
