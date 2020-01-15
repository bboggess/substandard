package com.example.substandard.player.client;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;

import com.example.substandard.player.server.MusicService;

import java.util.List;

public class BaseMediaBrowserAdapter {
    private static final String TAG = BaseMediaBrowserAdapter.class.getSimpleName();

    private Context context;

    private MediaBrowserCompat browser;

    private MediaControllerCompat controller;

    private MediaConnectionCallback connectionCallback;
    private MediaSubscriptionCallback subscriptionCallback;
    private MediaControllerCallback controllerCallback;

    public BaseMediaBrowserAdapter(Context context) {
        this.context = context.getApplicationContext();
        connectionCallback = new MediaConnectionCallback();
        subscriptionCallback = new MediaSubscriptionCallback();
        controllerCallback = new MediaControllerCallback();
    }

    public void onPlaybackStateChanged() {

    }

    public void onChildrenLoaded() {

    }

    public void onStart() {
        ComponentName browserComponent = new ComponentName(context, MusicService.class);
        browser = new MediaBrowserCompat(context, browserComponent, connectionCallback, null);
        browser.connect();
    }

    public void onStop() {
        browser.disconnect();
        controller.unregisterCallback(controllerCallback);
    }

    public MediaControllerCompat.TransportControls getTransportControl() {
        if (null == controller) {
            return null;
        }

        return controller.getTransportControls();
    }

    private class MediaConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                controller = new MediaControllerCompat(context, browser.getSessionToken());
                controller.registerCallback(controllerCallback);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }

            browser.subscribe(browser.getRoot(), subscriptionCallback);
        }
    }

    private class MediaSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            BaseMediaBrowserAdapter.this.onChildrenLoaded();
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            BaseMediaBrowserAdapter.this.onPlaybackStateChanged();
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
        }
    }

}
