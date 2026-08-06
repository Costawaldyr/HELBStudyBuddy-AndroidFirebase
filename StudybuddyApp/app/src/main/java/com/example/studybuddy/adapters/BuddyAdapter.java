package com.example.studybuddy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.models.Buddy;
import com.example.studybuddy.models.Match;
import com.example.studybuddy.utils.AvatarLoader;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter that displays both AI Study Buddies and matched human users in a horizontal list.
 * This adapter uses a single list of Objects to polymorphicly handle Buddy and Match models.
 */
public class BuddyAdapter extends RecyclerView.Adapter<BuddyAdapter.ViewHolder>
{
    private static final String AI_BADGE = "🤖";
    private static final String MATCH_BADGE = "👤";

    private List<Object> items = new ArrayList<>();
    private OnBuddyClickListener listener;

    /**
     * Interface to handle clicks on AI buddies or matched users.
     */
    public interface OnBuddyClickListener
    {
        void onAIBuddyClick(Buddy buddy);
        void onMatchClick(Match match);
    }

    /**
     * Sets the listener for buddy/match click events.
     *
     * @param listener The listener implementation.
     */
    public void setOnBuddyClickListener(OnBuddyClickListener listener)
    {
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_buddy_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        Object item = items.get(position);
        if (item instanceof Buddy)
        {
            holder.bindAI((Buddy) item);
        }
        else if (item instanceof Match)
        {
            holder.bindMatch((Match) item);
        }
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    /**
     * ViewHolder class for buddy and match items.
     */
    class ViewHolder extends RecyclerView.ViewHolder
    {
        private final CircleImageView imgBuddy;
        private final TextView txtName, txtBadge, txtCountdown;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            imgBuddy = itemView.findViewById(R.id.imgBuddy);
            txtName = itemView.findViewById(R.id.txtName);
            txtBadge = itemView.findViewById(R.id.txtBadge);
            txtCountdown = itemView.findViewById(R.id.txtCountdown);
        }

        /**
         * Binds an AI Buddy model to the view.
         *
         * @param buddy The AI Buddy to display.
         */
        public void bindAI(Buddy buddy)
        {
            txtName.setText(buddy.getName());
            txtBadge.setText(AI_BADGE);
            txtCountdown.setVisibility(View.GONE);

            AvatarLoader.loadBuddyAvatar(
                    itemView.getContext(),
                    buddy.getImageUrl(),
                    buddy.getCourseEmoji(),
                    buddy.getCourseColor(),
                    imgBuddy
            );

            itemView.setOnClickListener(v ->
            {
                if (listener != null)
                {
                    listener.onAIBuddyClick(buddy);
                }
            });
        }

        /**
         * Binds a human Match model to the view.
         *
         * @param match The Match to display.
         */
        public void bindMatch(Match match)
        {
            txtName.setText(match.getName());
            txtBadge.setText(MATCH_BADGE);
            txtCountdown.setVisibility(View.VISIBLE);
            txtCountdown.setText(match.getTimeRemainingText());

            Glide.with(itemView.getContext())
                    .load(match.getProfileImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imgBuddy);

            itemView.setOnClickListener(v ->
            {
                boolean hasListener = (listener != null);
                if (hasListener)
                {
                    listener.onMatchClick(match);
                }
            });
        }
    }
}
