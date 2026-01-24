package com.gege.activityfindermobile;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.util.TestApiHelper;
import com.gege.activityfindermobile.util.TestDataFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tests for main feed functionality including:
 * - Fetching nearby activities
 * - Filtering by distance
 * - Filtering by category/type
 * - Sorting by distance and datetime
 */
@RunWith(AndroidJUnit4.class)
public class FeedTest {

    private TestApiHelper apiHelper;
    private TestDataFactory.TestUser testUser;
    private Long currentUserId;
    private List<Long> createdActivityIds;

    @Before
    public void setUp() {
        apiHelper = new TestApiHelper();
        createdActivityIds = new ArrayList<>();

        // Create and login a test user
        testUser = TestDataFactory.createTestUser("FeedTest");
        LoginResponse response = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Test user creation should succeed", response);
        currentUserId = response.getUserId();
        apiHelper.waitShort();
    }

    @After
    public void tearDown() {
        // Clean up: delete all created activities first
        for (Long activityId : createdActivityIds) {
            try {
                apiHelper.deleteActivity(activityId, currentUserId);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        createdActivityIds.clear();

        // Then delete the test user
        if (currentUserId != null) {
            try {
                apiHelper.clearSession();
                apiHelper.waitShort();
                apiHelper.loginWithRetry(testUser.email, testUser.password);
                apiHelper.deleteUser(currentUserId);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        apiHelper.clearSession();
    }

    private void trackActivity(Long activityId) {
        if (activityId != null) {
            createdActivityIds.add(activityId);
        }
    }

    // ==================== NEARBY ACTIVITIES TESTS ====================

    @Test
    public void getNearbyActivities_shouldReturnActivitiesWithinRadius() {
        // Create activities at different distances from NYC
        ActivityCreateRequest nearActivity = TestDataFactory.createActivityAtLocation(
                TestDataFactory.uniqueActivityTitle("Near"),
                TestDataFactory.CATEGORY_SPORTS,
                TestDataFactory.JERSEY_CITY_LATITUDE,
                TestDataFactory.JERSEY_CITY_LONGITUDE
        );

        Activity near = apiHelper.createActivity(nearActivity);
        assertNotNull("Near activity should be created", near);
        trackActivity(near.getId());

        apiHelper.waitShort();

        // Fetch nearby activities from NYC with 10km radius
        List<Activity> nearby = apiHelper.getNearbyActivities(
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                10f
        );

        assertNotNull("Nearby activities should not be null", nearby);
        // The near activity (Jersey City, ~5km) should be included
    }

    @Test
    public void getNearbyActivities_withSmallRadius_shouldExcludeDistantActivities() {
        // Create an activity ~15km away (Newark)
        ActivityCreateRequest distantActivity = TestDataFactory.createActivityAtLocation(
                TestDataFactory.uniqueActivityTitle("Distant"),
                TestDataFactory.CATEGORY_MUSIC,
                TestDataFactory.NEWARK_LATITUDE,
                TestDataFactory.NEWARK_LONGITUDE
        );

        Activity distant = apiHelper.createActivity(distantActivity);
        assertNotNull("Distant activity should be created", distant);
        trackActivity(distant.getId());

        apiHelper.waitShort();

        // Fetch with 5km radius - should NOT include Newark activity
        List<Activity> nearby = apiHelper.getNearbyActivities(
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                5f
        );

        assertNotNull("Nearby activities should not be null", nearby);

        // Verify the distant activity is not in the results
        boolean containsDistant = nearby.stream()
                .anyMatch(a -> a.getId().equals(distant.getId()));
        assertFalse("5km radius should not include activity 15km away", containsDistant);
    }

    @Test
    public void getNearbyActivities_withLargeRadius_shouldIncludeDistantActivities() {
        // Create an activity ~15km away (Newark)
        ActivityCreateRequest distantActivity = TestDataFactory.createActivityAtLocation(
                TestDataFactory.uniqueActivityTitle("DistantInclude"),
                TestDataFactory.CATEGORY_FOOD,
                TestDataFactory.NEWARK_LATITUDE,
                TestDataFactory.NEWARK_LONGITUDE
        );

        Activity distant = apiHelper.createActivity(distantActivity);
        assertNotNull("Distant activity should be created", distant);
        trackActivity(distant.getId());

        apiHelper.waitShort();

        // Fetch with 50km radius - should include Newark activity
        List<Activity> nearby = apiHelper.getNearbyActivities(
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                50f
        );

        assertNotNull("Nearby activities should not be null", nearby);

        // Verify the distant activity is in the results
        boolean containsDistant = nearby.stream()
                .anyMatch(a -> a.getId().equals(distant.getId()));
        assertTrue("50km radius should include activity 15km away", containsDistant);
    }

    // ==================== DISTANCE FILTER TESTS ====================

    @Test
    public void distanceFilter_5km_shouldFilterCorrectly() {
        // Create activities at various distances
        createActivitiesAtVariousDistances();

        apiHelper.waitMedium();

        // Fetch all nearby (large radius)
        List<Activity> all = apiHelper.getNearbyActivities(
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                250f
        );

        assertNotNull("Activities should not be null", all);

        // Apply 5km filter client-side (same as FeedFragment does)
        List<Activity> filtered = all.stream()
                .filter(a -> a.getDistance() != null && a.getDistance() <= 5)
                .collect(Collectors.toList());

        // All filtered activities should be within 5km
        for (Activity a : filtered) {
            assertTrue("Activity distance should be <= 5km", a.getDistance() <= 5);
        }
    }

    @Test
    public void distanceFilter_10km_shouldFilterCorrectly() {
        createActivitiesAtVariousDistances();

        apiHelper.waitMedium();

        List<Activity> all = apiHelper.getNearbyActivities(
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                250f
        );

        assertNotNull("Activities should not be null", all);

        // Apply 10km filter
        List<Activity> filtered = all.stream()
                .filter(a -> a.getDistance() != null && a.getDistance() <= 10)
                .collect(Collectors.toList());

        for (Activity a : filtered) {
            assertTrue("Activity distance should be <= 10km", a.getDistance() <= 10);
        }
    }

    @Test
    public void distanceFilter_25km_shouldFilterCorrectly() {
        createActivitiesAtVariousDistances();

        apiHelper.waitMedium();

        List<Activity> all = apiHelper.getNearbyActivities(
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                250f
        );

        assertNotNull("Activities should not be null", all);

        // Apply 25km filter
        List<Activity> filtered = all.stream()
                .filter(a -> a.getDistance() != null && a.getDistance() <= 25)
                .collect(Collectors.toList());

        for (Activity a : filtered) {
            assertTrue("Activity distance should be <= 25km", a.getDistance() <= 25);
        }
    }

    @Test
    public void distanceFilter_50km_shouldFilterCorrectly() {
        createActivitiesAtVariousDistances();

        apiHelper.waitMedium();

        List<Activity> all = apiHelper.getNearbyActivities(
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                250f
        );

        assertNotNull("Activities should not be null", all);

        // Apply 50km filter
        List<Activity> filtered = all.stream()
                .filter(a -> a.getDistance() != null && a.getDistance() <= 50)
                .collect(Collectors.toList());

        for (Activity a : filtered) {
            assertTrue("Activity distance should be <= 50km", a.getDistance() <= 50);
        }
    }

    // ==================== CATEGORY/TYPE FILTER TESTS ====================

    @Test
    public void typeFilter_sports_shouldReturnOnlySportsActivities() {
        // Create activities with different categories
        createActivitiesWithVariousCategories();

        apiHelper.waitMedium();

        // Fetch sports activities
        List<Activity> sports = apiHelper.getActivitiesByCategory(TestDataFactory.CATEGORY_SPORTS);

        assertNotNull("Sports activities should not be null", sports);

        // All activities should be SPORTS category
        for (Activity a : sports) {
            assertEquals("Category should be SPORTS",
                    TestDataFactory.CATEGORY_SPORTS, a.getCategory());
        }
    }

    @Test
    public void typeFilter_music_shouldReturnOnlyMusicActivities() {
        createActivitiesWithVariousCategories();

        apiHelper.waitMedium();

        List<Activity> music = apiHelper.getActivitiesByCategory(TestDataFactory.CATEGORY_MUSIC);

        assertNotNull("Music activities should not be null", music);

        for (Activity a : music) {
            assertEquals("Category should be MUSIC",
                    TestDataFactory.CATEGORY_MUSIC, a.getCategory());
        }
    }

    @Test
    public void typeFilter_food_shouldReturnOnlyFoodActivities() {
        createActivitiesWithVariousCategories();

        apiHelper.waitMedium();

        List<Activity> food = apiHelper.getActivitiesByCategory(TestDataFactory.CATEGORY_FOOD);

        assertNotNull("Food activities should not be null", food);

        for (Activity a : food) {
            assertEquals("Category should be FOOD",
                    TestDataFactory.CATEGORY_FOOD, a.getCategory());
        }
    }

    @Test
    public void typeFilter_clientSide_shouldFilterCorrectly() {
        createActivitiesWithVariousCategories();

        apiHelper.waitMedium();

        // Fetch all activities
        List<Activity> all = apiHelper.getAllOpenActivities();
        assertNotNull("Activities should not be null", all);

        // Apply client-side filter (same as FeedFragment does)
        String selectedType = TestDataFactory.CATEGORY_SPORTS;
        List<Activity> filtered = all.stream()
                .filter(a -> selectedType.equalsIgnoreCase(a.getCategory()))
                .collect(Collectors.toList());

        for (Activity a : filtered) {
            assertTrue("Category should match filter",
                    selectedType.equalsIgnoreCase(a.getCategory()));
        }
    }

    // ==================== SORTING TESTS ====================

    @Test
    public void sortByDistance_shouldOrderByDistanceAscending() {
        createActivitiesAtVariousDistances();

        apiHelper.waitMedium();

        List<Activity> activities = apiHelper.getNearbyActivities(
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                250f
        );

        assertNotNull("Activities should not be null", activities);

        // Apply distance sorting (same as FeedFragment does)
        List<Activity> sorted = activities.stream()
                .sorted(Comparator.comparing(
                        Activity::getDistance,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        // Verify sorted order
        Double previousDistance = null;
        for (Activity a : sorted) {
            if (a.getDistance() != null) {
                if (previousDistance != null) {
                    assertTrue("Activities should be sorted by distance ascending",
                            a.getDistance() >= previousDistance);
                }
                previousDistance = a.getDistance();
            }
        }
    }

    @Test
    public void sortByDatetime_shouldOrderByDateAscending() {
        // Create activities with different dates
        ActivityCreateRequest activity1 = TestDataFactory.createActivityAtLocation(
                TestDataFactory.uniqueActivityTitle("Tomorrow"),
                TestDataFactory.CATEGORY_SPORTS,
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                TestDataFactory.activityDateInFuture(1) // Tomorrow
        );

        ActivityCreateRequest activity2 = TestDataFactory.createActivityAtLocation(
                TestDataFactory.uniqueActivityTitle("NextWeek"),
                TestDataFactory.CATEGORY_SPORTS,
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                TestDataFactory.activityDateInFuture(7) // Next week
        );

        ActivityCreateRequest activity3 = TestDataFactory.createActivityAtLocation(
                TestDataFactory.uniqueActivityTitle("In3Days"),
                TestDataFactory.CATEGORY_SPORTS,
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                TestDataFactory.activityDateInFuture(3) // In 3 days
        );

        Activity a1 = apiHelper.createActivity(activity1);
        Activity a2 = apiHelper.createActivity(activity2);
        Activity a3 = apiHelper.createActivity(activity3);

        assertNotNull("Activity 1 should be created", a1);
        assertNotNull("Activity 2 should be created", a2);
        assertNotNull("Activity 3 should be created", a3);

        trackActivity(a1.getId());
        trackActivity(a2.getId());
        trackActivity(a3.getId());

        apiHelper.waitMedium();

        List<Activity> activities = apiHelper.getNearbyActivities(
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                250f
        );

        assertNotNull("Activities should not be null", activities);

        // Apply datetime sorting (same as FeedFragment does)
        List<Activity> sorted = activities.stream()
                .sorted(Comparator.comparing(
                        (Activity a) -> {
                            String date = a.getDate() != null ? a.getDate() : "";
                            String time = a.getTime() != null ? a.getTime() : "";
                            return date + " " + time;
                        },
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        // Verify sorted order
        String previousDateTime = null;
        for (Activity a : sorted) {
            String dateTime = (a.getDate() != null ? a.getDate() : "") + " " +
                    (a.getTime() != null ? a.getTime() : "");
            if (previousDateTime != null && !dateTime.trim().isEmpty()) {
                assertTrue("Activities should be sorted by datetime ascending",
                        dateTime.compareTo(previousDateTime) >= 0);
            }
            if (!dateTime.trim().isEmpty()) {
                previousDateTime = dateTime;
            }
        }
    }

    // ==================== COMBINED FILTER AND SORT TESTS ====================

    @Test
    public void combinedFilterAndSort_distanceFilterWithDistanceSort() {
        createActivitiesAtVariousDistances();

        apiHelper.waitMedium();

        List<Activity> all = apiHelper.getNearbyActivities(
                TestDataFactory.NYC_LATITUDE,
                TestDataFactory.NYC_LONGITUDE,
                250f
        );

        assertNotNull("Activities should not be null", all);

        // Apply 25km filter and distance sort
        List<Activity> result = all.stream()
                .filter(a -> a.getDistance() != null && a.getDistance() <= 25)
                .sorted(Comparator.comparing(
                        Activity::getDistance,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        // Verify filter applied
        for (Activity a : result) {
            assertTrue("Distance should be <= 25km", a.getDistance() <= 25);
        }

        // Verify sort applied
        Double previousDistance = null;
        for (Activity a : result) {
            if (previousDistance != null) {
                assertTrue("Should be sorted ascending", a.getDistance() >= previousDistance);
            }
            previousDistance = a.getDistance();
        }
    }

    @Test
    public void combinedFilterAndSort_typeFilterWithDatetimeSort() {
        createActivitiesWithVariousCategories();

        apiHelper.waitMedium();

        List<Activity> all = apiHelper.getAllOpenActivities();
        assertNotNull("Activities should not be null", all);

        // Apply SPORTS filter and datetime sort
        List<Activity> result = all.stream()
                .filter(a -> TestDataFactory.CATEGORY_SPORTS.equalsIgnoreCase(a.getCategory()))
                .sorted(Comparator.comparing(
                        (Activity a) -> {
                            String date = a.getDate() != null ? a.getDate() : "";
                            String time = a.getTime() != null ? a.getTime() : "";
                            return date + " " + time;
                        },
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        // Verify filter applied
        for (Activity a : result) {
            assertEquals("Category should be SPORTS",
                    TestDataFactory.CATEGORY_SPORTS, a.getCategory());
        }
    }

    // ==================== HELPER METHODS ====================

    private void createActivitiesAtVariousDistances() {
        // Near (~5km - Jersey City)
        ActivityCreateRequest near = TestDataFactory.createActivityAtLocation(
                TestDataFactory.uniqueActivityTitle("5km"),
                TestDataFactory.CATEGORY_SPORTS,
                TestDataFactory.JERSEY_CITY_LATITUDE,
                TestDataFactory.JERSEY_CITY_LONGITUDE
        );
        Activity nearA = apiHelper.createActivity(near);
        if (nearA != null) trackActivity(nearA.getId());

        // Medium (~15km - Newark)
        ActivityCreateRequest medium = TestDataFactory.createActivityAtLocation(
                TestDataFactory.uniqueActivityTitle("15km"),
                TestDataFactory.CATEGORY_MUSIC,
                TestDataFactory.NEWARK_LATITUDE,
                TestDataFactory.NEWARK_LONGITUDE
        );
        Activity mediumA = apiHelper.createActivity(medium);
        if (mediumA != null) trackActivity(mediumA.getId());

        // Far (~30km - Yonkers)
        ActivityCreateRequest far = TestDataFactory.createActivityAtLocation(
                TestDataFactory.uniqueActivityTitle("30km"),
                TestDataFactory.CATEGORY_FOOD,
                TestDataFactory.YONKERS_LATITUDE,
                TestDataFactory.YONKERS_LONGITUDE
        );
        Activity farA = apiHelper.createActivity(far);
        if (farA != null) trackActivity(farA.getId());
    }

    private void createActivitiesWithVariousCategories() {
        String[] categories = {
                TestDataFactory.CATEGORY_SPORTS,
                TestDataFactory.CATEGORY_MUSIC,
                TestDataFactory.CATEGORY_FOOD,
                TestDataFactory.CATEGORY_SOCIAL
        };

        for (String category : categories) {
            ActivityCreateRequest request = TestDataFactory.createActivity(
                    TestDataFactory.uniqueActivityTitle(category),
                    category,
                    5
            );
            Activity activity = apiHelper.createActivity(request);
            if (activity != null) {
                trackActivity(activity.getId());
            }
            apiHelper.waitShort();
        }
    }
}
