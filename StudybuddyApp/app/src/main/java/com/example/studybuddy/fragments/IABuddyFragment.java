package com.example.studybuddy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.example.studybuddy.activities.BuddyChatActivity;
import com.example.studybuddy.adapters.IABuddyAdapter;
import com.example.studybuddy.models.Buddy;
import com.example.studybuddy.utils.MyConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the user's AI Study Buddies in a list.
 * Allows users to start a chat, delete, or edit their study buddies.
 */
public class IABuddyFragment extends Fragment
{
    private RecyclerView recyclerView;
    private IABuddyAdapter adapter;
    private final List<Buddy> buddyList = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_i_a_buddy, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new IABuddyAdapter(
                buddy ->
                {
                    Intent intent = new Intent(getActivity(), BuddyChatActivity.class);
                    intent.putExtra(MyConstants.EXTRA_BUDDY_ID, buddy.getId());
                    intent.putExtra(MyConstants.EXTRA_BUDDY_NAME, buddy.getName());
                    intent.putExtra(MyConstants.EXTRA_COURSE_NAME, buddy.getCourseName());
                    intent.putExtra(MyConstants.EXTRA_COURSE_EMOJI, buddy.getCourseEmoji());
                    intent.putExtra(MyConstants.EXTRA_COURSE_COLOR, buddy.getCourseColor());
                    intent.putExtra(MyConstants.EXTRA_BUDDY_IMAGE_URL, buddy.getImageUrl());
                    intent.putExtra(MyConstants.EXTRA_SYSTEM_PROMPT, buddy.getSystemPrompt());
                    startActivity(intent);
                },
                new IABuddyAdapter.OnBuddySwipeListener()
                {
                    @Override
                    public void onDelete(Buddy buddy)
                    {
                        deleteBuddy(buddy);
                    }

                    @Override
                    public void onEdit(Buddy buddy)
                    {
                        editBuddy(buddy);
                    }
                }
        );

        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new IABuddyAdapter.SwipeCallback(adapter)
        );
        itemTouchHelper.attachToRecyclerView(recyclerView);

        loadBuddies();

        return view;
    }

    private void loadBuddies()
    {
        db.collection(MyConstants.COLLECTION_USERS)
                .document(currentUid)
                .collection(MyConstants.COLLECTION_BUDDIES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {
                    buddyList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots)
                    {
                        Buddy buddy = doc.toObject(Buddy.class);
                        if (buddy != null)
                        {
                            buddy.setId(doc.getId());
                            buddyList.add(buddy);
                        }
                    }
                    adapter.setBuddies(buddyList);
                });
    }

    private void deleteBuddy(Buddy buddy)
    {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Buddy")
                .setMessage("Do you really want to delete " + buddy.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) ->
                {
                    db.collection(MyConstants.COLLECTION_USERS)
                            .document(currentUid)
                            .collection(MyConstants.COLLECTION_BUDDIES)
                            .document(buddy.getId())
                            .delete()
                            .addOnSuccessListener(aVoid ->
                            {
                                Toast.makeText(getContext(), buddy.getName() + " has been deleted", Toast.LENGTH_SHORT).show();
                                loadBuddies();
                            })
                            .addOnFailureListener(e ->
                            {
                                Toast.makeText(getContext(), "Error during deletion", Toast.LENGTH_SHORT).show();
                                adapter.notifyDataSetChanged();
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) ->
                {
                    adapter.notifyDataSetChanged();
                })
                .show();
    }

    private void editBuddy(Buddy buddy)
    {
        CreateBuddyBottomSheet sheet = CreateBuddyBottomSheet.newInstanceForEdit(
                buddy.getId(),
                buddy.getName(),
                buddy.getCourseId()
        );
        sheet.show(getParentFragmentManager(), "EditBuddy");
    }
}
