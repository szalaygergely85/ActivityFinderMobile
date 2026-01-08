package com.gege.activityfindermobile.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

/** Utility class for common UI operations */
public class UiUtil {

    private UiUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Show a short toast message
     *
     * @param context Context
     * @param message Message to display
     */
    public static void showToast(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show a long toast message
     *
     * @param context Context
     * @param message Message to display
     */
    public static void showLongToast(Context context, String message) {
        if (context != null && message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Get color from resources
     *
     * @param context Context
     * @param colorRes Color resource ID
     * @return Color int value
     */
    public static int getColor(Context context, @ColorRes int colorRes) {
        return ContextCompat.getColor(context, colorRes);
    }

    /**
     * Convert DP to pixels
     *
     * @param context Context
     * @param dp DP value
     * @return Pixel value
     */
    public static int dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Convert pixels to DP
     *
     * @param context Context
     * @param px Pixel value
     * @return DP value
     */
    public static int pxToDp(Context context, float px) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(px / density);
    }
}
