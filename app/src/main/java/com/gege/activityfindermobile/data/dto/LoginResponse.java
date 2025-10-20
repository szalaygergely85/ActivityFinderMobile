package com.gege.activityfindermobile.data.dto;

import com.gege.activityfindermobile.data.model.User;
import com.google.gson.annotations.SerializedName;

/** Response model for login/register endpoints Contains JWT token and user information */
public class LoginResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("user")
    private User user;

    public LoginResponse() {}

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
