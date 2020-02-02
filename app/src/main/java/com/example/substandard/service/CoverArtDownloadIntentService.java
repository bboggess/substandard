package com.example.substandard.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;

import com.example.substandard.cover.CacheManager;
import com.example.substandard.cover.SubsonicCoverArt;

import java.io.IOException;

/**
 * IntentService to obtain the image. As of now, this does not necessarily mean downloading
 * from the Subsonic server. Once downloaded, the cover will be cached, and the service will
 * decide whether to get the image locally or remotely. As the user, this distinction doesn't
 * matter to you.
 *
 * To use this, you need to register a receiver. There are two required extras: a string
 * with the ID of the album art, and the result receiver. The loaded album art Bitmap will be
 * stored as an extra in the result receiver.
 */
public class CoverArtDownloadIntentService extends IntentService {
    private static final String TAG = CoverArtDownloadIntentService.class.getSimpleName();

    /**
     * Keys for obtaining arguments from the calling Intent
     */
    public static final String BITMAP_EXTRA_KEY = "bitmap";
    public static final String IMAGE_PATH_EXTRA_KEY = "path";
    public static final String RESULT_RECEIVER_EXTRA_KEY = "receiver";

    /**
     * Constants for communicating success/failure of the download with the Receiver
     */
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_FAILED = 1;

    public CoverArtDownloadIntentService() {
        super(CoverArtDownloadIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String path = intent.getStringExtra(IMAGE_PATH_EXTRA_KEY);
        ResultReceiver resultReceiver = intent.getParcelableExtra(RESULT_RECEIVER_EXTRA_KEY);

        Bundle args = new Bundle();
        // We try to load the image. If successful, place Bitmap in the Bundle and
        // send to receiver. Else, send error message.
        try {
            Bitmap albumCover;
            CacheManager cache = CacheManager.getInstance(this);
            if (cache.isBitmapInCache(path)) {
                albumCover = cache.getBitmapFromCache(path);
            } else {
                SubsonicCoverArt onlineArt = new SubsonicCoverArt(path);
                albumCover =  onlineArt.download(this);
            }
            args.putParcelable(BITMAP_EXTRA_KEY, albumCover);
            resultReceiver.send(STATUS_SUCCESS, args);
        } catch (IOException e) {
            args.putString(Intent.EXTRA_TEXT, e.toString());
            resultReceiver.send(STATUS_FAILED, args);
        }
    }
}
