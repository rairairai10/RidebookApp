package notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.ridebook.ChatActivity;
import com.example.ridebook.PostDetailActivity;
import com.example.ridebook.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class FirebaseMesesaging extends FirebaseMessagingService {

    private static final String ADMIN_CHANNEL_ID = "admin_channel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        String savedCurrentUser = sp.getString("Current_USERID", "None");

        String notificationType = message.getData().get("notificationType");

        if (notificationType != null) {
            if (notificationType.equals("PostNotification")) {
                String sender = message.getData().get("sender");
                String pId = message.getData().get("pId");
                String pTitle = message.getData().get("pTitle");

                if (sender != null && !sender.equals(savedCurrentUser)) {
                    showPostNotification("" + pId, "" + pTitle);
                }
            } else if (notificationType.equals("ChatNotification")) {
                String sent = message.getData().get("sent");
                String user = message.getData().get("user");
                FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();

                if (fUser != null && sent != null && sent.equals(fUser.getUid())) {
                    if (!savedCurrentUser.equals(user)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sendOandAboveNotification(message);
                        } else {
                            sendNormalNotification(message);
                        }
                    }
                }
            }
        }


    }

    private void showPostNotification(String pId, String pTitle) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            setupPostNotificationChannel(notificationManager);
        }

        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("postId",pId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),R.drawable.llogo);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,""+ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.llogo)
                .setLargeIcon(largeIcon)
                .setContentTitle(pTitle)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationID, notificationBuilder.build());
    }

    private void setupPostNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "New Notification";
        String channelDescription = "Device to device post notification";

        NotificationChannel adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(channelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.GREEN);
        adminChannel.enableVibration(true);

        if (notificationManager!=null){
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

    private void sendNormalNotification(RemoteMessage message) {
        String user = message.getData().get("user");
        String icon = message.getData().get("icon");
        String title = message.getData().get("title");
        String body = message.getData().get("body");

        RemoteMessage.Notification notification = message.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i,intent , PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if(i>0){
            j=i;
        }
        notificationManager.notify(j,builder.build());
    }

    private void sendOandAboveNotification(RemoteMessage message) {
        String user = message.getData().get("user");
        String icon = message.getData().get("icon");
        String title = message.getData().get("title");
        String body = message.getData().get("body");

        RemoteMessage.Notification notification = message.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("hisUid", user);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        NotificationCompat.Builder builder = notification1.getONotifications(title, body,pIntent, defSoundUri , icon);



        int j = 0;
        if(i>0){
            j=i;
        }
        notification1.getManager().notify(j,builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            updateToken(token);
        }
        super.onNewToken(token);
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        ref.child(user.getUid()).setValue(token);

    }
}
