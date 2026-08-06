package com.example.studybuddy.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.studybuddy.R;
import com.example.studybuddy.activities.WelcomeActivity;
import com.example.studybuddy.data.StudyLocationData;
import com.example.studybuddy.models.StudyLocation;
import com.example.studybuddy.profile.ProfileManager;
import com.example.studybuddy.models.Profile;
import com.example.studybuddy.service.MapboxGeocodingRepository;
import com.example.studybuddy.utils.MyConstants;
import com.example.studybuddy.utils.MyGlobals;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Fragment responsible for displaying and editing the user's profile information.
 * Handles profile image updates, personal details, study locations, and subjects.
 */
public class ProfileFragment extends Fragment
{
    private CircleImageView profileImage;
    private ImageView btnEditPhoto;
    private TextView tvName, tvEmail, tvSchool, tvProgram, tvYear, tvBio, tvStudyLocation;
    private TextView tvStatsPomodoros, tvStatsStudentId, tvStatsStreak;
    private LinearLayout layoutSubjects;

    private MaterialButton btnEditProfile, btnLogout;
    private ImageView btnSettings;

    private View editSection;
    private TextInputEditText etEditName, etEditBio, etEditMatricule;
    private AutoCompleteTextView etEditLocation;
    private LinearLayout layoutEditSubjects;
    private MaterialButton btnSaveProfile, btnCancelEdit;

    private boolean isEditMode = false;
    private Profile currentProfile;

    private ProfileManager profileManager;
    private MapboxGeocodingRepository geocodingRepository;
    private FirebaseAuth mAuth;
    private String currentUid;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setupLaunchers();
        setupListeners();

        profileManager = new ProfileManager();
        geocodingRepository = new MapboxGeocodingRepository(requireContext());
        mAuth = FirebaseAuth.getInstance();

        boolean isUserLoggedIn = (mAuth.getCurrentUser() != null);
        if (isUserLoggedIn)
        {
            currentUid = mAuth.getCurrentUser().getUid();
            buildSubjectCheckboxes();
            loadProfileData();
        }
        else
        {
            logoutUser();
        }

        return view;
    }

    private void initViews(View view)
    {
        profileImage = view.findViewById(R.id.profile_image);
        btnEditPhoto = view.findViewById(R.id.btn_edit_photo);

        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvSchool = view.findViewById(R.id.tv_school);
        tvProgram = view.findViewById(R.id.tv_program);
        tvYear = view.findViewById(R.id.tv_year);
        tvBio = view.findViewById(R.id.tv_bio);
        tvStudyLocation = view.findViewById(R.id.tv_study_location);

        tvStatsPomodoros = view.findViewById(R.id.tv_stats_pomodoros);
        tvStatsStudentId = view.findViewById(R.id.tv_stats_student_id);
        tvStatsStreak = view.findViewById(R.id.tv_stats_streak);

        layoutSubjects = view.findViewById(R.id.layout_subjects);

        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnSettings = view.findViewById(R.id.btn_settings);

        editSection = view.findViewById(R.id.edit_section);
        etEditName = view.findViewById(R.id.et_edit_name);
        etEditBio = view.findViewById(R.id.et_edit_bio);
        etEditLocation = view.findViewById(R.id.et_edit_location);
        etEditMatricule = view.findViewById(R.id.et_edit_matricule);
        layoutEditSubjects = view.findViewById(R.id.layout_edit_subjects);
        btnSaveProfile = view.findViewById(R.id.btn_save_profile);
        btnCancelEdit = view.findViewById(R.id.btn_cancel_edit);

        if (editSection != null)
        {
            editSection.setVisibility(View.GONE);
        }
    }

    private void buildSubjectCheckboxes()
    {
        if (layoutEditSubjects == null)
        {
            return;
        }

        layoutEditSubjects.removeAllViews();

        for (String subject : MyConstants.ALL_SUBJECTS)
        {
            CheckBox cb = new CheckBox(requireContext());
            cb.setText(subject);
            cb.setTextSize(13f);
            cb.setTag(subject);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(MyConstants.ZERO, MyConstants.FOUR, MyConstants.ZERO,MyConstants.FOUR);
            cb.setLayoutParams(params);
            layoutEditSubjects.addView(cb);
        }
    }

    private void setupListeners()
    {
        if (btnEditPhoto != null)
        {
            btnEditPhoto.setOnClickListener(v -> checkPermissionAndPickImage());
        }

        if (btnSettings != null)
        {
            btnSettings.setOnClickListener(v ->
            {
                androidx.navigation.fragment.NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_settings);
            });
        }

        if (btnLogout != null)
        {
            btnLogout.setOnClickListener(v -> logoutUser());
        }

        if (btnEditProfile != null)
        {
            btnEditProfile.setOnClickListener(v ->
            {
                if (isEditMode)
                {
                    saveProfileChanges();
                }
                else
                {
                    enterEditMode();
                }
            });
        }

        if (btnSaveProfile != null)
        {
            btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
        }

        if (btnCancelEdit != null)
        {
            btnCancelEdit.setOnClickListener(v -> exitEditMode());
        }
    }

    private void enterEditMode()
    {
        isEditMode = true;
        if (editSection != null)
        {
            editSection.setVisibility(View.VISIBLE);
        }
        if (btnEditProfile != null)
        {
            btnEditProfile.setText(R.string.save);
        }

        if (currentProfile == null)
        {
            return;
        }

        if (etEditName != null)
        {
            etEditName.setText(currentProfile.getName());
        }
        if (etEditBio != null)
        {
            etEditBio.setText(currentProfile.getBio());
        }

        if (etEditLocation != null)
        {
            List<StudyLocation> locations = StudyLocationData.getStudyLocations();
            List<String> locationNames = new ArrayList<>();
            for (StudyLocation loc : locations)
            {
                locationNames.add(loc.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, locationNames);
            etEditLocation.setAdapter(adapter);
            etEditLocation.setText(currentProfile.getStudyLocation(), false);
        }

        if (etEditMatricule != null)
        {
            etEditMatricule.setText(currentProfile.getMatricule());
        }

        if (layoutEditSubjects != null)
        {
            List<String> profileSubjects = currentProfile.getSubjects();
            if (profileSubjects == null)
            {
                profileSubjects = new ArrayList<>();
            }

            for (int i = 0; i < layoutEditSubjects.getChildCount(); i++)
            {
                View child = layoutEditSubjects.getChildAt(i);
                if (child instanceof CheckBox)
                {
                    CheckBox cb = (CheckBox) child;
                    String subjectTag = (String) cb.getTag();
                    cb.setChecked(profileSubjects.contains(subjectTag));
                }
            }
        }
    }

    private void exitEditMode()
    {
        isEditMode = false;
        if (editSection != null)
        {
            editSection.setVisibility(View.GONE);
        }
        if (btnEditProfile != null)
        {
            btnEditProfile.setText(R.string.edit_profile);
        }
    }

    private void saveProfileChanges()
    {
        boolean hasNameInput = (etEditName != null);
        boolean hasBioInput = (etEditBio != null);
        boolean hasLocationInput = (etEditLocation != null);
        boolean hasMatriculeInput = (etEditMatricule != null);

        String newName = hasNameInput ? etEditName.getText().toString().trim() : "";
        String newBio = hasBioInput ? etEditBio.getText().toString().trim() : "";
        String newLocation = hasLocationInput ? etEditLocation.getText().toString().trim() : "";
        String newMatricule = hasMatriculeInput ? etEditMatricule.getText().toString().trim() : "";

        if (newName.isEmpty())
        {
            if (hasNameInput)
            {
                etEditName.setError("Name cannot be empty");
            }
            return;
        }

        List<String> newSubjects = new ArrayList<>();
        if (layoutEditSubjects != null)
        {
            for (int i = 0; i < layoutEditSubjects.getChildCount(); i++)
            {
                View child = layoutEditSubjects.getChildAt(i);
                if (child instanceof CheckBox && ((CheckBox) child).isChecked())
                {
                    newSubjects.add((String) ((CheckBox) child).getTag());
                }
            }
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(MyConstants.FIELD_NAME, newName);
        updates.put(MyConstants.FIELD_BIO, newBio);
        updates.put(MyConstants.FIELD_STUDY_LOCATION, newLocation);
        updates.put(MyConstants.FIELD_SUBJECTS, newSubjects);
        updates.put(MyConstants.FIELD_MATRICULE, newMatricule);

        StudyLocation resolvedSpot = findSpotByName(newLocation);
        if (resolvedSpot != null)
        {
            updates.put(MyConstants.FIELD_STUDY_LOCATION_LAT, resolvedSpot.getLat());
            updates.put(MyConstants.FIELD_STUDY_LOCATION_LNG, resolvedSpot.getLng());
            persistProfileUpdates(updates, newName, newBio, newLocation, newMatricule, newSubjects,
                    resolvedSpot.getLat(), resolvedSpot.getLng());
            return;
        }

        if (newLocation.isEmpty())
        {
            updates.put(MyConstants.FIELD_STUDY_LOCATION_LAT, null);
            updates.put(MyConstants.FIELD_STUDY_LOCATION_LNG, null);
            persistProfileUpdates(updates, newName, newBio, newLocation, newMatricule, newSubjects, null, null);
            return;
        }

        MyGlobals.showToast(requireActivity(), "Locating address…");
        geocodingRepository.geocode(newLocation, new MapboxGeocodingRepository.GeocodingCallback()
        {
            @Override
            public void onSuccess(double latitude, double longitude, String placeName)
            {
                if (!isAdded())
                {
                    return;
                }
                updates.put(MyConstants.FIELD_STUDY_LOCATION_LAT, latitude);
                updates.put(MyConstants.FIELD_STUDY_LOCATION_LNG, longitude);
                persistProfileUpdates(updates, newName, newBio, newLocation, newMatricule, newSubjects, latitude, longitude);
            }

            @Override
            public void onError(String errorMessage)
            {
                if (!isAdded())
                {
                    return;
                }
                updates.put(MyConstants.FIELD_STUDY_LOCATION_LAT, null);
                updates.put(MyConstants.FIELD_STUDY_LOCATION_LNG, null);
                MyGlobals.showToast(requireActivity(),
                        "Address not found on the map (" + errorMessage + ")");
                persistProfileUpdates(updates, newName, newBio, newLocation, newMatricule, newSubjects, null, null);
            }
        });
    }

    private void persistProfileUpdates(Map<String, Object> updates,
                                       String newName,
                                       String newBio,
                                       String newLocation,
                                       String newMatricule,
                                       List<String> newSubjects,
                                       Double newLat,
                                       Double newLng)
    {
        profileManager.updateProfile(currentUid, updates, new ProfileManager.UpdateCallback()
        {
            @Override
            public void onSuccess()
            {
                if (!isAdded())
                {
                    return;
                }
                MyGlobals.showToast(requireActivity(), "Profile updated successfully!");

                if (currentProfile != null)
                {
                    currentProfile.setName(newName);
                    currentProfile.setBio(newBio);
                    currentProfile.setStudyLocation(newLocation);
                    currentProfile.setMatricule(newMatricule);
                    currentProfile.setSubjects(newSubjects);
                    currentProfile.setStudyLocationLat(newLat);
                    currentProfile.setStudyLocationLng(newLng);
                }
                updateUI(currentProfile);
                exitEditMode();
            }

            @Override
            public void onError(String error)
            {
                if (!isAdded())
                {
                    return;
                }
                MyGlobals.showToast(requireActivity(), "Error updating profile: " + error);
            }
        });
    }

    private StudyLocation findSpotByName(String locationName)
    {
        if (locationName == null || locationName.isEmpty())
        {
            return null;
        }

        for (StudyLocation spot : StudyLocationData.getStudyLocations())
        {
            String spotName = spot.getName();
            boolean isMatch = spotName.equalsIgnoreCase(locationName) ||
                    locationName.toLowerCase().contains(spotName.toLowerCase());
            if (isMatch)
            {
                return spot;
            }
        }
        return null;
    }

    private void loadProfileData()
    {
        if (currentUid == null)
        {
            return;
        }

        profileManager.loadProfile(currentUid, new ProfileManager.ProfileCallback()
        {
            @Override
            public void onSuccess(Profile profile)
            {
                if (!isAdded())
                {
                    return;
                }
                currentProfile = profile;
                updateUI(profile);
            }

            @Override
            public void onError(String error)
            {
                if (!isAdded())
                {
                    return;
                }
                MyGlobals.showToast(requireActivity(), "Error loading profile data.");
            }
        });
    }

    private void updateUI(Profile profile)
    {
        if (profile == null || !isAdded())
        {
            return;
        }

        if (tvName != null)
        {
            tvName.setText(profile.getName());
        }
        if (tvEmail != null)
        {
            tvEmail.setText(profile.getEmail());
        }

        if (tvSchool != null)
        {
            String school = profile.getSchool();
            tvSchool.setText((school != null && !school.isEmpty()) ? school : MyConstants.DEFAULT_SCHOOL_NAME);
        }
        if (tvProgram != null)
        {
            String program = profile.getProgram();
            tvProgram.setText((program != null && !program.isEmpty()) ? program : MyConstants.DEFAULT_PROGRAM);
        }
        if (tvYear != null)
        {
            String year = profile.getYear();
            tvYear.setText((year != null && !year.isEmpty()) ? year : MyConstants.DEFAULT_YEAR);
        }
        if (tvBio != null)
        {
            String bio = profile.getBio();
            tvBio.setText((bio != null && !bio.isEmpty()) ? bio : MyConstants.DEFAULT_BIO);
        }
        if (tvStudyLocation != null)
        {
            tvStudyLocation.setText(profile.getStudyLocation());
        }

        if (tvStatsStudentId != null)
        {
            String matricule = profile.getMatricule();
            tvStatsStudentId.setText((matricule != null && !matricule.isEmpty()) ? matricule : MyConstants.EMPTY_DASH);
        }
        if (tvStatsStreak != null)
        {
            tvStatsStreak.setText(String.valueOf(profile.getStreak()));
        }

        String imageUrl = profile.getProfileImageUrl();
        if (profileImage != null && imageUrl != null && !imageUrl.isEmpty())
        {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(profileImage);
        }

        if (layoutSubjects != null)
        {
            layoutSubjects.removeAllViews();
            List<String> subjects = profile.getSubjects();
            if (subjects != null)
            {
                for (String subject : subjects)
                {
                    layoutSubjects.addView(createSubjectChip(subject));
                }
            }
        }
    }

    private TextView createSubjectChip(String subject)
    {
        TextView chip = new TextView(requireContext());
        chip.setText(subject);
        chip.setTextSize(12f);
        chip.setTextColor(0xFF3949AB);
        chip.setBackgroundResource(R.drawable.bg_chip);
        chip.setPadding(MyConstants.SIXTEEN, MyConstants.ZERO, MyConstants.SIXTEEN, MyConstants.ZERO);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(MyConstants.ZERO,MyConstants.ZERO,MyConstants.EIGHT, MyConstants.EIGHT);
        chip.setLayoutParams(params);
        return chip;
    }

    private void setupLaunchers()
    {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result ->
                {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null)
                    {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null)
                        {
                            uploadProfileImage(selectedImage);
                        }
                    }
                }
        );

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted ->
                {
                    if (isGranted)
                    {
                        openImagePicker();
                    }
                    else
                    {
                        MyGlobals.showToast(requireActivity(), "Permission denied. Cannot change photo.");
                    }
                }
        );
    }

    private void checkPermissionAndPickImage()
    {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
        {
            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
        }
        else
        {
            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void openImagePicker()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri)
    {
        if (!isAdded())
        {
            return;
        }
        MyGlobals.showToast(requireActivity(), "Uploading photo...");

        profileManager.uploadProfileImage(currentUid, imageUri, new ProfileManager.UploadCallback()
        {
            @Override
            public void onSuccess(String imageUrl)
            {
                if (!isAdded())
                {
                    return;
                }
                Glide.with(ProfileFragment.this).load(imageUrl).into(profileImage);
                MyGlobals.showToast(requireActivity(), "Profile photo updated!");
            }

            @Override
            public void onError(String error)
            {
                if (!isAdded())
                {
                    return;
                }
                MyGlobals.showToast(requireActivity(), "Upload failed: " + error);
            }
        });
    }

    private void logoutUser()
    {
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
