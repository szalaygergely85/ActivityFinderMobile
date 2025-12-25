package com.gege.activityfindermobile.data.model;

import com.google.gson.annotations.SerializedName;

public class ActivityGalleryAccess {
    @SerializedName("hasAccess")
    private Boolean hasAccess;

    @SerializedName("reason")
    private String reason;

    @SerializedName("canUpload")
    private Boolean canUpload;

    @SerializedName("photoCount")
    private Integer photoCount;

    @SerializedName("maxPhotos")
    private Integer maxPhotos;

    // Constructor
    public ActivityGalleryAccess() {}

    // Getters and Setters
    public Boolean getHasAccess() {
        return hasAccess;
    }

    public void setHasAccess(Boolean hasAccess) {
        this.hasAccess = hasAccess;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getCanUpload() {
        return canUpload;
    }

    public void setCanUpload(Boolean canUpload) {
        this.canUpload = canUpload;
    }

    public Integer getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }

    public Integer getMaxPhotos() {
        return maxPhotos;
    }

    public void setMaxPhotos(Integer maxPhotos) {
        this.maxPhotos = maxPhotos;
    }
}
