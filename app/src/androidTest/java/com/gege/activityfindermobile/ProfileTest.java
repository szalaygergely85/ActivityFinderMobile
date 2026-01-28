package com.gege.activityfindermobile;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.dto.UserProfileUpdateRequest;
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.util.TestApiHelper;
import com.gege.activityfindermobile.util.TestDataFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

/**
 * Comprehensive tests for user profile operations. Tests profile viewing, updating, and photo
 * management.
 */
@RunWith(AndroidJUnit4.class)
public class ProfileTest {

    private TestApiHelper apiHelper;
    private TestDataFactory.TestUser testUser;
    private Long currentUserId;

    @Before
    public void setUp() {
        apiHelper = new TestApiHelper();
        testUser = TestDataFactory.createTestUser("ProfileTest");
        LoginResponse response =
                apiHelper.createUser(
                        testUser.fullName, testUser.email, testUser.password, testUser.birthDate);
        assertNotNull("Test user creation should succeed", response);
        currentUserId = response.getUserId();
        apiHelper.waitShort();
    }

    @After
    public void tearDown() {
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

    // ==================== GET PROFILE TESTS ====================

    @Test
    public void getUserById_withValidId_shouldReturnProfile() {
        User user = apiHelper.getUserById(currentUserId);

        assertNotNull("User should be fetched", user);
        assertEquals("User ID should match", currentUserId, user.getId());
        assertEquals("Full name should match", testUser.fullName, user.getFullName());
        assertEquals("Email should match", testUser.email, user.getEmail());
    }

    @Test
    public void getUserById_withInvalidId_shouldReturnNull() {
        User user = apiHelper.getUserById(999999999L);

        assertNull("Non-existent user should return null", user);
    }

    @Test
    public void getCurrentUserProfile_shouldReturnLoggedInUser() {
        User user = apiHelper.getCurrentUserProfile();

        assertNotNull("Current user profile should be fetched", user);
        assertEquals("User ID should match current user", currentUserId, user.getId());
    }

    // ==================== UPDATE PROFILE TESTS ====================

    @Test
    public void updateProfile_withValidBio_shouldSucceed() {
        String newBio = "This is my updated test bio " + System.currentTimeMillis();
        UserProfileUpdateRequest request = TestDataFactory.createBioUpdateRequest(newBio);

        User updatedUser = apiHelper.updateUserProfile(request);

        assertNotNull("Profile update should succeed", updatedUser);
        assertEquals("Bio should be updated", newBio, updatedUser.getBio());
    }

    @Test
    public void updateProfile_withFullName_shouldSucceed() {
        String newName = TestDataFactory.uniqueFullName("Renamed");
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName(newName);

        User updatedUser = apiHelper.updateUserProfile(request);

        assertNotNull("Profile update should succeed", updatedUser);
        assertEquals("Name should be updated", newName, updatedUser.getFullName());
    }

    @Test
    public void updateProfile_withInterests_shouldSucceed() {
        List<String> interests = Arrays.asList("Sports", "Music", "Technology");
        UserProfileUpdateRequest request = TestDataFactory.createProfileUpdateRequest(interests);

        User updatedUser = apiHelper.updateUserProfile(request);

        assertNotNull("Profile update should succeed", updatedUser);
        assertNotNull("Interests should not be null", updatedUser.getInterests());
        assertEquals("Should have 3 interests", 3, updatedUser.getInterests().size());
        assertTrue("Should contain Sports", updatedUser.getInterests().contains("Sports"));
    }

    @Test
    public void updateProfile_withMaxInterests_shouldSucceed() {
        // Test with 6 interests (typical max)
        List<String> interests =
                Arrays.asList("Sports", "Music", "Art", "Technology", "Gaming", "Travel");
        UserProfileUpdateRequest request = TestDataFactory.createProfileUpdateRequest(interests);

        User updatedUser = apiHelper.updateUserProfile(request);

        assertNotNull("Profile update should succeed", updatedUser);
        assertNotNull("Interests should not be null", updatedUser.getInterests());
        assertTrue("Should have up to 6 interests", updatedUser.getInterests().size() <= 6);
    }

    @Test
    public void updateProfile_withLocation_shouldSucceed() {
        UserProfileUpdateRequest request =
                TestDataFactory.createLocationUpdateRequest("New York", 40.7128, -74.0060);

        User updatedUser = apiHelper.updateUserProfile(request);

        assertNotNull("Profile update should succeed", updatedUser);
        assertEquals("City should be updated", "New York", updatedUser.getCity());
    }

    @Test
    public void updateProfile_withEmptyBio_shouldSucceed() {
        UserProfileUpdateRequest request = TestDataFactory.createBioUpdateRequest("");

        User updatedUser = apiHelper.updateUserProfile(request);

        // Behavior depends on backend - either null or empty string
        assertNotNull("Profile update should succeed", updatedUser);
    }

    @Test
    public void updateProfile_withLongBio_shouldSucceed() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("This is a very long bio. ");
        }
        String longBio = sb.toString();
        UserProfileUpdateRequest request = TestDataFactory.createBioUpdateRequest(longBio);

        User updatedUser = apiHelper.updateUserProfile(request);

        assertNotNull("Profile update should succeed", updatedUser);
    }

    @Test
    public void updateProfile_withAllFields_shouldSucceed() {
        UserProfileUpdateRequest request = TestDataFactory.createProfileUpdateRequest();
        request.setCity("Los Angeles");
        request.setLatitude(34.0522);
        request.setLongitude(-118.2437);

        User updatedUser = apiHelper.updateUserProfile(request);

        assertNotNull("Profile update should succeed", updatedUser);
        assertNotNull("Bio should be set", updatedUser.getBio());
        assertNotNull("Interests should be set", updatedUser.getInterests());
    }

    @Test
    public void updateProfile_withoutAuthentication_shouldFail() {
        apiHelper.clearSession();

        UserProfileUpdateRequest request = TestDataFactory.createProfileUpdateRequest();
        User updatedUser = apiHelper.updateUserProfile(request);

        assertNull("Profile update without auth should fail", updatedUser);

        // Re-login for cleanup
        apiHelper.login(testUser.email, testUser.password);
    }

    // ==================== USER PHOTOS TESTS ====================

    @Test
    public void getMyPhotos_forNewUser_shouldReturnEmptyList() {
        List<UserPhoto> photos = apiHelper.getMyPhotos();

        assertNotNull("Photos list should not be null", photos);
        assertEquals("New user should have no photos", 0, photos.size());
    }

    @Test
    public void getUserPhotos_forOtherUser_shouldWork() {
        // Create another user
        TestDataFactory.TestUser otherUser = TestDataFactory.createTestUser("OtherUser");
        LoginResponse otherResponse =
                apiHelper.createUser(
                        otherUser.fullName,
                        otherUser.email,
                        otherUser.password,
                        otherUser.birthDate);
        assertNotNull("Other user creation should succeed", otherResponse);
        Long otherUserId = otherResponse.getUserId();

        try {
            // Switch back to original user
            apiHelper.clearSession();
            apiHelper.waitShort();
            apiHelper.login(testUser.email, testUser.password);

            // Get other user's photos
            List<UserPhoto> photos = apiHelper.getUserPhotos(otherUserId);

            assertNotNull("Photos list should not be null", photos);
        } finally {
            // Cleanup other user
            apiHelper.clearSession();
            apiHelper.waitShort();
            apiHelper.login(otherUser.email, otherUser.password);
            apiHelper.deleteUser(otherUserId);

            // Re-login as original user for tearDown
            apiHelper.clearSession();
            apiHelper.waitShort();
            apiHelper.login(testUser.email, testUser.password);
        }
    }
}
