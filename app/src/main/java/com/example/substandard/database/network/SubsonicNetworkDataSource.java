package com.example.substandard.database.network;


import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.substandard.AppExecutors;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.Song;

import org.json.JSONException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class for connecting database methods with network methods.
 * Uses singleton instantiation pattern -- use getInstance !
 */
public class SubsonicNetworkDataSource {
    private static final String TAG = SubsonicNetworkDataSource.class.getSimpleName();

    // Singleton instantiation
    private final static Object LOCK = new Object();
    private static SubsonicNetworkDataSource instance;
    private final Context context;

    private final MutableLiveData<List<Artist>> artists;
    private final MutableLiveData<List<Album>> albums;
    private final MutableLiveData<List<Song>> songs;

    public MutableLiveData<List<Album>> getAlbums() {
        return albums;
    }

    public MutableLiveData<List<Song>> getSongs() {
        return songs;
    }

    private final AppExecutors executors;

    private SubsonicNetworkDataSource(Context context, AppExecutors executors) {
        this.context = context;
        this.executors = executors;
        this.artists = new MutableLiveData<>();
        this.albums = new MutableLiveData<>();
        this.songs = new MutableLiveData<>();
    }

    /**
     *
     * @param context A Context for getting user preferences, etc.
     * @param executors Executors for running tasks off of main thread
     * @return our data source if already exists, otherwise creates new one
     */
    public static SubsonicNetworkDataSource getInstance(Context context, AppExecutors executors) {
        if (instance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "created new network data source");
                instance = new SubsonicNetworkDataSource(context.getApplicationContext(), executors);
            }
        }
        return instance;
    }

    /**
     *
     * @return the LiveData from the data source
     */
    public LiveData<List<Artist>> getArtists() {
        return artists;
    }

    private enum Task {
        FETCH_ARTISTS_TASK,
        FETCH_ALBUMS_FOR_ARTIST_TASK,
        FETCH_SIMILAR_ARTISTS_TASK
    }

    private class NetworkTask {
        static final int ARTIST_ID_KEY = 1;
        private Task task;
        private Map<Integer, String> additionalParams;

        NetworkTask(Task task) {
            this(task, Collections.EMPTY_MAP);
        }

        NetworkTask(Task task, Map<Integer, String> additionalParams) {
            this.task = task;
            this.additionalParams = additionalParams;
        }

        void putExtraInt(int key, String value) {
            additionalParams.put(key, value);
        }

        String getExtraInt(int key) {
            return additionalParams.get(key);
        }
    }

    private void scheduleNetworkTask(final MutableLiveData<Object> data, final NetworkTask networkTask) {
        AppExecutors.getInstance().networkIo().execute(new Runnable() {
            @Override
            public void run() {
                SubsonicNetworkUtils.SubsonicUser user = SubsonicNetworkUtils.
                        getSubsonicUserFromPreferences(context);
                if (user.getServerAddress() == null || user.getServerAddress().equals("")) {
                    Log.d(TAG, "scheduleNetworkTask: no server found");
                    return;
                }

                try {
                    switch (networkTask.task) {
                        case FETCH_ARTISTS_TASK:
                            data.postValue(SubsonicNetworkUtils.getAllArtists(user));
                            break;
                        case FETCH_ALBUMS_FOR_ARTIST_TASK:
                            String artistId = networkTask.getExtraInt(NetworkTask.ARTIST_ID_KEY);
                            data.postValue(SubsonicNetworkUtils.getArtistAlbums(artistId, user));
                            break;
                        case FETCH_SIMILAR_ARTISTS_TASK:
                            artistId = networkTask.getExtraInt(NetworkTask.ARTIST_ID_KEY);
                            data.postValue(SubsonicNetworkUtils.getSimilarArtists(artistId, user));
                    }
                } catch (IOException e) {
                    Log.d(TAG, "scheduleNetworkTask: I/O error on server request");
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.d(TAG, "scheduleNetworkTask: malformed JSON from server request." +
                            "Did you pass correct address?");
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Fetches artists from the server and tries to update the LiveData
     */
    public void fetchArtists() {
        AppExecutors.getInstance().networkIo().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "fetching artists");
                SubsonicNetworkUtils.SubsonicUser user = SubsonicNetworkUtils
                        .getSubsonicUserFromPreferences(context);
                // Make sure we have a user present in preferences. Hacky
                if (user.getServerAddress() == null || user.getServerAddress().equals("")) {
                    Log.d(TAG, "fetchArtists: no server address found");
                    return;
                }

                try {
                    List<Artist> downloadedArtistList = SubsonicNetworkUtils.getAllArtists(user);
                    artists.postValue(downloadedArtistList);
                } catch (IOException e) {
                    Log.d(TAG, "fetchArtists: I/O error on server request");
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.d(TAG, "fetchArtists: malformed JSON from server request." +
                            "Did you pass correct address?");
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * This does all of the work to initialize the library. Fetches all artists, and then
     * for each artist gets all of their albums, and for each album all of their songs. These
     * are posted to the LiveData as they are obtained.
     *
     * This is Subsonic's fault for not having an option to get all albums or all songs.
     */
    public void initializeLibrary() {
        AppExecutors.getInstance().networkIo().execute(new Runnable() {
            @Override
            public void run() {
                SubsonicNetworkUtils.SubsonicUser user = SubsonicNetworkUtils
                        .getSubsonicUserFromPreferences(context);
                // Make sure we have a user present in preferences. Hacky
                if (user.getServerAddress() == null || user.getServerAddress().equals("")) {
                    Log.d(TAG, "fetchArtists: no server address found");
                    return;
                }

                // God forgive me for this
                try {
                    List<Artist> downloadedArtistList = SubsonicNetworkUtils.getAllArtists(user);
                    artists.postValue(downloadedArtistList);
                    for (Artist artist : downloadedArtistList) {
                        List<Album> albumsByArtist = SubsonicNetworkUtils.getArtistAlbums(artist.getId(), user);
                        albums.postValue(albumsByArtist);
                        for (Album album : albumsByArtist) {
                            songs.postValue(SubsonicNetworkUtils.getAlbum(album.getId(), user));
                        }
                    }
                } catch (IOException e) {
                    Log.d(TAG, "fetchArtists: I/O error on server request");
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.d(TAG, "fetchArtists: malformed JSON from server request." +
                            "Did you pass correct address?");
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Grabs all albums by the given artist present on the server
     * @param artist artist whose albums we want
     * @return List of albums (as live data because background threads)
     */
    public LiveData<List<Album>> fetchAlbumsForArtist(final Artist artist) {
        final MutableLiveData<List<Album>> albums = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "fetching albums by " + artist.getName());
                SubsonicNetworkUtils.SubsonicUser user = SubsonicNetworkUtils
                        .getSubsonicUserFromPreferences(context);
                try {
                    List<Album> albumsByArtist = SubsonicNetworkUtils.getArtistAlbums(artist.getId(), user);
                    albums.postValue(albumsByArtist);
                } catch (IOException e) {
                    Log.d(TAG, "fetchArtists: I/O error on server request");
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.d(TAG, "fetchArtists: malformed JSON from server request." +
                            "Did you pass correct address?");
                    e.printStackTrace();
                }
            }
        });
        return albums;
    }

    public LiveData<List<Artist>> fetchSimilarArtists(final String artistId) {
        final MutableLiveData<List<Artist>> similarArtists = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "fetching similar artists");
                SubsonicNetworkUtils.SubsonicUser user = SubsonicNetworkUtils
                        .getSubsonicUserFromPreferences(context);
                try {
                    List<Artist> artists = SubsonicNetworkUtils.getSimilarArtists(artistId, user);
                    similarArtists.postValue(artists);
                } catch (IOException e) {
                    Log.d(TAG, "fetchArtists: I/O error on server request");
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.d(TAG, "fetchArtists: malformed JSON from server request. " +
                            "Did you pass correct address?");
                    e.printStackTrace();
                }
            }
        });
        return similarArtists;
    }

    public LiveData<Bitmap> fetchCoverArt(final Album album) {
        final MutableLiveData<Bitmap> coverArt = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "fetching cover art for album: " + album.getName());
                SubsonicNetworkUtils.SubsonicUser user = SubsonicNetworkUtils
                        .getSubsonicUserFromPreferences(context);
                try {
                    Bitmap downloadedArt = SubsonicNetworkUtils.getCoverArt(album, user);
                    coverArt.postValue(downloadedArt);
                } catch (IOException e) {
                    Log.d(TAG, "fetchCoverArt: failed to get image");
                    e.printStackTrace();
                }
            }
        });

        return coverArt;
    }

    public LiveData<List<Song>> fetchAlbum(final String albumId) {
        final MutableLiveData<List<Song>> songs = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(new Runnable() {
            @Override
            public void run() {
                SubsonicNetworkUtils.SubsonicUser user = SubsonicNetworkUtils
                        .getSubsonicUserFromPreferences(context);

                try {
                    List<Song> downloadedSongs = SubsonicNetworkUtils.getAlbum(albumId, user);
                    songs.postValue(downloadedSongs);
                } catch (IOException e) {
                    Log.d(TAG, "fetchAlbum: failed to album");
                    e.printStackTrace();
                } catch (JSONException e) {
                    Log.d(TAG, "fetchAlbum: malformed JSON from server request. "
                        + "Did you pass in a valid address?");
                    e.printStackTrace();
                }
            }
        });

        return songs;
    }

    public LiveData<List<Song>> fetchAlbum(final Album album) {
        Log.d(TAG, "fetching songs from album: " + album.getName());
        return fetchAlbum(album.getId());
    }

    public LiveData<Boolean> authenticateUser(final SubsonicNetworkUtils.SubsonicUser user) {
        final MutableLiveData<Boolean> isSuccessful = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(new Runnable() {
            @Override
            public void run() {
                boolean result = SubsonicNetworkUtils.authenticate(user);
                isSuccessful.postValue(result);
            }
        });

        return isSuccessful;
    }

}
