package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

/** Response model for login/register endpoints. Contains JWT tokens and user information */
public class LoginResponse {
    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("refreshToken")
    private String refreshToken;

    @SerializedName("type")
    private String type = "Bearer";

    @SerializedName("userId")
    private Long userId;

    @SerializedName("email")
    private String email;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("badge")
    private String badge;

    public LoginResponse() {}

    public LoginResponse(String accessToken, String refreshToken, Long userId, String email,
                        String fullName, String profileImageUrl, Double rating, String badge) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
        this.rating = rating;
        this.badge = badge;
    }

    public LoginResponse(String accessToken, String refreshToken, String type, Long userId,
                        String email, String fullName, String profileImageUrl, Double rating, String badge) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.type = type;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.profileImageUrl = profileImageUrl;
        this.rating = rating;
        this.badge = badge;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }
}
