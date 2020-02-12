package com.example.substandard.service.download;

import android.content.Context;

import com.example.substandard.R;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class DownloadRepository {
    private final Cache audioDownloadCache;
    private static final String DOWNLOAD_DIRECTORY = "songs/";
    private final Context context;
    private final DownloadManager downloadManager;

    private static DownloadRepository instance;
    private static final Object LOCK = new Object();

    private DownloadRepository(Context context) {
        this.context = context.getApplicationContext();
        ExoDatabaseProvider databaseProvider = new ExoDatabaseProvider(context);
        audioDownloadCache = new SimpleCache(getAudioDownloadDirectory(), new NoOpCacheEvictor(), databaseProvider);
        downloadManager = createDownloadManager();
    }

    public static DownloadRepository getInstance(Context context) {
        if (null == instance) {
            synchronized (LOCK) {
                instance = new DownloadRepository(context);
            }
        }

        return instance;
    }

    private DownloadManager createDownloadManager() {
        ExoDatabaseProvider databaseProvider = new ExoDatabaseProvider(context);
        DefaultHttpDataSourceFactory dataSourceFactory =
                new DefaultHttpDataSourceFactory(getUserAgent());
        return new DownloadManager(context, databaseProvider, audioDownloadCache, dataSourceFactory);
    }

    DownloadManager getDownloadManager() {
        return downloadManager;
    }

    private String getUserAgent() {
        return Util.getUserAgent(context, context.getString(R.string.app_name));
    }

    public Cache getAudioDownloadCache() {
        return audioDownloadCache;
    }

    private File getAudioDownloadDirectory() {
        return new File(context.getFilesDir().getAbsolutePath(), DOWNLOAD_DIRECTORY);
    }
}
