package com.example.studybuddy.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.studybuddy.R;
import com.example.studybuddy.activities.StudentChatActivity;
import com.example.studybuddy.profile.MatchManager;
import com.example.studybuddy.utils.MyConstants;

public class IncomingMatchDialog extends DialogFragment
{
    private static final String ARG_MATCH_ID = "match_id";
    private static final String ARG_OTHER_NAME = "other_name";
    private static final String ARG_OTHER_ID = "other_id";
    private static final String ARG_SUBJECT = "subject";

    public static IncomingMatchDialog newInstance(String matchId, String otherUserId, String otherUserName, String subject)
    {
        IncomingMatchDialog dialog = new IncomingMatchDialog();
        Bundle args = new Bundle();
        args.putString(ARG_MATCH_ID, matchId);
        args.putString(ARG_OTHER_ID, otherUserId);
        args.putString(ARG_OTHER_NAME, otherUserName);
        args.putString(ARG_SUBJECT, subject);
        dialog.setArguments(args);
        dialog.setCancelable(false);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        String matchId = requireArguments().getString(ARG_MATCH_ID, "");
        String otherUserId = requireArguments().getString(ARG_OTHER_ID, "");
        String otherName = requireArguments().getString(ARG_OTHER_NAME, getString(R.string.student_default_name));
        String subject = requireArguments().getString(ARG_SUBJECT, "");

        String message = getString(R.string.incoming_match_message, otherName, subject);

        MatchManager matchManager = new MatchManager();

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.new_buddy_title)
                .setMessage(message)
                .setPositiveButton(R.string.accept, (dialog, which) ->
                        matchManager.acceptMatch(matchId, new MatchManager.SimpleCallback()
                        {
                            @Override
                            public void onSuccess()
                            {
                                if (getActivity() == null)
                                {
                                    return;
                                }

                                Intent intent = new Intent(getActivity(), StudentChatActivity.class);
                                intent.putExtra(MyConstants.EXTRA_MATCH_ID, matchId);
                                intent.putExtra(MyConstants.EXTRA_USER_ID, otherUserId);
                                intent.putExtra(MyConstants.EXTRA_USER_NAME, otherName);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(String error)
                            {
                                dismiss();
                            }
                        })
                )
                .setNegativeButton(R.string.decline, (dialog, which) ->
                        matchManager.declineMatch(matchId, new MatchManager.SimpleCallback()
                        {
                            @Override
                            public void onSuccess()
                            {
                                dismiss();
                            }

                            @Override
                            public void onError(String error)
                            {
                                dismiss();
                            }
                        })
                )
                .create();
    }
}
