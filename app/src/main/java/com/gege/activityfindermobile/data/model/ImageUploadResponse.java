package com.gege.activityfindermobile.data.model;

import com.google.gson.annotations.SerializedName;

public class ImageUploadResponse {
    @SerializedName("url")
    private String url;

    // Constructor
    public ImageUploadResponse() {}

    public ImageUploadResponse(String url) {
        this.url = url;
    }

    // Getters and Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
