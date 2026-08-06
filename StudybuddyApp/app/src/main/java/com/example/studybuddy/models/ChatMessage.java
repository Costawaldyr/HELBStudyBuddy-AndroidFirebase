package com.example.studybuddy.models;

import static com.example.studybuddy.utils.MyConstants.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage
{
    public static final int TYPE_USER = 0;
    public static final int TYPE_BUDDY = 1;
    public static final int TYPE_BUDDY_TYPING = 2;

    private final String text;
    private final int type;
    private final String timestamp;

    public ChatMessage(String text, int type)
    {
        this.text = text;
        this.type = type;
        this.timestamp = new SimpleDateFormat(TIME_FORMAT_HM, Locale.getDefault()).format(new Date());
    }

    public ChatMessage(String text, int type, String timestamp)
    {
        this.text = text;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getText()
    {
        return text;
    }

    public int getType()
    {
        return type;
    }

    public String getTimestamp()
    {
        return timestamp;
    }
}
