package com.example.studybuddy.service;

import static com.example.studybuddy.utils.MyConstants.*;

import android.content.Context;
import android.util.Log;

import com.example.studybuddy.notification.NotificationHelper;
import com.example.studybuddy.profile.MatchManager;
import com.example.studybuddy.models.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MatchingService
{
    private final FirebaseFirestore db;
    private final MatchManager matchManager;
    private final String currentUid;
    private Context context;

    public interface MatchingCallback
    {
        void onMatchFound(String otherUserId, String subject);

        void onNoMatchFound();

        void onError(String error);
    }

    public MatchingService()
    {
        this(null);
    }

    public MatchingService(Context context)
    {
        this.context = context;
        db = FirebaseFirestore.getInstance();
        matchManager = new MatchManager();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        currentUid = (user != null) ? user.getUid() : "";
    }

    public void findPotentialMatches(MatchingCallback callback)
    {
        if (currentUid.isEmpty())
        {
            callback.onError(ERROR_NOT_AUTHENTICATED);
            return;
        }

        db.collection(COLLECTION_USERS)
                .document(currentUid)
                .get()
                .addOnSuccessListener(snapshot ->
                {
                    if (!snapshot.exists())
                    {
                        callback.onNoMatchFound();
                        return;
                    }

                    Profile profile = snapshot.toObject(Profile.class);
                    List<String> subjects = (profile != null) ? profile.getSubjects() : null;

                    if (subjects == null || subjects.isEmpty())
                    {
                        Log.d(MATCHING_TAG, LOG_NO_SUBJECTS);
                        callback.onNoMatchFound();
                        return;
                    }

                    findUsersWithSameSubjects(subjects, callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void findUsersWithSameSubjects(List<String> subjects, MatchingCallback callback)
    {
        List<String> alreadyReported = new ArrayList<>();
        int[] queriesDone = {ZERO};
        boolean[] matchFound = {false};

        for (String subject : subjects)
        {
            db.collection(COLLECTION_USERS)
                    .whereArrayContains(FIELD_SUBJECTS, subject)
                    .limit(FIVE)
                    .get()
                    .addOnSuccessListener(querySnapshot ->
                    {
                        queriesDone[ZERO]++;

                        for (QueryDocumentSnapshot doc : querySnapshot)
                        {
                            String candidateUid = doc.getId();

                            if (candidateUid.equals(currentUid))
                            {
                                continue;
                            }

                            if (alreadyReported.contains(candidateUid))
                            {
                                continue;
                            }

                            if (!matchFound[ZERO])
                            {
                                matchFound[ZERO] = true;
                                alreadyReported.add(candidateUid);
                                Log.d(MATCHING_TAG, String.format(LOG_MATCH_FOUND_TEMPLATE, candidateUid, subject));

                                if (context != null)
                                {
                                    String name = doc.getString(FIELD_NAME);
                                    if (name == null)
                                    {
                                        name = DEFAULT_STUDENT_LABEL;
                                    }
                                    NotificationHelper.sendMatchFoundNotification(context, name, subject, candidateUid);
                                }

                                callback.onMatchFound(candidateUid, subject);
                                return;
                            }
                        }

                        if (queriesDone[ZERO] == subjects.size() && !matchFound[ZERO])
                        {
                            Log.d(MATCHING_TAG, LOG_NO_MATCH_FINAL);
                            callback.onNoMatchFound();
                        }
                    })
                    .addOnFailureListener(e ->
                    {
                        queriesDone[ZERO]++;
                        Log.e(MATCHING_TAG, String.format(LOG_QUERY_FAILED_TEMPLATE, subject), e);
                        if (queriesDone[ZERO] == subjects.size() && !matchFound[ZERO])
                        {
                            callback.onError(e.getMessage());
                        }
                    });
        }
    }
}
