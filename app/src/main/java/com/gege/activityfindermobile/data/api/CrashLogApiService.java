package com.gege.activityfindermobile.data.api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/** API Service for crash logs */
public interface CrashLogApiService {

    /** Submit a crash log to the backend */
    @POST("api/crash-logs")
    Call<Map<String, Object>> submitCrashLog(@Body Map<String, Object> crashLog);
}
