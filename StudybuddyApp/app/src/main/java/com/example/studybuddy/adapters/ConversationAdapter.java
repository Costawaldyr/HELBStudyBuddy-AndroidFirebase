package com.example.studybuddy.adapters;

import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.models.Conversation;
import com.example.studybuddy.utils.AvatarLoader;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter for displaying a list of conversations.
 * Handles both AI buddy chats and human student matches.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder>
{
    private List<Conversation> conversations = new ArrayList<>();
    private OnConversationClickListener listener;

    /**
     * Interface for handling conversation click events.
     */
    public interface OnConversationClickListener
    {
        void onConversationClick(Conversation conversation);
        void onConversationLongClick(Conversation conversation);
    }

    /**
     * Sets the click listener for conversation items.
     *
     * @param listener The listener to handle clicks and long clicks.
     */
    public void setOnConversationClickListener(OnConversationClickListener listener)
    {
        this.listener = listener;
    }

    /**
     * Updates the list of conversations and refreshes the UI.
     *
     * @param conversations The new list of conversations.
     */
    public void setConversations(List<Conversation> conversations)
    {
        this.conversations = conversations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Conversation conversation = conversations.get(position);
        holder.bind(conversation);
    }

    @Override
    public int getItemCount()
    {
        return conversations.size();
    }

    /**
     * ViewHolder for conversation items.
     */
    class ViewHolder extends RecyclerView.ViewHolder
    {
        private static final String STATUS_AI = "🤖 AI";
        private static final String STATUS_EXPIRED = "⏳ Expired";
        private static final String STATUS_ACTIVE = "💬 Active";

        private static final float ALPHA_EXPIRED = 0.6f;
        private static final float ALPHA_FULL = 1.0f;

        private final MaterialCardView card;
        private final CircleImageView imgProfile;
        private final TextView txtName, txtLastMessage, txtTime;
        private final Chip badgeStatus;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            card = itemView.findViewById(R.id.cardConversation);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtName = itemView.findViewById(R.id.txtName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            badgeStatus = itemView.findViewById(R.id.badgeStatus);
        }

        /**
         * Binds conversation data to the view components.
         *
         * @param conversation The conversation model to display.
         */
        public void bind(Conversation conversation)
        {
            txtName.setText(conversation.getParticipantName());
            txtLastMessage.setText(conversation.getLastMessage());

            boolean hasLastTimestamp = (conversation.getLastTimestamp() != null);
            if (hasLastTimestamp)
            {
                txtTime.setText(DateUtils.getRelativeTimeSpanString(
                        conversation.getLastTimestamp().toDate().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS));
            }

            // Avatar logic based on conversation type
            boolean isAiConversation = conversation.isAi();
            if (isAiConversation)
            {
                AvatarLoader.loadBuddyAvatar(
                        itemView.getContext(),
                        conversation.getParticipantImage(),
                        conversation.getParticipantEmoji(),
                        conversation.getParticipantColor(),
                        imgProfile);
            }
            else
            {
                AvatarLoader.loadInto(
                        itemView.getContext(),
                        conversation.getParticipantId(),
                        conversation.getParticipantImage(),
                        imgProfile);
            }

            // Status badge logic
            boolean isExpiredConversation = conversation.isExpiredNow();
            if (isAiConversation)
            {
                badgeStatus.setText(STATUS_AI);
                badgeStatus.setChipBackgroundColorResource(android.R.color.holo_purple);
                badgeStatus.setTextColor(Color.WHITE);
                card.setAlpha(ALPHA_FULL);
            }
            else if (isExpiredConversation)
            {
                badgeStatus.setText(STATUS_EXPIRED);
                badgeStatus.setChipBackgroundColorResource(android.R.color.darker_gray);
                badgeStatus.setTextColor(Color.WHITE);
                card.setAlpha(ALPHA_EXPIRED);
            }
            else
            {
                badgeStatus.setText(STATUS_ACTIVE);
                badgeStatus.setChipBackgroundColorResource(android.R.color.holo_green_dark);
                badgeStatus.setTextColor(Color.WHITE);
                card.setAlpha(ALPHA_FULL);
            }

            itemView.setOnClickListener(v ->
            {
                if (listener != null)
                {
                    listener.onConversationClick(conversation);
                }
            });

            itemView.setOnLongClickListener(v ->
            {
                if (listener != null)
                {
                    listener.onConversationLongClick(conversation);
                }
                return true;
            });
        }
    }
}
