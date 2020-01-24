package com.example.substandard.player.client;

import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;

import com.example.substandard.player.server.MusicService;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class which exposes client functionality for the MediaPlayer server. Encapsulates
 * the {@link MediaBrowserCompat}, {@link MediaControllerCompat}, and various needed
 * callbacks of which the user can be blissfully ignorant. By extending this class and
 * implementing certain empty methods, functionality can be customized to suit UI needs
 * without having to create your own callbacks to the browser or controller.
 *
 * {@method getTransportControl} can be used to control the MediaPlayer from the UI.
 */
public class BaseMediaBrowserAdapter {
    private static final String TAG = BaseMediaBrowserAdapter.class.getSimpleName();

    private final Context context;

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

    /**
     * Override this method to set behavior for a change in playback state, e.g. when the player
     * is paused.
     */
    public void onPlaybackStateChanged(PlaybackStateCompat state) {

    }

    /**
     * Override this method to set behavior for when the MediaBrowser loads.
     */
    public void onChildrenLoaded() {

    }

    /**
     * Override this method to perform setup upon connection to media session
     */
    public void onConnected(MediaControllerCompat controller) {

    }

    public void onMetadataChanged(MediaMetadataCompat metadata) {

    }

    /**
     * All setup behavior needed to be performed from the onStart method of the Activity using
     * this class. Make sure to call!
     */
    public void onStart() {
        ComponentName browserComponent = new ComponentName(context, MusicService.class);
        browser = new MediaBrowserCompat(context, browserComponent, connectionCallback, null);
        browser.connect();
    }

    /**
     * All cleanup needed to be performed from the onStop method of the Activity using
     * this class. You MUST call this when you are done using the BaseMediaBrowserAdapter.
     */
    public void onStop() {
        browser.disconnect();
        if (null != controller) {
            controller.unregisterCallback(controllerCallback);
        }
    }

    /**
     * This provides the interface for controlling the session's media playback. The returned
     * TransportControls can be used to play media, navigate the play queue, stop playback, etc.
     * @return null if controller is not yet connected
     */
    public MediaControllerCompat.TransportControls getTransportControl() {
        if (null == controller) {
            return null;
        }

        return controller.getTransportControls();
    }

    public void addQueueItem(MediaDescriptionCompat media) {
        if (null != controller) {
            controller.addQueueItem(media);
        }
    }

    public void addQueueItem(MediaMetadataCompat metadata) {
        addQueueItem(metadata.getDescription());
    }

    public void addQueueItem(MediaDescriptionCompat media, int index) {
        if (null != controller) {
            controller.addQueueItem(media, index);
        }
    }

    public void removeQueueItem(MediaDescriptionCompat media) {
        if (null != controller) {
            controller.removeQueueItem(media);
        }
    }

    public void clearQueue() {
        if (null != controller && controller.getQueue() != null) {
            for (MediaSessionCompat.QueueItem queueItem : controller.getQueue()) {
                controller.removeQueueItem(queueItem.getDescription());
            }
        }
    }

    public MediaMetadataCompat getMetadata() {
        if (null == controller) {
            return null;
        }

        return controller.getMetadata();
    }

    public List<MediaSessionCompat.QueueItem> getQueue() {
        if (null == controller) {
            return new ArrayList<>();
        }

        return controller.getQueue();
    }

    public int getRepeatMode() {
        if (null == controller) {
            return PlaybackStateCompat.REPEAT_MODE_INVALID;
        }

        return controller.getRepeatMode();
    }

    public int getShuffleMode() {
        if (null == controller) {
            return PlaybackStateCompat.SHUFFLE_MODE_INVALID;
        }

        return controller.getShuffleMode();
    }

    public void registerCallback(MediaControllerCompat.Callback callback) {
        if (null != controller) {
            controller.registerCallback(callback);
        }
    }

    public void unregisterCallback(MediaControllerCompat.Callback callback) {
        if (null != controller) {
            controller.unregisterCallback(callback);
        }
    }

    /**
     * Handles all callbacks when the media browser connects to the media server.
     */
    private class MediaConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            super.onConnected();
            try {
                controller = new MediaControllerCompat(context, browser.getSessionToken());
                controller.registerCallback(controllerCallback);
                BaseMediaBrowserAdapter.this.onConnected(controller);

                // Sync with the UI
                controllerCallback.onMetadataChanged(controller.getMetadata());
                controllerCallback.onPlaybackStateChanged(controller.getPlaybackState());
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

    /**
     * This class is used for interactions between the app and the media session. Playback
     * commands can be sent from the app to the session, and the session will inform registered
     * callbacks of any state changes or media metadata.
     */
    private class MediaControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            BaseMediaBrowserAdapter.this.onPlaybackStateChanged(state);
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            BaseMediaBrowserAdapter.this.onMetadataChanged(metadata);
        }
    }

}
