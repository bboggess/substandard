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

import com.example.substandard.AppExecutors;
import com.example.substandard.R;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.AlbumAndAllSongs;
import com.example.substandard.database.data.AlbumAndArtist;
import com.example.substandard.database.data.AlbumDao;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.ArtistAndAllAlbums;
import com.example.substandard.database.data.ArtistDao;
import com.example.substandard.database.data.Song;
import com.example.substandard.database.data.SongDao;
import com.example.substandard.database.network.subsonic.SubsonicNetworkDataSource;
import com.example.substandard.database.network.subsonic.SubsonicNetworkRequest;
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

    private SubsonicLibraryRepository(final ArtistDao artistDao,
                                      final AlbumDao albumDao,
                                      final SongDao songDao,
                                      final SubsonicNetworkDataSource dataSource,
                                      final AppExecutors executors) {
        this.artistDao = artistDao;
        this.albumDao = albumDao;
        this.songDao = songDao;
        this.dataSource = dataSource;
        // Only used to schedule database operations off of main thread

        // sets live data in the data source to update database when changed
        dataSource.getArtists().observeForever(artists -> {
            Log.d(TAG, "Artist database updated");
            executors.diskIO().execute(() -> {
                artistDao.insertAll(artists);
                Log.d(TAG, "artists written to DAO");
            });
        });

        dataSource.getAlbums().observeForever(albums -> {
            Log.d(TAG, "Album database updated");
            executors.diskIO().execute(() -> albumDao.insertAll(albums));
        });

        dataSource.getSongs().observeForever(songs -> {
            Log.d(TAG, "Song database updated");
            executors.diskIO().execute(() -> {
                for (Song song : songs) {
                    Log.d(TAG, "song id: " + song.getId()
                        + ", album id: " + song.getAlbumId()
                        + ", artist: " + song.getArtistName());
                }
                songDao.insertAll(songs);
            });
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
        dataSource.refreshUser();
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

    /**
     *
     * @return all albums present in the database, ordered by name
     */
    public LiveData<List<Album>> getAlbums() {
        Log.d(TAG, "obtaining albums from DAO");

        return albumDao.loadAll();
    }

    /**
     *
     * @return all songs present in the database, ordered by name (?)
     */
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

    /**
     * Tries to get the image associated to the artist.
     * @param artist whose image we want
     * @param context for accessing the internal storage
     * @return either a livedata wrapped around the artist's image, or null if there is an issue
     *         accessing the internal storage
     */
    public LiveData<Bitmap> getArtistImage(Artist artist, Context context) {
        final MutableLiveData<Bitmap> image = new MutableLiveData<>();
        File externalDir = context.getExternalFilesDir(null);
        if (null == externalDir) {
            return null;
        }

        String path = externalDir.getAbsolutePath() + "/artist_images/" + artist.getId() + ".png";
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

    /**
     *
     * @param albumId id of album to search for
     * @return album along with all songs on the album
     */
    public LiveData<AlbumAndAllSongs> getAlbum(String albumId) {
        return albumDao.getAlbumWithAllSongs(albumId);
    }

    /**
     * Queries the database for album objects
     * @param albumIds list of all albums IDs to search for
     * @return LiveData wrapping album objects with given IDs
     */
    public LiveData<List<Album>> getAlbums(List<String> albumIds) {
        return albumDao.loadAlbumsById(albumIds);
    }

    /**
     * Queries database for album and corresponding artist
     * @param albumId id of album to search for
     * @return album and the album artist
     */
    public LiveData<AlbumAndArtist> getAlbumAndArtist(String albumId) {
        return albumDao.getAlbumWithArtist(albumId);
    }

    /**
     * Same as getAlbumAndArtist, but can search for multiple IDs at once
     */
    public LiveData<List<AlbumAndArtist>> getAlbumsWithArtist(List<String> albumIds) {
        return albumDao.getAlbumsWithArtist(albumIds);
    }

    /**
     * Queries database for artist and all albums by the artist
     * @param artistId artist to search for
     * @return query result
     */
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
     * @param id song to download
     * @param toWrite file to write to if download successful
     */
    public void downloadSong(String id, File toWrite) {
        dataSource.writeTrackToFile(id, toWrite);
        songDao.setAvailableOffline(id, true);
    }

    /**
     * Get URL from which one can directly stream
     * @param id song to stream
     * @return URL from which to stream
     */
    public URL streamSong(String id) throws MalformedURLException {
        return dataSource.getStreamUrl(id);
    }

    /**
     * Query the server for newest albums
     * @return list of IDs of albums
     */
    public LiveData<List<String>> getNewestAlbums() {
        return dataSource.fetchAlbumList(SubsonicNetworkRequest.AlbumListType.NEWEST);
    }

    /**
     * Query server for random albums
     * @return list of IDs of albums
     */
    public LiveData<List<String>> getRandomAlbums() {
        return dataSource.fetchAlbumList(SubsonicNetworkRequest.AlbumListType.RANDOM);
    }

    /**
     * Query server for most frequently played albums
     * @return list of IDs of albums
     */
    public LiveData<List<String>> getFavoriteAlbums() {
        return dataSource.fetchAlbumList(SubsonicNetworkRequest.AlbumListType.FREQUENT);
    }

    /**
     * Query server for most recently played albums
     * @return list of IDs of albums
     */
    public LiveData<List<String>> getRecentAlbums() {
        return dataSource.fetchAlbumList(SubsonicNetworkRequest.AlbumListType.RECENT);
    }

    /**
     * Loads a song as an Observable
     * @param id song to search for
     * @return observable which will contain song if found in database
     */
    public Single<Song> getSong(String id) {
        return songDao.loadSongById(id);
    }

    // TODO replace return type in DAO with Single<String> (???)
    public String getAlbumName(String id) {
        return albumDao.loadAlbumName(id);
    }
}
