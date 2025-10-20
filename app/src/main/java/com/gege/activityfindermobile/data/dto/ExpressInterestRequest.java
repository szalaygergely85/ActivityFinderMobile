package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class ExpressInterestRequest {
    @SerializedName("isFriend")
    private Boolean isFriend;

    // Constructor
    public ExpressInterestRequest() {}

    public ExpressInterestRequest(Boolean isFriend) {
        this.isFriend = isFriend;
    }

    // Getters and Setters
    public Boolean getIsFriend() { return isFriend; }
    public void setIsFriend(Boolean isFriend) { this.isFriend = isFriend; }
}
