package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class RefreshTokenRequest {
    @SerializedName("refreshToken")
    private String refreshToken;

    // Constructor
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and Setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
