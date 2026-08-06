package com.example.studybuddy.data;

import com.example.studybuddy.utils.MyConstants;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Persists and reads conversation summaries used by the messages list.
 */
public class ConversationManager
{
    private final FirebaseFirestore db;
    private final String userId;

    public ConversationManager()
    {
        db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    /**
     * Retrieves all active conversations for the current user.
     */
    public ListenerRegistration getAllConversations(EventListener<QuerySnapshot> listener)
    {
        if (userId == null || userId.isEmpty())
        {
            return null;
        }
        return db.collection(MyConstants.COLLECTION_USERS).document(userId)
                .collection(MyConstants.COLLECTION_CONVERSATIONS)
                .whereEqualTo(MyConstants.FIELD_HAS_MESSAGES, true)
                .orderBy(MyConstants.FIELD_LAST_TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }


    /**
     * Updates the full conversation state, including message counts and timestamps.
     */
    public void updateConversation(String ownerId,
                                   String participantId,
                                   String participantName,
                                   String participantImage,
                                   String participantEmoji,
                                   String participantColor,
                                   String type,
                                   String lastMessage,
                                   boolean isExpired,
                                   String matchId,
                                   Timestamp expiresAt)
    {
        if (ownerId == null || ownerId.isEmpty())
        {
            return;
        }

        DocumentReference convRef = db.collection(MyConstants.COLLECTION_USERS).document(ownerId)
                .collection(MyConstants.COLLECTION_CONVERSATIONS).document(participantId);

        Map<String, Object> data = new HashMap<>();
        data.put(MyConstants.FIELD_PARTICIPANT_ID, participantId);
        data.put(MyConstants.FIELD_PARTICIPANT_NAME, participantName);
        data.put(MyConstants.FIELD_PARTICIPANT_IMAGE, participantImage);
        data.put(MyConstants.FIELD_PARTICIPANT_EMOJI, participantEmoji);
        data.put(MyConstants.FIELD_PARTICIPANT_COLOR, participantColor);
        data.put(MyConstants.FIELD_TYPE, type);
        data.put(MyConstants.FIELD_LAST_MESSAGE, lastMessage);
        data.put(MyConstants.FIELD_LAST_TIMESTAMP, FieldValue.serverTimestamp());
        data.put(MyConstants.FIELD_IS_EXPIRED, isExpired);
        data.put(MyConstants.FIELD_HAS_MESSAGES, true);
        data.put(MyConstants.FIELD_MESSAGE_COUNT, FieldValue.increment(MyConstants.ONE));
        if (matchId != null && !matchId.isEmpty())
        {
            data.put(MyConstants.FIELD_MATCH_ID, matchId);
        }
        if (expiresAt != null)
        {
            data.put(MyConstants.FIELD_EXPIRES_AT, expiresAt);
        }

        convRef.set(data, SetOptions.merge());
    }

    /**
     * Convenience method for AI buddy conversations.
     */
    public void updateConversation(String participantId,
                                   String participantName,
                                   String participantImage,
                                   String participantEmoji,
                                   String type,
                                   String lastMessage,
                                   boolean isExpired)
    {
        updateConversation(userId, participantId, participantName, participantImage,
                participantEmoji, null, type, lastMessage, isExpired, null, null);
    }


    /**
     * Convenience method for student-to-student conversations.
     */
    public void updateConversation(String participantId,
                                   String participantName,
                                   String participantImage,
                                   String type,
                                   String lastMessage,
                                   boolean isExpired,
                                   String matchId,
                                   Timestamp expiresAt)
    {
        updateConversation(userId, participantId, participantName, participantImage,
                null, null, type, lastMessage, isExpired, matchId, expiresAt);
    }

    /**
     * Updates student-to-student conversations without explicit expiration.
     */
    public void updateConversation(String participantId,
                                   String participantName,
                                   String participantImage,
                                   String type,
                                   String lastMessage,
                                   boolean isExpired,
                                   String matchId)
    {
        updateConversation(userId, participantId, participantName, participantImage,
                null, null, type, lastMessage, isExpired, matchId, null);
    }
}
