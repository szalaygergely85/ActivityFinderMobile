package com.gege.activityfindermobile;

import static org.junit.Assert.*;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.util.TestApiHelper;
import com.gege.activityfindermobile.util.TestDataFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive tests for user registration and login functionality.
 * Tests all success and error scenarios.
 */
@RunWith(AndroidJUnit4.class)
public class AuthenticationTest {

    private TestApiHelper apiHelper;
    private List<Long> createdUserIds;

    @Before
    public void setUp() {
        apiHelper = new TestApiHelper();
        createdUserIds = new ArrayList<>();
    }

    @After
    public void tearDown() {
        // Clean up all created users
        for (Long userId : createdUserIds) {
            try {
                apiHelper.deleteUser(userId);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        createdUserIds.clear();
        apiHelper.clearSession();
    }

    private void trackUser(Long userId) {
        if (userId != null) {
            createdUserIds.add(userId);
        }
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    public void register_withValidData_shouldSucceed() {
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("ValidReg");

        LoginResponse response = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Registration should succeed", response);
        trackUser(response.getUserId());

        assertNotNull("Should return user ID", response.getUserId());
        assertNotNull("Should return access token", response.getAccessToken());
        assertNotNull("Should return refresh token", response.getRefreshToken());
        assertEquals("Email should match", testUser.email, response.getEmail());
        assertEquals("Full name should match", testUser.fullName, response.getFullName());
    }

    @Test
    public void register_withMinimumAge_shouldSucceed() {
        // User exactly 18 years old
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUserWithAge(18);

        LoginResponse response = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("18-year-old registration should succeed", response);
        trackUser(response.getUserId());
        assertNotNull("Should return user ID", response.getUserId());
    }

    @Test
    public void register_withUnderage_shouldFail() {
        // User 17 years old - should be rejected
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUserWithAge(17);

        LoginResponse response = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNull("Underage registration should fail", response);
    }

    @Test
    public void register_withInvalidEmail_shouldFail() {
        LoginResponse response = apiHelper.createUser(
                TestDataFactory.uniqueFullName(),
                "invalid-email-format",
                TestDataFactory.defaultPassword(),
                TestDataFactory.defaultBirthDate()
        );

        assertNull("Registration with invalid email should fail", response);
    }

    @Test
    public void register_withEmptyEmail_shouldFail() {
        LoginResponse response = apiHelper.createUser(
                TestDataFactory.uniqueFullName(),
                "",
                TestDataFactory.defaultPassword(),
                TestDataFactory.defaultBirthDate()
        );

        assertNull("Registration with empty email should fail", response);
    }

    @Test
    public void register_withEmptyPassword_shouldFail() {
        LoginResponse response = apiHelper.createUser(
                TestDataFactory.uniqueFullName(),
                TestDataFactory.uniqueEmail(),
                "",
                TestDataFactory.defaultBirthDate()
        );

        assertNull("Registration with empty password should fail", response);
    }

    @Test
    public void register_withShortPassword_shouldFail() {
        LoginResponse response = apiHelper.createUser(
                TestDataFactory.uniqueFullName(),
                TestDataFactory.uniqueEmail(),
                "123",  // Too short
                TestDataFactory.defaultBirthDate()
        );

        assertNull("Registration with short password should fail", response);
    }

    @Test
    public void register_withEmptyFullName_shouldFail() {
        LoginResponse response = apiHelper.createUser(
                "",
                TestDataFactory.uniqueEmail(),
                TestDataFactory.defaultPassword(),
                TestDataFactory.defaultBirthDate()
        );

        assertNull("Registration with empty name should fail", response);
    }

    @Test
    public void register_withDuplicateEmail_shouldFail() {
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("Duplicate");

        // First registration should succeed
        LoginResponse firstResponse = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("First registration should succeed", firstResponse);
        trackUser(firstResponse.getUserId());

        // Second registration with same email should fail
        LoginResponse secondResponse = apiHelper.createUser(
                TestDataFactory.uniqueFullName(),  // Different name
                testUser.email,                     // Same email
                TestDataFactory.defaultPassword(),
                TestDataFactory.defaultBirthDate()
        );

        assertNull("Duplicate email registration should fail", secondResponse);
    }

    @Test
    public void register_withEmptyBirthDate_shouldFail() {
        LoginResponse response = apiHelper.createUser(
                TestDataFactory.uniqueFullName(),
                TestDataFactory.uniqueEmail(),
                TestDataFactory.defaultPassword(),
                ""
        );

        assertNull("Registration with empty birth date should fail", response);
    }

    @Test
    public void register_withInvalidBirthDateFormat_shouldFail() {
        LoginResponse response = apiHelper.createUser(
                TestDataFactory.uniqueFullName(),
                TestDataFactory.uniqueEmail(),
                TestDataFactory.defaultPassword(),
                "01-15-1990"  // Wrong format, should be yyyy-MM-dd
        );

        assertNull("Registration with invalid date format should fail", response);
    }

    // ==================== LOGIN TESTS ====================

    @Test
    public void login_withValidCredentials_shouldSucceed() {
        // First register a user
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("LoginValid");

        LoginResponse registerResponse = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Registration should succeed", registerResponse);
        trackUser(registerResponse.getUserId());

        // Clear session and wait for backend to fully commit
        apiHelper.clearSession();
        apiHelper.waitShort();

        // Now login
        LoginResponse loginResponse = apiHelper.loginWithRetry(testUser.email, testUser.password);

        assertNotNull("Login should succeed", loginResponse);
        assertNotNull("Should return access token", loginResponse.getAccessToken());
        assertNotNull("Should return refresh token", loginResponse.getRefreshToken());
        assertEquals("User ID should match", registerResponse.getUserId(), loginResponse.getUserId());
        assertEquals("Email should match", testUser.email, loginResponse.getEmail());
        assertEquals("Full name should match", testUser.fullName, loginResponse.getFullName());
    }

    @Test
    public void login_withWrongPassword_shouldFail() {
        // First register a user
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("LoginWrongPwd");

        LoginResponse registerResponse = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Registration should succeed", registerResponse);
        trackUser(registerResponse.getUserId());

        // Clear session and wait for backend
        apiHelper.clearSession();
        apiHelper.waitShort();

        // Try login with wrong password
        LoginResponse loginResponse = apiHelper.login(testUser.email, "WrongPassword123!");

        assertNull("Login with wrong password should fail", loginResponse);
    }

    @Test
    public void login_withNonExistentEmail_shouldFail() {
        LoginResponse response = apiHelper.login(
                "nonexistent_" + System.currentTimeMillis() + "@test.com",
                TestDataFactory.defaultPassword()
        );

        assertNull("Login with non-existent email should fail", response);
    }

    @Test
    public void login_withEmptyEmail_shouldFail() {
        LoginResponse response = apiHelper.login("", TestDataFactory.defaultPassword());

        assertNull("Login with empty email should fail", response);
    }

    @Test
    public void login_withEmptyPassword_shouldFail() {
        // First register a user
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("LoginEmptyPwd");

        LoginResponse registerResponse = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Registration should succeed", registerResponse);
        trackUser(registerResponse.getUserId());

        // Clear session and wait for backend
        apiHelper.clearSession();
        apiHelper.waitShort();

        // Try login with empty password
        LoginResponse loginResponse = apiHelper.login(testUser.email, "");

        assertNull("Login with empty password should fail", loginResponse);
    }

    @Test
    public void login_withCaseSensitiveEmail_shouldHandleCorrectly() {
        // First register a user with lowercase email
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("LoginCase");
        String lowercaseEmail = testUser.email.toLowerCase();

        LoginResponse registerResponse = apiHelper.createUser(
                testUser.fullName,
                lowercaseEmail,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Registration should succeed", registerResponse);
        trackUser(registerResponse.getUserId());

        // Clear session and wait for backend
        apiHelper.clearSession();
        apiHelper.waitShort();

        // Try login with uppercase email - behavior depends on backend implementation
        // This test documents the actual behavior
        LoginResponse loginResponse = apiHelper.login(lowercaseEmail.toUpperCase(), testUser.password);

        // Note: Whether this succeeds depends on backend email case-sensitivity
        // Update assertion based on expected behavior
        // Most systems treat email as case-insensitive
        if (loginResponse != null) {
            assertEquals("User ID should match if case-insensitive",
                    registerResponse.getUserId(), loginResponse.getUserId());
        }
        // If loginResponse is null, backend treats email as case-sensitive
    }

    @Test
    public void login_multipleTimesWithSameUser_shouldSucceed() {
        // Register a user
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("LoginMultiple");

        LoginResponse registerResponse = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Registration should succeed", registerResponse);
        trackUser(registerResponse.getUserId());

        // Wait for backend to commit
        apiHelper.waitShort();

        // Login multiple times
        for (int i = 0; i < 3; i++) {
            apiHelper.clearSession();
            apiHelper.waitShort(); // Wait between login attempts

            LoginResponse loginResponse = apiHelper.loginWithRetry(testUser.email, testUser.password);

            assertNotNull("Login attempt " + (i + 1) + " should succeed", loginResponse);
            assertEquals("User ID should match on attempt " + (i + 1),
                    registerResponse.getUserId(), loginResponse.getUserId());
        }
    }

    // ==================== COMBINED FLOW TESTS ====================

    @Test
    public void registerThenLoginThenDelete_fullFlow() {
        // Register
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("FullFlow");

        LoginResponse registerResponse = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Registration should succeed", registerResponse);
        Long userId = registerResponse.getUserId();
        assertNotNull("Should have user ID", userId);

        // Logout and login again
        apiHelper.clearSession();
        apiHelper.waitShort();

        LoginResponse loginResponse = apiHelper.loginWithRetry(testUser.email, testUser.password);
        assertNotNull("Login should succeed", loginResponse);
        assertEquals("User IDs should match", userId, loginResponse.getUserId());

        // Delete user
        boolean deleted = apiHelper.deleteUser(userId);
        assertTrue("User deletion should succeed", deleted);

        // Wait for deletion to be processed, then try to login again - should fail
        apiHelper.clearSession();
        apiHelper.waitShort();

        LoginResponse afterDeleteLogin = apiHelper.login(testUser.email, testUser.password);
        assertNull("Login after deletion should fail", afterDeleteLogin);

        // Don't track this user since we already deleted it
    }

    @Test
    public void register_tokensShouldBeValid() {
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("TokenTest");

        LoginResponse response = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        assertNotNull("Registration should succeed", response);
        trackUser(response.getUserId());

        // Validate token format (JWT tokens have 3 parts separated by dots)
        String accessToken = response.getAccessToken();
        assertNotNull("Access token should not be null", accessToken);
        assertFalse("Access token should not be empty", accessToken.isEmpty());

        String[] tokenParts = accessToken.split("\\.");
        assertEquals("JWT access token should have 3 parts", 3, tokenParts.length);

        String refreshToken = response.getRefreshToken();
        assertNotNull("Refresh token should not be null", refreshToken);
        assertFalse("Refresh token should not be empty", refreshToken.isEmpty());
    }
}
