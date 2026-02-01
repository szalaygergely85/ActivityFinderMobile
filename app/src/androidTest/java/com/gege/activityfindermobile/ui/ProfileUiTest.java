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

import android.Manifest;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.ui.main.MainActivity;
import com.gege.activityfindermobile.util.DeviceLocationHelper;
import com.gege.activityfindermobile.util.TestApiHelper;
import com.gege.activityfindermobile.util.TestDataFactory;

import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Functional UI tests for the Profile screen.
 * Tests are consolidated for efficiency - login/logout happens once per class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileUiTest {

    @ClassRule
    public static GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    );

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    private static TestApiHelper apiHelper;
    private static Long testUserId;
    private static String testEmail;
    private static String testPassword;
    private static String testFullName;
    private static boolean isLoggedIn = false;

    @BeforeClass
    public static void setUpClass() {
        apiHelper = new TestApiHelper();

        // Get device location
        DeviceLocationHelper locationHelper = new DeviceLocationHelper();
        locationHelper.acquireLocationAndSetForTests();

        // Create test user once for all tests
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
        } else {
            throw new RuntimeException("Failed to create test user via API");
        }

        apiHelper.waitMedium();
    }

    @AfterClass
    public static void tearDownClass() {
        // Clear app tokens
        UiTestHelper.clearAppSharedPreferences();

        // Delete test user once after all tests
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
        isLoggedIn = false;
    }

    private void ensureLoggedInAndOnProfile() {
        // First ensure we're logged in
        if (!isLoggedIn) {
            try {
                onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
                isLoggedIn = true;
            } catch (Exception e) {
                // Need to login
                try {
                    onView(withId(R.id.et_email))
                            .perform(scrollTo(), replaceText(testEmail), closeSoftKeyboard());
                    onView(withId(R.id.et_password))
                            .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());
                    onView(withId(R.id.btn_login))
                            .perform(scrollTo(), click());
                    waitFor(5000);
                    isLoggedIn = true;
                } catch (Exception ex) {
                    isLoggedIn = true; // Assume already logged in
                }
            }
        }

        // Navigate to profile
        onView(withId(R.id.nav_profile)).perform(click());
        waitFor(1000);
    }

    // ==================== CONSOLIDATED TESTS ====================

    /**
     * Tests profile display elements
     */
    @Test
    public void testProfileDisplayElements() {
        ensureLoggedInAndOnProfile();

        // User's name
        onView(withText(testFullName)).check(matches(isDisplayed()));

        // Edit button
        onView(withId(R.id.btn_edit_profile))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        // Settings button
        onView(withId(R.id.btn_settings))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        // My Activities option
        onView(withText("My Activities"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        // Participations option
        onView(withText("Participations"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    /**
     * Tests navigation to edit profile
     */
    @Test
    public void testEditProfileNavigation() {
        ensureLoggedInAndOnProfile();

        onView(withId(R.id.btn_edit_profile))
                .perform(scrollTo(), click());
        waitFor(1000);

        // Should show edit profile screen
        onView(withId(R.id.et_bio)).check(matches(isDisplayed()));

        // Go back
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(500);
    }

    /**
     * Tests settings and logout options
     */
    @Test
    public void testSettingsAndLogoutOptions() {
        ensureLoggedInAndOnProfile();

        // Navigate to settings
        onView(withId(R.id.btn_settings))
                .perform(scrollTo(), click());
        waitFor(1000);

        // Log Out option exists
        onView(withText("Log Out"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));

        // Delete Account option exists
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
