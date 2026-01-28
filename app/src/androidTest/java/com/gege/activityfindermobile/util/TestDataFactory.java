package com.gege.activityfindermobile.util;

import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.dto.UserProfileUpdateRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Factory for generating test data with unique identifiers.
 * Ensures test isolation by creating unique users and activities for each test.
 */
public class TestDataFactory {

    private static final String DEFAULT_PASSWORD = "TestPass123!";
    private static final String DEFAULT_BIRTH_DATE = "1990-01-15";

    /**
     * Generate a unique email for testing.
     */
    public static String uniqueEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    /**
     * Generate a unique email with a prefix.
     */
    public static String uniqueEmail(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    /**
     * Generate a unique full name.
     */
    public static String uniqueFullName() {
        return "Test User " + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * Generate a unique full name with prefix.
     */
    public static String uniqueFullName(String prefix) {
        return prefix + " " + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * Get default test password.
     */
    public static String defaultPassword() {
        return DEFAULT_PASSWORD;
    }

    /**
     * Get default birth date (adult user).
     */
    public static String defaultBirthDate() {
        return DEFAULT_BIRTH_DATE;
    }

    /**
     * Generate a birth date for a specific age.
     */
    public static String birthDateForAge(int age) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -age);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(cal.getTime());
    }

    /**
     * Generate a unique activity title.
     */
    public static String uniqueActivityTitle() {
        return "Test Activity " + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * Generate a unique activity title with prefix.
     */
    public static String uniqueActivityTitle(String prefix) {
        return prefix + " " + UUID.randomUUID().toString().substring(0, 6);
    }

    /**
     * Generate an activity date in the future (days from now).
     */
    public static String activityDateInFuture(int daysFromNow) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysFromNow);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        return sdf.format(cal.getTime());
    }

    /**
     * Generate an activity date tomorrow at 2 PM.
     */
    public static String activityDateTomorrow() {
        return activityDateInFuture(1);
    }

    /**
     * Generate an activity date next week.
     */
    public static String activityDateNextWeek() {
        return activityDateInFuture(7);
    }

    /**
     * Create a basic ActivityCreateRequest for testing.
     */
    public static ActivityCreateRequest createBasicActivity() {
        ActivityCreateRequest request = new ActivityCreateRequest();
        request.setTitle(uniqueActivityTitle());
        request.setDescription("This is a test activity for automated testing.");
        request.setActivityDate(activityDateTomorrow());
        request.setLocation("Test Location, Test City");
        request.setTotalSpots(5);
        request.setCategory("SPORTS");
        request.setLatitude(40.7128);
        request.setLongitude(-74.0060);
        return request;
    }

    /**
     * Create an ActivityCreateRequest with custom parameters.
     */
    public static ActivityCreateRequest createActivity(String title, String category, int totalSpots) {
        ActivityCreateRequest request = new ActivityCreateRequest();
        request.setTitle(title);
        request.setDescription("Test activity: " + title);
        request.setActivityDate(activityDateTomorrow());
        request.setLocation("Test Location, Test City");
        request.setTotalSpots(totalSpots);
        request.setCategory(category);
        request.setLatitude(40.7128);
        request.setLongitude(-74.0060);
        return request;
    }

    /**
     * Create an ActivityCreateRequest with all parameters.
     */
    public static ActivityCreateRequest createActivity(
            String title,
            String description,
            String activityDate,
            String location,
            int totalSpots,
            String category,
            double latitude,
            double longitude) {

        ActivityCreateRequest request = new ActivityCreateRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setActivityDate(activityDate);
        request.setLocation(location);
        request.setTotalSpots(totalSpots);
        request.setCategory(category);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        return request;
    }

    // ==================== Test User Presets ====================

    /**
     * Data class for test user credentials.
     */
    public static class TestUser {
        public final String fullName;
        public final String email;
        public final String password;
        public final String birthDate;

        public TestUser(String fullName, String email, String password, String birthDate) {
            this.fullName = fullName;
            this.email = email;
            this.password = password;
            this.birthDate = birthDate;
        }
    }

    /**
     * Create a unique test user with default values.
     */
    public static TestUser createTestUser() {
        return new TestUser(
                uniqueFullName(),
                uniqueEmail(),
                DEFAULT_PASSWORD,
                DEFAULT_BIRTH_DATE
        );
    }

    /**
     * Create a unique test user with custom name prefix.
     */
    public static TestUser createTestUser(String namePrefix) {
        return new TestUser(
                uniqueFullName(namePrefix),
                uniqueEmail(namePrefix.toLowerCase().replace(" ", "_")),
                DEFAULT_PASSWORD,
                DEFAULT_BIRTH_DATE
        );
    }

    /**
     * Create a test user with specific age.
     */
    public static TestUser createTestUserWithAge(int age) {
        return new TestUser(
                uniqueFullName(),
                uniqueEmail(),
                DEFAULT_PASSWORD,
                birthDateForAge(age)
        );
    }

    // ==================== Categories ====================

    public static final String CATEGORY_SPORTS = "SPORTS";
    public static final String CATEGORY_MUSIC = "MUSIC";
    public static final String CATEGORY_FOOD = "FOOD";
    public static final String CATEGORY_OUTDOORS = "OUTDOORS";
    public static final String CATEGORY_GAMES = "GAMES";
    public static final String CATEGORY_ARTS = "ARTS";
    public static final String CATEGORY_SOCIAL = "SOCIAL";
    public static final String CATEGORY_LEARNING = "LEARNING";

    // ==================== Location Presets ====================

    // New York City coordinates
    public static final double NYC_LATITUDE = 40.7128;
    public static final double NYC_LONGITUDE = -74.0060;

    // ~5km from NYC (Jersey City)
    public static final double JERSEY_CITY_LATITUDE = 40.7178;
    public static final double JERSEY_CITY_LONGITUDE = -74.0431;

    // ~15km from NYC (Newark)
    public static final double NEWARK_LATITUDE = 40.7357;
    public static final double NEWARK_LONGITUDE = -74.1724;

    // ~30km from NYC (Yonkers)
    public static final double YONKERS_LATITUDE = 40.9312;
    public static final double YONKERS_LONGITUDE = -73.8987;

    /**
     * Create an activity at specific coordinates.
     */
    public static ActivityCreateRequest createActivityAtLocation(
            String title, String category, double latitude, double longitude, String activityDate) {
        ActivityCreateRequest request = new ActivityCreateRequest();
        request.setTitle(title);
        request.setDescription("Test activity at location: " + title);
        request.setActivityDate(activityDate);
        request.setLocation("Test Location");
        request.setTotalSpots(5);
        request.setCategory(category);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        return request;
    }

    /**
     * Create an activity at specific coordinates with default date (tomorrow).
     */
    public static ActivityCreateRequest createActivityAtLocation(
            String title, String category, double latitude, double longitude) {
        return createActivityAtLocation(title, category, latitude, longitude, activityDateTomorrow());
    }

    // ==================== Profile Update Requests ====================

    /** Available interests (15 total). */
    public static final String[] AVAILABLE_INTERESTS = {
        "Sports", "Music", "Art", "Technology", "Gaming",
        "Travel", "Food", "Fitness", "Photography", "Reading",
        "Movies", "Dancing", "Cooking", "Hiking", "Yoga"
    };

    /** Create a basic profile update request. */
    public static UserProfileUpdateRequest createProfileUpdateRequest() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName(uniqueFullName("Updated"));
        request.setBio("This is my updated bio for testing purposes.");
        request.setInterests(Arrays.asList("Sports", "Music", "Technology"));
        request.setCity("New York");
        return request;
    }

    /** Create a profile update request with specific interests. */
    public static UserProfileUpdateRequest createProfileUpdateRequest(List<String> interests) {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName(uniqueFullName("Updated"));
        request.setBio("Updated bio with custom interests.");
        request.setInterests(interests);
        return request;
    }

    /** Create a profile update request with bio only. */
    public static UserProfileUpdateRequest createBioUpdateRequest(String bio) {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setBio(bio);
        return request;
    }

    /** Create a profile update request with location. */
    public static UserProfileUpdateRequest createLocationUpdateRequest(
            String city, Double latitude, Double longitude) {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setCity(city);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        return request;
    }

    /**
     * Get random interests (1 to count).
     *
     * @param count Number of interests to return (max 15)
     * @return List of random interests
     */
    public static List<String> randomInterests(int count) {
        List<String> all = new ArrayList<>(Arrays.asList(AVAILABLE_INTERESTS));
        Collections.shuffle(all);
        return all.subList(0, Math.min(count, all.size()));
    }

    // ==================== Report Reasons ====================

    public static final String REPORT_REASON_SPAM = "This content appears to be spam.";
    public static final String REPORT_REASON_INAPPROPRIATE =
            "This content is inappropriate or offensive.";
    public static final String REPORT_REASON_HARASSMENT = "This user is harassing other users.";
    public static final String REPORT_REASON_FAKE = "This appears to be a fake account or activity.";
    public static final String REPORT_REASON_SCAM = "This looks like a scam or fraudulent activity.";

    /** Create a unique report reason. */
    public static String uniqueReportReason() {
        return "Test report reason " + UUID.randomUUID().toString().substring(0, 8);
    }

    // ==================== Activity with Cover Image ====================

    /** Create an activity with cover image URL. */
    public static ActivityCreateRequest createActivityWithCover(String coverImageUrl) {
        ActivityCreateRequest request = createBasicActivity();
        request.setCoverImageUrl(coverImageUrl);
        return request;
    }

    /** Create an activity with custom title, category and cover. */
    public static ActivityCreateRequest createActivityWithCover(
            String title, String category, String coverImageUrl) {
        ActivityCreateRequest request = createActivity(title, category, 5);
        request.setCoverImageUrl(coverImageUrl);
        return request;
    }

    // ==================== Message Content ====================

    /** Create unique message content. */
    public static String uniqueMessageContent() {
        return "Test message " + UUID.randomUUID().toString().substring(0, 8);
    }
}
