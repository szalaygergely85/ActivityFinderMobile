package com.gege.activityfindermobile.data.dto;

import com.google.gson.annotations.SerializedName;

/** Request to register FCM device token */
public class DeviceTokenRequest {
    @SerializedName("fcmToken")
    private String fcmToken;

    public DeviceTokenRequest() {}

    public DeviceTokenRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
