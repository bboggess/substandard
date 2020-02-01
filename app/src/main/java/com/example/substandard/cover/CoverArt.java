package com.example.substandard.cover;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public abstract class CoverArt {
    private MutableLiveData<Bitmap> image;

    public abstract boolean isOffline();
    public abstract String getUrl();

    public LiveData<Bitmap> getImage() {
        return image;
    }

    void setImage(Bitmap image) {
        this.image.postValue(image);
    }
}
