package com.example.studybuddy.notification;

import static com.example.studybuddy.utils.MyConstants.*;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studybuddy.models.Buddy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Random;

public class BuddyMotivationWorker extends Worker
{
    private final Random random = new Random();

    public BuddyMotivationWorker(@NonNull Context context, @NonNull WorkerParameters params)
    {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork()
    {
        Context context = getApplicationContext();
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null)
        {
            sendGenericMotivation(context);
            return Result.success();
        }

        FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS).document(uid)
                .collection(COLLECTION_BUDDIES)
                .limit(MAX_BUDDIES_FOR_MOTIVATION)
                .get()
                .addOnSuccessListener(snapshots -> sendForFetchedBuddies(context, snapshots))
                .addOnFailureListener(error ->
                {
                    Log.e(WORKER_TAG_BUDDY_MOTIVATION, "Could not load buddies for motivation", error);
                    sendGenericMotivation(context);
                });

        return Result.success();
    }

    private void sendForFetchedBuddies(Context context, QuerySnapshot snapshots)
    {
        if (snapshots == null || snapshots.isEmpty())
        {
            sendGenericMotivation(context);
            return;
        }

        List<DocumentSnapshot> docs = snapshots.getDocuments();
        DocumentSnapshot pickedDoc = docs.get(random.nextInt(docs.size()));
        Buddy buddy = pickedDoc.toObject(Buddy.class);
        if (buddy == null)
        {
            sendGenericMotivation(context);
            return;
        }
        if (buddy.getId() == null)
        {
            buddy.setId(pickedDoc.getId());
        }

        String buddyName = pickStringOrDefault(buddy.getName(), DEFAULT_BUDDY_NAME_MOTIVATION);
        String courseName = pickStringOrDefault(buddy.getCourseName(), DEFAULT_COURSE_NAME_MOTIVATION);
        String template = CONTEXTUAL_TEMPLATES[random.nextInt(CONTEXTUAL_TEMPLATES.length)];
        String message = String.format(template, courseName);

        NotificationHelper.sendBuddyMotivation(context, buddyName, buddy.getId(), message);
    }

    private void sendGenericMotivation(Context context)
    {
        String message = GENERIC_TEMPLATES[random.nextInt(GENERIC_TEMPLATES.length)];
        NotificationHelper.sendBuddyMotivation(context, FALLBACK_AUTHOR_NAME, null, message);
    }

    private String pickStringOrDefault(String value, String fallback)
    {
        return (value != null && !value.isEmpty()) ? value : fallback;
    }
}
