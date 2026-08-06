package com.example.studybuddy.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.data.ConversationManager;
import com.example.studybuddy.models.ChatMessage;
import com.example.studybuddy.models.Match;
import com.example.studybuddy.notification.NotificationHelper;
import com.example.studybuddy.profile.MatchManager;
import com.example.studybuddy.ui.map.ChatMapActivity;
import com.example.studybuddy.utils.ChatMessageAdapter;
import com.example.studybuddy.utils.MyConstants;
import com.example.studybuddy.utils.MyGlobals;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for peer-to-peer student chat sessions.
 */
public class StudentChatActivity extends AppCompatActivity
{
    private static final String TAG = "StudentChatActivity";
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private ImageView imgAvatar;
    private TextView tvName, tvChatStatus, tvUserStatus;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private FloatingActionButton btnSend;

    private ChatMessageAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private ListenerRegistration messagesListener;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private FirebaseFirestore db;
    private String currentUid;
    private String matchId;
    private String otherUserId;
    private String otherUserName;
    private Match currentMatch;
    private long expiryTime = MyConstants.NEGATIVE_ONE;
    private boolean isChatVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_chat);

        initFirebase();
        getIntentData();
        initViews();
        setupToolbar();
        loadMatchData();
        setupMessageListener();
        startExpiryTimer();
    }

    /**
     * Initializes Firebase and checks for a logged-in user.
     */
    private void initFirebase()
    {
        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null)
        {
            currentUid = mAuth.getCurrentUser().getUid();
        }
        else
        {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Extracts parameters from the activity intent.
     */
    private void getIntentData()
    {
        matchId = getIntent().getStringExtra(MyConstants.EXTRA_MATCH_ID);
        otherUserId = getIntent().getStringExtra(MyConstants.EXTRA_USER_ID);
        otherUserName = getIntent().getStringExtra(MyConstants.EXTRA_USER_NAME);

        if (matchId == null || matchId.isEmpty() || otherUserId == null || otherUserId.isEmpty())
        {
            Toast.makeText(this, "Chat error: missing data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Finds and initializes UI components.
     */
    private void initViews()
    {
        imgAvatar = findViewById(R.id.img_student_avatar);
        tvName = findViewById(R.id.tv_student_name);
        tvChatStatus = findViewById(R.id.tv_chat_status);
        tvUserStatus = findViewById(R.id.tv_users_status);
        rvMessages = findViewById(R.id.rv_student_messages);
        etMessage = findViewById(R.id.et_student_message);
        btnSend = findViewById(R.id.btn_student_send);
        ImageView btnOpenChatMap = findViewById(R.id.btn_open_chat_map);

        adapter = new ChatMessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        if (otherUserName != null && !otherUserName.isEmpty())
        {
            tvName.setText(otherUserName);
        }

        btnSend.setOnClickListener(v -> sendMessage());

        if (btnOpenChatMap != null)
        {
            btnOpenChatMap.setOnClickListener(v -> openChatMap());
        }

        View inputCard = findViewById(R.id.input_bar_student);
        if (inputCard != null)
        {
            inputCard.setOnClickListener(v ->
            {
                etMessage.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                {
                    imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }
    }

    /**
     * Configures the top action bar.
     */
    private void setupToolbar()
    {
        MaterialToolbar toolbar = findViewById(R.id.student_chat_toolbar);
        MyGlobals.setupToolbar(this, toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Loads the match details from Firestore.
     */
    private void loadMatchData()
    {
        db.collection(MyConstants.COLLECTION_MATCHES)
                .document(matchId)
                .get()
                .addOnSuccessListener(doc ->
                {
                    if (!doc.exists())
                    {
                        Log.e(TAG, "Match not found: " + matchId);
                        Toast.makeText(this, "Match not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    currentMatch = doc.toObject(Match.class);
                    if (currentMatch == null)
                    {
                        return;
                    }

                    Timestamp expiresAt = currentMatch.getExpiresAt();
                    if (expiresAt != null)
                    {
                        expiryTime = expiresAt.toDate().getTime();
                    }
                    else
                    {
                        expiryTime = System.currentTimeMillis() + MyConstants.ONE_DAY_MS;
                    }

                    loadProfileImage();
                    updateExpiryStatus();
                })
                .addOnFailureListener(e ->
                {
                    Log.e(TAG, "Error loading match", e);
                    Toast.makeText(this, "Error loading match", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Loads the peer's profile picture.
     */
    private void loadProfileImage()
    {
        db.collection(MyConstants.COLLECTION_USERS)
                .document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {
                    if (documentSnapshot.exists())
                    {
                        String imageUrl = documentSnapshot.getString(MyConstants.FIELD_PROFILE_IMAGE);
                        if (imageUrl != null && !imageUrl.isEmpty())
                        {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .into(imgAvatar);
                        }
                    }
                });
    }

    /**
     * Establishes a real-time listener for chat messages.
     */
    private void setupMessageListener()
    {
        String chatId = getChatId(currentUid, otherUserId);

        messagesListener = db.collection(MyConstants.COLLECTION_CHATS)
                .document(chatId)
                .collection(MyConstants.COLLECTION_MESSAGES)
                .orderBy(MyConstants.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, error) ->
                {
                    if (error != null)
                    {
                        Log.e(TAG, "Error listening for messages", error);
                        return;
                    }

                    if (snapshots != null)
                    {
                        boolean isInitialLoad = messageList.isEmpty();
                        messageList.clear();

                        for (QueryDocumentSnapshot doc : snapshots)
                        {
                            String text = doc.getString(MyConstants.FIELD_TEXT);
                            String senderId = doc.getString(MyConstants.FIELD_SENDER_ID);
                            Timestamp ts = doc.getTimestamp(MyConstants.FIELD_TIMESTAMP);

                            String timeStr = (ts != null) ? TIME_FORMAT.format(ts.toDate()) : "";
                            boolean isFromCurrentUser = currentUid.equals(senderId);
                            int type = isFromCurrentUser ? ChatMessage.TYPE_USER : ChatMessage.TYPE_BUDDY;

                            messageList.add(new ChatMessage(text, type, timeStr));

                            if (!isInitialLoad && !doc.getMetadata().hasPendingWrites() && !isFromCurrentUser && !isChatVisible)
                            {
                                String displayName = (otherUserName != null && !otherUserName.isEmpty()) ? otherUserName : MyConstants.DEFAULT_STUDENT_NAME;
                                NotificationHelper.sendMessageReceived(this, displayName, matchId);
                            }
                        }

                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty())
                        {
                            rvMessages.smoothScrollToPosition(messageList.size() - MyConstants.ONE);
                        }
                    }
                });
    }

    /**
     * Generates a unique chat identifier between two users.
     */
    private String getChatId(String uid1, String uid2)
    {
        String pair = uid1.compareTo(uid2) < MyConstants.ZERO ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
        return matchId + "_" + pair;
    }

    /**
     * Sends the text message to Firestore.
     */
    private void sendMessage()
    {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty())
        {
            return;
        }

        if (expiryTime != MyConstants.NEGATIVE_ONE && System.currentTimeMillis() > expiryTime)
        {
            Toast.makeText(this, "This chat has expired", Toast.LENGTH_SHORT).show();
            return;
        }

        etMessage.setText("");

        String chatId = getChatId(currentUid, otherUserId);

        Map<String, Object> message = new HashMap<>();
        message.put(MyConstants.FIELD_TEXT, text);
        message.put(MyConstants.FIELD_SENDER_ID, currentUid);
        message.put(MyConstants.FIELD_TIMESTAMP, FieldValue.serverTimestamp());

        db.collection(MyConstants.COLLECTION_CHATS)
                .document(chatId)
                .collection(MyConstants.COLLECTION_MESSAGES)
                .add(message)
                .addOnSuccessListener(documentReference ->
                {
                    ConversationManager convManager = new ConversationManager();
                    Timestamp matchExpiry = (currentMatch != null) ? currentMatch.getExpiresAt() : null;
                    convManager.updateConversation(otherUserId, otherUserName, null, "user", text, false, matchId, matchExpiry);
                })
                .addOnFailureListener(e ->
                {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error sending message", e);
                });
    }

    /**
     * Starts the activity for showing participant locations.
     */
    private void openChatMap()
    {
        if (currentUid == null || otherUserId == null)
        {
            Toast.makeText(this, R.string.chat_map_no_participants, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] participantUids = new String[]{currentUid, otherUserId};
        String chatTitle = (otherUserName != null && !otherUserName.isEmpty())
                ? getString(R.string.chat_map_title) + " — " + otherUserName
                : getString(R.string.chat_map_title);
        startActivity(ChatMapActivity.createIntent(this, participantUids, chatTitle));
    }

    /**
     * Initializes the background timer for chat expiration.
     */
    private void startExpiryTimer()
    {
        timerRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                updateExpiryStatus();
                timerHandler.postDelayed(this, MyConstants.TIMER_INTERVAL_CHAT_MS);
            }
        };
        timerHandler.post(timerRunnable);
    }

    /**
     * Updates the UI based on remaining chat time.
     */
    private void updateExpiryStatus()
    {
        if (expiryTime == MyConstants.NEGATIVE_ONE)
        {
            tvChatStatus.setText(R.string.chat_calculating);
            return;
        }

        long diff = expiryTime - System.currentTimeMillis();

        if (diff <= MyConstants.ZERO)
        {
            tvChatStatus.setText("Expired");
            tvUserStatus.setText("Chat closed");
            etMessage.setEnabled(false);
            btnSend.setEnabled(false);

            if (timerHandler != null && timerRunnable != null)
            {
                timerHandler.removeCallbacks(timerRunnable);
            }

            if (matchId != null)
            {
                db.collection(MyConstants.COLLECTION_MATCHES).document(matchId)
                        .update(MyConstants.FIELD_STATUS, MyConstants.STATUS_EXPIRED, MyConstants.FIELD_IS_ACTIVE, false);
            }
        }
        else
        {
            long hours = diff / (MyConstants.SIXTY * MyConstants.SIXTY * MyConstants.THOUSAND);
            long minutes = (diff % (MyConstants.SIXTY * MyConstants.SIXTY * MyConstants.THOUSAND)) / (MyConstants.SIXTY * MyConstants.THOUSAND);

            String timeLeft = (hours > MyConstants.ZERO) ? hours + "h " + minutes + "m" : minutes + " minutes";
            tvChatStatus.setText("Expires in " + timeLeft);
            tvUserStatus.setText("● Online");
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        isChatVisible = true;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        isChatVisible = false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (messagesListener != null)
        {
            messagesListener.remove();
        }

        if (timerHandler != null && timerRunnable != null)
        {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}
