package com.example.studybuddy.models;

import com.example.studybuddy.R;
import com.google.firebase.Timestamp;

public class MatchCandidate
{
    private String userId = "";
    private String name = "";
    private String school = "";
    private String subject = "";
    private String profileImageUrl = "";
    private Timestamp timestamp;

    public MatchCandidate()
    {
    }

    public MatchCandidate(String userId, String name, String school, String subject, String profileImageUrl, Timestamp timestamp)
    {
        this.userId = userId;
        this.name = name;
        this.school = school;
        this.subject = subject;
        this.profileImageUrl = profileImageUrl;
        this.timestamp = timestamp;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = (userId != null) ? userId : "";
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = (name != null) ? name : "";
    }

    public String getSchool()
    {
        return school;
    }

    public void setSchool(String school)
    {
        this.school = (school != null) ? school : "";
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = (subject != null) ? subject : "";
    }

    public String getProfileImageUrl()
    {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl)
    {
        this.profileImageUrl = (profileImageUrl != null) ? profileImageUrl : "";
    }

    public Timestamp getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getDescriptionText(android.content.Context context)
    {
        boolean hasSubject = !subject.isEmpty();
        String subjectToShow = hasSubject ? subject : context.getString(R.string.this_subject);

        boolean hasSchool = !school.isEmpty();
        if (hasSchool)
        {
            return context.getString(R.string.match_candidate_description_with_school, name, school, subjectToShow);
        }
        else
        {
            return context.getString(R.string.match_candidate_description, name, subjectToShow);
        }
    }
}
