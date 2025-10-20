package com.gege.activityfindermobile.data.api;

import com.google.gson.annotations.SerializedName;

public class ParticipantStatusUpdateRequest {
    @SerializedName("status")
    private String status;

    public ParticipantStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
