package com.example.studybuddy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.activities.StudentChatActivity;
import com.example.studybuddy.adapters.MatchAdapter;
import com.example.studybuddy.models.Match;
import com.example.studybuddy.profile.MatchManager;
import com.example.studybuddy.profile.MatchProposalDialog;
import com.example.studybuddy.service.MatchingService;
import com.example.studybuddy.utils.MyConstants;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * MatchFragment — Displays active study matches and listens for incoming match requests.
 *
 * Two responsibilities:
 *   1. Show the list of currently ACTIVE matches (real people to chat with).
 *   2. Listen in real time for PENDING matches where the current user is the receiver,
 *      and show IncomingMatchDialog so they can accept or decline.
 *
 * The "Find a partner" button triggers MatchingService to find a candidate
 * and then shows MatchProposalDialog to confirm.
 */
public class MatchFragment extends Fragment
{
    private RecyclerView recyclerView;
    private MatchAdapter adapter;
    private TextView tvEmpty;
    private MaterialButton btnFindPartner;

    private final List<Match> matchList = new ArrayList<>();
    private Handler refreshHandler;
    private Runnable refreshRunnable;

    private MatchManager matchManager;
    private ListenerRegistration incomingMatchRegistration;

    private final List<String> shownMatchIds = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_match, container, false);

        matchManager = new MatchManager();

        initViews(view);
        setupAdapter();
        setupListeners();
        loadMatches();
        startIncomingMatchListener();
        startPeriodicRefresh();

        return view;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (incomingMatchRegistration != null)
        {
            incomingMatchRegistration.remove();
        }

        if (refreshHandler != null && refreshRunnable != null)
        {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        loadMatches();
    }

    private void initViews(View view)
    {
        recyclerView = view.findViewById(R.id.recycler_view);
        tvEmpty = view.findViewById(R.id.tv_empty);
        btnFindPartner = view.findViewById(R.id.btn_find_partner);
    }

    private void setupAdapter()
    {
        adapter = new MatchAdapter(match ->
        {
            String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String otherUserId = match.getOtherUserId(currentUid);

            Intent intent = new Intent(getActivity(), StudentChatActivity.class);
            intent.putExtra(MyConstants.EXTRA_MATCH_ID, match.getMatchId());
            intent.putExtra(MyConstants.EXTRA_USER_ID, otherUserId);
            intent.putExtra(MyConstants.EXTRA_USER_NAME, match.getName());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners()
    {
        if (btnFindPartner != null)
        {
            btnFindPartner.setOnClickListener(v -> findPotentialMatch());
        }
    }

    private void loadMatches()
    {
        matchManager.getAllMyMatches(new MatchManager.MatchesCallback()
        {
            @Override
            public void onSuccess(List<Match> matches)
            {
                if (!isAdded())
                {
                    return;
                }

                matchList.clear();
                matchList.addAll(matches);
                refreshUI();
            }

            @Override
            public void onError(String error)
            {
                if (!isAdded())
                {
                    return;
                }
                showEmpty("Loading error");
            }
        });
    }

    private void refreshUI()
    {
        if (matchList.isEmpty())
        {
            showEmpty("No active matches at the moment.\nTap the button below to find a partner!");
        }
        else
        {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setMatches(matchList);
        }
    }

    private void showEmpty(String message)
    {
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void startIncomingMatchListener()
    {
        incomingMatchRegistration = matchManager.listenForIncomingMatches(match ->
        {
            if (!isAdded())
            {
                return;
            }

            String matchId = match.getMatchId();

            if (shownMatchIds.contains(matchId))
            {
                return;
            }
            shownMatchIds.add(matchId);

            String otherUserId = match.getOtherUserId(
                    FirebaseAuth.getInstance().getCurrentUser().getUid());

            String otherName = match.getName() != null ? match.getName() : MyConstants.DEFAULT_STUDENT_NAME;
            String subject = match.getSubject() != null ? match.getSubject() : MyConstants.DEFAULT_SUBJECT;

            IncomingMatchDialog dialog = IncomingMatchDialog.newInstance(
                    matchId, otherUserId, otherName, subject);

            dialog.show(getParentFragmentManager(), "IncomingMatch_" + matchId);

            new Handler().postDelayed(MatchFragment.this::loadMatches, MyConstants.UI_REFRESH_DELAY_MS);
        });
    }

    private void findPotentialMatch()
    {
        if (btnFindPartner != null)
        {
            btnFindPartner.setEnabled(false);
        }

        MatchingService matchingService = new MatchingService(getContext());
        matchingService.findPotentialMatches(new MatchingService.MatchingCallback()
        {
            @Override
            public void onMatchFound(String otherUserId, String subject)
            {
                if (!isAdded())
                {
                    return;
                }
                if (btnFindPartner != null)
                {
                    btnFindPartner.setEnabled(true);
                }

                FirebaseFirestore.getInstance()
                        .collection(MyConstants.COLLECTION_USERS)
                        .document(otherUserId)
                        .get()
                        .addOnSuccessListener(doc ->
                        {
                            if (!isAdded())
                            {
                                return;
                            }
                            String name = doc.getString(MyConstants.FIELD_NAME);
                            if (name == null)
                            {
                                name = MyConstants.DEFAULT_STUDENT_NAME;
                            }

                            MatchProposalDialog dialog =
                                    MatchProposalDialog.newInstance(otherUserId, name, subject);
                            dialog.setOnMatchCreatedListener(() ->
                            {
                                new Handler().postDelayed(MatchFragment.this::loadMatches, MyConstants.MATCH_CREATION_DELAY_MS);
                            });
                            dialog.show(getParentFragmentManager(), "MatchProposal");
                        });
            }

            @Override
            public void onNoMatchFound()
            {
                if (!isAdded())
                {
                    return;
                }
                if (btnFindPartner != null)
                {
                    btnFindPartner.setEnabled(true);
                }
                showEmpty("No partners available for your subjects.\nTry again later!");
            }

            @Override
            public void onError(String error)
            {
                if (!isAdded())
                {
                    return;
                }
                if (btnFindPartner != null)
                {
                    btnFindPartner.setEnabled(true);
                }
                showEmpty("Error during search.");
            }
        });
    }

    private void startPeriodicRefresh()
    {
        refreshHandler = new Handler();
        refreshRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                if (getActivity() != null)
                {
                    adapter.notifyDataSetChanged();
                    refreshHandler.postDelayed(this, MyConstants.REFRESH_INTERVAL_MS);
                }
            }
        };
        refreshHandler.postDelayed(refreshRunnable, MyConstants.REFRESH_INTERVAL_MS);
    }

    public void refreshMatches()
    {
        loadMatches();
    }
}
