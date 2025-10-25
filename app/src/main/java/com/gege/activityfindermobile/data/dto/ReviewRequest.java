package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

public class ReviewRequest {
    @SerializedName("reviewedUserId")
    private Long reviewedUserId;

    @SerializedName("activityId")
    private Long activityId;

    @SerializedName("rating")
    private Integer rating;

    @SerializedName("comment")
    private String comment;

    // Constructor
    public ReviewRequest() {}

    public ReviewRequest(Long reviewedUserId, Long activityId, Integer rating, String comment) {
        this.reviewedUserId = reviewedUserId;
        this.activityId = activityId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public Long getReviewedUserId() {
        return reviewedUserId;
    }

    public void setReviewedUserId(Long reviewedUserId) {
        this.reviewedUserId = reviewedUserId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
