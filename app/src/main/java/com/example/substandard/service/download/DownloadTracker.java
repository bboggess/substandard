package com.example.substandard.service.download;

import android.content.Context;
import android.net.Uri;

import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadCursor;
import com.google.android.exoplayer2.offline.DownloadIndex;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.offline.DownloadService;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

public class DownloadTracker {

    private final Context context;

    private HashMap<Uri, Download> downloads;
    private DownloadIndex downloadIndex;

    public DownloadTracker(Context context, DownloadManager downloadManager) {
        this.context = context.getApplicationContext();
        this.downloadIndex = downloadManager.getDownloadIndex();
        downloads = new HashMap<>();
    }

    private void loadDownloads() {
        try (DownloadCursor loadedDownloads = downloadIndex.getDownloads()) {
            while (loadedDownloads.moveToNext()) {
                Download download = loadedDownloads.getDownload();
                downloads.put(download.request.uri, download);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestDownload(String mediaId, Uri mediaUri) {
        DownloadRequest request = new DownloadRequest(mediaId, DownloadRequest.TYPE_PROGRESSIVE,
                mediaUri, Collections.emptyList(), null, null);
        DownloadService.sendAddDownload(context, MusicDownloadService.class, request, false);
    }
}
