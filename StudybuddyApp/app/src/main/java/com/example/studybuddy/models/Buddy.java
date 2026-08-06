package com.example.studybuddy.models;

public class Buddy
{
    private String id;
    private String name;
    private String courseId;
    private String courseName;
    private String courseEmoji;
    private String imageUrl;
    private int iconResId;
    private String courseColor;
    private String quadrimester;
    private String systemPrompt;

    public Buddy()
    {
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getCourseId()
    {
        return courseId;
    }

    public String getCourseName()
    {
        return courseName;
    }

    public String getCourseEmoji()
    {
        return courseEmoji;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public int getIconResId()
    {
        return iconResId;
    }

    public String getCourseColor()
    {
        return courseColor;
    }

    public String getQuadrimester()
    {
        return quadrimester;
    }

    public String getSystemPrompt()
    {
        return systemPrompt;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setCourseId(String courseId)
    {
        this.courseId = courseId;
    }

    public void setCourseName(String courseName)
    {
        this.courseName = courseName;
    }

    public void setCourseEmoji(String courseEmoji)
    {
        this.courseEmoji = courseEmoji;
    }

    public void setImageUrl(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public void setIconResId(int iconResId)
    {
        this.iconResId = iconResId;
    }

    public void setCourseColor(String courseColor)
    {
        this.courseColor = courseColor;
    }

    public void setQuadrimester(String q)
    {
        this.quadrimester = q;
    }

    public void setSystemPrompt(String s)
    {
        this.systemPrompt = s;
    }
}
