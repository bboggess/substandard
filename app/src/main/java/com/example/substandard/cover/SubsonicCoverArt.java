package com.example.substandard.cover;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.substandard.service.CoverArtDownloadIntentService;
import com.example.substandard.service.CoverArtResultReceiver;

/**
 * Represents cover art which exists on the server, and not on the local disk.
 */
public class SubsonicCoverArt extends CoverArt implements CoverArtResultReceiver.CoverArtReceiver {
    @Override
    public boolean isOffline() {
        return false;
    }

    @Override
    public String getUrl() {
        return null;
    }

    /**
     * An online cover art must be downloaded to be used in the application.
     * @param context
     */
    public void download(Context context) {
        CoverArtResultReceiver resultReceiver = new CoverArtResultReceiver(new Handler());
        resultReceiver.setReceiver(this);

        Intent coverArtIntent = new Intent(context, CoverArtDownloadIntentService.class);
        coverArtIntent.putExtra(CoverArtDownloadIntentService.IMAGE_PATH_EXTRA_KEY, getUrl());
        coverArtIntent.putExtra(CoverArtDownloadIntentService.RESULT_RECEIVER_EXTRA_KEY, resultReceiver);
        context.startService(coverArtIntent);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == CoverArtDownloadIntentService.STATUS_SUCCESS) {
            setImage(resultData.getParcelable(CoverArtDownloadIntentService.BITMAP_EXTRA_KEY));
            // TODO save to disk and register with cache manager
        }
    }
}
