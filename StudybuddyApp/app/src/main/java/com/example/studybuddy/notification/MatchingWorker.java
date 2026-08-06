package com.example.studybuddy.notification;

import static com.example.studybuddy.utils.MyConstants.*;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studybuddy.service.MatchingService;

public class MatchingWorker extends Worker
{
    public MatchingWorker(@NonNull Context context, @NonNull WorkerParameters params)
    {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        Log.d(WORKER_TAG_MATCHING, LOG_MATCHING_STARTED);

        MatchingService matchingService = new MatchingService(getApplicationContext());
        matchingService.findPotentialMatches(new MatchingService.MatchingCallback()
        {
            @Override
            public void onMatchFound(String otherUserId, String subject)
            {
                Log.d(WORKER_TAG_MATCHING, String.format(LOG_MATCH_FOUND_WORKER, subject));
            }

            @Override
            public void onNoMatchFound()
            {
                Log.d(WORKER_TAG_MATCHING, LOG_NO_MATCH_WORKER);
            }

            @Override
            public void onError(String error)
            {
                Log.e(WORKER_TAG_MATCHING, String.format(LOG_MATCHING_ERROR_TEMPLATE, error));
            }
        });

        return Result.success();
    }
}
