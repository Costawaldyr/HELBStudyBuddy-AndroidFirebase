package com.example.studybuddy;

import static com.example.studybuddy.utils.MyConstants.*;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.studybuddy.adapters.NotificationAdapter;
import com.example.studybuddy.data.local.AppDatabase;
import com.example.studybuddy.data.local.NotificationEntity;
import com.example.studybuddy.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener
{
    private ActivityMainBinding binding;
    private NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = binding.bottomNavigation;
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null)
        {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);
        }

        checkNotificationPermission();
        setupNotificationDrawer();
    }

    private void setupNotificationDrawer()
    {
        notificationAdapter = new NotificationAdapter(this);
        binding.rvNotifications.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        binding.rvNotifications.setAdapter(notificationAdapter);

        AppDatabase.getDatabase(this).notificationDao().getAllNotifications().observe(this, notifications ->
        {
            if (notifications == null || notifications.isEmpty())
            {
                binding.layoutNoNotifications.setVisibility(android.view.View.VISIBLE);
                binding.rvNotifications.setVisibility(android.view.View.GONE);
            }
            else
            {
                binding.layoutNoNotifications.setVisibility(android.view.View.GONE);
                binding.rvNotifications.setVisibility(android.view.View.VISIBLE);
                notificationAdapter.setNotifications(notifications);
            }
        });

        binding.btnClearAll.setOnClickListener(v -> Executors.newSingleThreadExecutor().execute(() ->
        {
            AppDatabase.getDatabase(this).notificationDao().deleteAll();
        }));
    }

    @Override
    public void onNotificationClick(NotificationEntity notification)
    {
        if (binding.drawerLayout != null)
        {
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.END);
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null)
        {
            return;
        }
        NavController navController = navHostFragment.getNavController();

        String type = notification.getType();
        String targetId = notification.getTargetId();

        if (DB_TYPE_BUDDY.equals(type))
        {
            navController.navigate(R.id.buddies_menu);
        }
        else if (DB_TYPE_MESSAGE.equals(type) && targetId != null)
        {
            FirebaseFirestore.getInstance()
                    .collection(COLLECTION_MATCHES).document(targetId)
                    .get()
                    .addOnSuccessListener(doc ->
                    {
                        if (doc.exists())
                        {
                            String uid = FirebaseAuth.getInstance().getUid();
                            String user1 = doc.getString(FIELD_USER_ID_1_LEGACY);
                            String otherUserId = (uid != null && uid.equals(user1)) ? doc.getString(FIELD_USER_ID_2_LEGACY) : user1;
                            String otherName = (uid != null && uid.equals(user1)) ? doc.getString(FIELD_USER_NAME_2_LEGACY) : doc.getString(FIELD_USER_NAME_1_LEGACY);

                            Intent intent = new Intent(this, com.example.studybuddy.activities.StudentChatActivity.class);
                            intent.putExtra(EXTRA_MATCH_ID, targetId);
                            intent.putExtra(EXTRA_USER_ID, otherUserId);
                            intent.putExtra(EXTRA_USER_NAME, otherName);
                            startActivity(intent);
                        }
                    });
        }
        else if (DB_TYPE_MATCH.equals(type))
        {
            navController.navigate(R.id.dashboard_menu);
        }
    }

    @Override
    protected void onNewIntent(@androidx.annotation.NonNull Intent intent)
    {
        super.onNewIntent(intent);
        String navType = intent.getStringExtra(EXTRA_NAV_TYPE);
        String targetId = intent.getStringExtra(EXTRA_TARGET_ID);
        if (navType == null)
        {
            return;
        }

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null)
        {
            return;
        }
        NavController navController = navHostFragment.getNavController();

        switch (navType)
        {
            case NAV_TYPE_BUDDY:
                navController.navigate(R.id.buddies_menu);
                break;
            case NAV_TYPE_MESSAGE:
                if (targetId != null)
                {
                    FirebaseFirestore.getInstance()
                            .collection(COLLECTION_MATCHES).document(targetId)
                            .get()
                            .addOnSuccessListener(doc ->
                            {
                                if (doc.exists())
                                {
                                    String uid = FirebaseAuth.getInstance().getUid();
                                    String user1 = doc.getString(FIELD_USER_ID_1_LEGACY);
                                    String otherUserId = (uid != null && uid.equals(user1)) ? doc.getString(FIELD_USER_ID_2_LEGACY) : user1;
                                    String otherName = (uid != null && uid.equals(user1)) ? doc.getString(FIELD_USER_NAME_2_LEGACY) : doc.getString(FIELD_USER_NAME_1_LEGACY);

                                    Intent chatIntent = new Intent(this, com.example.studybuddy.activities.StudentChatActivity.class);
                                    chatIntent.putExtra(EXTRA_MATCH_ID, targetId);
                                    chatIntent.putExtra(EXTRA_USER_ID, otherUserId);
                                    chatIntent.putExtra(EXTRA_USER_NAME, otherName);
                                    startActivity(chatIntent);
                                }
                            });
                }
                break;
            case NAV_TYPE_MATCH:
                navController.navigate(R.id.dashboard_menu);
                break;
        }
    }

    @Override
    public void onNotificationDelete(NotificationEntity notification)
    {
        Executors.newSingleThreadExecutor().execute(() ->
        {
            AppDatabase.getDatabase(this).notificationDao().delete(notification);
        });
    }

    private void checkNotificationPermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIF_PERMISSION);
            }
        }
    }

    public void openNotificationDrawer()
    {
        if (binding.drawerLayout != null)
        {
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.END);
        }
    }
}
