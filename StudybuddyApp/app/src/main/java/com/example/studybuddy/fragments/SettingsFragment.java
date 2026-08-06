package com.example.studybuddy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.Fragment;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.studybuddy.R;
import com.example.studybuddy.models.Profile;
import com.example.studybuddy.notification.BuddyMotivationWorker;
import com.example.studybuddy.profile.ProfileManager;
import com.example.studybuddy.utils.MyConstants;
import com.example.studybuddy.utils.MyGlobals;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

/**
 * SettingsFragment — Allows users to customize application preferences such as notifications,
 * location sharing, dark mode, and language.
 */
public class SettingsFragment extends Fragment
{
    private MaterialToolbar toolbar;
    private SwitchCompat switchNotifications;
    private SwitchCompat switchLocation;
    private SwitchCompat switchDarkMode;
    private SwitchCompat switchSound;
    private SwitchCompat switchVibration;
    private MaterialButton btnLanguage;
    private MaterialButton btnTestNotification;

    private ProfileManager profileManager;
    private String currentUid;
    private boolean isInitialLoading = true;

    /**
     * Initializes the fragment view and loads user preferences from Firestore.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        profileManager = new ProfileManager();

        boolean isUserLoggedIn = (FirebaseAuth.getInstance().getCurrentUser() != null);
        if (isUserLoggedIn)
        {
            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        else
        {
            if (getActivity() != null)
            {
                getActivity().onBackPressed();
            }
            return view;
        }

        initViews(view);
        setupToolbar();
        loadSettingsFromProfile();
        setupListeners();

        return view;
    }

    private void initViews(View view)
    {
        toolbar = view.findViewById(R.id.settings_toolbar);
        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchLocation = view.findViewById(R.id.switch_location);
        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchSound = view.findViewById(R.id.switch_sound);
        switchVibration = view.findViewById(R.id.switch_vibration);
        btnLanguage = view.findViewById(R.id.btn_language);
        btnTestNotification = view.findViewById(R.id.btn_test_notification);
    }

    private void setupToolbar()
    {
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack()
        );
    }

    /**
     * Loads the user's settings from their profile in Firestore.
     */
    private void loadSettingsFromProfile()
    {
        isInitialLoading = true;
        profileManager.loadProfile(currentUid, new ProfileManager.ProfileCallback()
        {
            @Override
            public void onSuccess(Profile profile)
            {
                if (!isAdded())
                {
                    return;
                }

                if (profile != null)
                {
                    switchNotifications.setChecked(profile.isNotificationsEnabled());
                    switchLocation.setChecked(profile.isLocationSharingEnabled());
                    switchDarkMode.setChecked(profile.isDarkModeEnabled());
                    switchSound.setChecked(profile.isSoundEnabled());
                    switchVibration.setChecked(profile.isVibrationEnabled());

                    updateLanguageButtonDisplay(profile.getLanguage());
                }

                isInitialLoading = false;
            }

            @Override
            public void onError(String error)
            {
                if (!isAdded())
                {
                    return;
                }

                MyGlobals.showToast(requireActivity(), "Error loading settings: " + error);
                isInitialLoading = false;
            }
        });
    }

    private void updateLanguageButtonDisplay(String langCode)
    {
        if (langCode == null)
        {
            return;
        }

        switch (langCode)
        {
            case MyConstants.LANGUAGE_ENGLISH:
                btnLanguage.setText(R.string.english);
                break;
            case MyConstants.LANGUAGE_DUTCH:
                btnLanguage.setText(R.string.dutch);
                break;
            case MyConstants.LANGUAGE_FRENCH:
            default:
                btnLanguage.setText(R.string.french);
                break;
        }
    }

    private void setupListeners()
    {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (isInitialLoading)
            {
                return;
            }
            profileManager.updateNotificationSettings(currentUid, isChecked);
        });

        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (isInitialLoading)
            {
                return;
            }
            profileManager.updateLocationSharing(currentUid, isChecked);
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (isInitialLoading)
            {
                return;
            }
            profileManager.updateDarkMode(currentUid, isChecked);

            if (isChecked)
            {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            else
            {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        switchSound.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (isInitialLoading)
            {
                return;
            }
        });

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (isInitialLoading)
            {
                return;
            }
        });

        btnLanguage.setOnClickListener(v ->
        {
            displayLanguageSelectionDialog();
        });

        if (btnTestNotification != null)
        {
            btnTestNotification.setOnClickListener(v -> triggerTestNotification());
        }
    }

    /**
     * Enqueues a one-time BuddyMotivationWorker to fire a notification right now.
     */
    private void triggerTestNotification()
    {
        if (!isAdded())
        {
            return;
        }
        OneTimeWorkRequest request =
                new OneTimeWorkRequest.Builder(BuddyMotivationWorker.class).build();
        WorkManager.getInstance(requireContext()).enqueue(request);
        MyGlobals.showToast(requireActivity(),
                getString(R.string.settings_test_notification_scheduled));
    }

    /**
     * Shows a dialog to choose the application language.
     */
    private void displayLanguageSelectionDialog()
    {
        String[] languages = {
                getString(R.string.french),
                getString(R.string.english),
                getString(R.string.dutch)
        };
        String[] langCodes = {MyConstants.LANGUAGE_FRENCH, MyConstants.LANGUAGE_ENGLISH, MyConstants.LANGUAGE_DUTCH};

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_language)
                .setItems(languages, (dialog, index) ->
                {
                    String selectedLanguageCode = langCodes[index];
                    profileManager.updateLanguage(currentUid, selectedLanguageCode);
                    applyNewLocale(selectedLanguageCode);
                })
                .show();
    }

    /**
     * Applies the selected locale to the application.
     */
    private void applyNewLocale(String languageCode)
    {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(languageCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
        updateLanguageButtonDisplay(languageCode);
        MyGlobals.showToast(requireActivity(), "Language changed to " + languageCode.toUpperCase());
    }
}
