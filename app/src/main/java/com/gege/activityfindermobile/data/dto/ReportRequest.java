package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

/** Request to submit a report */
public class ReportRequest {
    @SerializedName("reportType")
    private String reportType;

    @SerializedName("reportedActivityId")
    private Long reportedActivityId;

    @SerializedName("reportedMessageId")
    private Long reportedMessageId;

    @SerializedName("reportedUserId")
    private Long reportedUserId;

    @SerializedName("reason")
    private String reason;

    public ReportRequest() {}

    // Constructor for reporting an activity
    public static ReportRequest forActivity(Long activityId, String reason) {
        ReportRequest request = new ReportRequest();
        request.reportType = "ACTIVITY";
        request.reportedActivityId = activityId;
        request.reason = reason;
        return request;
    }

    // Constructor for reporting a message
    public static ReportRequest forMessage(Long messageId, String reason) {
        ReportRequest request = new ReportRequest();
        request.reportType = "MESSAGE";
        request.reportedMessageId = messageId;
        request.reason = reason;
        return request;
    }

    // Constructor for reporting a user
    public static ReportRequest forUser(Long userId, String reason) {
        ReportRequest request = new ReportRequest();
        request.reportType = "USER";
        request.reportedUserId = userId;
        request.reason = reason;
        return request;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Long getReportedActivityId() {
        return reportedActivityId;
    }

    public void setReportedActivityId(Long reportedActivityId) {
        this.reportedActivityId = reportedActivityId;
    }

    public Long getReportedMessageId() {
        return reportedMessageId;
    }

    public void setReportedMessageId(Long reportedMessageId) {
        this.reportedMessageId = reportedMessageId;
    }

    public Long getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(Long reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
