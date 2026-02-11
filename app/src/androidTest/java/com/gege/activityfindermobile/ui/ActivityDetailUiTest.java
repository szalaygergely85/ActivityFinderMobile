package com.gege.activityfindermobile.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.Manifest;
import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.Activity;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Functional UI tests for the Activity Detail screen.
 * Tests are consolidated for efficiency - login/logout happens once per class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActivityDetailUiTest {

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
    private static String testActivityTitle;
    private static List<Long> createdActivityIds = new ArrayList<>();
    private static boolean isLoggedIn = false;

    @BeforeClass
    public static void setUpClass() {
        apiHelper = new TestApiHelper();

        // Get device location
        DeviceLocationHelper locationHelper = new DeviceLocationHelper();
        locationHelper.acquireLocationAndSetForTests();

        // Create test user once for all tests
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("ActivityDetailUiTest");
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

        // Create a test activity
        com.gege.activityfindermobile.data.dto.ActivityCreateRequest request = TestDataFactory.createBasicActivity();
        testActivityTitle = request.getTitle();
        Activity activity = apiHelper.createActivity(request);
        if (activity != null) {
            createdActivityIds.add(activity.getId());
        } else {
            throw new RuntimeException("Failed to create test activity via API");
        }

        apiHelper.waitMedium();
    }

    @AfterClass
    public static void tearDownClass() {
        // Clear app tokens
        UiTestHelper.clearAppSharedPreferences();

        // Clean up activities
        for (Long activityId : createdActivityIds) {
            try {
                apiHelper.deleteActivity(activityId, testUserId);
            } catch (Exception e) {
                // Ignore
            }
        }

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

    private void ensureLoggedInAndOnDetail() {
        // Each test gets a fresh MainActivity from ActivityScenarioRule.
        // Detect actual UI state instead of relying on static flags.

        // Wait for the activity to fully launch
        waitFor(2000);

        // Check if we need to login (login screen visible) or already on feed
        boolean onFeed = false;
        try {
            onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
            onFeed = true;
        } catch (Exception e) {
            // Not on feed - might be on login screen or detail
        }

        if (!onFeed) {
            // Try going back first (might be on detail from previous test)
            goBackToFeed();
            try {
                onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
                onFeed = true;
            } catch (Exception e) {
                // Still not on feed - must be on login screen
            }
        }

        if (!onFeed) {
            // Login
            try {
                onView(withId(R.id.et_email))
                        .perform(scrollTo(), replaceText(testEmail), closeSoftKeyboard());
                onView(withId(R.id.et_password))
                        .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());
                onView(withId(R.id.btn_login))
                        .perform(scrollTo(), click());
                waitFor(5000);
            } catch (Exception ex) {
                // Ignore - might already be logged in
            }
        }

        // Wait for feed to acquire location and load activities
        waitFor(5000);

        // Refresh and navigate to activity detail (retry to handle slow loading)
        Exception lastException = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                onView(withId(R.id.swipe_refresh)).perform(swipeDown());
                waitFor(5000);

                onView(withId(R.id.rv_activities))
                        .perform(RecyclerViewActions.scrollTo(
                                hasDescendant(withText(testActivityTitle))));
                waitFor(500);
                onView(allOf(withId(R.id.tv_activity_title), withText(testActivityTitle)))
                        .perform(click());
                waitFor(1000);
                lastException = null;
                break;
            } catch (Exception e) {
                lastException = e;
                waitFor(3000);
            }
        }
        if (lastException != null) {
            throw new RuntimeException("Could not find activity in feed after retries", lastException);
        }
    }

    private void goBackToFeed() {
        try {
            onView(withId(R.id.fab_back)).perform(click());
            waitFor(500);
        } catch (Exception e) {
            // Already on feed
        }
    }

    // ==================== CONSOLIDATED TESTS ====================

    /**
     * Tests all activity detail header elements
     */
    @Test
    public void testActivityDetailHeader() {
        ensureLoggedInAndOnDetail();

        // Back button
        onView(withId(R.id.fab_back)).check(matches(isDisplayed()));

        // Hero image
        onView(withId(R.id.iv_activity_hero)).check(matches(isDisplayed()));

        goBackToFeed();
    }

    /**
     * Tests activity information display
     */
    @Test
    public void testActivityInfoDisplay() {
        ensureLoggedInAndOnDetail();

        // Title, description
        onView(withId(R.id.tv_title)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.tv_description_full)).perform(scrollTo()).check(matches(isDisplayed()));

        // Date, time, location
        onView(withId(R.id.tv_date)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.tv_time)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.tv_location)).perform(scrollTo()).check(matches(isDisplayed()));

        // Spots and category
        onView(withId(R.id.tv_spots)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.tv_activity_category)).perform(scrollTo()).check(matches(isDisplayed()));

        goBackToFeed();
    }

    /**
     * Tests creator info and section headers
     */
    @Test
    public void testCreatorAndSections() {
        ensureLoggedInAndOnDetail();

        // Creator card
        onView(withId(R.id.card_creator)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.tv_creator_name)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.iv_creator_avatar)).perform(scrollTo()).check(matches(isDisplayed()));

        // Section headers
        onView(withText("Host")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("About this activity")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Location")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Participants")).perform(scrollTo()).check(matches(isDisplayed()));

        goBackToFeed();
    }

    /**
     * Tests map and directions
     */
    @Test
    public void testMapAndDirections() {
        ensureLoggedInAndOnDetail();

        onView(withId(R.id.map_container)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btn_get_directions)).perform(scrollTo()).check(matches(isDisplayed()));

        goBackToFeed();
    }

    /**
     * Tests back navigation
     */
    @Test
    public void testBackNavigation() {
        ensureLoggedInAndOnDetail();

        onView(withId(R.id.fab_back)).perform(click());
        waitFor(1000);

        // Should be back on feed
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
