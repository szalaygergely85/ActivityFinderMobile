package com.gege.activityfindermobile.data.callback;

/**
 * Callback interface for API responses that don't return data (e.g., DELETE operations)
 */
public interface ApiCallbackVoid {
    /**
     * Called when the API call is successful
     */
    void onSuccess();

    /**
     * Called when the API call fails
     * @param errorMessage The error message describing what went wrong
     */
    void onError(String errorMessage);
}
