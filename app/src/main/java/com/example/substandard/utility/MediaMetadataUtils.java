package com.example.substandard.utility;

import android.media.MediaMetadata;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;

import com.example.substandard.database.data.Song;

public class MediaMetadataUtils {
    private static final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
    private static final MediaDescriptionCompat.Builder descriptionBuilder = new MediaDescriptionCompat.Builder();

    public static MediaMetadataCompat convertSongToMediaMetadata(Song song) {
        // TODO Song needs a more convenient way of keeping track of album name
        return metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.getId())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtistName())
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, song.getGenre())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle())
                .putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, song.getTrackNum())
                .build();
    }

    public static MediaDescriptionCompat convertSongToMediaDescription(Song song) {
        return descriptionBuilder.setMediaId(song.getId())
                .build();
    }
}
