package com.example.studybuddy.models;

import static com.example.studybuddy.utils.MyConstants.*;

import com.google.firebase.Timestamp;

public class Match
{
    private String matchId;
    private String userId1;
    private String userId2;
    private String name;
    private String school;
    private String subject;
    private String location;
    private String profileImageUrl;
    private Timestamp matchedAt;
    private Timestamp expiresAt;
    private boolean isActive;
    private String status;

    public Match()
    {
    }

    public String getMatchId()
    {
        return matchId;
    }

    public void setMatchId(String matchId)
    {
        this.matchId = matchId;
    }

    public String getUserId1()
    {
        return userId1;
    }

    public void setUserId1(String userId1)
    {
        this.userId1 = userId1;
    }

    public String getUserId2()
    {
        return userId2;
    }

    public void setUserId2(String userId2)
    {
        this.userId2 = userId2;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSchool()
    {
        return school;
    }

    public void setSchool(String school)
    {
        this.school = school;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getProfileImageUrl()
    {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl)
    {
        this.profileImageUrl = profileImageUrl;
    }

    public Timestamp getMatchedAt()
    {
        return matchedAt;
    }

    public void setMatchedAt(Timestamp matchedAt)
    {
        this.matchedAt = matchedAt;
    }

    public Timestamp getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public boolean isActive()
    {
        return isActive;
    }

    public void setActive(boolean active)
    {
        isActive = active;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getOtherUserId(String currentUserId)
    {
        return currentUserId.equals(userId1) ? userId2 : userId1;
    }

    public boolean isExpiredByTime()
    {
        if (expiresAt == null)
        {
            return false;
        }
        return expiresAt.toDate().getTime() <= System.currentTimeMillis();
    }

    public boolean isCurrentlyActive()
    {
        boolean activeFlag = isActive || STATUS_ACTIVE.equalsIgnoreCase(status);
        return activeFlag && !isExpiredByTime();
    }

    public String getTimeRemainingText()
    {
        if (expiresAt == null)
        {
            return EMPTY_DASH;
        }

        long diff = expiresAt.toDate().getTime() - System.currentTimeMillis();
        if (diff <= ZERO)
        {
            return EXPIRED_LABEL;
        }

        long hours = diff / ONE_HOUR_MS;
        long minutes = (diff % ONE_HOUR_MS) / TIMER_INTERVAL_CHAT_MS;

        if (hours > ZERO)
        {
            return String.format(TIME_TEMPLATE_HM, hours, minutes);
        }
        return String.format(TIME_TEMPLATE_M, minutes);
    }
}
