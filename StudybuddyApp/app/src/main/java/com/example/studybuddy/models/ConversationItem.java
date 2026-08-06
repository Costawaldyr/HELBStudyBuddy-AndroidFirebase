package com.example.studybuddy.models;

public class ConversationItem
{
    public static final int TYPE_USER = 1;
    public static final int TYPE_AI = 2;

    private String id;
    private String name;
    private String imageUrl;
    private String lastMessage;
    private int messageCount;
    private int type;

    public ConversationItem(String id, String name, String imageUrl,
                            String lastMessage, int messageCount, int type)
    {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.lastMessage = lastMessage;
        this.messageCount = messageCount;
        this.type = type;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public String getLastMessage()
    {
        return lastMessage;
    }

    public int getMessageCount()
    {
        return messageCount;
    }

    public int getType()
    {
        return type;
    }
}
