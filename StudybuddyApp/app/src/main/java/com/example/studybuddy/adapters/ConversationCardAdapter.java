package com.example.studybuddy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.models.ConversationItem;
import com.google.android.material.chip.Chip;

import java.util.List;

/**
 * Simplified adapter for displaying conversation items in a card-based layout.
 * Primarily used for quick previews or legacy UI components.
 */
public class ConversationCardAdapter extends RecyclerView.Adapter<ConversationCardAdapter.VH>
{
    private static final String DEFAULT_TIME = "now";
    private static final String BADGE_AI = "🤖 AI";
    private static final String BADGE_USER = "💬 User";

    /**
     * Interface for handling clicks on conversation cards.
     */
    public interface OnClick
    {
        void onClick(ConversationItem item);
    }

    private final List<ConversationItem> items;
    private final OnClick listener;

    /**
     * Initializes the adapter with items and a click listener.
     *
     * @param items    List of conversation items to display.
     * @param listener Click listener for item interactions.
     */
    public ConversationCardAdapter(List<ConversationItem> items, OnClick listener)
    {
        this.items = items;
        this.listener = listener;
    }

    /**
     * ViewHolder for conversation card items.
     */
    static class VH extends RecyclerView.ViewHolder
    {
        ImageView img;
        TextView name, msg, time;
        Chip badge;

        public VH(View v)
        {
            super(v);
            img = v.findViewById(R.id.imgProfile);
            name = v.findViewById(R.id.txtName);
            msg = v.findViewById(R.id.txtLastMessage);
            time = v.findViewById(R.id.txtTime);
            badge = v.findViewById(R.id.badgeStatus);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos)
    {
        ConversationItem item = items.get(pos);

        h.name.setText(item.getName());
        h.msg.setText(item.getLastMessage());

        Glide.with(h.itemView)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_profile)
                .circleCrop()
                .into(h.img);

            h.time.setText(DEFAULT_TIME);

            boolean isAi = (item.getType() == ConversationItem.TYPE_AI);
        h.badge.setText(isAi ? BADGE_AI : BADGE_USER);

        h.itemView.setOnClickListener(v ->
        {
            if (listener != null)
            {
                listener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return (items != null) ? items.size() : 0;
    }
}
