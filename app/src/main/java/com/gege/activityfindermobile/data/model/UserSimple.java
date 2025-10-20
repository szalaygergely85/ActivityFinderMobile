package com.gege.activityfindermobile.data.model;

import com.google.gson.annotations.SerializedName;

public class UserSimple {
    @SerializedName("id")
    private Long id;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("badge")
    private String badge;

    // Constructor
    public UserSimple() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
}
