package com.example.studybuddy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.models.MatchCandidate;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Adapter responsible for displaying potential study partners in a list.
 * Provides interactions for accepting or rejecting match candidates.
 */
public class AvailableMatchAdapter extends RecyclerView.Adapter<AvailableMatchAdapter.ViewHolder>
{
    private static final int INDEX_NOT_FOUND = -1;
    private static final int START_POSITION = 0;

    private final List<MatchCandidate> candidates = new ArrayList<>();
    private final OnMatchDecisionListener listener;

    /**
     * Interface for handling match decision events.
     */
    public interface OnMatchDecisionListener
    {
        void onAccept(MatchCandidate candidate);
        void onReject(MatchCandidate candidate);
    }

    /**
     * Initializes the adapter with a listener for match decisions.
     *
     * @param listener The listener to handle accept/reject actions.
     */
    public AvailableMatchAdapter(OnMatchDecisionListener listener)
    {
        this.listener = listener;
    }

    /**
     * Updates the data set of match candidates.
     *
     * @param newCandidates The new list of candidates to display.
     */
    public void setCandidates(List<MatchCandidate> newCandidates)
    {
        candidates.clear();
        boolean hasNewData = (newCandidates != null && !newCandidates.isEmpty());
        if (hasNewData)
        {
            candidates.addAll(newCandidates);
        }
        notifyDataSetChanged();
    }

    /**
     * Removes a specific candidate from the list.
     * Usually called after a decision has been made for that candidate.
     *
     * @param candidate The candidate to remove.
     */
    public void removeCandidate(MatchCandidate candidate)
    {
        String targetUserId = candidate.getUserId();
        int foundIndex = findIndexById(targetUserId);

        boolean isFound = (foundIndex != INDEX_NOT_FOUND);
        if (isFound)
        {
            candidates.remove(foundIndex);
            notifyItemRemoved(foundIndex);
        }
    }

    /**
     * Finds the index of a candidate by their user ID.
     *
     * @param userId The ID of the user to find.
     * @return The index in the list, or INDEX_NOT_FOUND if not present.
     */
    private int findIndexById(String userId)
    {
        for (int i = START_POSITION; i < candidates.size(); i++)
        {
            MatchCandidate current = candidates.get(i);
            boolean isSameUser = current.getUserId().equals(userId);
            if (isSameUser)
            {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_available_match, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        MatchCandidate candidate = candidates.get(position);

        holder.tvName.setText(candidate.getName());
        holder.tvDescription.setText(candidate.getDescriptionText(holder.itemView.getContext()));

        String imageUrl = candidate.getProfileImageUrl();
        boolean hasImage = (imageUrl != null && !imageUrl.isEmpty());

        if (hasImage)
        {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(holder.imgAvatar);
        }
        else
        {
            holder.imgAvatar.setImageResource(R.drawable.ic_profile);
        }

        holder.btnAccept.setOnClickListener(v -> listener.onAccept(candidate));
        holder.btnReject.setOnClickListener(v -> listener.onReject(candidate));
    }

    @Override
    public int getItemCount()
    {
        return candidates.size();
    }

    /**
     * ViewHolder class for match candidate items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView imgAvatar;
        TextView tvName, tvDescription;
        MaterialButton btnAccept, btnReject;

        ViewHolder(View itemView)
        {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_candidate_avatar);
            tvName = itemView.findViewById(R.id.tv_candidate_name);
            tvDescription = itemView.findViewById(R.id.tv_candidate_description);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }
}
