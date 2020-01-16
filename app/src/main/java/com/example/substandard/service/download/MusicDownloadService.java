package com.example.substandard.service.download;

import android.app.Notification;

import androidx.annotation.Nullable;

import com.example.substandard.R;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationHelper;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.List;

public class MusicDownloadService extends DownloadService {
    private static final int FOREGROUND_NOTIFICATION_ID = 1;

    private static final String NOTIFICATION_CHANNEL_ID = "service.musicDownload.channel";

    private static final String DOWNLOAD_DIRECTORY = "songs";

    private DownloadManager downloadManager;

    private static int nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1;


    DownloadNotificationHelper notificationHelper;

    public MusicDownloadService() {
        super(FOREGROUND_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                NOTIFICATION_CHANNEL_ID,
                R.string.exo_download_notification_channel_name,
                R.string.exo_download_description);

        notificationHelper = new DownloadNotificationHelper(this, NOTIFICATION_CHANNEL_ID);

        createDownloadManager();
    }

    private void createDownloadManager() {
        ExoDatabaseProvider databaseProvider = new ExoDatabaseProvider(this);

        SimpleCache simpleCache = new SimpleCache(getDownloadDirectory(),
                new NoOpCacheEvictor(), databaseProvider);

        DefaultHttpDataSourceFactory dataSourceFactory =
                new DefaultHttpDataSourceFactory(getUserAgent());

        downloadManager = new DownloadManager(this, databaseProvider, simpleCache, dataSourceFactory);
    }

    private File getDownloadDirectory() {
        return new File(getFilesDir(), DOWNLOAD_DIRECTORY);
    }

    private String getUserAgent() {
        return Util.getUserAgent(this, getString(R.string.app_name));
    }

    @Override
    protected DownloadManager getDownloadManager() {
        return downloadManager;
    }

    @Nullable
    @Override
    protected Scheduler getScheduler() {
        return null;
    }

    @Override
    protected Notification getForegroundNotification(List<Download> downloads) {
        return notificationHelper.buildProgressNotification(R.drawable.exo_notification_small_icon,
                null,
                null,
                downloads);
    }
}
