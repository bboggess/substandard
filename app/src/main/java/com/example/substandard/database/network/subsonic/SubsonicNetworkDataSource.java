package com.example.substandard.database.network.subsonic;


import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.substandard.AppExecutors;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;
import com.example.substandard.database.data.Song;
import com.example.substandard.database.network.NetworkRequestUtils;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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

    /*
     * live data that is updated by the data source
     */
    private final MutableLiveData<List<Artist>> artists;
    private final MutableLiveData<List<Album>> albums;
    private final MutableLiveData<List<Song>> songs;

    public MutableLiveData<List<Album>> getAlbums() {
        return albums;
    }

    public MutableLiveData<List<Song>> getSongs() {
        return songs;
    }

    private SubsonicUser user;


    private SubsonicNetworkDataSource(Context context) {
        this.context = context;
        this.artists = new MutableLiveData<>();
        this.albums = new MutableLiveData<>();
        this.songs = new MutableLiveData<>();
        refreshUser();
    }

    /**
     * Read the user from the shared preferences file
     */
    public void refreshUser() {
        user = SubsonicNetworkUtils.getSubsonicUserFromPreferences(context);
    }

    /**
     * This class uses the singleton design pattern. Use this method to access this class.
     * @param context A Context for getting user preferences, etc.
     * @return our data source if already exists, otherwise creates new one
     */
    public static SubsonicNetworkDataSource getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                Log.d(TAG, "created new network data source");
                instance = new SubsonicNetworkDataSource(context.getApplicationContext());
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

    /**
     * I am hoping to abstract out the repetitive bullshit of setting up tasks on the network
     * thread in every method in this class. Still a WIP
     */
    private enum Task {
        FETCH_ARTISTS_TASK,
        FETCH_ALBUMS_FOR_ARTIST_TASK,
        FETCH_SIMILAR_ARTISTS_TASK
    }

    /**
     * This is a WIP for abstracting out logic involved in making REST requests. I will probably
     * never get to this.
     */
    private static class NetworkTask {
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

    /* Also a WIP -- not used currently */
    private void scheduleNetworkTask(final MutableLiveData<Object> data, final NetworkTask networkTask) {
        AppExecutors.getInstance().networkIo().execute(new Runnable() {
            @Override
            public void run() {
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
        AppExecutors.getInstance().networkIo().execute(() -> {
            Log.d(TAG, "fetching artists");
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
        });
    }

    /**
     * Writes an image to the disk to use as the artist's thumbnail
     * @param image bitmap to  write
     * @param artist artist whose id we want to associate image with
     * @throws IOException issue creating file/writing to disk
//     */
    private void writeArtistImageToDisk(Bitmap image, Artist artist) throws IOException {
        File cacheDir = new File(context.getExternalFilesDir(null), "artist_images/");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        String filename = cacheDir.getAbsolutePath() + "/" + artist.getId() + ".png";
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream stream = new FileOutputStream(file);
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
    }

    /**
     * Tries to download artist image. If there is no artist image, will use album cover of track as a
     * backup.
     *
     * @param artist whose image we would like to find
     * @param topSong The top song by artist, for fetching album art
     * @param user user for authenticating requests
     * @throws IOException if cannot write image to disk for some reason
     */
    private void downloadArtistImage(Artist artist, Song topSong, SubsonicUser user) throws IOException {
        Bitmap image;
        try {
            image = NetworkRequestUtils.getBitmapFromURL(new URL(artist.getImageUrl()));
        } catch (MalformedURLException e) {
            image = SubsonicNetworkUtils.getCoverArt(topSong, user);
        }

        // I guess sometimes I have neither in the library? Seem bad
        if (image == null) {
            Log.d(TAG, "downloadArtistImage: no bitmap for " + artist.getName() + topSong.getTitle());
            return;
        }

        writeArtistImageToDisk(image, artist);
    }

    /**
     * Tries to download artist image, and will use album cover if cannot find. This is needed
     * because some artists have no top tracks, but I also cannot just pass in an ID string because
     * the rest API is so overloaded with IDs meaning different things that it doesn't know what
     * I want and gives me garbage. I am probably partly to blame.
     *
     * @param artist whose image we want to download
     * @param backup if no artist image is found, use cover of this album
     * @param user for authenticating requests
     * @throws IOException if unable to write image to disk
     */
    private void downloadArtistImage(Artist artist, Album backup, SubsonicUser user) throws
            IOException, JSONException {
        Bitmap image;
        try {
            String url = SubsonicNetworkUtils.getArtistThumbnail(artist, user);
            image = NetworkRequestUtils.getBitmapFromURL(new URL(url));
        } catch (MalformedURLException e) {
            image = SubsonicNetworkUtils.getCoverArt(backup, user);
        }

        // I guess sometimes I have neither in the library? Seem bad
        if (image == null) {
            Log.d(TAG, "downloadArtistImage: no bitmap for " + artist.getName() + backup.getName());
            return;
        }

        writeArtistImageToDisk(image, artist);
    }

    /**
     * This does all of the work to initialize the library. Fetches all artists, and then
     * for each artist gets all of their albums, and for each album all of their songs. These
     * are posted to the LiveData as they are obtained.
     *
     * This is Subsonic's fault for not having an option to get all albums or all songs.
     * I've just learned this option exists. Fuck. I ought to revisit everything about this.
     */
    public void initializeLibrary() {
        AppExecutors.getInstance().networkIo().execute(() -> {
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

                    // In a long line of inconsistencies, some artists don't even have top tracks
                    Song topTrack = SubsonicNetworkUtils.getTopSong(artist, user);
                    if (null != topTrack) {
                        downloadArtistImage(artist, topTrack, user);
                    } else {
                        downloadArtistImage(artist, albumsByArtist.get(0), user);
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
        });
    }

    /**
     * Grabs all albums by the given artist present on the server
     * @param artist artist whose albums we want
     * @return List of albums (as live data because background threads)
     */
    public LiveData<List<Album>> fetchAlbumsForArtist(final Artist artist) {
        final MutableLiveData<List<Album>> albums = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(() -> {
            Log.d(TAG, "fetching albums by " + artist.getName());
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
        });
        return albums;
    }

    /**
     * Grabs list of similar artists from the Subsonic server
     * @param artistId id of artist whose similar artists to retrieve
     * @return live data that will be updated with a list of similar artists
     */
    public LiveData<List<Artist>> fetchSimilarArtists(final String artistId) {
        final MutableLiveData<List<Artist>> similarArtists = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(() -> {
            Log.d(TAG, "fetching similar artists");
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
        });
        return similarArtists;
    }

    /**
     * Download album art from the Subsonic server
     * @param album album whose cover we want
     * @return LiveData which will be updated with a Bitmap of the cover art
     */
    public LiveData<Bitmap> fetchCoverArt(final Album album) {
        final MutableLiveData<Bitmap> coverArt = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(() -> {
            Log.d(TAG, "fetching cover art for album: " + album.getName());

            try {
                Bitmap downloadedArt = SubsonicNetworkUtils.getCoverArt(album, user);
                coverArt.postValue(downloadedArt);
            } catch (IOException e) {
                Log.d(TAG, "fetchCoverArt: failed to get image");
                e.printStackTrace();
            }
        });

        return coverArt;
    }

    /**
     * Gets all songs present on an album
     * @param albumId album to fetch
     * @return LiveData updated with list of all songs on the album
     */
    public LiveData<List<Song>> fetchAlbum(final String albumId) {
        final MutableLiveData<List<Song>> songs = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(() -> {
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
        });

        return songs;
    }

    /**
     * Fetches all songs on the album. See
     * @param album album whose tracks to get
     * @return list of tracks on album (wrapped in live data)
     */
    public LiveData<List<Song>> fetchAlbum(final Album album) {
        Log.d(TAG, "fetching songs from album: " + album.getName());
        return fetchAlbum(album.getId());
    }

    /**
     * Check whether the login credentials are valid
     * @param user check whether this is a valid Subsonic user
     * @return true if valid, false if not
     */
    public LiveData<Boolean> authenticateUser(final SubsonicUser user) {
        final MutableLiveData<Boolean> isSuccessful = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(() -> {
            boolean result = SubsonicNetworkUtils.authenticate(user);
            isSuccessful.postValue(result);
        });

        return isSuccessful;
    }

    /**
     * Downloads the track and writes to given file on a background thread
     * @param id the id of the track to download
     * @param toWrite file to write the track to
     */
    public void writeTrackToFile(final String id, final File toWrite) {
        AppExecutors.getInstance().networkIo().execute(() -> {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            try {
                SubsonicNetworkUtils.downloadSong(id, toWrite, downloadManager, user);
            } catch (MalformedURLException e) {
                Log.d(TAG, "failed to download file: " + id);
                e.printStackTrace();
            }
        });
    }

    /**
     * Fetches the URL at which a song can be streamed
     * @param songId id of song to stream
     * @return URL that can be directly streamed from
     * @throws MalformedURLException if unable to form valid URL from song id
     */
    public URL getStreamUrl(String songId) throws MalformedURLException{
        return SubsonicNetworkUtils.getStream(songId, user);
    }

    /**
     * Downloads a list of five albums from the Subsonic server, according to a specified category
     * @param type The type of list to get. Options are recently played albums, newest albums, most
     *             played albums, and random albums.
     * @return LiveData to be updated with IDs of the albums given from the server
     */
    public LiveData<List<String>> fetchAlbumList(SubsonicNetworkRequest.AlbumListType type) {
        MutableLiveData<List<String>> albumList = new MutableLiveData<>();
        AppExecutors.getInstance().networkIo().execute(() -> {
            try {
                List<String> ids = SubsonicNetworkUtils.getAlbumList(type, user);
                albumList.postValue(ids);
            } catch (IOException e) {
                Log.d(TAG, "fetchAlbumList: failed to fetch album list");
                e.printStackTrace();
            } catch (JSONException e) {
                Log.d(TAG, "fetchAlbumList: malformed JSON from server request. "
                        + "Did you pass in a valid address?");
                e.printStackTrace();
            }
        });

        return albumList;
    }

}
