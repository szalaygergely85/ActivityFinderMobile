package com.gege.activityfindermobile.data.model;

import com.google.gson.annotations.SerializedName;

public class UserPhoto {
    @SerializedName("id")
    private Long id;

    @SerializedName("photoUrl")
    private String photoUrl;

    @SerializedName("isProfilePicture")
    private Boolean isProfilePicture;

    @SerializedName("displayOrder")
    private Integer displayOrder;

    @SerializedName("uploadedAt")
    private String uploadedAt;

    // Constructor
    public UserPhoto() {}

    public UserPhoto(String photoUrl, Boolean isProfilePicture, Integer displayOrder) {
        this.photoUrl = photoUrl;
        this.isProfilePicture = isProfilePicture;
        this.displayOrder = displayOrder;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Boolean getIsProfilePicture() {
        return isProfilePicture;
    }

    public void setIsProfilePicture(Boolean isProfilePicture) {
        this.isProfilePicture = isProfilePicture;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(String uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
