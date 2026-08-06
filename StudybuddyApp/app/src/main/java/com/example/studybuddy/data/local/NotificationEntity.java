package com.example.studybuddy.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String message;
    private long timestamp;
    private String type; // e.g., "buddy", "message", "match"
    private String targetId; // e.g., buddyId, matchId

    public NotificationEntity(String title, String message, long timestamp, String type, String targetId) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.targetId = targetId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public String getTargetId() { return targetId; }
}
