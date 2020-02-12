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
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DownloadTracker {

    private final Context context;

    private final Map<String, Download> downloads;
    private final DownloadIndex downloadIndex;

    public DownloadTracker(Context context) {
        this.context = context.getApplicationContext();
        DownloadManager downloadManager = DownloadRepository.getInstance(context).getDownloadManager();
        this.downloadIndex = downloadManager.getDownloadIndex();
        downloads = new HashMap<>();
        downloadManager.addListener(new DownloadManagerListener());
    }

    private void loadDownloads() {
        try (DownloadCursor loadedDownloads = downloadIndex.getDownloads()) {
            while (loadedDownloads.moveToNext()) {
                Download download = loadedDownloads.getDownload();
                downloads.put(download.request.id, download);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isDownloaded(String id) {
        Download download = downloads.get(id);
        return download != null && download.state != Download.STATE_FAILED;
    }

    public void requestDownload(String mediaId, URL url) {
        requestDownload(mediaId, Uri.parse(url.toString()));
    }

    public void requestDownload(String mediaId, Uri mediaUri) {
        DownloadRequest request = new DownloadRequest(mediaId, DownloadRequest.TYPE_PROGRESSIVE,
                mediaUri, Collections.emptyList(), null, null);
        DownloadService.sendAddDownload(context, MusicDownloadService.class, request, false);
    }

    /**
     * Listener which updates the downloads map whenever downloads are removed/changed.
     */
    private class DownloadManagerListener implements DownloadManager.Listener {
        @Override
        public void onDownloadChanged(DownloadManager downloadManager, Download download) {
            downloads.put(download.request.id, download);
        }

        @Override
        public void onDownloadRemoved(DownloadManager downloadManager, Download download) {
            downloads.remove(download.request.id);
        }
    }
}
