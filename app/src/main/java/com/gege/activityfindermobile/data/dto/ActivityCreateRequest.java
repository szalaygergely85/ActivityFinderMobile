package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class ActivityCreateRequest {
    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("activityDate")
    private String activityDate;

    @SerializedName("location")
    private String location;

    @SerializedName("placeId")
    private String placeId;

    @SerializedName("totalSpots")
    private Integer totalSpots;

    @SerializedName("category")
    private String category;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    // Constructor
    public ActivityCreateRequest() {}

    public ActivityCreateRequest(
            String title,
            String description,
            String activityDate,
            String location,
            Integer totalSpots,
            String category) {
        this.title = title;
        this.description = description;
        this.activityDate = activityDate;
        this.location = location;
        this.totalSpots = totalSpots;
        this.category = category;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(String activityDate) {
        this.activityDate = activityDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Integer getTotalSpots() {
        return totalSpots;
    }

    public void setTotalSpots(Integer totalSpots) {
        this.totalSpots = totalSpots;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
