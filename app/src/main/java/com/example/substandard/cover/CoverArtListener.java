package com.example.substandard.cover;

import android.graphics.Bitmap;

/**
 * An interface for UI components which need to load and display
 * album art. The cover art will be loaded in a background thread, and
 * onCoverLoad is called when that task is finished, delivering the cover
 * image.
 */
public interface CoverArtListener {
    /**
     * Called when the cover art is finished loading.
     * @param coverArtImage The fully loaded cover art image.
     */
    void onCoverLoad(Bitmap coverArtImage);
}
