package com.example.studybuddy.profile;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.studybuddy.models.Profile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import com.example.studybuddy.utils.MyConstants;

public class ProfileManager
{
    private static final String TAG = "ProfileManager";

    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final FirebaseAuth mAuth;

    public interface ProfileCallback
    {
        void onSuccess(Profile profile);
        void onError(String error);
    }

    public interface UploadCallback
    {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public interface UpdateCallback
    {
        void onSuccess();
        void onError(String error);
    }

    public ProfileManager()
    {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Crée un profil par défaut pour un nouvel utilisateur
     */
    public void createDefaultProfile(String uid, String name, String email)
    {
        Profile profile = new Profile(uid, name, email);

        db.collection(MyConstants.COLLECTION_USERS)
                .document(uid)
                .set(profile)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Profile created successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating profile", e));
    }

    /**
     * Charge le profil d'un utilisateur
     */
    public void loadProfile(String uid, ProfileCallback callback)
    {
        db.collection(MyConstants.COLLECTION_USERS)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {
                    if (documentSnapshot.exists())
                    {
                        Profile profile = documentSnapshot.toObject(Profile.class);
                        callback.onSuccess(profile);
                    }
                    else
                    {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null)
                        {
                            createDefaultProfile(uid, user.getDisplayName(), user.getEmail());
                            callback.onSuccess(new Profile(uid, user.getDisplayName(), user.getEmail()));
                        }
                        else
                        {
                            callback.onError(MyConstants.ERROR_PROFILE_NOT_FOUND);
                        }
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Met à jour le profil
     */
    public void updateProfile(String uid, Map<String, Object> updates, UpdateCallback callback)
    {
        db.collection(MyConstants.COLLECTION_USERS)
                .document(uid)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Upload une image de profil
     */
    public void uploadProfileImage(String uid, Uri imageUri, UploadCallback callback)
    {
        StorageReference storageRef = storage.getReference()
                .child(MyConstants.STORAGE_PATH_PROFILE_IMAGES)
                .child(uid + MyConstants.IMAGE_EXT_JPG);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                {
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put(MyConstants.FIELD_PROFILE_IMAGE_URL, uri.toString());

                                    updateProfile(uid, updates, new UpdateCallback()
                                    {
                                        @Override
                                        public void onSuccess()
                                        {
                                            callback.onSuccess(uri.toString());
                                        }

                                        @Override
                                        public void onError(String error)
                                        {
                                            callback.onError(error);
                                        }
                                    });
                                })
                                .addOnFailureListener(e -> callback.onError(e.getMessage()))
                )
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Met à jour les paramètres de notification
     */
    public void updateNotificationSettings(String uid, boolean enabled)
    {
        Map<String, Object> updates = new HashMap<>();
        updates.put(MyConstants.FIELD_NOTIFICATIONS_ENABLED, enabled);

        db.collection(MyConstants.COLLECTION_USERS)
                .document(uid)
                .update(updates);
    }

    /**
     * Met à jour le partage de localisation
     */
    public void updateLocationSharing(String uid, boolean enabled)
    {
        Map<String, Object> updates = new HashMap<>();
        updates.put(MyConstants.FIELD_LOCATION_SHARING_ENABLED, enabled);

        db.collection(MyConstants.COLLECTION_USERS)
                .document(uid)
                .update(updates);
    }

    /**
     * Met à jour le mode sombre
     */
    public void updateDarkMode(String uid, boolean enabled)
    {
        Map<String, Object> updates = new HashMap<>();
        updates.put(MyConstants.FIELD_DARK_MODE, enabled);

        db.collection(MyConstants.COLLECTION_USERS)
                .document(uid)
                .update(updates);
    }

    /**
     * Met à jour la langue
     */
    public void updateLanguage(String uid, String language)
    {
        Map<String, Object> updates = new HashMap<>();
        updates.put(MyConstants.FIELD_LANGUAGE, language);

        db.collection(MyConstants.COLLECTION_USERS)
                .document(uid)
                .update(updates);
    }
}