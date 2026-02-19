package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

/** Request to update notification preferences */
public class NotificationPreferencesRequest {
    @SerializedName("activityUpdatesEnabled")
    private Boolean activityUpdatesEnabled;

    @SerializedName("remindersEnabled")
    private Boolean remindersEnabled;

    public NotificationPreferencesRequest() {}

    public NotificationPreferencesRequest(Boolean activityUpdatesEnabled, Boolean remindersEnabled) {
        this.activityUpdatesEnabled = activityUpdatesEnabled;
        this.remindersEnabled = remindersEnabled;
    }

    public Boolean getActivityUpdatesEnabled() {
        return activityUpdatesEnabled;
    }

    public void setActivityUpdatesEnabled(Boolean activityUpdatesEnabled) {
        this.activityUpdatesEnabled = activityUpdatesEnabled;
    }

    public Boolean getRemindersEnabled() {
        return remindersEnabled;
    }

    public void setRemindersEnabled(Boolean remindersEnabled) {
        this.remindersEnabled = remindersEnabled;
    }
}
