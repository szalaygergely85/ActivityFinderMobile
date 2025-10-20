package com.gege.activityfindermobile.data.model;

import com.google.gson.annotations.SerializedName;

/** Model for user reports */
public class Report {
    @SerializedName("id")
    private Long id;

    @SerializedName("reporterId")
    private Long reporterId;

    @SerializedName("reporterName")
    private String reporterName;

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

    @SerializedName("status")
    private String status;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("resolvedAt")
    private String resolvedAt;

    public Report() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
