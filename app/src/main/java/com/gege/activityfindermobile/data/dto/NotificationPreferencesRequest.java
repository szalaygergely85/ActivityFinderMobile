package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

/** Request to update notification preferences */
public class NotificationPreferencesRequest {
    @SerializedName("notificationsEnabled")
    private Boolean notificationsEnabled;

    public NotificationPreferencesRequest() {}

    public NotificationPreferencesRequest(Boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(Boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}
