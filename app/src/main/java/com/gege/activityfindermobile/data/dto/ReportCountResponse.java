package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

/** Response for report count */
public class ReportCountResponse {
    @SerializedName("reportCount")
    private Integer reportCount;

    public ReportCountResponse() {}

    public ReportCountResponse(Integer reportCount) {
        this.reportCount = reportCount;
    }

    public Integer getReportCount() {
        return reportCount;
    }

    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }
}
