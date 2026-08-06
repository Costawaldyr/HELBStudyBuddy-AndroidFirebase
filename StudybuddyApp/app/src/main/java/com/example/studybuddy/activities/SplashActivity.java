package com.example.studybuddy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy.R;
import com.example.studybuddy.utils.MyGlobals;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Initial activity that handles user session checks and splash animation.
 */
public class SplashActivity extends AppCompatActivity
{
    private static final int SPLASH_DURATION_MS = 3000;
    private static final int PROGRESS_MAX = 100;
    private static final int PROGRESS_MIN = 0;
    private static final int TIMER_INTERVAL_MS = 30;

    private static final String COLLECTION_USERS = "users";

    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progress_bar);

        checkUserSessionAndStartSplash();
    }

    /**
     * Checks if a user is already logged in and exists in Firestore.
     */
    private void checkUserSessionAndStartSplash()
    {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            db.collection(COLLECTION_USERS)
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot != null && documentSnapshot.exists())
                        {
                            MyGlobals.goToHomePage(SplashActivity.this);
                            finish();
                        }
                        else
                        {
                            startSplashAnimation();
                        }
                    })
                    .addOnFailureListener(error -> startSplashAnimation());
        }
        else
        {
            startSplashAnimation();
        }
    }

    /**
     * Starts the progress bar animation and navigates to WelcomeActivity upon completion.
     */
    private void startSplashAnimation()
    {
        progressBar.setMax(PROGRESS_MAX);
        progressBar.setProgress(PROGRESS_MIN);

        new CountDownTimer(SPLASH_DURATION_MS, TIMER_INTERVAL_MS)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                long elapsedTime = SPLASH_DURATION_MS - millisUntilFinished;
                int currentProgress = (int) (elapsedTime * PROGRESS_MAX / SPLASH_DURATION_MS);
                progressBar.setProgress(currentProgress);
            }

            @Override
            public void onFinish()
            {
                progressBar.setProgress(PROGRESS_MAX);
                Intent welcomeIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
                startActivity(welcomeIntent);
                finish();
            }
        }.start();
    }
}
