package com.gege.activityfindermobile.data.model;

import com.google.gson.annotations.SerializedName;

public class CoverImage {
    @SerializedName("id")
    private Long id;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("displayName")
    private String displayName;

    public CoverImage() {}

    public CoverImage(Long id, String imageUrl, String displayName) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
