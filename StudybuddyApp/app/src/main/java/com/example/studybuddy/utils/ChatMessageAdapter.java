package com.example.studybuddy.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.models.ChatMessage;

import java.util.List;

/**
 * Adapter for rendering chat messages.
 */
public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder>
{
    private final List<ChatMessage> messages;
    private OnMessageLongClickListener longClickListener;

    public interface OnMessageLongClickListener
    {
        void onMessageLongClick(ChatMessage message);
    }

    public void setOnMessageLongClickListener(OnMessageLongClickListener listener)
    {
        this.longClickListener = listener;
    }

    public ChatMessageAdapter(List<ChatMessage> messages)
    {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position)
    {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        int layout;
        if (viewType == ChatMessage.TYPE_USER)
        {
            layout = R.layout.item_message_user;
        }
        else if (viewType == ChatMessage.TYPE_BUDDY_TYPING)
        {
            layout = R.layout.item_message_typing;
        }
        else
        {
            layout = R.layout.item_message_buddy;
        }

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new MessageViewHolder(view, longClickListener, messages);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position)
    {
        ChatMessage message = messages.get(position);
        if (holder.tvMessage != null)
        {
            holder.tvMessage.setText(message.getText());
        }
        if (holder.tvTime != null)
        {
            holder.tvTime.setText(message.getTimestamp());
        }
    }

    @Override
    public int getItemCount()
    {
        return (messages != null) ? messages.size() : MyConstants.ZERO;
    }

    /**
     * ViewHolder for chat message items.
     */
    static class MessageViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvMessage, tvTime;

        MessageViewHolder(@NonNull View itemView, OnMessageLongClickListener listener, List<ChatMessage> messages)
        {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_text);
            tvTime = itemView.findViewById(R.id.tv_message_time);

            itemView.setOnLongClickListener(v ->
            {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION)
                {
                    listener.onMessageLongClick(messages.get(position));
                    return true;
                }
                return false;
            });
        }
    }
}
