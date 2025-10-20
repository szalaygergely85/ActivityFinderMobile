package com.gege.activityfindermobile.data.model;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("id")
    private Long id;

    @SerializedName("reviewerId")
    private Long reviewerId;

    @SerializedName("reviewerName")
    private String reviewerName;

    @SerializedName("reviewerAvatar")
    private String reviewerAvatar;

    @SerializedName("reviewedUserId")
    private Long reviewedUserId;

    @SerializedName("reviewedUserName")
    private String reviewedUserName;

    @SerializedName("activityId")
    private Long activityId;

    @SerializedName("activityTitle")
    private String activityTitle;

    @SerializedName("rating")
    private Integer rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("createdAt")
    private String createdAt;

    // Constructor
    public Review() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReviewerId() { return reviewerId; }
    public void setReviewerId(Long reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getReviewerAvatar() { return reviewerAvatar; }
    public void setReviewerAvatar(String reviewerAvatar) { this.reviewerAvatar = reviewerAvatar; }

    public Long getReviewedUserId() { return reviewedUserId; }
    public void setReviewedUserId(Long reviewedUserId) { this.reviewedUserId = reviewedUserId; }

    public String getReviewedUserName() { return reviewedUserName; }
    public void setReviewedUserName(String reviewedUserName) { this.reviewedUserName = reviewedUserName; }

    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }

    public String getActivityTitle() { return activityTitle; }
    public void setActivityTitle(String activityTitle) { this.activityTitle = activityTitle; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
