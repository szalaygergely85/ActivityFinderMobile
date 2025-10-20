package com.gege.activityfindermobile.data.callback;

/**
 * Generic callback interface for API responses
 * @param <T> The type of data expected in the response
 */
public interface ApiCallback<T> {
    /**
     * Called when the API call is successful
     * @param data The response data
     */
    void onSuccess(T data);

    /**
     * Called when the API call fails
     * @param errorMessage The error message describing what went wrong
     */
    void onError(String errorMessage);
}
