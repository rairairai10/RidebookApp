package notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class OreoAndAboveNotification extends ContextWrapper {

    private static final String CHANNEL_ID = "some_id";
    private static final String CHANNEL_NAME = "FirebaseAPP";

    private NotificationManager notificationManager;

    public OreoAndAboveNotification(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getManager().createNotificationChannel(notificationChannel);
        }
    }

    public NotificationManager getManager() {
        if (notificationManager == null) {
            notificationManager = getSystemService(NotificationManager.class);
        }
        return notificationManager;
    }

    public NotificationCompat.Builder getONotifications(String title,
                                                        String body,
                                                        PendingIntent pIntent,
                                                        Uri soundUri,
                                                        String icon) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentIntent(pIntent)
                .setContentTitle(title)
                .setContentText(body)  // Add content text
                .setSmallIcon(android.R.drawable.ic_notification_overlay)  // Replace with your icon
                .setSound(soundUri)
                .setAutoCancel(true);  // Automatically dismiss the notification when tapped
    }
}
