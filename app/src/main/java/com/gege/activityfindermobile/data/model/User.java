package com.gege.activityfindermobile.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class User {
    @SerializedName(
            value = "id",
            alternate = {"userId"})
    private Long id;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("email")
    private String email;

    @SerializedName("bio")
    private String bio;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("completedActivities")
    private Integer completedActivities;

    @SerializedName("interests")
    private List<String> interests;

    @SerializedName("badge")
    private String badge;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("photos")
    private List<UserPhoto> photos;

    @SerializedName("city")
    private String city;

    // Constructor
    public User() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public Integer getCompletedActivities() {
        return completedActivities;
    }

    public void setCompletedActivities(Integer completedActivities) {
        this.completedActivities = completedActivities;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<UserPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<UserPhoto> photos) {
        this.photos = photos;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
