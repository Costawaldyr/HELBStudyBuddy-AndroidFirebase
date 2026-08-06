package com.example.studybuddy.activities;

import static com.example.studybuddy.utils.MyConstants.*;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy.R;
import com.example.studybuddy.models.Profile;
import com.example.studybuddy.validator.RegisterFormValidator;
import com.example.studybuddy.utils.MyGlobals;
import com.example.studybuddy.validator.ValidationResult;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity
{
    private static final String TAG = "RegisterActivity";

    private MaterialToolbar registerToolbar;
    private TextInputLayout nameInputLayout, emailInputLayout, passwordInputLayout;
    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private Button registerButton;
    private View progressBar;
    private TextView loginLink;
    private View stepAccountView;
    private View stepSubjectsView;
    private LinearLayout layoutCheckboxes;
    private MaterialButton btnFinish;

    private int currentStep = STEP_ACCOUNT;
    private String pendingUid;
    private String pendingName;
    private String pendingEmail;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        MyGlobals.setupToolbar(this, registerToolbar);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                if (currentStep == STEP_SUBJECTS)
                {
                    showStep(STEP_ACCOUNT);
                }
                else
                {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (savedInstanceState != null)
        {
            restoreState(savedInstanceState);
        }
        else
        {
            showStep(STEP_ACCOUNT);
        }

        registerButton.setOnClickListener(v -> tryRegister());
        loginLink.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_STEP, currentStep);
        outState.putString(KEY_PENDING_UID, pendingUid);
        outState.putString(KEY_PENDING_NAME, pendingName);
        outState.putString(KEY_PENDING_EMAIL, pendingEmail);

        if (nameEditText.getText() != null)
        {
            outState.putString(KEY_FIELD_NAME_STATE, nameEditText.getText().toString());
        }

        if (emailEditText.getText() != null)
        {
            outState.putString(KEY_FIELD_EMAIL_STATE, emailEditText.getText().toString());
        }

        if (passwordEditText.getText() != null)
        {
            outState.putString(KEY_FIELD_PASSWORD_STATE, passwordEditText.getText().toString());
        }

        ArrayList<String> checked = new ArrayList<>(getSelectedSubjects());
        outState.putStringArrayList(KEY_CHECKED_SUBJECTS, checked);
    }

    private void restoreState(@NonNull Bundle saved)
    {
        pendingUid = saved.getString(KEY_PENDING_UID);
        pendingName = saved.getString(KEY_PENDING_NAME);
        pendingEmail = saved.getString(KEY_PENDING_EMAIL);

        String savedName = saved.getString(KEY_FIELD_NAME_STATE);
        String savedEmail = saved.getString(KEY_FIELD_EMAIL_STATE);
        String savedPassword = saved.getString(KEY_FIELD_PASSWORD_STATE);

        if (savedName != null)
        {
            nameEditText.setText(savedName);
        }

        if (savedEmail != null)
        {
            emailEditText.setText(savedEmail);
        }

        if (savedPassword != null)
        {
            passwordEditText.setText(savedPassword);
        }

        ArrayList<String> checkedSubjects = saved.getStringArrayList(KEY_CHECKED_SUBJECTS);
        if (checkedSubjects != null && layoutCheckboxes != null)
        {
            for (int i = ZERO; i < layoutCheckboxes.getChildCount(); i++)
            {
                View child = layoutCheckboxes.getChildAt(i);
                if (child instanceof CheckBox)
                {
                    CheckBox cb = (CheckBox) child;
                    cb.setChecked(checkedSubjects.contains(cb.getTag()));
                }
            }
        }

        showStep(saved.getInt(KEY_STEP, STEP_ACCOUNT));
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null)
        {
            return;
        }

        db.collection(COLLECTION_USERS).document(user.getUid()).get()
                .addOnSuccessListener(doc ->
                {
                    if (doc.exists())
                    {
                        MyGlobals.goToHomePage(this);
                    }
                    else
                    {
                        pendingUid = user.getUid();
                        pendingEmail = user.getEmail();
                        pendingName = user.getDisplayName();

                        if (emailEditText != null)
                        {
                            emailEditText.setText(pendingEmail);
                        }

                        if (pendingName != null && !pendingName.isEmpty())
                        {
                            nameEditText.setText(pendingName);
                            showStep(STEP_SUBJECTS);
                        }
                        else
                        {
                            showStep(STEP_ACCOUNT);
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error checking user profile", e));
    }

    private void initViews()
    {
        registerToolbar = findViewById(R.id.register_toolbar);
        stepAccountView = findViewById(R.id.step_account);
        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
        loginLink = findViewById(R.id.loginLink);
        stepSubjectsView = findViewById(R.id.step_subjects);
        layoutCheckboxes = findViewById(R.id.layout_checkboxes);
        btnFinish = findViewById(R.id.btn_finish_register);

        buildCheckboxes();
    }

    private void buildCheckboxes()
    {
        if (layoutCheckboxes == null)
        {
            return;
        }

        layoutCheckboxes.removeAllViews();

        for (String subject : ALL_SUBJECTS)
        {
            CheckBox cb = new CheckBox(this);
            cb.setText(subject);
            cb.setTextSize(CHECKBOX_TEXT_SIZE);
            cb.setTextColor(Color.WHITE);
            cb.setTag(subject);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(ZERO, MARGIN_SUBJECT_CB, ZERO, MARGIN_SUBJECT_CB);
            cb.setLayoutParams(params);
            layoutCheckboxes.addView(cb);
        }
    }

    private void showStep(int step)
    {
        currentStep = step;

        if (step == STEP_ACCOUNT)
        {
            stepAccountView.setVisibility(View.VISIBLE);
            stepSubjectsView.setVisibility(View.GONE);

            registerToolbar.setTitle(R.string.create_account);
            registerToolbar.setNavigationIcon(null);

            registerButton.setText(pendingUid != null ? "Continue" : "Next");

            if (pendingUid != null)
            {
                emailEditText.setEnabled(false);
                passwordInputLayout.setVisibility(View.GONE);
                nameEditText.setEnabled(true);
            }
            else
            {
                emailEditText.setEnabled(true);
                passwordInputLayout.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            stepAccountView.setVisibility(View.GONE);
            stepSubjectsView.setVisibility(View.VISIBLE);

            registerToolbar.setTitle(R.string.your_subjects);
            registerToolbar.setNavigationIcon(R.drawable.ic_back);
            registerToolbar.setNavigationOnClickListener(v -> showStep(STEP_ACCOUNT));

            btnFinish.setOnClickListener(v -> finishRegistration());
        }
    }

    public void tryRegister()
    {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        nameInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        RegisterFormValidator validator = new RegisterFormValidator();
        ValidationResult nameResult = validator.validateName(name);

        if (pendingUid != null)
        {
            if (!nameResult.isValid())
            {
                nameInputLayout.setError(nameResult.getError());
            }
            else
            {
                pendingName = name;
                showStep(STEP_SUBJECTS);
            }
            return;
        }

        ValidationResult emailResult = validator.validateEmail(email);
        ValidationResult passwordResult = validator.validatePassword(password);

        if (!nameResult.isValid())
        {
            nameInputLayout.setError(nameResult.getError());
        }
        if (!emailResult.isValid())
        {
            emailInputLayout.setError(emailResult.getError());
        }
        if (!passwordResult.isValid())
        {
            passwordInputLayout.setError(passwordResult.getError());
        }

        if (nameResult.isValid() && emailResult.isValid() && passwordResult.isValid())
        {
            createFirebaseUser(name, email, password);
        }
    }

    private void createFirebaseUser(String name, String email, String password)
    {
        MyGlobals.setLoadingState(progressBar, registerButton, true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task ->
                {
                    if (task.isSuccessful())
                    {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null)
                        {
                            return;
                        }

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        user.updateProfile(profileUpdates).addOnCompleteListener(profileTask ->
                        {
                            if (!profileTask.isSuccessful())
                            {
                                Log.w(TAG, "updateProfile:failure", profileTask.getException());
                            }

                            pendingUid = user.getUid();
                            pendingName = name;
                            pendingEmail = email;

                            MyGlobals.setLoadingState(progressBar, registerButton, false);
                            showStep(STEP_SUBJECTS);
                        });
                    }
                    else
                    {
                        MyGlobals.setLoadingState(progressBar, registerButton, false);
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());

                        String errorMsg = (task.getException() != null) ? task.getException().getMessage() : "Registration failed.";
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void finishRegistration()
    {
        List<String> selectedSubjects = getSelectedSubjects();

        if (selectedSubjects.isEmpty())
        {
            MyGlobals.showToast(this, "Please select at least one subject");
            return;
        }

        if (btnFinish != null)
        {
            btnFinish.setEnabled(false);
        }

        saveUserToFirebase(pendingUid, pendingName, pendingEmail, selectedSubjects);
    }

    private List<String> getSelectedSubjects()
    {
        List<String> selected = new ArrayList<>();
        if (layoutCheckboxes == null)
        {
            return selected;
        }

        for (int i = ZERO; i < layoutCheckboxes.getChildCount(); i++)
        {
            View child = layoutCheckboxes.getChildAt(i);
            if (child instanceof CheckBox)
            {
                CheckBox cb = (CheckBox) child;
                if (cb.isChecked())
                {
                    selected.add((String) cb.getTag());
                }
            }
        }
        return selected;
    }

    private void saveUserToFirebase(String uid, String name, String email, List<String> subjects)
    {
        Profile profile = new Profile(uid, name, email);
        profile.setSchool(DEFAULT_SCHOOL);
        profile.setProgram(DEFAULT_PROGRAM_IT);
        profile.setYear(DEFAULT_YEAR_IT);
        profile.setSubjects(subjects);
        profile.setCreatedAt(Timestamp.now());
        profile.setLastActive(Timestamp.now());

        db.collection(COLLECTION_USERS)
                .document(uid)
                .set(profile)
                .addOnSuccessListener(aVoid ->
                {
                    MyGlobals.showToast(this, "Account created! Welcome");
                    MyGlobals.goToHomePage(this);
                })
                .addOnFailureListener(e ->
                {
                    if (btnFinish != null)
                    {
                        btnFinish.setEnabled(true);
                    }
                    MyGlobals.showToast(this, "Error saving profile: " + e.getMessage());
                });
    }
}
