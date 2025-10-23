package com.gege.activityfindermobile.data.model;

import com.google.gson.annotations.SerializedName;

public class Participant {
    @SerializedName(
            value = "id",
            alternate = {"participantId"})
    private Long id;

    @SerializedName("activity")
    private Activity activity;

    @SerializedName("activityId")
    private Long activityId;

    @SerializedName("activityTitle")
    private String activityTitle;

    @SerializedName("user")
    private UserSimple user;

    // Legacy flat fields (for backward compatibility)
    @SerializedName("userId")
    private Long userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("userAvatar")
    private String userAvatar;

    @SerializedName("userRating")
    private Double userRating;

    @SerializedName("userBadge")
    private String userBadge;

    @SerializedName("status")
    private String status;

    @SerializedName("isFriend")
    private Boolean isFriend;

    @SerializedName("joinedAt")
    private String joinedAt;

    @SerializedName("applicationAttempts")
    private Integer applicationAttempts;

    // Constructor
    public Participant() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Long getActivityId() {
        if (activity != null && activity.getId() != null) {
            return activity.getId();
        }
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getActivityTitle() {
        if (activity != null && activity.getTitle() != null) {
            return activity.getTitle();
        }
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public UserSimple getUser() {
        return user;
    }

    public void setUser(UserSimple user) {
        this.user = user;
    }

    // Smart getters that check both nested user object and flat fields
    public Long getUserId() {
        if (user != null && user.getId() != null) {
            return user.getId();
        }
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        if (user != null && user.getFullName() != null) {
            return user.getFullName();
        }
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        if (user != null && user.getProfileImageUrl() != null) {
            return user.getProfileImageUrl();
        }
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public Double getUserRating() {
        if (user != null && user.getRating() != null) {
            return user.getRating();
        }
        return userRating;
    }

    public void setUserRating(Double userRating) {
        this.userRating = userRating;
    }

    public String getUserBadge() {
        if (user != null && user.getBadge() != null) {
            return user.getBadge();
        }
        return userBadge;
    }

    public void setUserBadge(String userBadge) {
        this.userBadge = userBadge;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsFriend() {
        return isFriend;
    }

    public void setIsFriend(Boolean isFriend) {
        this.isFriend = isFriend;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Integer getApplicationAttempts() {
        return applicationAttempts;
    }

    public void setApplicationAttempts(Integer applicationAttempts) {
        this.applicationAttempts = applicationAttempts;
    }
}
