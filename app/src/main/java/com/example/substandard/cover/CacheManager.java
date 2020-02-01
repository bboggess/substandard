package com.example.substandard.cover;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Exposes the BitmapDiskCache to the rest of the application. Should be used off
 * of the main thread.
 */
public class CacheManager {
    private static final String TAG = CacheManager.class.getSimpleName();

    private BitmapDiskCache diskCache;
    private static final int MAX_CACHE_SIZE = 1024 * 1024 * 10;
    private static final String CACHE_DIR = "covers";

    private static CacheManager instance;

    private CacheManager(Context context) throws IOException {
        diskCache = BitmapDiskCache.getCache(getCacheDir(context), MAX_CACHE_SIZE);
    }

    public synchronized static CacheManager getInstance(Context context) throws IOException {
        Log.d(TAG, "accessing cache manager");
        if (null == instance) {
            Log.d(TAG, "creating new cache manager");
            instance = new CacheManager(context);
        }

        return instance;
    }

    private File getCacheDir(Context context) {
        return new File(context.getCacheDir(), CACHE_DIR);
    }

    public void addBitmapToCache(String key, Bitmap image) throws IOException {
        Log.d(TAG, "adding bitmap to cache: " + key);
        if (null != diskCache && !diskCache.contains(key)) {
            diskCache.put(key, image);
        }
    }

    public boolean isBitmapInCache(String key) {
        return null != diskCache && diskCache.contains(key);
    }

    public Bitmap getBitmapFromCache(String key) throws IOException {
        Log.d(TAG, "retrieving bitmap from cache: " + key);
        Bitmap toReturn = null;
        if (null != diskCache && diskCache.contains(key)) {
            toReturn = diskCache.get(key);
        }

        return toReturn;
    }
}
