package com.example.studybuddy.models;

import static com.example.studybuddy.utils.MyConstants.*;

import com.google.firebase.Timestamp;

import java.util.List;

public class Profile
{
    private String uid;
    private String name;
    private String email;
    private String school;
    private String program;
    private String year;
    private String profileImageUrl;
    private List<String> subjects;
    private String studyLocation;
    private Double studyLocationLat;
    private Double studyLocationLng;
    private String bio;
    private String matricule;
    private Timestamp createdAt;
    private Timestamp lastActive;
    private int streak;
    private String fcmToken;

    private boolean notificationsEnabled;
    private boolean locationSharingEnabled;
    private boolean darkModeEnabled;
    private boolean soundEnabled;
    private boolean vibrationEnabled;
    private String language;

    public Profile()
    {
    }

    public Profile(String uid, String name, String email)
    {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.notificationsEnabled = true;
        this.locationSharingEnabled = false;
        this.darkModeEnabled = false;
        this.soundEnabled = true;
        this.vibrationEnabled = true;
        this.language = LANGUAGE_FRENCH;
        this.streak = ZERO;
    }

    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getSchool()
    {
        return school;
    }

    public void setSchool(String school)
    {
        this.school = school;
    }

    public String getProgram()
    {
        return program;
    }

    public void setProgram(String program)
    {
        this.program = program;
    }

    public String getYear()
    {
        return year;
    }

    public void setYear(String year)
    {
        this.year = year;
    }

    public String getProfileImageUrl()
    {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl)
    {
        this.profileImageUrl = profileImageUrl;
    }

    public List<String> getSubjects()
    {
        return subjects;
    }

    public void setSubjects(List<String> subjects)
    {
        this.subjects = subjects;
    }

    public String getStudyLocation()
    {
        return studyLocation;
    }

    public void setStudyLocation(String studyLocation)
    {
        this.studyLocation = studyLocation;
    }

    public Double getStudyLocationLat()
    {
        return studyLocationLat;
    }

    public void setStudyLocationLat(Double studyLocationLat)
    {
        this.studyLocationLat = studyLocationLat;
    }

    public Double getStudyLocationLng()
    {
        return studyLocationLng;
    }

    public void setStudyLocationLng(Double studyLocationLng)
    {
        this.studyLocationLng = studyLocationLng;
    }

    public boolean hasStudyCoordinates()
    {
        return studyLocationLat != null && studyLocationLng != null;
    }

    public String getBio()
    {
        return bio;
    }

    public void setBio(String bio)
    {
        this.bio = bio;
    }

    public String getMatricule()
    {
        return matricule;
    }

    public void setMatricule(String matricule)
    {
        this.matricule = matricule;
    }

    public Timestamp getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt)
    {
        this.createdAt = createdAt;
    }

    public Timestamp getLastActive()
    {
        return lastActive;
    }

    public void setLastActive(Timestamp lastActive)
    {
        this.lastActive = lastActive;
    }

    public int getStreak()
    {
        return streak;
    }

    public void setStreak(int streak)
    {
        this.streak = streak;
    }

    public String getFcmToken()
    {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken)
    {
        this.fcmToken = fcmToken;
    }

    public boolean isNotificationsEnabled()
    {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled)
    {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isLocationSharingEnabled()
    {
        return locationSharingEnabled;
    }

    public void setLocationSharingEnabled(boolean locationSharingEnabled)
    {
        this.locationSharingEnabled = locationSharingEnabled;
    }

    public boolean isDarkModeEnabled()
    {
        return darkModeEnabled;
    }

    public void setDarkModeEnabled(boolean darkModeEnabled)
    {
        this.darkModeEnabled = darkModeEnabled;
    }

    public boolean isSoundEnabled()
    {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled)
    {
        this.soundEnabled = soundEnabled;
    }

    public boolean isVibrationEnabled()
    {
        return vibrationEnabled;
    }

    public void setVibrationEnabled(boolean vibrationEnabled)
    {
        this.vibrationEnabled = vibrationEnabled;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }
}
