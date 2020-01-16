package com.example.substandard.player;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.example.substandard.R;

public class AudioNotificationUtils {
    private static final String AUDIO_NOTIFICATION_CHANNEL_ID = "substandard.player.channel";

    public static int createNotificationId() {
        return (int) System.currentTimeMillis();
    }

    public static NotificationCompat.Builder buildNotification(Context context, MediaSessionCompat mediaSession) {
        NotificationCompat.Action prevAction =
                new NotificationCompat.Action(R.drawable.exo_controls_previous,
                        context.getString(R.string.prev_track),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
        NotificationCompat.Action pauseAction =
                new NotificationCompat.Action(R.drawable.exo_controls_pause,
                        context.getString(R.string.pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_PLAY_PAUSE));
        NotificationCompat.Action nextAction =
                new NotificationCompat.Action(R.drawable.exo_controls_next,
                        context.getString(R.string.next_track),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        MediaControllerCompat controller = mediaSession.getController();
        MediaDescriptionCompat description = controller.getMetadata().getDescription();
        Bitmap albumArt = description.getIconBitmap();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, AUDIO_NOTIFICATION_CHANNEL_ID)
                .setContentIntent(controller.getSessionActivity())
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                        PlaybackStateCompat.ACTION_STOP))
                .addAction(prevAction)
                .addAction(pauseAction)
                .addAction(nextAction)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                                PlaybackStateCompat.ACTION_STOP)))
                .setShowWhen(false)
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(albumArt)
                .setSmallIcon(R.drawable.exo_notification_small_icon);

        if (isVersionLessThanO()) {
            builder.setPriority(NotificationManager.IMPORTANCE_HIGH);
        } else {
            whenVersionLessThanO(context, notificationManager);
        }

        return builder;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static void whenVersionLessThanO(Context context, NotificationManager notificationManager) {
        NotificationChannel channel = new NotificationChannel(AUDIO_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
    }

    private static boolean isVersionLessThanO() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O;
    }
}
