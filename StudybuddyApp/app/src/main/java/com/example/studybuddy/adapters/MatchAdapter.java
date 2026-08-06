package com.example.studybuddy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.models.Match;
import com.example.studybuddy.utils.AvatarLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * RecyclerView adapter for the "Matches" tab.
 * Renders both active and expired matches with status indicators.
 */
public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder>
{
    private static final String DEFAULT_SCHOOL = "HELB";
    private static final String DEFAULT_LOCATION = "Location not specified";
    private static final int COLOR_ACTIVE_TEXT = 0xFF0F5132;
    private static final int COLOR_EXPIRED_TEXT = 0xFF374151;
    private static final float ALPHA_ACTIVE = 1.0f;
    private static final float ALPHA_EXPIRED = 0.5f;

    private List<Match> matches = new ArrayList<>();
    private final OnMatchClickListener listener;
    private final String currentUid;

    /**
     * Interface for handling interactions with matches.
     */
    public interface OnMatchClickListener
    {
        /**
         * Triggered when the user taps the chat button on an active match.
         *
         * @param match The match associated with the chat.
         */
        void onChatClick(Match match);
    }

    /**
     * Initializes the adapter and retrieves the current user's UID for identification.
     *
     * @param listener The listener to handle chat requests.
     */
    public MatchAdapter(OnMatchClickListener listener)
    {
        this.listener = listener;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.currentUid = (user != null) ? user.getUid() : "";
    }

    /**
     * Updates the list of matches displayed in the RecyclerView.
     *
     * @param matches The new list of matches.
     */
    public void setMatches(List<Match> matches)
    {
        this.matches = (matches != null) ? matches : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position)
    {
        holder.bind(matches.get(position));
    }

    @Override
    public int getItemCount()
    {
        return matches.size();
    }

    /**
     * ViewHolder for individual match items.
     */
    class MatchViewHolder extends RecyclerView.ViewHolder
    {
        private final CircleImageView imgAvatar;
        private final TextView tvName, tvSchool, tvSubject, tvLocation, tvExpiry, tvStatus;
        private final View btnChat;

        MatchViewHolder(@NonNull View itemView)
        {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_match_avatar);
            tvName = itemView.findViewById(R.id.tv_match_name);
            tvSchool = itemView.findViewById(R.id.tv_match_school);
            tvSubject = itemView.findViewById(R.id.tv_match_subject);
            tvLocation = itemView.findViewById(R.id.tv_match_location);
            tvExpiry = itemView.findViewById(R.id.tv_expiry_time);
            tvStatus = itemView.findViewById(R.id.tv_match_status);
            btnChat = itemView.findViewById(R.id.btn_match_chat);
        }

        /**
         * Binds match data to the UI components.
         *
         * @param match The match model to display.
         */
        void bind(Match match)
        {
            tvName.setText(match.getName());

            String school = match.getSchool();
            tvSchool.setText((school != null && !school.isEmpty()) ? school : DEFAULT_SCHOOL);

            tvSubject.setText(String.format("Reviewing: %s", match.getSubject()));

            String location = match.getLocation();
            tvLocation.setText((location != null && !location.isEmpty())
                    ? location : DEFAULT_LOCATION);

            tvExpiry.setText(match.getTimeRemainingText());

            String otherUserId = match.getOtherUserId(currentUid);
            AvatarLoader.loadInto(
                    itemView.getContext(),
                    otherUserId,
                    match.getProfileImageUrl(),
                    imgAvatar);

            applyStatusVisuals(match);
        }

        private void applyStatusVisuals(Match match)
        {
            boolean isActive = match.isCurrentlyActive();

            if (isActive)
            {
                tvStatus.setText(R.string.match_status_active);
                tvStatus.setBackgroundResource(R.drawable.bg_status_active);
                tvStatus.setTextColor(COLOR_ACTIVE_TEXT);

                btnChat.setEnabled(true);
                btnChat.setAlpha(ALPHA_ACTIVE);
                btnChat.setOnClickListener(v ->
                {
                    if (listener != null)
                    {
                        listener.onChatClick(match);
                    }
                });
            }
            else
            {
                tvStatus.setText(R.string.match_status_expired);
                tvStatus.setBackgroundResource(R.drawable.bg_status_expired);
                tvStatus.setTextColor(COLOR_EXPIRED_TEXT);

                btnChat.setEnabled(false);
                btnChat.setAlpha(ALPHA_EXPIRED);
                btnChat.setOnClickListener(v -> Toast.makeText(
                                itemView.getContext(),
                                R.string.match_chat_unavailable,
                                Toast.LENGTH_SHORT)
                        .show());
            }
        }
    }
}
