package com.example.substandard.cover;

import android.graphics.Bitmap;

public abstract class CoverArt {
    private Bitmap image;

    public abstract boolean isOffline();
    public abstract String getUrl();

    public Bitmap getImage() {
        return image;
    }
}
