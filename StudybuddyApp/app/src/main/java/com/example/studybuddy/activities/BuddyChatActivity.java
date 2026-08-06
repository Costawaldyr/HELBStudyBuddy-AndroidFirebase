package com.example.studybuddy.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.BuildConfig;
import com.example.studybuddy.R;
import com.example.studybuddy.data.ConversationManager;
import com.example.studybuddy.models.ChatMessage;
import com.example.studybuddy.notification.NotificationHelper;
import com.example.studybuddy.profile.MatchProposalDialog;
import com.example.studybuddy.service.MatchingService;
import com.example.studybuddy.utils.ChatMessageAdapter;
import com.example.studybuddy.utils.MyConstants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Activity for AI buddy chat interactions.
 */
public class BuddyChatActivity extends AppCompatActivity
{
    private TextView tvBuddyName;
    private TextView tvStatus;
    private EditText edtMessage;
    private RecyclerView rvMessages;

    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messageList;
    private List<JSONObject> conversationHistory;
    private OkHttpClient httpClient;
    private MatchingService matchingService;
    private int retryCount = MyConstants.ZERO;

    private String buddyId;
    private String buddyName;
    private String courseEmoji;
    private String systemPrompt;
    private String currentUserId;
    private FirebaseFirestore db;

    private boolean isChatVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy_chat);

        initializeDatabaseAndUser();
        findViews();
        setupChatRecyclerView();
        loadBuddyDataFromIntent();
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

    /**
     * Initializes Firestore and retrieves the current user.
     */
    private void initializeDatabaseAndUser()
    {
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
            currentUserId = user.getUid();
        }
    }

    /**
     * Links UI components and sets up listeners.
     */
    private void findViews()
    {
        ImageButton btnBack = findViewById(R.id.btn_back_chat);
        tvBuddyName = findViewById(R.id.tv_buddy_chat_name);
        tvStatus = findViewById(R.id.tv_buddy_status);
        rvMessages = findViewById(R.id.rv_messages);
        edtMessage = findViewById(R.id.et_message);
        FloatingActionButton btnSend = findViewById(R.id.btn_send_message);

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> handleSendMessage());
    }

    /**
     * Configures the message list and adapter.
     */
    private void setupChatRecyclerView()
    {
        messageList = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);
        messageAdapter.setOnMessageLongClickListener(this::saveMessageToCloud);

        httpClient = new OkHttpClient();
        matchingService = new MatchingService(this);
        conversationHistory = new ArrayList<>();
    }

    /**
     * Loads buddy data from intent or database.
     */
    private void loadBuddyDataFromIntent()
    {
        buddyId = pickExtra(MyConstants.EXTRA_BUDDY_ID, MyConstants.EXTRA_BUDDY_ID_LEGACY);
        buddyName = pickExtra(MyConstants.EXTRA_BUDDY_NAME, MyConstants.EXTRA_BUDDY_NAME_LEGACY);
        courseEmoji = getIntent().getStringExtra(MyConstants.EXTRA_COURSE_EMOJI);
        String courseName = getIntent().getStringExtra(MyConstants.EXTRA_COURSE_NAME);
        String basePrompt = getIntent().getStringExtra(MyConstants.EXTRA_SYSTEM_PROMPT);

        if (buddyName != null && !buddyName.isEmpty() && basePrompt != null && !basePrompt.isEmpty())
        {
            updateBuddyUI(buddyName, courseName, basePrompt);
            loadSavedChatHistory();
        }
        else
        {
            fetchBuddyInfoFromFirestore();
        }
    }

    /**
     * Selects between primary and legacy intent extras.
     */
    private String pickExtra(String primary, String legacy)
    {
        String value = getIntent().getStringExtra(primary);
        if (value == null || value.isEmpty())
        {
            value = getIntent().getStringExtra(legacy);
        }
        return value;
    }

    /**
     * Updates the UI with buddy information.
     */
    private void updateBuddyUI(String name, String course, String prompt)
    {
        tvBuddyName.setText(name);

        String statusText = getString(R.string.online_status);
        if (course != null && !course.isEmpty())
        {
            statusText = statusText + " · " + course;
        }
        tvStatus.setText(statusText);

        if (prompt != null && !prompt.isEmpty())
        {
            systemPrompt = prompt + MyConstants.LANGUAGE_MIRROR_INSTRUCTION;
        }
    }

    /**
     * Retrieves buddy details from Firestore.
     */
    private void fetchBuddyInfoFromFirestore()
    {
        db.collection(MyConstants.COLLECTION_USERS).document(currentUserId)
                .collection(MyConstants.COLLECTION_BUDDIES).document(buddyId)
                .get()
                .addOnSuccessListener(doc ->
                {
                    if (doc.exists())
                    {
                        buddyName = doc.getString(MyConstants.FIELD_NAME);
                        String course = doc.getString(MyConstants.FIELD_COURSE_NAME);
                        String prompt = doc.getString(MyConstants.FIELD_SYSTEM_PROMPT);
                        courseEmoji = doc.getString(MyConstants.FIELD_COURSE_EMOJI);
                        updateBuddyUI(buddyName, course, prompt);
                    }
                    loadSavedChatHistory();
                })
                .addOnFailureListener(e -> loadSavedChatHistory());
    }

    /**
     * Retrieves previous messages from Firestore.
     */
    private void loadSavedChatHistory()
    {
        db.collection(MyConstants.COLLECTION_USERS).document(currentUserId)
                .collection(MyConstants.COLLECTION_CONVERSATIONS).document(buddyId)
                .collection(MyConstants.COLLECTION_MESSAGES)
                .orderBy(MyConstants.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(results ->
                {
                    if (results.isEmpty())
                    {
                        showWelcomeMessage();
                        return;
                    }
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : results)
                    {
                        String messageText = doc.getString(MyConstants.FIELD_TEXT);
                        Long typeVal = doc.getLong(MyConstants.FIELD_TYPE);
                        int type = (typeVal != null) ? typeVal.intValue() : ChatMessage.TYPE_BUDDY;
                        addMessageToUi(messageText, type, false);
                        addMessageToAiHistory(type, messageText);
                    }
                });
    }

    /**
     * Records a message in the conversation history for AI context.
     */
    private void addMessageToAiHistory(int type, String text)
    {
        try
        {
            JSONObject historyItem = new JSONObject();
            historyItem.put(MyConstants.CLAUDE_KEY_ROLE, type == ChatMessage.TYPE_USER ? MyConstants.ROLE_USER : MyConstants.ROLE_ASSISTANT);
            historyItem.put(MyConstants.CLAUDE_KEY_CONTENT, text);
            conversationHistory.add(historyItem);
        }
        catch (Exception ignored)
        {
        }
    }

    /**
     * Saves a specific message to the user's saved collection.
     */
    private void saveMessageToCloud(ChatMessage message)
    {
        db.collection(MyConstants.COLLECTION_USERS).document(currentUserId)
                .collection(MyConstants.COLLECTION_SAVED)
                .add(message)
                .addOnSuccessListener(ref -> Toast.makeText(this, MyConstants.SAVED_TOAST, Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, MyConstants.SAVE_ERROR_TOAST, Toast.LENGTH_SHORT).show());
    }

    /**
     * Displays the initial buddy welcome message.
     */
    private void showWelcomeMessage()
    {
        String welcome = getString(R.string.buddy_opening_message, buddyName);
        addMessageToUi(welcome, ChatMessage.TYPE_BUDDY, true);
    }

    /**
     * Handles sending a new message.
     */
    private void handleSendMessage()
    {
        String text = edtMessage.getText().toString().trim();
        if (text.isEmpty())
        {
            return;
        }

        addMessageToUi(text, ChatMessage.TYPE_USER, true);
        edtMessage.setText("");
        addMessageToAiHistory(ChatMessage.TYPE_USER, text);
        addMessageToUi(MyConstants.TYPING_PLACEHOLDER, ChatMessage.TYPE_BUDDY_TYPING, false);
        requestAiResponse();
    }

    /**
     * Sends the current conversation to Claude for a response.
     */
    private void requestAiResponse()
    {
        try
        {
            JSONArray messagesLog = new JSONArray();
            for (JSONObject msg : conversationHistory)
            {
                messagesLog.put(msg);
            }

            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put(MyConstants.CLAUDE_KEY_MODEL, MyConstants.CLAUDE_MODEL);
            jsonPayload.put(MyConstants.CLAUDE_KEY_MAX_TOKENS, MyConstants.MAX_RESPONSE_TOKENS);
            jsonPayload.put(MyConstants.CLAUDE_KEY_SYSTEM, systemPrompt);
            jsonPayload.put(MyConstants.CLAUDE_KEY_MESSAGES, messagesLog);

            RequestBody body = RequestBody.create(jsonPayload.toString(), MediaType.parse(MyConstants.CONTENT_TYPE_JSON));
            Request apiRequest = new Request.Builder()
                    .url(MyConstants.CLAUDE_API_URL)
                    .addHeader(MyConstants.HEADER_API_KEY, BuildConfig.CLAUDE_API_KEY)
                    .addHeader(MyConstants.HEADER_VERSION, MyConstants.ANTHROPIC_VERSION_HEADER)
                    .addHeader(MyConstants.HEADER_CONTENT_TYPE, MyConstants.CONTENT_TYPE_JSON)
                    .post(body)
                    .build();

            httpClient.newCall(apiRequest).enqueue(new Callback()
            {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e)
                {
                    runOnUiThread(() ->
                    {
                        hideTypingIndicator();
                        addMessageToUi(getString(R.string.connection_error), ChatMessage.TYPE_BUDDY, true);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
                {
                    String rawBody = response.body().string();
                    if (response.code() == MyConstants.HTTP_STATUS_OVERLOADED)
                    {
                        handleApiOverload();
                        return;
                    }
                    retryCount = MyConstants.ZERO;
                    runOnUiThread(() -> processApiResponse(rawBody));
                }
            });
        }
        catch (Exception e)
        {
            hideTypingIndicator();
            Toast.makeText(this, MyConstants.SEND_ERROR_PREFIX + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Retries the API request if the service is overloaded.
     */
    private void handleApiOverload()
    {
        if (retryCount < MyConstants.MAX_API_RETRIES)
        {
            retryCount++;
            new Handler(Looper.getMainLooper()).postDelayed(this::requestAiResponse, MyConstants.RETRY_DELAY_MS);
            return;
        }
        retryCount = MyConstants.ZERO;
        runOnUiThread(() ->
        {
            hideTypingIndicator();
            addMessageToUi(getString(R.string.claude_overloaded), ChatMessage.TYPE_BUDDY, true);
        });
    }

    /**
     * Processes the raw JSON response from Claude.
     */
    private void processApiResponse(String jsonResponse)
    {
        hideTypingIndicator();
        try
        {
            JSONObject root = new JSONObject(jsonResponse);
            JSONArray contents = root.getJSONArray(MyConstants.CLAUDE_KEY_CONTENT);
            String replyText = contents.getJSONObject(MyConstants.FIRST_CONTENT_INDEX).getString(MyConstants.FIELD_TEXT);

            if (replyText.contains(MyConstants.MATCH_TAG))
            {
                replyText = replyText.replace(MyConstants.MATCH_TAG, "").trim();
                startSearchingForStudyPartners();
            }

            addMessageToAiHistory(ChatMessage.TYPE_BUDDY, replyText);
            addMessageToUi(replyText, ChatMessage.TYPE_BUDDY, true);

            if (!isChatVisible)
            {
                NotificationHelper.sendBuddyMotivation(this, buddyName, buddyId, replyText);
            }
        }
        catch (Exception e)
        {
            addMessageToUi(getString(R.string.understanding_problem), ChatMessage.TYPE_BUDDY, true);
        }
    }

    /**
     * Triggers the matching process for study partners.
     */
    private void startSearchingForStudyPartners()
    {
        matchingService.findPotentialMatches(new MatchingService.MatchingCallback()
        {
            @Override
            public void onMatchFound(String otherId, String subject)
            {
                db.collection(MyConstants.COLLECTION_USERS).document(otherId).get().addOnSuccessListener(doc ->
                {
                    if (!doc.exists() || isFinishing())
                    {
                        return;
                    }
                    String name = doc.getString(MyConstants.FIELD_NAME);
                    if (name == null || name.isEmpty())
                    {
                        name = getString(R.string.etudiant_default);
                    }
                    MatchProposalDialog dialog = MatchProposalDialog.newInstance(otherId, name, subject);
                    dialog.show(getSupportFragmentManager(), MyConstants.DIALOG_TAG_MATCH);
                });
            }

            @Override
            public void onNoMatchFound()
            {
            }

            @Override
            public void onError(String error)
            {
            }
        });
    }

    /**
     * Adds a message to the UI and optionally saves it.
     */
    private void addMessageToUi(String text, int type, boolean shouldSave)
    {
        messageList.add(new ChatMessage(text, type));
        int lastPos = messageList.size() - MyConstants.ONE;
        messageAdapter.notifyItemInserted(lastPos);
        rvMessages.smoothScrollToPosition(lastPos);

        if (shouldSave)
        {
            persistMessageInDatabase(text, type);
        }
    }

    /**
     * Saves a message to the conversation collection in Firestore.
     */
    private void persistMessageInDatabase(String text, int type)
    {
        Map<String, Object> data = new HashMap<>();
        data.put(MyConstants.FIELD_TEXT, text);
        data.put(MyConstants.FIELD_TYPE, type);
        data.put(MyConstants.FIELD_TIMESTAMP, FieldValue.serverTimestamp());

        db.collection(MyConstants.COLLECTION_USERS).document(currentUserId)
                .collection(MyConstants.COLLECTION_CONVERSATIONS).document(buddyId)
                .collection(MyConstants.COLLECTION_MESSAGES)
                .add(data);

        ConversationManager manager = new ConversationManager();
        manager.updateConversation(buddyId, buddyName, null, courseEmoji, MyConstants.CONVERSATION_TYPE_AI, text, false);
    }

    /**
     * Removes the typing indicator from the message list.
     */
    private void hideTypingIndicator()
    {
        for (int i = messageList.size() - MyConstants.ONE; i >= MyConstants.ZERO; i--)
        {
            if (messageList.get(i).getType() == ChatMessage.TYPE_BUDDY_TYPING)
            {
                messageList.remove(i);
                messageAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }
}
