package com.gege.activityfindermobile.utils;

/** Utility class for string operations */
public class StringUtil {

    private StringUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Check if a string is null or empty
     *
     * @param str String to check
     * @return true if string is null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not empty
     *
     * @param str String to check
     * @return true if string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Get string or default value if null/empty
     *
     * @param str String to check
     * @param defaultValue Default value to return if string is null/empty
     * @return Original string or default value
     */
    public static String getOrDefault(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * Capitalize first letter of a string
     *
     * @param str String to capitalize
     * @return Capitalized string or empty string if null
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return "";
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Truncate string to specified length and add ellipsis if needed
     *
     * @param str String to truncate
     * @param maxLength Maximum length
     * @return Truncated string with ellipsis or original if shorter
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Check if two strings are equal (null-safe)
     *
     * @param str1 First string
     * @param str2 Second string
     * @return true if strings are equal
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * Check if two strings are equal ignoring case (null-safe)
     *
     * @param str1 First string
     * @param str2 Second string
     * @return true if strings are equal ignoring case
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }
}
