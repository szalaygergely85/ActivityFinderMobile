package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.dto.ReportCountResponse;
import com.gege.activityfindermobile.data.dto.ReportRequest;
import com.gege.activityfindermobile.data.model.Report;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/** API Service for reporting */
public interface ReportApiService {

    /** Submit a new report */
    @POST("api/reports")
    Call<Report> submitReport(@Body ReportRequest request);

    /** Get my reports (reports submitted by current user) */
    @GET("api/reports/my-reports")
    Call<List<Report>> getMyReports();

    /** Get pending reports (admin only) */
    @GET("api/reports/pending")
    Call<List<Report>> getPendingReports();

    /** Get reports by type and status (admin only) */
    @GET("api/reports")
    Call<List<Report>> getReportsByTypeAndStatus(
            @Query("type") String reportType, @Query("status") String status);

    /** Update report status (admin only) */
    @PATCH("api/reports/{reportId}/status")
    Call<Report> updateReportStatus(
            @Path("reportId") Long reportId, @Query("status") String status);

    /** Get activity report count */
    @GET("api/reports/activity/{activityId}/count")
    Call<ReportCountResponse> getActivityReportCount(@Path("activityId") Long activityId);

    /** Get message report count */
    @GET("api/reports/message/{messageId}/count")
    Call<ReportCountResponse> getMessageReportCount(@Path("messageId") Long messageId);
}
