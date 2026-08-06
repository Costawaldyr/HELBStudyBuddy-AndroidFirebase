package com.example.studybuddy.notification;

import static com.example.studybuddy.utils.MyConstants.*;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(REMINDER_TAG, LOG_REMINDER_RECEIVED);

        String quote = (intent != null) ? intent.getStringExtra(EXTRA_QUOTE) : null;
        String selectedQuote = (quote != null && !quote.isEmpty()) ? quote : DEFAULT_REMINDER_QUOTE;
        String message = String.format(REMINDER_MESSAGE_TEMPLATE, selectedQuote);

        NotificationHelper.sendBuddyMotivation(context, FALLBACK_AUTHOR_NAME, message);
    }
}
