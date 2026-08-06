package com.example.studybuddy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.models.Conversation;
import com.example.studybuddy.utils.AvatarLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Horizontal carousel adapter showing the avatars of the most-recent conversations.
 * Tapping an avatar allows the user to quickly navigate to the corresponding chat.
 */
public class TopUsersAdapter extends RecyclerView.Adapter<TopUsersAdapter.VH>
{
    private List<Conversation> items = new ArrayList<>();
    private OnTopUserClickListener listener;

    /**
     * Interface for handling click events on the top user avatars.
     */
    public interface OnTopUserClickListener
    {
        void onTopUserClick(Conversation conversation);
    }

    /**
     * Sets the click listener for the carousel items.
     *
     * @param listener The listener to handle click events.
     */
    public void setOnTopUserClickListener(OnTopUserClickListener listener)
    {
        this.listener = listener;
    }

    /**
     * Updates the data set for the carousel.
     *
     * @param list The list of recent conversations to display.
     */
    public void setItems(List<Conversation> list)
    {
        this.items = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for top user avatar items.
     */
    static class VH extends RecyclerView.ViewHolder
    {
        final ImageView img;

        VH(View v)
        {
            super(v);
            img = v.findViewById(R.id.imgTopUser);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos)
    {
        Conversation conversation = items.get(pos);

        boolean isAi = conversation.isAi();
        if (isAi)
        {
            AvatarLoader.loadBuddyAvatar(
                    h.itemView.getContext(),
                    conversation.getParticipantImage(),
                    conversation.getParticipantEmoji(),
                    conversation.getParticipantColor(),
                    h.img);
        }
        else
        {
            AvatarLoader.loadInto(
                    h.itemView.getContext(),
                    conversation.getParticipantId(),
                    conversation.getParticipantImage(),
                    h.img);
        }

        h.itemView.setOnClickListener(v ->
        {
            if (listener != null)
            {
                listener.onTopUserClick(conversation);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }
}
