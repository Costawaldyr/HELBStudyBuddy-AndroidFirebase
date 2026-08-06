package com.example.studybuddy;

import static com.example.studybuddy.utils.MyConstants.*;

import android.app.Application;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.studybuddy.notification.BuddyMotivationWorker;
import com.example.studybuddy.notification.MatchingWorker;
import com.example.studybuddy.notification.NotificationHelper;
import com.mapbox.common.MapboxOptions;

import java.util.concurrent.TimeUnit;

public class StudyBuddyApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        String mapboxToken = getString(R.string.mapbox_access_token);
        MapboxOptions.setAccessToken(mapboxToken);

        NotificationHelper.createChannels(this);

        scheduleBuddyMotivations();
        scheduleMatchingSearch();
    }

    private void scheduleBuddyMotivations()
    {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                BuddyMotivationWorker.class,
                NOTIFICATION_WORK_INTERVAL_HOURS,
                TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_NAME_BUDDY_MOTIVATION,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }

    private void scheduleMatchingSearch()
    {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                MatchingWorker.class,
                MATCHING_WORK_INTERVAL_HOURS,
                TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                WORK_NAME_MATCHING_SEARCH,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }
}
