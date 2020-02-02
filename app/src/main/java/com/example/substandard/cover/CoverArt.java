package com.example.substandard.cover;

import android.graphics.Bitmap;

// NOTE: this may not be needed due to shifting design
public abstract class CoverArt {
    private Bitmap image;

    public abstract boolean isOffline();
    public abstract String getUrl();

    public Bitmap getImage() {
        return image;
    }

    void setImage(Bitmap image) {
        this.image = image;
    }
}
