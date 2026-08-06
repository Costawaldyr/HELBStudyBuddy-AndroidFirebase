package com.example.studybuddy.models;

public class Course
{
    private String id;
    private String name;
    private String code;
    private String emoji;
    private String quadrimester;
    private String color;

    public Course()
    {
    }

    public Course(String id, String name, String code, String emoji, String quadrimester, String color)
    {
        this.id = id;
        this.name = name;
        this.code = code;
        this.emoji = emoji;
        this.quadrimester = quadrimester;
        this.color = color;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getCode()
    {
        return code;
    }

    public String getEmoji()
    {
        return emoji;
    }

    public String getQuadrimester()
    {
        return quadrimester;
    }

    public String getColor()
    {
        return color;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public void setEmoji(String emoji)
    {
        this.emoji = emoji;
    }

    public void setQuadrimester(String quadrimester)
    {
        this.quadrimester = quadrimester;
    }

    public void setColor(String color)
    {
        this.color = color;
    }
}
