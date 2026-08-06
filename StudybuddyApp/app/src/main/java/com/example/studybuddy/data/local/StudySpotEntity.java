package com.example.studybuddy.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "study_spots")
public class StudySpotEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String name;
    public String address;
    public double latitude;
    public double longitude;
    public boolean isFavorite;

    public StudySpotEntity(String name, String address, double latitude, double longitude) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isFavorite = false;
    }
}
