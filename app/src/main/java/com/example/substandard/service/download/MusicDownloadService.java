package com.example.substandard.service.download;

import android.app.Notification;

import androidx.annotation.Nullable;

import com.example.substandard.R;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationHelper;

import java.io.File;
import java.util.List;

public class MusicDownloadService extends DownloadService {
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    private static final int JOB_SCHEDULER_ID = 78124;
    private static final String NOTIFICATION_CHANNEL_ID = "service.musicDownload.channel";
    private static int nextNotificationId = FOREGROUND_NOTIFICATION_ID + 1;

    private static final String DOWNLOAD_DIRECTORY = "songs/";

    private DownloadNotificationHelper notificationHelper;

    public MusicDownloadService() {
        super(FOREGROUND_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                NOTIFICATION_CHANNEL_ID,
                R.string.exo_download_notification_channel_name,
                R.string.exo_download_description);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationHelper = new DownloadNotificationHelper(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
    }

    private File getDownloadDirectory() {
        return new File(getFilesDir().getAbsolutePath(), DOWNLOAD_DIRECTORY);
    }

    @Override
    protected DownloadManager getDownloadManager() {
        return DownloadRepository.getInstance(this).getDownloadManager();
    }

    @Nullable
    @Override
    protected Scheduler getScheduler() {
        return new PlatformScheduler(this, JOB_SCHEDULER_ID);
    }

    @Override
    protected Notification getForegroundNotification(List<Download> downloads) {
        return notificationHelper.buildProgressNotification(R.drawable.exo_notification_small_icon,
                null,
                null,
                downloads);
    }
}
