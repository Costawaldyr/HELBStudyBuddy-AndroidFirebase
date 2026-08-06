package com.example.studybuddy.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.studybuddy.notification.NotificationHelper;
import com.example.studybuddy.data.local.AppDatabase;

import com.bumptech.glide.Glide;
import com.example.studybuddy.MainActivity;
import com.example.studybuddy.R;
import com.example.studybuddy.activities.BuddyChatActivity;
import com.example.studybuddy.activities.StudentChatActivity;
import com.example.studybuddy.models.Buddy;
import com.example.studybuddy.models.Match;
import com.example.studybuddy.models.Profile;
import com.example.studybuddy.profile.MatchManager;
import com.example.studybuddy.profile.MatchProposalDialog;
import com.example.studybuddy.profile.ProfileManager;
import com.example.studybuddy.service.MatchingService;
import com.example.studybuddy.service.QuoteApiService;
import com.example.studybuddy.service.QuoteResponse;
import com.example.studybuddy.utils.MyConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * DashboardFragment — The main screen where students see their stats, 
 * active buddies, available matches, and the Pomodoro timer.
 */
public class DashboardFragment extends Fragment
{
    private static final String TAG = "DashboardFragment";

    private TextView tvGreeting, tvQuote, badgeNotification;
    private TextView tvValueBuddies, tvValueMatches, tvValueStreak;
    private ImageView imgProfileDashboard;
    private MaterialButton btnFindMatch;
    private androidx.recyclerview.widget.RecyclerView rvAvailableMatches, rvNotificationsDashboard;
    private com.example.studybuddy.adapters.AvailableMatchAdapter availableMatchAdapter;
    private com.example.studybuddy.adapters.NotificationAdapter notificationAdapter;
    private TextView tvNoAvailableMatches;
    private LinearLayout layoutBuddiesContainer, layoutNotificationsSection;
    private MaterialCardView cardAddBuddy;

    private TextView tvPomodoroTimer;
    private MaterialButton btnPomodoroStart, btnPomodoroReset;
    private android.os.CountDownTimer pomodoroTimer;

    private boolean isTimerRunning = false;
    private long timeLeftInMillis = MyConstants.POMODORO_TOTAL_TIME_MS;
    private String currentQuoteText;

    private final List<Buddy> buddyList = new ArrayList<>();
    private Handler quoteHandler;
    private Runnable quoteRunnable;
    private ListenerRegistration buddiesListener;

    private FirebaseFirestore db;
    private ProfileManager profileManager;
    private MatchManager matchManager;
    private AppDatabase localDb;

    public DashboardFragment()
    {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        localDb = AppDatabase.getDatabase(requireContext());
        db = FirebaseFirestore.getInstance();
        profileManager = new ProfileManager();
        matchManager = new MatchManager();

        findViews(rootView);
        setupActions(rootView);
        loadData();

        return rootView;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (buddiesListener != null)
        {
            buddiesListener.remove();
        }
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (quoteHandler != null)
        {
            quoteHandler.removeCallbacks(quoteRunnable);
        }
    }

    private void findViews(View view)
    {
        imgProfileDashboard = view.findViewById(R.id.imgProfileDashboard);
        tvGreeting = view.findViewById(R.id.tv_greeting);
        tvQuote = view.findViewById(R.id.tv_quote);

        if (tvQuote != null)
        {
            tvQuote.setText(R.string.motivation_default_quote);
        }

        btnFindMatch = view.findViewById(R.id.btn_find_match);

        setupStatLabel(view.findViewById(R.id.stat_buddies), R.string.stat_buddies_label);
        setupStatLabel(view.findViewById(R.id.stat_matches), R.string.stat_matches_label);
        setupStatLabel(view.findViewById(R.id.stat_streak), R.string.stat_streak_label);

        tvValueBuddies = view.findViewById(R.id.stat_buddies).findViewById(R.id.tv_stat_value);
        tvValueMatches = view.findViewById(R.id.stat_matches).findViewById(R.id.tv_stat_value);
        tvValueStreak = view.findViewById(R.id.stat_streak).findViewById(R.id.tv_stat_value);

        layoutBuddiesContainer = view.findViewById(R.id.layout_buddies_container);
        cardAddBuddy = view.findViewById(R.id.card_add_buddy);
        layoutNotificationsSection = view.findViewById(R.id.layout_notifications_section);
        rvNotificationsDashboard = view.findViewById(R.id.rv_notifications_dashboard);
        rvAvailableMatches = view.findViewById(R.id.rv_available_matches);
        tvNoAvailableMatches = view.findViewById(R.id.tv_no_available_matches);

        tvPomodoroTimer = view.findViewById(R.id.tv_pomodoro_timer);
        btnPomodoroStart = view.findViewById(R.id.btn_pomodoro_start);
        btnPomodoroReset = view.findViewById(R.id.btn_pomodoro_reset);

        setupMatchesRecycler();
        setupNotificationsRecycler();
        setupTimerUI();
    }

    private void setupStatLabel(View view, int stringResId)
    {
        if (view != null)
        {
            TextView label = view.findViewById(R.id.tv_stat_label);
            if (label != null)
            {
                label.setText(stringResId);
            }
        }
    }

    private void setupNotificationsRecycler()
    {
        if (rvNotificationsDashboard == null)
        {
            return;
        }

        notificationAdapter = new com.example.studybuddy.adapters.NotificationAdapter(new com.example.studybuddy.adapters.NotificationAdapter.OnNotificationClickListener()
        {
            @Override
            public void onNotificationClick(com.example.studybuddy.data.local.NotificationEntity notification)
            {
                if (getActivity() instanceof MainActivity)
                {
                    ((MainActivity) getActivity()).onNotificationClick(notification);
                }
            }

            @Override
            public void onNotificationDelete(com.example.studybuddy.data.local.NotificationEntity notification)
            {
                if (getActivity() instanceof MainActivity)
                {
                    ((MainActivity) getActivity()).onNotificationDelete(notification);
                }
            }
        });

        rvNotificationsDashboard.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        rvNotificationsDashboard.setAdapter(notificationAdapter);

        View currentView = getView();
        if (currentView != null)
        {
            View btnClear = currentView.findViewById(R.id.btn_clear_notifications_dashboard);
            if (btnClear != null)
            {
                btnClear.setOnClickListener(v ->
                {
                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> localDb.notificationDao().deleteAll());
                });
            }
        }
    }

    private void setupMatchesRecycler()
    {
        if (rvAvailableMatches == null)
        {
            return;
        }

        availableMatchAdapter = new com.example.studybuddy.adapters.AvailableMatchAdapter(
                new com.example.studybuddy.adapters.AvailableMatchAdapter.OnMatchDecisionListener()
                {
                    @Override
                    public void onAccept(com.example.studybuddy.models.MatchCandidate candidate)
                    {
                        if (btnFindMatch != null)
                        {
                            btnFindMatch.setEnabled(false);
                        }

                        matchManager.acceptCandidate(candidate, new MatchManager.AcceptCandidateCallback()
                        {
                            @Override
                            public void onSuccess(String matchId, String otherUserId, String otherUserName)
                            {
                                if (!isAdded())
                                {
                                    return;
                                }
                                availableMatchAdapter.removeCandidate(candidate);
                                updateStats();
                                goToChat(matchId, otherUserId, otherUserName);
                                if (btnFindMatch != null)
                                {
                                    btnFindMatch.setEnabled(true);
                                }
                            }

                            @Override
                            public void onError(String error)
                            {
                                if (!isAdded())
                                {
                                    return;
                                }
                                if (btnFindMatch != null)
                                {
                                    btnFindMatch.setEnabled(true);
                                }
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onReject(com.example.studybuddy.models.MatchCandidate candidate)
                    {
                        matchManager.rejectCandidate(candidate, new MatchManager.SimpleCallback()
                        {
                            @Override
                            public void onSuccess()
                            {
                                if (!isAdded())
                                {
                                    return;
                                }
                                availableMatchAdapter.removeCandidate(candidate);
                            }

                            @Override
                            public void onError(String error)
                            {
                                if (!isAdded())
                                {
                                    return;
                                }
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

        rvAvailableMatches.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false));
        rvAvailableMatches.setAdapter(availableMatchAdapter);
    }

    private void goToChat(String matchId, String userId, String userName)
    {
        Intent chatIntent = new Intent(requireContext(), StudentChatActivity.class);
        chatIntent.putExtra(MyConstants.EXTRA_MATCH_ID, matchId);
        chatIntent.putExtra(MyConstants.EXTRA_USER_ID, userId);

        String nameToShow = (userName != null && !userName.isEmpty()) ? userName : getString(R.string.student_default_name);
        chatIntent.putExtra(MyConstants.EXTRA_USER_NAME, nameToShow);

        startActivity(chatIntent);
    }

    private void setupTimerUI()
    {
        if (btnPomodoroStart == null)
        {
            return;
        }

        btnPomodoroStart.setOnClickListener(v ->
        {
            if (isTimerRunning)
            {
                pauseTimer();
            }
            else
            {
                startTimer();
            }
        });

        if (btnPomodoroReset != null)
        {
            btnPomodoroReset.setOnClickListener(v -> resetTimer());
        }

        refreshTimerText();
    }

    private void startTimer()
    {
        if (pomodoroTimer != null)
        {
            pomodoroTimer.cancel();
        }

        isTimerRunning = true;
        updateTimerButtonText();

        pomodoroTimer = new android.os.CountDownTimer(timeLeftInMillis, MyConstants.TIMER_TICK_INTERVAL_MS)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                timeLeftInMillis = millisUntilFinished;
                refreshTimerText();
            }

            @Override
            public void onFinish()
            {
                isTimerRunning = false;
                timeLeftInMillis = MyConstants.POMODORO_TOTAL_TIME_MS;
                refreshTimerText();
                updateTimerButtonText();

                Context context = getContext();
                if (context != null)
                {
                    Toast.makeText(context, getString(R.string.session_finished), Toast.LENGTH_LONG).show();
                    NotificationHelper.sendBuddyMotivation(context, MyConstants.TIMER_CATEGORY_NAME, getString(R.string.session_finished_notif));
                }
            }
        }.start();
    }

    private void pauseTimer()
    {
        if (pomodoroTimer != null)
        {
            pomodoroTimer.cancel();
        }
        isTimerRunning = false;
        updateTimerButtonText();
    }

    private void resetTimer()
    {
        if (pomodoroTimer != null)
        {
            pomodoroTimer.cancel();
        }
        isTimerRunning = false;
        timeLeftInMillis = MyConstants.POMODORO_TOTAL_TIME_MS;
        refreshTimerText();
        updateTimerButtonText();
    }

    private void updateTimerButtonText()
    {
        if (btnPomodoroStart == null)
        {
            return;
        }

        if (isTimerRunning)
        {
            btnPomodoroStart.setText(getString(R.string.pomodoro_pause));
        }
        else
        {
            if (timeLeftInMillis < MyConstants.POMODORO_TOTAL_TIME_MS)
            {
                btnPomodoroStart.setText(getString(R.string.pomodoro_resume));
            }
            else
            {
                btnPomodoroStart.setText(getString(R.string.pomodoro_start));
            }
        }
    }

    private void refreshTimerText()
    {
        int totalSeconds = (int) (timeLeftInMillis / MyConstants.THOUSAND);
        int minutes = totalSeconds / (int) MyConstants.SIXTY;
        int seconds = totalSeconds % (int) MyConstants.SIXTY;

        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        if (tvPomodoroTimer != null)
        {
            tvPomodoroTimer.setText(formattedTime);
        }
    }

    private void setupActions(View view)
    {
        View btnNotif = view.findViewById(R.id.btn_notifications);
        if (btnNotif != null)
        {
            btnNotif.setOnClickListener(v -> openDrawer());
        }

        View btnAllBuddies = view.findViewById(R.id.btn_see_all_buddies);
        if (btnAllBuddies != null)
        {
            btnAllBuddies.setOnClickListener(v -> navigateToBuddies());
        }

        if (btnFindMatch != null)
        {
            btnFindMatch.setOnClickListener(v -> searchForPartners());
        }

        if (cardAddBuddy != null)
        {
            cardAddBuddy.setOnClickListener(v -> showBuddyDialog());
        }
    }

    private void showBuddyDialog()
    {
        CreateBuddyBottomSheet.newInstance().show(getChildFragmentManager(), MyConstants.FRAGMENT_TAG_CREATE_BUDDY);
    }

    private void loadData()
    {
        showUserGreeting();
        fetchQuote();
        startAutomaticQuoteUpdates();
        fetchBuddiesFromCloud();
        fetchUserAvatar();
        updateStats();
        observeNotifications();
        fetchPotentialMatches();
        scheduleReminder();
    }

    private void scheduleReminder()
    {
        Context ctx = getContext();
        if (ctx == null)
        {
            return;
        }

        AlarmManager manager = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent reminderIntent = new Intent(ctx, com.example.studybuddy.notification.ReminderReceiver.class);

        String quoteValue = (currentQuoteText != null && !currentQuoteText.isEmpty()) ? currentQuoteText : getString(R.string.motivation_default_quote);
        reminderIntent.putExtra(MyConstants.EXTRA_QUOTE, quoteValue);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pending = PendingIntent.getBroadcast(ctx, MyConstants.ZERO, reminderIntent, flags);
        long timeToTrigger = System.currentTimeMillis() + MyConstants.INITIAL_REMINDER_DELAY_MS;

        if (manager != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !manager.canScheduleExactAlarms())
            {
                manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeToTrigger, pending);
            }
            else
            {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeToTrigger, pending);
            }
        }
    }

    private void fetchPotentialMatches()
    {
        matchManager.getAvailableMatches(new MatchManager.MatchesCandidateCallback()
        {
            @Override
            public void onSuccess(List<com.example.studybuddy.models.MatchCandidate> candidates)
            {
                if (!isAdded())
                {
                    return;
                }

                if (candidates.isEmpty())
                {
                    if (tvNoAvailableMatches != null)
                    {
                        tvNoAvailableMatches.setVisibility(View.VISIBLE);
                        tvNoAvailableMatches.setText(R.string.no_candidates_found);
                    }
                    if (rvAvailableMatches != null)
                    {
                        rvAvailableMatches.setVisibility(View.GONE);
                    }
                    return;
                }

                if (availableMatchAdapter != null)
                {
                    availableMatchAdapter.setCandidates(candidates);
                }

                if (tvNoAvailableMatches != null)
                {
                    tvNoAvailableMatches.setVisibility(View.GONE);
                }

                if (rvAvailableMatches != null)
                {
                    rvAvailableMatches.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String error)
            {
                Log.e(TAG, "Failed to load matches: " + error);
            }
        });
    }

    private void navigateToBuddies()
    {
        androidx.navigation.fragment.NavHostFragment.findNavController(this).navigate(R.id.buddies_menu);
    }

    private void updateStats()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
        {
            return;
        }

        db.collection(MyConstants.COLLECTION_USERS).document(user.getUid()).collection(MyConstants.COLLECTION_BUDDIES).get()
                .addOnSuccessListener(result ->
                {
                    if (isAdded() && tvValueBuddies != null)
                    {
                        tvValueBuddies.setText(String.valueOf(result.size()));
                    }
                });

        matchManager.getMyMatches(new MatchManager.MatchesCallback()
        {
            @Override
            public void onSuccess(List<Match> matches)
            {
                if (!isAdded())
                {
                    return;
                }
                String totalMatches = String.valueOf(matches.size());

                if (tvValueMatches != null)
                {
                    tvValueMatches.setText(totalMatches);
                }

                if (tvValueStreak != null)
                {
                    tvValueStreak.setText(totalMatches);
                }
            }

            @Override
            public void onError(String error)
            {
                Log.e(TAG, "Failed to update stats: " + error);
            }
        });
    }

    private void observeNotifications()
    {
        if (badgeNotification == null)
        {
            return;
        }

        localDb.notificationDao().getAllNotifications().observe(getViewLifecycleOwner(), list ->
        {
            boolean hasNotifications = (list != null && !list.isEmpty());

            badgeNotification.setVisibility(hasNotifications ? View.VISIBLE : View.GONE);

            if (layoutNotificationsSection != null)
            {
                layoutNotificationsSection.setVisibility(hasNotifications ? View.VISIBLE : View.GONE);
            }

            if (hasNotifications)
            {
                badgeNotification.setText(String.valueOf(list.size()));
                if (notificationAdapter != null)
                {
                    notificationAdapter.setNotifications(list);
                }
            }
        });
    }

    private void openDrawer()
    {
        if (getActivity() instanceof MainActivity)
        {
            ((MainActivity) getActivity()).openNotificationDrawer();
        }
    }

    private void fetchUserAvatar()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
        {
            return;
        }

        profileManager.loadProfile(user.getUid(), new ProfileManager.ProfileCallback()
        {
            @Override
            public void onSuccess(Profile profile)
            {
                if (!(isAdded() && imgProfileDashboard != null))
                {
                    return;
                }

                String url = profile.getProfileImageUrl();
                if (url != null && !url.isEmpty())
                {
                    Glide.with(requireContext())
                            .load(url)
                            .placeholder(R.drawable.ic_profile)
                            .circleCrop()
                            .into(imgProfileDashboard);
                }
            }

            @Override
            public void onError(String error)
            {
                Log.w(TAG, "Profile image failed: " + error);
            }
        });
    }

    private void searchForPartners()
    {
        new MatchingService(requireContext()).findPotentialMatches(new MatchingService.MatchingCallback()
        {
            @Override
            public void onMatchFound(String otherUserId, String subject)
            {
                if (!isAdded())
                {
                    return;
                }
                db.collection(MyConstants.COLLECTION_USERS).document(otherUserId).get().addOnSuccessListener(snapshot ->
                {
                    if (!isAdded() || !snapshot.exists())
                    {
                        return;
                    }

                    String nameString = snapshot.getString(MyConstants.FIELD_NAME);
                    String finalName = (nameString != null && !nameString.isEmpty()) ? nameString : getString(R.string.student_default_name);

                    MatchProposalDialog proposal = MatchProposalDialog.newInstance(otherUserId, finalName, subject);
                    proposal.setOnMatchCreatedListener(DashboardFragment.this::updateStats);
                    proposal.show(getParentFragmentManager(), MyConstants.FRAGMENT_TAG_MATCH_PROPOSAL);
                });
            }

            @Override
            public void onNoMatchFound()
            {
                if (isAdded())
                {
                    Toast.makeText(requireContext(), R.string.no_partner_found, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error)
            {
                if (isAdded())
                {
                    Toast.makeText(requireContext(), R.string.error_searching_matches, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showUserGreeting()
    {
        String nameResult = getString(R.string.student_default_name);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null)
        {
            String fullName = user.getDisplayName();
            if (fullName != null && !fullName.isEmpty())
            {
                nameResult = fullName.split(" ")[MyConstants.ZERO];
            }
            else
            {
                String emailStr = user.getEmail();
                if (emailStr != null && !emailStr.isEmpty())
                {
                    nameResult = emailStr.split("@")[MyConstants.ZERO];
                }
            }
        }

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String finalGreeting;

        boolean morning = (hour >= MyConstants.HOUR_MORNING_START && hour < MyConstants.HOUR_AFTERNOON_START);
        boolean afternoon = (hour >= MyConstants.HOUR_AFTERNOON_START && hour < MyConstants.HOUR_EVENING_START);

        if (morning)
        {
            finalGreeting = getString(R.string.good_morning) + ", " + nameResult;
        }
        else if (afternoon)
        {
            finalGreeting = getString(R.string.good_afternoon) + ", " + nameResult;
        }
        else
        {
            finalGreeting = getString(R.string.good_evening) + ", " + nameResult;
        }

        if (tvGreeting != null)
        {
            tvGreeting.setText(finalGreeting);
        }
    }

    private void fetchBuddiesFromCloud()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
        {
            return;
        }

        buddiesListener = db.collection(MyConstants.COLLECTION_USERS).document(user.getUid())
                .collection(MyConstants.COLLECTION_BUDDIES)
                .limit(MyConstants.MAX_DASHBOARD_CANDIDATES)
                .addSnapshotListener((snapshot, err) ->
                {
                    if (err != null || snapshot == null)
                    {
                        return;
                    }

                    buddyList.clear();
                    for (QueryDocumentSnapshot doc : snapshot)
                    {
                        Buddy item = doc.toObject(Buddy.class);
                        item.setId(doc.getId());
                        buddyList.add(item);
                    }

                    if (getActivity() != null)
                    {
                        getActivity().runOnUiThread(this::showBuddyItems);
                    }
                });
    }

    private void showBuddyItems()
    {
        if (layoutBuddiesContainer == null)
        {
            return;
        }

        layoutBuddiesContainer.removeAllViews();
        layoutBuddiesContainer.addView(cardAddBuddy);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (Buddy buddy : buddyList)
        {
            View cardView = inflater.inflate(R.layout.item_buddy_card_horizontal, layoutBuddiesContainer, false);

            TextView tvEmoji = cardView.findViewById(R.id.tv_buddy_emoji);
            TextView tvName = cardView.findViewById(R.id.tv_buddy_name);
            TextView tvCourse = cardView.findViewById(R.id.tv_buddy_course);
            MaterialCardView mCard = (MaterialCardView) cardView;

            tvEmoji.setText(buddy.getCourseEmoji());
            tvName.setText(buddy.getName());
            tvCourse.setText(buddy.getCourseName());

            String hexCode = buddy.getCourseColor();
            if (hexCode != null && !hexCode.isEmpty())
            {
                try
                {
                    mCard.setStrokeColor(android.graphics.Color.parseColor(hexCode));
                }
                catch (Exception ignored)
                {
                }
            }

            cardView.setOnClickListener(v -> startBuddyChat(buddy));
            layoutBuddiesContainer.addView(cardView);
        }
    }

    private void startBuddyChat(Buddy buddy)
    {
        Intent intent = new Intent(requireContext(), BuddyChatActivity.class);
        intent.putExtra(MyConstants.EXTRA_BUDDY_ID, buddy.getId());
        intent.putExtra(MyConstants.EXTRA_BUDDY_NAME, buddy.getName());
        intent.putExtra(MyConstants.EXTRA_COURSE_NAME, buddy.getCourseName());
        intent.putExtra(MyConstants.EXTRA_COURSE_EMOJI, buddy.getCourseEmoji());
        intent.putExtra(MyConstants.EXTRA_COURSE_COLOR, buddy.getCourseColor());
        intent.putExtra(MyConstants.EXTRA_BUDDY_IMAGE_URL, buddy.getImageUrl());
        intent.putExtra(MyConstants.EXTRA_SYSTEM_PROMPT, buddy.getSystemPrompt());
        startActivity(intent);
    }

    private void fetchQuote()
    {
        Retrofit buildRetrofit = new Retrofit.Builder()
                .baseUrl(MyConstants.QUOTE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuoteApiService quoteService = buildRetrofit.create(QuoteApiService.class);

        quoteService.getRandomQuote().enqueue(new Callback<QuoteResponse[]>()
        {
            @Override
            public void onResponse(@NonNull Call<QuoteResponse[]> call,
                                   @NonNull Response<QuoteResponse[]> response)
            {
                if (!isAdded())
                {
                    return;
                }

                QuoteResponse[] data = response.body();
                if (response.isSuccessful() && data != null && data.length > MyConstants.ZERO)
                {
                    QuoteResponse first = data[MyConstants.ZERO];
                    String quoteText = first.getQuote();
                    String author = first.getAuthor();

                    if (quoteText == null || quoteText.isEmpty())
                    {
                        applyFallbackQuote();
                        return;
                    }
                    String finalAuthor = (author != null && !author.isEmpty())
                            ? author : MyConstants.FALLBACK_AUTHOR_NAME;

                    currentQuoteText = "\"" + quoteText + "\"\n— " + finalAuthor;

                    if (tvQuote != null)
                    {
                        tvQuote.setText(currentQuoteText);
                    }
                }
                else
                {
                    applyFallbackQuote();
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuoteResponse[]> call, @NonNull Throwable t)
            {
                applyFallbackQuote();
            }
        });
    }

    private void applyFallbackQuote()
    {
        if (isAdded() && tvQuote != null && currentQuoteText == null)
        {
            tvQuote.setText(R.string.motivation_default_quote);
        }
    }

    private void startAutomaticQuoteUpdates()
    {
        quoteHandler = new Handler(Looper.getMainLooper());
        quoteRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                fetchQuote();
                quoteHandler.postDelayed(this, MyConstants.QUOTE_REFRESH_INTERVAL_MS);
            }
        };
        quoteHandler.postDelayed(quoteRunnable, MyConstants.QUOTE_REFRESH_INTERVAL_MS);
    }
}
