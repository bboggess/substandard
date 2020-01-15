package com.example.substandard.database;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;

import com.example.substandard.database.data.Song;
import com.example.substandard.utility.InjectorUtils;

import java.io.File;

/**
 * Handles all interfacing between the database and local files. This allows the music player to
 * be completely ignorant of the distinction between online and local files. You simply ask
 * LocalMusicLibrary for a song (by giving the ID) and it does whatever needs to be done to get
 * the file to you.
 *
 * Call getInstance to use this class.
 */
public class LocalMusicLibrary {

    private static final String SONG_DIR = "song/";
    private static final String COVER_DIR = "cover/";

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

    private String getFileExtension(String id) {
        return repository.getSongSuffix(id);
    }

    private File getSongStorageDir() {
        return new File(context.getFilesDir(), SONG_DIR);
    }

    private File getCoverArtStorageDir() {
        return new File(context.getFilesDir(), COVER_DIR);
    }

    private void addSongToLocalLibrary(String id) {
        repository.downloadSong(id, getSongFile(id));
    }

    public File getSongFile(String id) {
        return new File(getSongStorageDir(), id + getFileExtension(id));
    }

    public String getSongFilename(String id) {
        return getSongFile(id).getAbsolutePath();
    }

    public MediaMetadataCompat getMetadata(String id, MediaMetadataCompat.Builder builder) {
        Song song = repository.getSong(id);
        String albumName = repository.getAlbumName(song.getAlbumId());
        return builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtistName())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, albumName)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, song.getGenre())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .build();
    }
}
