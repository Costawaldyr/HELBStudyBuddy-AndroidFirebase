package com.example.studybuddy.notification;

import static com.example.studybuddy.utils.MyConstants.*;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.studybuddy.MainActivity;
import com.example.studybuddy.R;
import com.example.studybuddy.data.local.AppDatabase;
import com.example.studybuddy.data.local.NotificationEntity;

import java.util.concurrent.Executors;

public final class NotificationHelper
{
    private NotificationHelper()
    {
    }

    public static void createChannels(Context context)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            return;
        }

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null)
        {
            return;
        }

        NotificationChannel buddyChannel = new NotificationChannel(CHANNEL_ID_BUDDY, CHANNEL_NAME_BUDDY, NotificationManager.IMPORTANCE_HIGH);
        buddyChannel.setDescription(CHANNEL_DESC_BUDDY);
        manager.createNotificationChannel(buddyChannel);

        NotificationChannel messageChannel = new NotificationChannel(CHANNEL_ID_MESSAGE, CHANNEL_NAME_MESSAGE, NotificationManager.IMPORTANCE_HIGH);
        messageChannel.setDescription(CHANNEL_DESC_MESSAGE);
        manager.createNotificationChannel(messageChannel);
    }

    public static void sendBuddyMotivation(Context context, String buddyName, @Nullable String buddyId, String message)
    {
        PendingIntent intent = buildPendingIntent(context, REQUEST_CODE_BUDDY, NAV_TYPE_BUDDY, buddyId);

        NotificationCompat.Builder builder = baseBuilder(context, CHANNEL_ID_BUDDY, buddyName + SENDER_SUFFIX, message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(intent);

        post(context, NOTIF_ID_BUDDY, builder);
        persist(context, buddyName, message, DB_TYPE_BUDDY, buddyId);
    }

    public static void sendBuddyMotivation(Context context, String buddyName, String message)
    {
        sendBuddyMotivation(context, buddyName, null, message);
    }

    public static void sendMessageReceived(Context context, String senderName, String matchId)
    {
        String body = String.format(NEW_MESSAGE_BODY_TEMPLATE, senderName);
        PendingIntent intent = buildPendingIntent(context, REQUEST_CODE_MESSAGE, NAV_TYPE_MESSAGE, matchId);

        NotificationCompat.Builder builder = baseBuilder(context, CHANNEL_ID_MESSAGE, NEW_MESSAGE_TITLE, body)
                .setContentIntent(intent);

        post(context, NOTIF_ID_MESSAGE, builder);
        persist(context, NEW_MESSAGE_TITLE, body, DB_TYPE_MESSAGE, matchId);
    }

    public static void sendMatchFoundNotification(Context context, String userName, String subject, String otherUserId)
    {
        String body = String.format(NEW_MATCH_BODY_TEMPLATE, userName, subject);
        PendingIntent intent = buildPendingIntent(context, REQUEST_CODE_MATCH, NAV_TYPE_MATCH, otherUserId);

        NotificationCompat.Builder builder = baseBuilder(context, CHANNEL_ID_MESSAGE, NEW_MATCH_TITLE, body)
                .setContentIntent(intent);

        post(context, NOTIF_ID_MATCH, builder);
        persist(context, NEW_MATCH_TITLE, body, DB_TYPE_MATCH, otherUserId);
    }

    private static NotificationCompat.Builder baseBuilder(Context context, String channelId, String title, String body)
    {
        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_message_notif)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true);
    }

    private static PendingIntent buildPendingIntent(Context context, int requestCode, String navType, @Nullable String targetId)
    {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_NAV_TYPE, navType);
        if (targetId != null)
        {
            intent.putExtra(EXTRA_TARGET_ID, targetId);
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static void post(Context context, int notificationId, NotificationCompat.Builder builder)
    {
        boolean canPost = ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        if (!canPost)
        {
            return;
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private static void persist(Context context, String title, String message, String type, @Nullable String targetId)
    {
        NotificationEntity entity = new NotificationEntity(title, message, System.currentTimeMillis(), type, targetId);
        Executors.newSingleThreadExecutor().execute(() -> AppDatabase.getDatabase(context).notificationDao().insert(entity));
    }
}
