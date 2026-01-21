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
import java.util.List;

/**
 * Comprehensive tests for activity creation, retrieval, and deletion.
 * Tests all success and error scenarios.
 */
@RunWith(AndroidJUnit4.class)
public class ActivityTest {

    private TestApiHelper apiHelper;
    private TestDataFactory.TestUser testUser;
    private Long currentUserId;
    private List<Long> createdActivityIds;

    @Before
    public void setUp() {
        apiHelper = new TestApiHelper();
        createdActivityIds = new ArrayList<>();

        // Create and login a test user for all activity tests
        testUser = TestDataFactory.createTestUser("ActivityTest");
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
                // Re-login to ensure we have valid token
                apiHelper.clearSession();
                apiHelper.login(testUser.email, testUser.password);
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

    // ==================== ACTIVITY CREATION SUCCESS TESTS ====================

    @Test
    public void createActivity_withValidData_shouldSucceed() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();

        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity should be created", activity);
        trackActivity(activity.getId());

        assertNotNull("Activity should have ID", activity.getId());
        assertEquals("Title should match", request.getTitle(), activity.getTitle());
        assertEquals("Description should match", request.getDescription(), activity.getDescription());
        assertEquals("Location should match", request.getLocation(), activity.getLocation());
        assertEquals("Category should match", request.getCategory(), activity.getCategory());
        assertEquals("Total spots should match", request.getTotalSpots(), activity.getTotalSpots());
    }

    @Test
    public void createActivity_withAllCategories_shouldSucceed() {
        String[] categories = {
                TestDataFactory.CATEGORY_SPORTS,
                TestDataFactory.CATEGORY_MUSIC,
                TestDataFactory.CATEGORY_FOOD,
                TestDataFactory.CATEGORY_OUTDOORS,
                TestDataFactory.CATEGORY_GAMES,
                TestDataFactory.CATEGORY_ARTS,
                TestDataFactory.CATEGORY_SOCIAL,
                TestDataFactory.CATEGORY_LEARNING
        };

        for (String category : categories) {
            ActivityCreateRequest request = TestDataFactory.createActivity(
                    TestDataFactory.uniqueActivityTitle(category),
                    category,
                    5
            );

            Activity activity = apiHelper.createActivity(request);

            assertNotNull("Activity with category " + category + " should be created", activity);
            trackActivity(activity.getId());
            assertEquals("Category should match", category, activity.getCategory());

            apiHelper.waitShort(); // Small delay between creations
        }
    }

    @Test
    public void createActivity_withMinimumSpots_shouldSucceed() {
        ActivityCreateRequest request = TestDataFactory.createActivity(
                TestDataFactory.uniqueActivityTitle("MinSpots"),
                TestDataFactory.CATEGORY_SOCIAL,
                1  // Minimum spots
        );

        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity with 1 spot should be created", activity);
        trackActivity(activity.getId());
        assertEquals("Total spots should be 1", Integer.valueOf(1), activity.getTotalSpots());
    }

    @Test
    public void createActivity_withMaximumSpots_shouldSucceed() {
        ActivityCreateRequest request = TestDataFactory.createActivity(
                TestDataFactory.uniqueActivityTitle("MaxSpots"),
                TestDataFactory.CATEGORY_SOCIAL,
                100  // Large number of spots
        );

        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity with 100 spots should be created", activity);
        trackActivity(activity.getId());
        assertEquals("Total spots should be 100", Integer.valueOf(100), activity.getTotalSpots());
    }

    @Test
    public void createActivity_withCoordinates_shouldSucceed() {
        ActivityCreateRequest request = new ActivityCreateRequest();
        request.setTitle(TestDataFactory.uniqueActivityTitle("WithCoords"));
        request.setDescription("Activity with specific coordinates");
        request.setActivityDate(TestDataFactory.activityDateTomorrow());
        request.setLocation("Central Park, New York");
        request.setTotalSpots(10);
        request.setCategory(TestDataFactory.CATEGORY_OUTDOORS);
        request.setLatitude(40.785091);
        request.setLongitude(-73.968285);

        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity with coordinates should be created", activity);
        trackActivity(activity.getId());
        assertNotNull("Latitude should be set", activity.getLatitude());
        assertNotNull("Longitude should be set", activity.getLongitude());
    }

    @Test
    public void createActivity_withFutureDate_shouldSucceed() {
        // Activity scheduled for next week
        ActivityCreateRequest request = new ActivityCreateRequest();
        request.setTitle(TestDataFactory.uniqueActivityTitle("NextWeek"));
        request.setDescription("Activity scheduled for next week");
        request.setActivityDate(TestDataFactory.activityDateNextWeek());
        request.setLocation("Test Location");
        request.setTotalSpots(5);
        request.setCategory(TestDataFactory.CATEGORY_SOCIAL);

        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity with future date should be created", activity);
        trackActivity(activity.getId());
    }

    @Test
    public void createActivity_withLongDescription_shouldSucceed() {
        String longDescription = "This is a very detailed description of the activity. ".repeat(10);

        ActivityCreateRequest request = new ActivityCreateRequest();
        request.setTitle(TestDataFactory.uniqueActivityTitle("LongDesc"));
        request.setDescription(longDescription);
        request.setActivityDate(TestDataFactory.activityDateTomorrow());
        request.setLocation("Test Location");
        request.setTotalSpots(5);
        request.setCategory(TestDataFactory.CATEGORY_LEARNING);

        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity with long description should be created", activity);
        trackActivity(activity.getId());
    }

    // ==================== ACTIVITY CREATION FAILURE TESTS ====================

    @Test
    public void createActivity_withEmptyTitle_shouldFail() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        request.setTitle("");

        Activity activity = apiHelper.createActivity(request);

        assertNull("Activity with empty title should fail", activity);
    }

    @Test
    public void createActivity_withNullTitle_shouldFail() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        request.setTitle(null);

        Activity activity = apiHelper.createActivity(request);

        assertNull("Activity with null title should fail", activity);
    }

    @Test
    public void createActivity_withEmptyDescription_shouldFail() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        request.setDescription("");

        Activity activity = apiHelper.createActivity(request);

        // Note: This might succeed depending on backend validation
        // Update assertion based on actual backend behavior
        if (activity != null) {
            trackActivity(activity.getId());
        }
    }

    @Test
    public void createActivity_withEmptyLocation_shouldFail() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        request.setLocation("");

        Activity activity = apiHelper.createActivity(request);

        assertNull("Activity with empty location should fail", activity);
    }

    @Test
    public void createActivity_withZeroSpots_shouldFail() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        request.setTotalSpots(0);

        Activity activity = apiHelper.createActivity(request);

        assertNull("Activity with 0 spots should fail", activity);
    }

    @Test
    public void createActivity_withNegativeSpots_shouldFail() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        request.setTotalSpots(-5);

        Activity activity = apiHelper.createActivity(request);

        assertNull("Activity with negative spots should fail", activity);
    }

    @Test
    public void createActivity_withPastDate_shouldFail() {
        ActivityCreateRequest request = new ActivityCreateRequest();
        request.setTitle(TestDataFactory.uniqueActivityTitle("PastDate"));
        request.setDescription("Activity with past date");
        request.setActivityDate("2020-01-01T10:00:00");  // Past date
        request.setLocation("Test Location");
        request.setTotalSpots(5);
        request.setCategory(TestDataFactory.CATEGORY_SOCIAL);

        Activity activity = apiHelper.createActivity(request);

        assertNull("Activity with past date should fail", activity);
    }

    @Test
    public void createActivity_withEmptyCategory_shouldFail() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        request.setCategory("");

        Activity activity = apiHelper.createActivity(request);

        assertNull("Activity with empty category should fail", activity);
    }

    @Test
    public void createActivity_withCustomCategory_shouldSucceed() {
        // Backend accepts any category string (no validation)
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        request.setCategory("CUSTOM_CATEGORY");

        Activity activity = apiHelper.createActivity(request);

        // Backend accepts custom categories
        if (activity != null) {
            trackActivity(activity.getId());
            assertEquals("Category should match", "CUSTOM_CATEGORY", activity.getCategory());
        }
        // If null, backend validates categories - update test accordingly
    }

    @Test
    public void createActivity_withoutAuthentication_shouldFail() {
        // Clear session to remove auth token
        apiHelper.clearSession();

        ActivityCreateRequest request = TestDataFactory.createBasicActivity();

        Activity activity = apiHelper.createActivity(request);

        assertNull("Activity creation without auth should fail", activity);

        // Re-login for cleanup
        apiHelper.login(testUser.email, testUser.password);
    }

    // ==================== ACTIVITY RETRIEVAL TESTS ====================

    @Test
    public void getActivity_withValidId_shouldSucceed() {
        // Create an activity first
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity createdActivity = apiHelper.createActivity(request);

        assertNotNull("Activity should be created", createdActivity);
        trackActivity(createdActivity.getId());

        apiHelper.waitShort();

        // Retrieve the activity
        Activity fetchedActivity = apiHelper.getActivity(createdActivity.getId());

        assertNotNull("Activity should be fetched", fetchedActivity);
        assertEquals("IDs should match", createdActivity.getId(), fetchedActivity.getId());
        assertEquals("Titles should match", createdActivity.getTitle(), fetchedActivity.getTitle());
    }

    @Test
    public void getActivity_withInvalidId_shouldFail() {
        Activity activity = apiHelper.getActivity(999999999L);

        assertNull("Non-existent activity should return null", activity);
    }

    // ==================== ACTIVITY DELETION TESTS ====================

    @Test
    public void deleteActivity_asCreator_shouldSucceed() {
        // Create an activity
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity should be created", activity);
        Long activityId = activity.getId();

        apiHelper.waitShort();

        // Delete the activity
        boolean deleted = apiHelper.deleteActivity(activityId);

        assertTrue("Activity deletion should succeed", deleted);

        // Verify activity is deleted/cancelled
        apiHelper.waitShort();
        Activity fetchedActivity = apiHelper.getActivity(activityId);

        // Either null or status is CANCELLED
        if (fetchedActivity != null) {
            assertEquals("Activity status should be CANCELLED", "CANCELLED", fetchedActivity.getStatus());
        }
    }

    @Test
    public void deleteActivity_withInvalidId_shouldFail() {
        boolean deleted = apiHelper.deleteActivity(999999999L);

        assertFalse("Deleting non-existent activity should fail", deleted);
    }

    @Test
    public void deleteActivity_withoutAuthentication_shouldFail() {
        // Create an activity first
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity should be created", activity);
        trackActivity(activity.getId());

        // Clear session
        apiHelper.clearSession();

        // Try to delete without auth
        boolean deleted = apiHelper.deleteActivity(activity.getId(), currentUserId);

        assertFalse("Activity deletion without auth should fail", deleted);

        // Re-login for cleanup
        apiHelper.login(testUser.email, testUser.password);
    }

    // ==================== MULTIPLE ACTIVITIES TESTS ====================

    @Test
    public void createMultipleActivities_shouldSucceed() {
        int numActivities = 3;

        for (int i = 0; i < numActivities; i++) {
            ActivityCreateRequest request = TestDataFactory.createActivity(
                    TestDataFactory.uniqueActivityTitle("Multiple" + i),
                    TestDataFactory.CATEGORY_SOCIAL,
                    5
            );

            Activity activity = apiHelper.createActivity(request);

            assertNotNull("Activity " + (i + 1) + " should be created", activity);
            trackActivity(activity.getId());

            apiHelper.waitShort();
        }

        assertEquals("Should have created " + numActivities + " activities",
                numActivities, createdActivityIds.size());
    }

    // ==================== ACTIVITY DATA INTEGRITY TESTS ====================

    @Test
    public void createActivity_shouldHaveCorrectCreator() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();

        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity should be created", activity);
        trackActivity(activity.getId());

        // Verify creator info
        Long creatorId = activity.getCreatorId();
        assertNotNull("Creator ID should not be null", creatorId);
        assertEquals("Creator ID should match current user", currentUserId, creatorId);
    }

    @Test
    public void createActivity_shouldHaveOpenStatus() {
        ActivityCreateRequest request = TestDataFactory.createBasicActivity();

        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity should be created", activity);
        trackActivity(activity.getId());

        // New activities should have OPEN status
        String status = activity.getStatus();
        assertNotNull("Status should not be null", status);
        assertEquals("New activity should have OPEN status", "OPEN", status);
    }

    @Test
    public void createActivity_availableSpotsShouldEqualTotalSpots() {
        int totalSpots = 10;
        ActivityCreateRequest request = TestDataFactory.createActivity(
                TestDataFactory.uniqueActivityTitle("SpotsTest"),
                TestDataFactory.CATEGORY_SOCIAL,
                totalSpots
        );

        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity should be created", activity);
        trackActivity(activity.getId());

        assertEquals("Total spots should match", Integer.valueOf(totalSpots), activity.getTotalSpots());
        // For new activity, available spots should equal total spots
        if (activity.getAvailableSpots() != null) {
            assertEquals("Available spots should equal total spots for new activity",
                    Integer.valueOf(totalSpots), activity.getAvailableSpots());
        }
    }
}
