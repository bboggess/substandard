package com.example.substandard.database.network;


import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.substandard.AppExecutors;
import com.example.substandard.database.data.Album;
import com.example.substandard.database.data.Artist;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

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
    private final AppExecutors executors;

    private SubsonicNetworkDataSource(Context context, AppExecutors executors) {
        this.context = context;
        this.executors = executors;
        this.artists = new MutableLiveData<>();
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
}
