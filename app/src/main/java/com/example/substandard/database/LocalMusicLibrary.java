package com.example.substandard.database;

import android.content.Context;
import android.util.Log;

import com.example.substandard.database.data.Song;
import com.example.substandard.utility.InjectorUtils;

import java.io.File;
import java.net.URL;

import io.reactivex.Scheduler;
import io.reactivex.SingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Handles all interfacing between the database and local files. This allows the music player to
 * be completely ignorant of the distinction between online and local files. You simply ask
 * LocalMusicLibrary for a song (by giving the ID) and it does whatever needs to be done to get
 * the file to you. (this is not yet implemented though)
 *
 * Call getInstance to use this class.
 */
public class LocalMusicLibrary {
    private static final String TAG = LocalMusicLibrary.class.getSimpleName();

    private static final String COVER_DIR = "cover/";

    private static final String ROOT = "root";

    private static final Object LOCK = new Object();

    private final SubsonicLibraryRepository repository;
    private final Context context;
    private static LocalMusicLibrary instance;

    private LocalMusicLibrary(Context context) {
        this.context = context.getApplicationContext();
        repository = InjectorUtils.provideLibraryRepository(context);
    }

    public static LocalMusicLibrary getInstance(Context context) {
        if (null == instance) {
            synchronized (LOCK) {
                instance = new LocalMusicLibrary(context);
            }
        }

        return instance;
    }

    private File getCoverArtStorageDir() {
        return new File(context.getFilesDir(), COVER_DIR);
    }

    public String getRoot() {
        return ROOT;
    }

    public URL getStream(String id) {
        return repository.streamSong(id);
    }

    public void processSongRequest(SongLoadRequest request) {
        Log.d(TAG, "processSongRequest: " + request.getSongId());
        Scheduler ioScheduler = Schedulers.io();
        repository.getSong(request.getSongId()).subscribeOn(ioScheduler).subscribe(request);
    }


    /**
     * All requests to load a song from the library must implement this interface.
     * getSongId() should return the id of the requested song, and the Observer methods will
     * be fired when the song is loaded.
     */
    public interface SongLoadRequest extends SingleObserver<Song> {
        String getSongId();
    }

}
