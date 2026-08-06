package com.example.studybuddy.models;

import static com.example.studybuddy.utils.MyConstants.*;

import com.google.firebase.Timestamp;

public class Conversation
{
    private String conversationId;
    private String matchId;
    private String type;
    private String lastMessage;
    private Timestamp lastTimestamp;
    private boolean isExpired;
    private Timestamp expiresAt;
    private String participantId;
    private String participantName;
    private String participantImage;
    private String participantEmoji;
    private String participantColor;
    private int messageCount;

    public Conversation()
    {
    }

    public String getConversationId()
    {
        return conversationId;
    }

    public void setConversationId(String conversationId)
    {
        this.conversationId = conversationId;
    }

    public String getMatchId()
    {
        return matchId;
    }

    public void setMatchId(String matchId)
    {
        this.matchId = matchId;
    }

    public int getMessageCount()
    {
        return messageCount;
    }

    public void setMessageCount(int messageCount)
    {
        this.messageCount = messageCount;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getLastMessage()
    {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage)
    {
        this.lastMessage = lastMessage;
    }

    public Timestamp getLastTimestamp()
    {
        return lastTimestamp;
    }

    public void setLastTimestamp(Timestamp lastTimestamp)
    {
        this.lastTimestamp = lastTimestamp;
    }

    public boolean isExpired()
    {
        return isExpired;
    }

    public void setExpired(boolean expired)
    {
        isExpired = expired;
    }

    public Timestamp getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt)
    {
        this.expiresAt = expiresAt;
    }

    public String getParticipantId()
    {
        return participantId;
    }

    public void setParticipantId(String participantId)
    {
        this.participantId = participantId;
    }

    public String getParticipantName()
    {
        return participantName;
    }

    public void setParticipantName(String participantName)
    {
        this.participantName = participantName;
    }

    public String getParticipantImage()
    {
        return participantImage;
    }

    public void setParticipantImage(String participantImage)
    {
        this.participantImage = participantImage;
    }

    public String getParticipantEmoji()
    {
        return participantEmoji;
    }

    public void setParticipantEmoji(String participantEmoji)
    {
        this.participantEmoji = participantEmoji;
    }

    public String getParticipantColor()
    {
        return participantColor;
    }

    public void setParticipantColor(String participantColor)
    {
        this.participantColor = participantColor;
    }

    public boolean isAi()
    {
        return CONVERSATION_TYPE_AI.equals(type);
    }

    public boolean isExpiredNow()
    {
        if (isAi())
        {
            return false;
        }
        if (expiresAt != null)
        {
            return expiresAt.toDate().getTime() <= System.currentTimeMillis();
        }
        return isExpired;
    }
}
