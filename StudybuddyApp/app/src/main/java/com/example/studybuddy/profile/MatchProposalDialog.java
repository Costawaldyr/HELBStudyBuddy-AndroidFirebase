package com.example.studybuddy.profile;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.studybuddy.R;
import com.example.studybuddy.activities.StudentChatActivity;
import com.example.studybuddy.models.Match;
import com.example.studybuddy.profile.MatchManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * MatchProposalDialog — Shown to the INITIATOR after MatchingService finds a candidate.
 *
 * Flow:
 *   1. User confirms → createMatch() writes a PENDING document to Firestore.
 *   2. A real-time listener watches that document.
 *   3. When the receiver accepts → status becomes ACTIVE → chat opens automatically.
 *   4. If the dialog is dismissed before that, the listener is cleaned up.
 *
 * AI-assisted (Claude) — reviewed by Waldyr Costa Dos Santos Lima
 */
public class MatchProposalDialog extends DialogFragment
{

    private static final String TAG            = "MatchProposalDialog";
    private static final String ARG_OTHER_ID   = "other_id";
    private static final String ARG_OTHER_NAME = "other_name";
    private static final String ARG_SUBJECT    = "subject";

    private ListenerRegistration matchStatusListener;

    public interface OnMatchCreatedListener
    {
        void onMatchCreated();
    }

    private OnMatchCreatedListener matchCreatedListener;

    public void setOnMatchCreatedListener(OnMatchCreatedListener listener)
    {
        this.matchCreatedListener = listener;
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    public static MatchProposalDialog newInstance(String otherUserId,
                                                  String otherUserName,
                                                  String subject)
    {
        MatchProposalDialog dialog = new MatchProposalDialog();
        Bundle args = new Bundle();
        args.putString(ARG_OTHER_ID,   otherUserId);
        args.putString(ARG_OTHER_NAME, otherUserName);
        args.putString(ARG_SUBJECT,    subject);
        dialog.setArguments(args);
        return dialog;
    }

    // ── Dialog build ──────────────────────────────────────────────────────────

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        String otherUserId   = requireArguments().getString(ARG_OTHER_ID,   "");
        String otherUserName = requireArguments().getString(ARG_OTHER_NAME, getString(R.string.student_default_name));
        String subject       = requireArguments().getString(ARG_SUBJECT,    "");

        String message = getString(R.string.match_proposal_message, subject, otherUserName);

        MatchManager matchManager = new MatchManager();

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.buddy_found_title)
                .setMessage(message)
                .setPositiveButton(R.string.yes_contact, (dialog, which) ->
                {
                    matchManager.createMatch(
                            otherUserId,
                            otherUserName,
                            subject,
                            new MatchManager.CreateCallback()
                            {
                                @Override
                                public void onSuccess(String matchId)
                                {
                                    if (matchCreatedListener != null)
                                    {
                                        matchCreatedListener.onMatchCreated();
                                    }
                                    listenForAcceptance(matchId, otherUserId, otherUserName);
                                }

                                @Override
                                public void onError(String error)
                                {
                                    Log.e(TAG, "Failed to create match: " + error);
                                }
                            }
                    );
                })
                .setNegativeButton(R.string.no_thanks, (dialog, which) -> dismiss())
                .create();
    }

    // ── Listen for the receiver's acceptance ─────────────────────────────────

    /**
     * Watches the match document in real time.
     * When status == ACTIVE (receiver accepted), opens StudentChatActivity immediately.
     */
    private void listenForAcceptance(String matchId, String otherUserId, String otherUserName)
    {
        DocumentReference matchRef = FirebaseFirestore.getInstance()
                .collection(com.example.studybuddy.utils.MyConstants.COLLECTION_MATCHES)
                .document(matchId);

        matchStatusListener = matchRef.addSnapshotListener((snapshot, error) ->
        {
            if (error != null || snapshot == null || !snapshot.exists())
            {
                return;
            }

            String status = snapshot.getString(com.example.studybuddy.utils.MyConstants.FIELD_STATUS);

            if (com.example.studybuddy.utils.MyConstants.STATUS_ACTIVE.equals(status))
            {
                if (matchStatusListener != null)
                {
                    matchStatusListener.remove();
                    matchStatusListener = null;
                }
                if (getActivity() == null || !isAdded())
                {
                    return;
                }

                Intent intent = new Intent(getActivity(), StudentChatActivity.class);
                intent.putExtra(com.example.studybuddy.utils.MyConstants.EXTRA_MATCH_ID,  matchId);
                intent.putExtra(com.example.studybuddy.utils.MyConstants.EXTRA_USER_ID,   otherUserId);
                intent.putExtra(com.example.studybuddy.utils.MyConstants.EXTRA_USER_NAME, otherUserName);
                startActivity(intent);
                dismiss();

            }
            else if (com.example.studybuddy.utils.MyConstants.STATUS_DECLINED.equals(status))
            {
                if (matchStatusListener != null)
                {
                    matchStatusListener.remove();
                    matchStatusListener = null;
                }
            }
        });
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (matchStatusListener != null)
        {
            matchStatusListener.remove();
            matchStatusListener = null;
        }
    }
}