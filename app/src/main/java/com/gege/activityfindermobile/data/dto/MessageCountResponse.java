package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

/** Response object for message count */
public class MessageCountResponse {
    @SerializedName("messageCount")
    private Integer messageCount;

    public MessageCountResponse() {}

    public MessageCountResponse(Integer messageCount) {
        this.messageCount = messageCount;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
}
