package com.gege.activityfindermobile.data.api;

import com.gege.activityfindermobile.data.dto.LoginRequest;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.dto.UserProfileUpdateRequest;
import com.gege.activityfindermobile.data.dto.UserRegistrationRequest;
import com.gege.activityfindermobile.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserApiService {

    @POST("api/users/register")
    Call<LoginResponse> registerUser(@Body UserRegistrationRequest request);

    @POST("api/users/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    // Also support the new /api/auth endpoints from backend
    @POST("api/auth/register")
    Call<LoginResponse> registerUserAuth(@Body UserRegistrationRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> loginUserAuth(@Body LoginRequest request);

    @GET("api/users/{id}")
    Call<User> getUserById(@Path("id") Long id);

    @GET("api/users/email/{email}")
    Call<User> getUserByEmail(@Path("email") String email);

    @PUT("api/users/{id}")
    Call<User> updateUserProfile(
            @Path("id") Long id,
            @Header("User-Id") Long userId,
            @Body UserProfileUpdateRequest request);

    @GET("api/users")
    Call<List<User>> getAllActiveUsers();

    @GET("api/users/search")
    Call<List<User>> searchUsers(@Query("name") String name);

    @GET("api/users/interest/{interest}")
    Call<List<User>> getUsersByInterest(@Path("interest") String interest);

    @GET("api/users/top-rated")
    Call<List<User>> getTopRatedUsers();

    @DELETE("api/users/{id}")
    Call<Void> deactivateUser(@Path("id") Long id);
}
