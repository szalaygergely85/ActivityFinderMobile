package com.gege.activityfindermobile;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.util.DeviceLocationHelper;
import com.gege.activityfindermobile.util.TestApiHelper;
import com.gege.activityfindermobile.util.TestDataFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Example test class demonstrating how to use TestApiHelper and TestDataFactory.
 *
 * These tests run against the real backend (make sure it's running on localhost:8080).
 */
@RunWith(AndroidJUnit4.class)
public class ExampleApiTest {

    private TestApiHelper apiHelper;
    private TestDataFactory.TestUser testUser;
    private Long createdUserId;
    private Long createdActivityId;

    @Before
    public void setUp() {
        apiHelper = new TestApiHelper();
        testUser = TestDataFactory.createTestUser("Example");

        // Get device location and set it in TestDataFactory
        DeviceLocationHelper locationHelper = new DeviceLocationHelper();
        locationHelper.acquireLocationAndSetForTests();
    }

    @After
    public void tearDown() {
        // Re-login to ensure we have auth token for cleanup
        if (createdUserId != null && apiHelper.getCurrentAccessToken() == null) {
            apiHelper.loginWithRetry(testUser.email, testUser.password);
        }

        // Clean up: delete created activity first, then user
        if (createdActivityId != null && createdUserId != null) {
            apiHelper.deleteActivity(createdActivityId, createdUserId);
        }
        if (createdUserId != null) {
            apiHelper.deleteUser(createdUserId);
        }
        apiHelper.clearSession();
    }

    @Test
    public void testUserRegistrationAndLogin() {
        // Create user
        LoginResponse registerResponse = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Registration should succeed", registerResponse);
        assertNotNull("Should have user ID", registerResponse.getUserId());
        assertNotNull("Should have access token", registerResponse.getAccessToken());
        assertEquals("Email should match", testUser.email, registerResponse.getEmail());

        createdUserId = registerResponse.getUserId();

        // Clear session and login again
        apiHelper.clearSession();
        apiHelper.waitShort(); // Allow backend to fully process registration

        LoginResponse loginResponse = apiHelper.loginWithRetry(testUser.email, testUser.password);

        assertNotNull("Login should succeed", loginResponse);
        assertEquals("User ID should match", createdUserId, loginResponse.getUserId());
    }

    @Test
    public void testCreateAndDeleteActivity() {
        // First create and login user
        LoginResponse registerResponse = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Registration should succeed", registerResponse);
        createdUserId = registerResponse.getUserId();

        // Create activity
        ActivityCreateRequest activityRequest = TestDataFactory.createBasicActivity();
        Activity activity = apiHelper.createActivity(activityRequest);

        assertNotNull("Activity should be created", activity);
        assertNotNull("Activity should have ID", activity.getId());
        assertEquals("Title should match", activityRequest.getTitle(), activity.getTitle());

        createdActivityId = activity.getId();

        // Verify activity can be retrieved
        Activity fetchedActivity = apiHelper.getActivity(createdActivityId);
        assertNotNull("Should fetch activity", fetchedActivity);
        assertEquals("IDs should match", createdActivityId, fetchedActivity.getId());

        // Delete activity
        boolean deleted = apiHelper.deleteActivity(createdActivityId);
        assertTrue("Activity should be deleted", deleted);
        createdActivityId = null; // Already deleted, don't try again in tearDown
    }

    @Test
    public void testCreateActivityWithCustomData() {
        // Create user
        LoginResponse registerResponse = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull(registerResponse);
        createdUserId = registerResponse.getUserId();

        // Create activity with custom parameters
        String customTitle = TestDataFactory.uniqueActivityTitle("Custom Event");
        ActivityCreateRequest request = TestDataFactory.createActivity(
                customTitle,
                TestDataFactory.CATEGORY_MUSIC,
                10
        );

        Activity activity = apiHelper.createActivity(request);

        assertNotNull("Activity should be created", activity);
        assertEquals("Title should match", customTitle, activity.getTitle());
        assertEquals("Category should match", TestDataFactory.CATEGORY_MUSIC, activity.getCategory());
        assertEquals("Total spots should match", Integer.valueOf(10), activity.getTotalSpots());

        createdActivityId = activity.getId();
    }
}
