package com.example.studybuddy.profile;

import android.util.Log;

import com.example.studybuddy.models.Match;
import com.example.studybuddy.models.MatchCandidate;
import com.example.studybuddy.utils.MyConstants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles finding and managing study partners.
 */
public class MatchManager
{
    private static final String TAG = "MatchManager";

    private final FirebaseFirestore db;
    private final String currentUid;

    public interface MatchesCallback
    {
        void onSuccess(List<Match> matches);
        void onError(String error);
    }

    public interface CreateCallback
    {
        void onSuccess(String matchId);
        void onError(String error);
    }

    public interface SimpleCallback
    {
        void onSuccess();
        void onError(String error);
    }

    public interface AcceptCandidateCallback
    {
        void onSuccess(String matchId, String otherUserId, String otherUserName);
        void onError(String error);
    }

    public interface MatchesCandidateCallback
    {
        void onSuccess(List<MatchCandidate> candidates);
        void onError(String error);
    }

    public interface IncomingMatchListener
    {
        void onMatchReceived(Match match);
    }

    public MatchManager()
    {
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            currentUid = user.getUid();
        }
        else
        {
            currentUid = "";
        }
    }

    /**
     * Finds candidates that are not rejected or already matched.
     */
    public void getAvailableMatches(MatchesCandidateCallback callback)
    {
        if (currentUid.isEmpty())
        {
            callback.onError("User not authenticated");
            return;
        }

        db.collection(MyConstants.COLLECTION_USERS).document(currentUid)
                .collection(MyConstants.COLLECTION_REJECTED)
                .get()
                .addOnSuccessListener(rejectedSnaps -> {
                    Set<String> excludedIds = new HashSet<>();
                    for (QueryDocumentSnapshot r : rejectedSnaps)
                    {
                        excludedIds.add(r.getId());
                    }

                    db.collection(MyConstants.COLLECTION_MATCHES)
                            .get()
                            .addOnSuccessListener(matchSnaps -> {
                                for (QueryDocumentSnapshot m : matchSnaps)
                                {
                                    addMatchedPeers(m, excludedIds);
                                }
                                fetchAndFilterUsers(excludedIds, callback);
                            })
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Excludes users from already existing matches.
     */
    private void addMatchedPeers(QueryDocumentSnapshot matchDoc, Set<String> excludedIds)
    {
        String userA = matchDoc.getString(MyConstants.FIELD_USER_ID_1);
        String userB = matchDoc.getString(MyConstants.FIELD_USER_ID_2);

        boolean iAmA = currentUid.equals(userA);
        boolean iAmB = currentUid.equals(userB);

        if (iAmA && userB != null)
        {
            excludedIds.add(userB);
        }
        if (iAmB && userA != null)
        {
            excludedIds.add(userA);
        }
    }

    /**
     * Filters all users to find available candidates.
     */
    private void fetchAndFilterUsers(Set<String> excludedIds, MatchesCandidateCallback callback)
    {
        db.collection(MyConstants.COLLECTION_USERS)
                .get()
                .addOnSuccessListener(userSnapshots -> {
                    List<MatchCandidate> list = new ArrayList<>();
                    if (userSnapshots == null || userSnapshots.isEmpty())
                    {
                        callback.onSuccess(list);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : userSnapshots)
                    {
                        String id = doc.getId();

                        boolean isMe = id.equals(currentUid);
                        boolean isExcluded = excludedIds.contains(id);
                        if (isMe || isExcluded)
                        {
                            continue;
                        }

                        MatchCandidate candidate = buildCandidate(doc);
                        if (candidate != null)
                        {
                            list.add(candidate);
                        }
                    }

                    java.util.Collections.shuffle(list);
                    if (list.size() > MyConstants.MAX_DASHBOARD_CANDIDATES)
                    {
                        list = new ArrayList<>(list.subList(MyConstants.ZERO, MyConstants.MAX_DASHBOARD_CANDIDATES));
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Constructs a candidate object from Firestore data.
     */
    @SuppressWarnings("unchecked")
    private MatchCandidate buildCandidate(QueryDocumentSnapshot doc)
    {
        try
        {
            List<String> subjects = (List<String>) doc.get(MyConstants.FIELD_SUBJECTS);
            if (subjects == null || subjects.isEmpty())
            {
                return null;
            }

            MatchCandidate person = new MatchCandidate();
            person.setUserId(doc.getId());

            String name = doc.getString(MyConstants.FIELD_NAME);
            person.setName(name != null && !name.isEmpty() ? name : MyConstants.DEFAULT_STUDENT_NAME);

            String school = doc.getString(MyConstants.FIELD_SCHOOL);
            person.setSchool(school != null && !school.isEmpty() ? school : MyConstants.DEFAULT_SCHOOL_NAME);

            person.setSubject(subjects.get(MyConstants.ZERO));
            person.setProfileImageUrl(doc.getString(MyConstants.FIELD_PROFILE_IMAGE));
            person.setTimestamp(Timestamp.now());
            return person;
        }
        catch (Exception e)
        {
            Log.e(TAG, "Problem reading user data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Accepts a candidate by creating and activating a match.
     */
    public void acceptCandidate(MatchCandidate candidate, AcceptCandidateCallback callback)
    {
        createMatch(candidate.getUserId(), candidate.getName(), candidate.getSubject(),
                new CreateCallback()
                {
                    @Override
                    public void onSuccess(String matchId)
                    {
                        acceptMatch(matchId, new SimpleCallback()
                        {
                            @Override
                            public void onSuccess()
                            {
                                callback.onSuccess(matchId, candidate.getUserId(), candidate.getName());
                            }

                            @Override
                            public void onError(String error)
                            {
                                callback.onError(error);
                            }
                        });
                    }

                    @Override
                    public void onError(String error)
                    {
                        callback.onError(error);
                    }
                });
    }

    /**
     * Rejects a candidate to prevent them from appearing again.
     */
    public void rejectCandidate(MatchCandidate candidate, SimpleCallback callback)
    {
        Map<String, Object> data = new HashMap<>();
        data.put(MyConstants.FIELD_REJECTED_AT, Timestamp.now());

        db.collection(MyConstants.COLLECTION_USERS).document(currentUid)
                .collection(MyConstants.COLLECTION_REJECTED)
                .document(candidate.getUserId())
                .set(data)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Creates a new match entry in Firestore.
     */
    public void createMatch(String otherUserId, String otherUserName, String subject, CreateCallback callback)
    {
        if (currentUid.isEmpty())
        {
            return;
        }

        db.collection(MyConstants.COLLECTION_USERS).document(currentUid).get()
                .addOnSuccessListener(snapshot -> {
                    String myName = snapshot.getString(MyConstants.FIELD_NAME);
                    String mySchool = snapshot.getString(MyConstants.FIELD_SCHOOL);
                    String myPhoto = snapshot.getString(MyConstants.FIELD_PROFILE_IMAGE);

                    Map<String, Object> data = new HashMap<>();
                    data.put(MyConstants.FIELD_USER_ID_1, currentUid);
                    data.put(MyConstants.FIELD_USER_ID_2, otherUserId);
                    data.put(MyConstants.FIELD_INITIATOR_ID, currentUid);
                    data.put(MyConstants.FIELD_RECEIVER_NAME, otherUserName);

                    data.put(MyConstants.FIELD_NAME, (myName != null && !myName.isEmpty()) ? myName : MyConstants.DEFAULT_STUDENT_NAME);
                    data.put(MyConstants.FIELD_SCHOOL, (mySchool != null && !mySchool.isEmpty()) ? mySchool : MyConstants.DEFAULT_SCHOOL_NAME);
                    data.put(MyConstants.FIELD_SUBJECT, subject);
                    data.put(MyConstants.FIELD_PROFILE_IMAGE, (myPhoto != null) ? myPhoto : "");

                    data.put(MyConstants.FIELD_MATCHED_AT, Timestamp.now());

                    long expiryTime = System.currentTimeMillis() + MyConstants.MATCH_DURATION_MS;
                    data.put(MyConstants.FIELD_EXPIRES_AT, new Timestamp(new Date(expiryTime)));

                    data.put(MyConstants.FIELD_STATUS, MyConstants.STATUS_PENDING);
                    data.put(MyConstants.FIELD_IS_ACTIVE, false);

                    db.collection(MyConstants.COLLECTION_MATCHES).add(data)
                            .addOnSuccessListener(ref -> callback.onSuccess(ref.getId()))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                });
    }

    /**
     * Marks a match as active.
     */
    public void acceptMatch(String matchId, SimpleCallback callback)
    {
        Map<String, Object> updates = new HashMap<>();
        updates.put(MyConstants.FIELD_STATUS, MyConstants.STATUS_ACTIVE);
        updates.put(MyConstants.FIELD_IS_ACTIVE, true);

        db.collection(MyConstants.COLLECTION_MATCHES).document(matchId).update(updates)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Declines a match request.
     */
    public void declineMatch(String matchId, SimpleCallback callback)
    {
        db.collection(MyConstants.COLLECTION_MATCHES).document(matchId)
                .update(MyConstants.FIELD_STATUS, MyConstants.STATUS_DECLINED, MyConstants.FIELD_IS_ACTIVE, false)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Listens for pending matches where current user is the receiver.
     */
    public ListenerRegistration listenForIncomingMatches(IncomingMatchListener listener)
    {
        if (currentUid.isEmpty())
        {
            return null;
        }

        return db.collection(MyConstants.COLLECTION_MATCHES)
                .whereEqualTo(MyConstants.FIELD_USER_ID_2, currentUid)
                .whereEqualTo(MyConstants.FIELD_STATUS, MyConstants.STATUS_PENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null)
                    {
                        return;
                    }

                    for (QueryDocumentSnapshot doc : snapshots)
                    {
                        Match m = convertToMatch(doc);
                        if (m != null)
                        {
                            listener.onMatchReceived(m);
                        }
                    }
                });
    }

    /**
     * Retrieves all active matches for the current user.
     */
    public void getMyMatches(MatchesCallback callback)
    {
        if (currentUid.isEmpty())
        {
            return;
        }

        db.collection(MyConstants.COLLECTION_MATCHES)
                .whereEqualTo(MyConstants.FIELD_STATUS, MyConstants.STATUS_ACTIVE)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Match> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots)
                    {
                        String u1 = doc.getString(MyConstants.FIELD_USER_ID_1);
                        String u2 = doc.getString(MyConstants.FIELD_USER_ID_2);

                        if (currentUid.equals(u1) || currentUid.equals(u2))
                        {
                            Match m = convertToMatch(doc);
                            if (m != null)
                            {
                                list.add(m);
                            }
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Retrieves all match history (active and expired).
     */
    public void getAllMyMatches(MatchesCallback callback)
    {
        if (currentUid.isEmpty())
        {
            return;
        }

        db.collection(MyConstants.COLLECTION_MATCHES)
                .whereIn(MyConstants.FIELD_STATUS, java.util.Arrays.asList(MyConstants.STATUS_ACTIVE, MyConstants.STATUS_EXPIRED))
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Match> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshots)
                    {
                        String u1 = doc.getString(MyConstants.FIELD_USER_ID_1);
                        String u2 = doc.getString(MyConstants.FIELD_USER_ID_2);

                        if (!currentUid.equals(u1) && !currentUid.equals(u2))
                        {
                            continue;
                        }

                        Match m = convertToMatch(doc);
                        if (m != null)
                        {
                            list.add(m);
                        }
                    }

                    java.util.Collections.sort(list, (a, b) -> {
                        boolean aActive = a.isCurrentlyActive();
                        boolean bActive = b.isCurrentlyActive();
                        if (aActive != bActive)
                        {
                            return aActive ? MyConstants.NEGATIVE_ONE : MyConstants.ONE;
                        }
                        Timestamp ta = a.getMatchedAt();
                        Timestamp tb = b.getMatchedAt();
                        if (ta == null && tb == null)
                        {
                            return MyConstants.ZERO;
                        }
                        if (ta == null)
                        {
                            return MyConstants.ONE;
                        }
                        if (tb == null)
                        {
                            return MyConstants.NEGATIVE_ONE;
                        }
                        return tb.compareTo(ta);
                    });

                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Maps a Firestore document to a Match model.
     */
    private Match convertToMatch(QueryDocumentSnapshot doc)
    {
        try
        {
            Match m = new Match();
            m.setMatchId(doc.getId());
            m.setName(doc.getString(MyConstants.FIELD_NAME));
            m.setSubject(doc.getString(MyConstants.FIELD_SUBJECT));
            m.setSchool(doc.getString(MyConstants.FIELD_SCHOOL));
            m.setProfileImageUrl(doc.getString(MyConstants.FIELD_PROFILE_IMAGE));
            m.setUserId1(doc.getString(MyConstants.FIELD_USER_ID_1));
            m.setUserId2(doc.getString(MyConstants.FIELD_USER_ID_2));
            m.setMatchedAt(doc.getTimestamp(MyConstants.FIELD_MATCHED_AT));
            m.setExpiresAt(doc.getTimestamp(MyConstants.FIELD_EXPIRES_AT));
            m.setStatus(doc.getString(MyConstants.FIELD_STATUS));
            Boolean activeFlag = doc.getBoolean(MyConstants.FIELD_IS_ACTIVE);
            if (activeFlag != null)
            {
                m.setActive(activeFlag);
            }

            String initiatorId = doc.getString(MyConstants.FIELD_INITIATOR_ID);
            boolean iWasTheInitiator = currentUid.equals(initiatorId);
            if (iWasTheInitiator)
            {
                String recName = doc.getString(MyConstants.FIELD_RECEIVER_NAME);
                if (recName != null)
                {
                    m.setName(recName);
                }
            }
            return m;
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
