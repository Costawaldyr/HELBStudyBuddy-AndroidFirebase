package com.example.studybuddy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.activities.BuddyChatActivity;
import com.example.studybuddy.activities.StudentChatActivity;
import com.example.studybuddy.adapters.ConversationAdapter;
import com.example.studybuddy.adapters.ConversationCardAdapter;
import com.example.studybuddy.adapters.TopUsersAdapter;
import com.example.studybuddy.data.ConversationManager;
import com.example.studybuddy.models.Buddy;
import com.example.studybuddy.models.Conversation;
import com.example.studybuddy.models.ConversationItem;
import com.example.studybuddy.models.Match;
import com.example.studybuddy.utils.MyConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.*;


/**
 * Fragment that displays a list of active conversations (both with AI buddies and other students).
 * Includes a horizontal "top conversations" carousel and a vertical list of all chats.
 */
public class MessageListFragment extends Fragment implements ConversationAdapter.OnConversationClickListener
{
    private RecyclerView rvTopUsers, rvConversations;
    private ConversationAdapter conversationAdapter;
    private TopUsersAdapter topUsersAdapter;

    private List<Conversation> allConversations = new ArrayList<>();
    private List<Conversation> topUsers = new ArrayList<>();

    private ConversationManager conversationManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_message_list, container, false);

        rvTopUsers = view.findViewById(R.id.rvTopUsers);
        rvConversations = view.findViewById(R.id.rvConversations);

        rvTopUsers.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );

        rvConversations.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        conversationAdapter = new ConversationAdapter();
        conversationAdapter.setOnConversationClickListener(this);

        topUsersAdapter = new TopUsersAdapter();
        topUsersAdapter.setOnTopUserClickListener(this::onConversationClick);

        rvConversations.setAdapter(conversationAdapter);
        rvTopUsers.setAdapter(topUsersAdapter);

        conversationManager = new ConversationManager();

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        loadConversations();
    }

    /**
     * Fetches all conversations from the ConversationManager and updates the UI.
     * Sorts conversations by recency and identifies top users for the carousel.
     */
    private void loadConversations()
    {
        conversationManager.getAllConversations((value, error) ->
        {
            if (error != null)
            {
                return;
            }

            if (value == null)
            {
                return;
            }

            allConversations = value.toObjects(Conversation.class);

            Collections.sort(allConversations, (a, b) ->
                    Long.compare(recencyScore(b), recencyScore(a))
            );

            List<Conversation> ranked = new ArrayList<>(allConversations);
            Collections.sort(ranked, (a, b) ->
            {
                int byCount = Long.compare(b.getMessageCount(), a.getMessageCount());
                if (byCount != MyConstants.ZERO)
                {
                    return byCount;
                }
                return Long.compare(recencyScore(b), recencyScore(a));
            });

            topUsers = ranked.size() > MyConstants.MAX_TOP_USERS
                    ? new ArrayList<>(ranked.subList(MyConstants.ZERO, MyConstants.MAX_TOP_USERS))
                    : ranked;

            topUsersAdapter.setItems(topUsers);
            conversationAdapter.setConversations(allConversations);
        });
    }

    /**
     * Calculates the recency score based on the last message timestamp.
     */
    private long recencyScore(Conversation c)
    {
        if (c.getLastTimestamp() == null)
        {
            return MyConstants.ZERO;
        }
        return c.getLastTimestamp().toDate().getTime();
    }

    @Override
    public void onConversationClick(Conversation conversation)
    {
        if (conversation.isAi())
        {
            openAiChat(conversation);
        }
        else
        {
            openUserChat(conversation);
        }
    }

    @Override
    public void onConversationLongClick(Conversation conversation)
    {
    }

    /**
     * Opens the AI Buddy chat activity with the selected conversation details.
     */
    private void openAiChat(Conversation c)
    {
        Intent i = new Intent(getContext(), BuddyChatActivity.class);
        i.putExtra(MyConstants.EXTRA_BUDDY_ID, c.getParticipantId());
        i.putExtra(MyConstants.EXTRA_BUDDY_NAME, c.getParticipantName());
        i.putExtra(MyConstants.EXTRA_COURSE_EMOJI, c.getParticipantEmoji());
        i.putExtra(MyConstants.EXTRA_COURSE_COLOR, c.getParticipantColor());
        i.putExtra(MyConstants.EXTRA_BUDDY_IMAGE_URL, c.getParticipantImage());
        startActivity(i);
    }

    /**
     * Opens the Student chat activity. Resolves matchId if missing.
     */
    private void openUserChat(Conversation c)
    {
        if (c.getMatchId() != null && !c.getMatchId().isEmpty())
        {
            launchStudentChat(c, c.getMatchId());
            return;
        }

        resolveMatchIdAndOpen(c);
    }

    /**
     * Looks up any match document linking the current user to the
     * conversation participant.
     */
    private void resolveMatchIdAndOpen(Conversation c)
    {
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid == null || c.getParticipantId() == null)
        {
            Toast.makeText(getContext(), "Match not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection(MyConstants.COLLECTION_MATCHES)
                .get()
                .addOnSuccessListener(snaps ->
                {
                    String foundMatchId = null;
                    com.google.firebase.Timestamp latest = null;

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snaps)
                    {
                        String u1 = doc.getString(MyConstants.FIELD_USER_ID_1);
                        String u2 = doc.getString(MyConstants.FIELD_USER_ID_2);
                        boolean involvesPair =
                                (currentUid.equals(u1) && c.getParticipantId().equals(u2))
                                        || (currentUid.equals(u2) && c.getParticipantId().equals(u1));
                        if (!involvesPair)
                        {
                            continue;
                        }

                        com.google.firebase.Timestamp matched = doc.getTimestamp(MyConstants.FIELD_MATCHED_AT);
                        if (foundMatchId == null
                                || (matched != null && (latest == null || matched.compareTo(latest) > MyConstants.ZERO)))
                        {
                            foundMatchId = doc.getId();
                            latest = matched;
                        }
                    }

                    if (foundMatchId == null)
                    {
                        Toast.makeText(getContext(), "Match not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new com.example.studybuddy.data.ConversationManager().updateConversation(
                            c.getParticipantId(),
                            c.getParticipantName(),
                            c.getParticipantImage(),
                            "user",
                            c.getLastMessage() != null ? c.getLastMessage() : "",
                            c.isExpired(),
                            foundMatchId);

                    launchStudentChat(c, foundMatchId);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Match not found", Toast.LENGTH_SHORT).show());
    }

    /**
     * Launches the StudentChatActivity with the provided conversation and match ID.
     */
    private void launchStudentChat(Conversation c, String matchId)
    {
        Intent i = new Intent(getContext(), StudentChatActivity.class);
        i.putExtra(MyConstants.EXTRA_MATCH_ID, matchId);
        i.putExtra(MyConstants.EXTRA_USER_ID, c.getParticipantId());
        i.putExtra(MyConstants.EXTRA_USER_NAME, c.getParticipantName());
        startActivity(i);
    }
}
