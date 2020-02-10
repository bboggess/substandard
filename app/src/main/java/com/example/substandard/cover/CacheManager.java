package com.example.substandard.cover;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import java.io.File;
import java.io.IOException;

/**
 * Exposes the BitmapDiskCache to the rest of the application. Should be used off
 * of the main thread, as everything (even constructor) can involve disk operations.
 * Stores a small subset of bitmaps in a memory cache, allowing faster access to
 * frequently requested images.
 */
public class CacheManager {
    private static final String TAG = CacheManager.class.getSimpleName();

    private BitmapDiskCache diskCache;
    private static final int MAX_CACHE_SIZE = 1024 * 1024 * 10;
    private static final String CACHE_DIR = "covers/";

    private LruCache<String, Bitmap> memoryCache;

    private static final Object LOCK = new Object();
    private static CacheManager instance;

    private CacheManager(Context context) throws IOException {
        initMemoryCache();
        diskCache = BitmapDiskCache.getCache(getCacheDir(context), MAX_CACHE_SIZE);
    }

    private int getMaxCacheSize() {
        // Gets the max amount of memory available to our app (in kilobytes)
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        return maxMemory / 10;
    }

    private void initMemoryCache() {
        memoryCache = new LruCache<String, Bitmap>(getMaxCacheSize()) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
                // keeps track of cache size in kilobytes, not number of items
                return value.getByteCount() / 1024;
            }
        };
    }

    public static CacheManager getInstance(Context context) throws IOException {
        Log.d(TAG, "accessing cache manager");
        synchronized (LOCK) {
            if (null == instance) {
                Log.d(TAG, "creating new cache manager");
                instance = new CacheManager(context);
            }
        }

        return instance;
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    private void addBitmapToMemCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    private File getCacheDir(Context context) {
        return new File(context.getFilesDir(), CACHE_DIR);
    }

    /**
     * Stores the image in the cache
     * @param key
     * @param image
     * @throws IOException
     */
    public void addBitmapToCache(String key, Bitmap image) throws IOException {
        Log.d(TAG, "adding bitmap to cache: " + key);
        addBitmapToMemCache(key, image);
        if (null != diskCache && !diskCache.contains(key)) {
            diskCache.put(key, image);
        }
    }

    /**
     * Checks whether the image is stored in the disk cache
     * @param key
     * @return
     */
    public boolean isBitmapInCache(String key) {
        return null != diskCache && diskCache.contains(key);
    }

    /**
     * Retrieves the image from the cache
     * @param key
     * @return
     * @throws IOException
     */
    public Bitmap getBitmapFromCache(String key) throws IOException {
        Log.d(TAG, "retrieving bitmap from cache: " + key);
        // check the mem cache first
        Bitmap toReturn = getBitmapFromMemCache(key);
        if (null == toReturn && null != diskCache && diskCache.contains(key)) {
            toReturn = diskCache.get(key);
        }

        return toReturn;
    }
}
