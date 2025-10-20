package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

/** Request object for sending a message */
public class MessageRequest {
    @SerializedName("messageText")
    private String messageText;

    public MessageRequest() {}

    public MessageRequest(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
