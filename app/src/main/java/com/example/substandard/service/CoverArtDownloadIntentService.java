package com.example.substandard.service;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;

import com.example.substandard.database.network.SubsonicNetworkUtils;

import java.io.IOException;

/**
 * IntentService to download album cover from the server. You must pass in a CoverArtResultReceiver
 * and the path used to request the album cover to the Intent. E.g. call getAlbumCover() on your
 * Album object and send that to the Intent.
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
        SubsonicNetworkUtils.SubsonicUser requestUser = SubsonicNetworkUtils
                .getSubsonicUserFromPreferences(getApplicationContext());

        Bundle args = new Bundle();
        // We try to download the image. If successful, place Bitmap in the Bundle and
        // send to receiver. Else, send error message.
        try {
            Bitmap albumCover = SubsonicNetworkUtils.getCoverArt(path, requestUser);
            args.putParcelable(BITMAP_EXTRA_KEY, albumCover);
            resultReceiver.send(STATUS_SUCCESS, args);
        } catch (IOException e) {
            args.putString(Intent.EXTRA_TEXT, e.toString());
            resultReceiver.send(STATUS_FAILED, args);
        }
    }
}
