package com.example.substandard.cover;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.substandard.database.network.SubsonicNetworkUtils;

import java.io.IOException;

/**
 * Represents cover art which exists on the server, and not on the local disk.
 */
public class SubsonicCoverArt extends CoverArt {
    private static final String TAG = SubsonicCoverArt.class.getSimpleName();
    private String path;
    @Override
    public boolean isOffline() {
        return false;
    }

    @Override
    public String getUrl() {
        return path;
    }

    public SubsonicCoverArt(String path) {
        this.path = path;
    }

    /**
     * An online cover art must be downloaded to be used in the application. Do this
     * off of the main thread, please.
     * @param context
     */
    public Bitmap download(Context context) throws IOException {
        Log.d(TAG, "downloading coverArt: " + getUrl());
        SubsonicNetworkUtils.SubsonicUser requestUser = SubsonicNetworkUtils
                .getSubsonicUserFromPreferences(context);
        Bitmap toReturn = SubsonicNetworkUtils.getCoverArt(getUrl(), requestUser);
        CacheManager.getInstance(context).addBitmapToCache(getUrl(), toReturn);
        return toReturn;
    }

}
