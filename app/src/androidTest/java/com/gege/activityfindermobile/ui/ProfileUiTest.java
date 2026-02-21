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
import com.gege.activityfindermobile.util.TestLoginHelper;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Functional UI tests for the Profile screen.
 * Each test does a hard reset and fresh login.
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
        UiTestHelper.clearAppSharedPreferences();

        apiHelper = new TestApiHelper();

        DeviceLocationHelper locationHelper = new DeviceLocationHelper();
        locationHelper.acquireLocationAndSetForTests();

        // 1. Create test user via API
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("ProfileUiTest");
        testEmail = testUser.email;
        testPassword = testUser.password;
        testFullName = testUser.fullName;

        var createResponse = apiHelper.createUser(
                testUser.fullName, testUser.email, testUser.password, testUser.birthDate);
        if (createResponse == null) {
            throw new RuntimeException("Failed to create test user via API");
        }
        testUserId = createResponse.getUserId();

        // 2. Login via API — writes session to SharedPreferences and navigates to feed
        TestLoginHelper.loginAndOpenFeed(
                activityRule.getScenario(), apiHelper, testEmail, testPassword);
        waitFor(2000);

        // 3. Navigate to profile
        onView(withId(R.id.nav_profile)).perform(click());
        waitFor(2000);
    }

    @After
    public void tearDown() {
        UiTestHelper.clearAppSharedPreferences();

        if (testUserId != null) {
            try {
                apiHelper.loginWithRetry(testEmail, testPassword);
                apiHelper.deleteUser(testUserId);
            } catch (Exception e) {
                // Ignore
            }
        }
        apiHelper.clearSession();
    }

    @Test
    public void testProfileScreen() {
        // User's name
        onView(withText(testFullName)).check(matches(isDisplayed()));

        // Edit and settings buttons (in fixed header, no scroll needed)
        onView(withId(R.id.btn_edit_profile)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_settings)).check(matches(isDisplayed()));

        // Menu options
        onView(withText("My Activities")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Participations")).perform(scrollTo()).check(matches(isDisplayed()));

        // Navigate to edit profile
        onView(withId(R.id.btn_edit_profile)).perform(click());
        waitFor(1000);
        onView(withId(R.id.et_bio)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(500);

        // Navigate to settings
        onView(withId(R.id.btn_settings)).perform(click());
        waitFor(1000);
        onView(withText("Log Out")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Delete Account")).perform(scrollTo()).check(matches(isDisplayed()));
    }

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
