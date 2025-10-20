package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

/** Response for unread notification count */
public class UnreadCountResponse {
    @SerializedName("unreadCount")
    private Integer unreadCount;

    public UnreadCountResponse() {}

    public UnreadCountResponse(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }
}
