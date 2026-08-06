package com.example.studybuddy.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.studybuddy.MainActivity;
import com.example.studybuddy.activities.WelcomeActivity;

public class MyGlobals
{
    public static void goToHomePage(Activity activity)
    {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void goToOnboarding(Activity activity)
    {
        Intent intent = new Intent(activity, WelcomeActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void showToast(Activity activity, String message)
    {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public static void setLoadingState(View progressBar, View button, boolean show)
    {
        if (progressBar == null || button == null)
        {
            return;
        }

        int visibility = show ? View.VISIBLE : View.GONE;
        boolean isClickable = !show;

        progressBar.setVisibility(visibility);
        button.setEnabled(isClickable);
    }

    public static void setupToolbar(Activity activity, Toolbar toolbar)
    {
        if (activity == null || toolbar == null)
        {
            return;
        }

        if (!(activity instanceof AppCompatActivity))
        {
            return;
        }

        AppCompatActivity appCompatActivity = (AppCompatActivity) activity;

        appCompatActivity.setSupportActionBar(toolbar);

        if (appCompatActivity.getSupportActionBar() == null)
        {
            return;
        }

        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appCompatActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(v -> activity.onBackPressed());
    }
}
