package com.gege.activityfindermobile.data.repository;

import android.util.Log;

import com.gege.activityfindermobile.data.api.ReportApiService;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.dto.ReportCountResponse;
import com.gege.activityfindermobile.data.dto.ReportRequest;
import com.gege.activityfindermobile.data.model.Report;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ReportRepository {
    private static final String TAG = "ReportRepository";
    private final ReportApiService reportApiService;

    @Inject
    public ReportRepository(ReportApiService reportApiService) {
        this.reportApiService = reportApiService;
    }

    /** Submit a report */
    public void submitReport(ReportRequest request, ApiCallback<Report> callback) {
        reportApiService
                .submitReport(request)
                .enqueue(
                        new Callback<Report>() {
                            @Override
                            public void onResponse(Call<Report> call, Response<Report> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Report submitted successfully");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg = "Failed to submit report: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Report> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Report an activity */
    public void reportActivity(Long activityId, String reason, ApiCallback<Report> callback) {
        ReportRequest request = ReportRequest.forActivity(activityId, reason);
        submitReport(request, callback);
    }

    /** Report a message */
    public void reportMessage(Long messageId, String reason, ApiCallback<Report> callback) {
        ReportRequest request = ReportRequest.forMessage(messageId, reason);
        submitReport(request, callback);
    }

    /** Report a user */
    public void reportUser(Long userId, String reason, ApiCallback<Report> callback) {
        ReportRequest request = ReportRequest.forUser(userId, reason);
        submitReport(request, callback);
    }

    /** Get my reports */
    public void getMyReports(ApiCallback<List<Report>> callback) {
        reportApiService
                .getMyReports()
                .enqueue(
                        new Callback<List<Report>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Report>> call, Response<List<Report>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Fetched " + response.body().size() + " reports");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg = "Failed to fetch reports: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Report>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get pending reports (admin only) */
    public void getPendingReports(ApiCallback<List<Report>> callback) {
        reportApiService
                .getPendingReports()
                .enqueue(
                        new Callback<List<Report>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Report>> call, Response<List<Report>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(
                                            TAG,
                                            "Fetched "
                                                    + response.body().size()
                                                    + " pending reports");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to fetch pending reports: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Report>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get reports by type and status (admin only) */
    public void getReportsByTypeAndStatus(
            String reportType, String status, ApiCallback<List<Report>> callback) {
        reportApiService
                .getReportsByTypeAndStatus(reportType, status)
                .enqueue(
                        new Callback<List<Report>>() {
                            @Override
                            public void onResponse(
                                    Call<List<Report>> call, Response<List<Report>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Fetched filtered reports");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg = "Failed to fetch reports: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Report>> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Update report status (admin only) */
    public void updateReportStatus(Long reportId, String status, ApiCallback<Report> callback) {
        reportApiService
                .updateReportStatus(reportId, status)
                .enqueue(
                        new Callback<Report>() {
                            @Override
                            public void onResponse(Call<Report> call, Response<Report> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Log.d(TAG, "Report status updated");
                                    callback.onSuccess(response.body());
                                } else {
                                    String errorMsg =
                                            "Failed to update report status: " + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<Report> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get activity report count */
    public void getActivityReportCount(Long activityId, ApiCallback<Integer> callback) {
        reportApiService
                .getActivityReportCount(activityId)
                .enqueue(
                        new Callback<ReportCountResponse>() {
                            @Override
                            public void onResponse(
                                    Call<ReportCountResponse> call,
                                    Response<ReportCountResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Integer count = response.body().getReportCount();
                                    Log.d(TAG, "Activity report count: " + count);
                                    callback.onSuccess(count);
                                } else {
                                    String errorMsg =
                                            "Failed to fetch activity report count: "
                                                    + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<ReportCountResponse> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }

    /** Get message report count */
    public void getMessageReportCount(Long messageId, ApiCallback<Integer> callback) {
        reportApiService
                .getMessageReportCount(messageId)
                .enqueue(
                        new Callback<ReportCountResponse>() {
                            @Override
                            public void onResponse(
                                    Call<ReportCountResponse> call,
                                    Response<ReportCountResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Integer count = response.body().getReportCount();
                                    Log.d(TAG, "Message report count: " + count);
                                    callback.onSuccess(count);
                                } else {
                                    String errorMsg =
                                            "Failed to fetch message report count: "
                                                    + response.code();
                                    Log.e(TAG, errorMsg);
                                    callback.onError(errorMsg);
                                }
                            }

                            @Override
                            public void onFailure(Call<ReportCountResponse> call, Throwable t) {
                                String errorMsg = "Network error: " + t.getMessage();
                                Log.e(TAG, errorMsg, t);
                                callback.onError(errorMsg);
                            }
                        });
    }
}
