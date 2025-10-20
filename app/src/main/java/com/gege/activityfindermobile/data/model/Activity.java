package com.gege.activityfindermobile.data.model;

import com.google.gson.annotations.SerializedName;

public class Activity {
    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("activityDate")
    private String activityDate;

    @SerializedName("date")
    private String date;

    @SerializedName("time")
    private String time;

    @SerializedName("location")
    private String location;

    @SerializedName("totalSpots")
    private Integer totalSpots;

    @SerializedName("availableSpots")
    private Integer availableSpots;

    @SerializedName("reservedForFriendsSpots")
    private Integer reservedForFriendsSpots;

    @SerializedName("category")
    private String category;

    @SerializedName("status")
    private String status;

    @SerializedName("trending")
    private Boolean trending;

    @SerializedName("creatorId")
    private Long creatorId;

    @SerializedName("creatorName")
    private String creatorName;

    @SerializedName(value = "creatorImageUrl", alternate = {"creatorAvatar"})
    private String creatorAvatar;

    @SerializedName("creatorRating")
    private Double creatorRating;

    @SerializedName("creatorBadge")
    private String creatorBadge;

    @SerializedName(value = "participantsCount", alternate = {"interestedUsersCount"})
    private Integer interestedUsersCount;

    @SerializedName("createdAt")
    private String createdAt;

    // Constructor
    public Activity() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getActivityDate() { return activityDate; }
    public void setActivityDate(String activityDate) {
        this.activityDate = activityDate;
        parseActivityDate();
    }

    public String getDate() {
        if (date == null && activityDate != null) {
            parseActivityDate();
        }
        return date;
    }
    public void setDate(String date) { this.date = date; }

    public String getTime() {
        if (time == null && activityDate != null) {
            parseActivityDate();
        }
        return time;
    }
    public void setTime(String time) { this.time = time; }

    /**
     * Parse activityDate (ISO 8601 format: "2024-10-25T08:00:00") into separate date and time
     */
    private void parseActivityDate() {
        if (activityDate == null || activityDate.isEmpty()) {
            return;
        }

        try {
            // Expected format: "2024-10-25T08:00:00"
            String[] parts = activityDate.split("T");
            if (parts.length >= 1) {
                // Parse date: "2024-10-25" -> "Oct 25, 2024"
                String[] dateParts = parts[0].split("-");
                if (dateParts.length == 3) {
                    int year = Integer.parseInt(dateParts[0]);
                    int month = Integer.parseInt(dateParts[1]);
                    int day = Integer.parseInt(dateParts[2]);

                    String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                    this.date = monthNames[month - 1] + " " + day + ", " + year;
                }

                if (parts.length >= 2) {
                    // Parse time: "08:00:00" -> "08:00 AM"
                    String timePart = parts[1];
                    String[] timeParts = timePart.split(":");
                    if (timeParts.length >= 2) {
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = Integer.parseInt(timeParts[1]);

                        String amPm = hour >= 12 ? "PM" : "AM";
                        int displayHour = hour % 12;
                        if (displayHour == 0) displayHour = 12;

                        this.time = String.format("%02d:%02d %s", displayHour, minute, amPm);
                    }
                }
            }
        } catch (Exception e) {
            // If parsing fails, leave date and time as null
            this.date = null;
            this.time = null;
        }
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getTotalSpots() { return totalSpots; }
    public void setTotalSpots(Integer totalSpots) { this.totalSpots = totalSpots; }

    public Integer getAvailableSpots() { return availableSpots; }
    public void setAvailableSpots(Integer availableSpots) { this.availableSpots = availableSpots; }

    public Integer getReservedForFriendsSpots() { return reservedForFriendsSpots; }
    public void setReservedForFriendsSpots(Integer reservedForFriendsSpots) {
        this.reservedForFriendsSpots = reservedForFriendsSpots;
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getTrending() { return trending; }
    public void setTrending(Boolean trending) { this.trending = trending; }

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getCreatorAvatar() { return creatorAvatar; }
    public void setCreatorAvatar(String creatorAvatar) { this.creatorAvatar = creatorAvatar; }

    public Double getCreatorRating() { return creatorRating; }
    public void setCreatorRating(Double creatorRating) { this.creatorRating = creatorRating; }

    public String getCreatorBadge() { return creatorBadge; }
    public void setCreatorBadge(String creatorBadge) { this.creatorBadge = creatorBadge; }

    public Integer getInterestedUsersCount() { return interestedUsersCount; }
    public void setInterestedUsersCount(Integer interestedUsersCount) {
        this.interestedUsersCount = interestedUsersCount;
    }

    /**
     * Get current participant count
     * First tries participantsCount from API, then falls back to calculation
     */
    public Integer getParticipantsCount() {
        if (interestedUsersCount != null) {
            return interestedUsersCount;
        }
        // Fallback: calculate from totalSpots - availableSpots
        if (totalSpots != null && availableSpots != null) {
            return totalSpots - availableSpots;
        }
        return 0;
    }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
