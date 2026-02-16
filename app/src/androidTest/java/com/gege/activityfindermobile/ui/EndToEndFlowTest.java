package com.gege.activityfindermobile.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * End-to-end UI flow tests.
 * Tests complete user journeys through the application.
 * Each test does a hard reset and fresh login.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EndToEndFlowTest {

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

    @Before
    public void setUp() {
        // Hard reset - clear preferences and recreate activity to ensure login screen
        UiTestHelper.clearAppSharedPreferences();
        activityRule.getScenario().recreate();
        waitFor(1000);

        apiHelper = new TestApiHelper();

        DeviceLocationHelper locationHelper = new DeviceLocationHelper();
        locationHelper.acquireLocationAndSetForTests();

        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("E2ETest");
        testEmail = testUser.email;
        testPassword = testUser.password;

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
    public void testEndToEndFlows() {
        // ==================== LOGIN/REGISTER NAVIGATION ====================
        // Start on login screen
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));

        // Navigate to register
        onView(withId(R.id.tv_sign_up)).perform(scrollTo(), click());
        waitFor(1000);
        onView(withId(R.id.btn_register)).perform(scrollTo()).check(matches(isDisplayed()));

        // Navigate back to login
        onView(withId(R.id.tv_sign_in)).perform(scrollTo(), click());
        waitFor(1000);
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));

        // ==================== LOGIN ====================
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(testEmail), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.btn_login))
                .perform(scrollTo(), click());
        waitFor(8000);

        // ==================== MAIN NAVIGATION ====================
        // Verify on feed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // Navigate to create activity and back
        onView(withId(R.id.fab_create)).perform(click());
        waitFor(1000);
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(3000);
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // Navigate to profile and back
        onView(withId(R.id.nav_profile)).perform(click());
        waitFor(1000);
        onView(withId(R.id.nav_feed)).perform(click());
        waitFor(1000);
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // ==================== SEARCH AND FILTER ====================
        // Open search
        onView(withId(R.id.btn_search)).perform(click());
        waitFor(500);
        onView(withId(R.id.search_card)).check(matches(isDisplayed()));

        // Enter search text
        onView(withId(R.id.et_search))
                .perform(replaceText("Basketball"), closeSoftKeyboard());

        // Close search
        onView(withId(R.id.btn_search)).perform(click());
        waitFor(500);

        // Open filter dialog
        onView(withId(R.id.btn_filter)).perform(click());
        waitFor(500);

        // ==================== CREATE ACTIVITY FORM ====================
        // Navigate to create activity
        onView(withId(R.id.fab_create)).perform(click());
        waitFor(1000);

        // Fill in form fields
        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText("Test Event"), closeSoftKeyboard());
        onView(withId(R.id.et_description))
                .perform(scrollTo(), replaceText("This is a test event description"), closeSoftKeyboard());
        onView(withId(R.id.actv_location))
                .perform(scrollTo(), replaceText("Test Location"), closeSoftKeyboard());
        onView(withId(R.id.et_total_spots))
                .perform(scrollTo(), replaceText("10"), closeSoftKeyboard());

        // Navigate back without saving
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(1000);
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // ==================== ACTIVITY DETAIL ====================
        // Create an activity to view
        apiHelper.createActivity(TestDataFactory.createBasicActivity());
        waitFor(2000);

        // Refresh to see the activity
        onView(withId(R.id.swipe_refresh)).perform(swipeDown());
        waitFor(3000);

        // Click on first activity
        onView(withId(R.id.rv_activities))
                .perform(actionOnItemAtPosition(0, click()));
        waitFor(1000);

        // Should see activity detail (back button)
        onView(withId(R.id.fab_back)).check(matches(isDisplayed()));

        // Navigate back
        onView(withId(R.id.fab_back)).perform(click());
        waitFor(1000);
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
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
