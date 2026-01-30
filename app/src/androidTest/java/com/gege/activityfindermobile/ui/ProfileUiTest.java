package com.gege.activityfindermobile.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.ui.main.MainActivity;
import com.gege.activityfindermobile.util.TestApiHelper;
import com.gege.activityfindermobile.util.TestDataFactory;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

/**
 * Functional UI tests for the Profile screen.
 * Tests profile display, edit functionality, and navigation.
 * Requires a logged-in user to access the profile screen.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileUiTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    );

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private TestApiHelper apiHelper;
    private Long testUserId;
    private String testEmail;
    private String testPassword;
    private String testFullName;

    @Before
    public void setUp() {
        apiHelper = new TestApiHelper();

        // Create a test user and login
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("ProfileUiTest");
        testEmail = testUser.email;
        testPassword = testUser.password;
        testFullName = testUser.fullName;

        var response = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        if (response != null) {
            testUserId = response.getUserId();
        }

        // Login via UI
        loginViaUi(testEmail, testPassword);
        waitFor(2000);

        // Navigate to profile tab
        navigateToProfile();
    }

    @After
    public void tearDown() {
        if (testUserId != null) {
            try {
                apiHelper.clearSession();
                apiHelper.waitShort();
                apiHelper.loginWithRetry(testEmail, testPassword);
                apiHelper.deleteUser(testUserId);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        apiHelper.clearSession();
    }

    private void loginViaUi(String email, String password) {
        // Enter credentials and login
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.btn_login))
                .perform(scrollTo(), click());
        waitFor(3000);

        // Verify login succeeded
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    private void navigateToProfile() {
        // Click on profile tab in bottom navigation
        onView(withId(R.id.nav_profile)).perform(click());
        waitFor(1000);
    }

    // ==================== PROFILE DISPLAY TESTS ====================

    @Test
    public void profile_userNameDisplayed() {
        // User's name should be displayed
        onView(withText(testFullName)).check(matches(isDisplayed()));
    }

    @Test
    public void profile_editButtonDisplayed() {
        onView(withId(R.id.btn_edit_profile))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void profile_settingsButtonDisplayed() {
        onView(withId(R.id.btn_settings))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== MY ACTIVITIES SECTION TESTS ====================

    @Test
    public void profile_myActivitiesOptionDisplayed() {
        onView(withText("My Activities"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void profile_participationsOptionDisplayed() {
        onView(withText("Participations"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== EDIT PROFILE NAVIGATION TESTS ====================

    @Test
    public void profile_editButtonNavigatesToEditScreen() {
        onView(withId(R.id.btn_edit_profile))
                .perform(scrollTo(), click());
        waitFor(1000);

        // Should show edit profile screen elements
        onView(withId(R.id.et_bio)).check(matches(isDisplayed()));
    }

    // ==================== SETTINGS NAVIGATION TESTS ====================

    @Test
    public void profile_settingsNavigatesToSettingsScreen() {
        onView(withId(R.id.btn_settings))
                .perform(scrollTo(), click());
        waitFor(1000);

        // Should show settings screen - verify by checking for Log Out option
        onView(withText("Log Out"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== LOGOUT FUNCTIONALITY TESTS ====================

    @Test
    public void profile_logoutOptionExists() {
        // Navigate to settings first
        onView(withId(R.id.btn_settings))
                .perform(scrollTo(), click());
        waitFor(1000);

        // Look for logout option
        onView(withText("Log Out"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== ACCOUNT DELETION TESTS ====================

    @Test
    public void profile_deleteAccountOptionExists() {
        onView(withId(R.id.btn_settings))
                .perform(scrollTo(), click());
        waitFor(1000);

        onView(withText("Delete Account"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== HELPER METHODS ====================

    private void waitFor(long millis) {
        onView(ViewMatchers.isRoot()).perform(waitForAction(millis));
    }

    private static ViewAction waitForAction(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
