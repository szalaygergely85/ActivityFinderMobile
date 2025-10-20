package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserProfileUpdateRequest {
    @SerializedName("fullName")
    private String fullName;

    @SerializedName("bio")
    private String bio;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    @SerializedName("interests")
    private List<String> interests;

    // Constructor
    public UserProfileUpdateRequest() {}

    public UserProfileUpdateRequest(String fullName, String bio, String profileImageUrl, List<String> interests) {
        this.fullName = fullName;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
        this.interests = interests;
    }

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
}
