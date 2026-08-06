package com.example.studybuddy.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StudySpotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StudySpotEntity studySpot);

    @Update
    void update(StudySpotEntity studySpot);

    @Delete
    void delete(StudySpotEntity studySpot);

    @Query("SELECT * FROM study_spots ORDER BY name ASC")
    LiveData<List<StudySpotEntity>> getAllStudySpots();

    @Query("SELECT * FROM study_spots WHERE isFavorite = 1")
    LiveData<List<StudySpotEntity>> getFavoriteStudySpots();
}
