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
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

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
 * Functional UI tests for the Feed screen.
 * Tests are consolidated for efficiency - login/logout happens once per class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FeedUiTest {

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
    private static boolean isLoggedIn = false;

    @BeforeClass
    public static void setUpClass() {
        apiHelper = new TestApiHelper();

        // Get device location
        DeviceLocationHelper locationHelper = new DeviceLocationHelper();
        locationHelper.acquireLocationAndSetForTests();

        // Create test user once for all tests
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("FeedUiTest");
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

    private void ensureLoggedIn() {
        if (isLoggedIn) {
            // Check if still on feed
            try {
                onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
                return;
            } catch (Exception e) {
                // Need to re-login
            }
        }

        // Login
        try {
            onView(withId(R.id.et_email))
                    .perform(scrollTo(), replaceText(testEmail), closeSoftKeyboard());
            onView(withId(R.id.et_password))
                    .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());
            onView(withId(R.id.btn_login))
                    .perform(scrollTo(), click());
            waitFor(8000);
            onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
            isLoggedIn = true;
        } catch (Exception e) {
            // Already on feed
            isLoggedIn = true;
        }
    }

    // ==================== CONSOLIDATED TESTS ====================

    /**
     * Tests all feed header elements in one test
     */
    @Test
    public void testFeedHeaderElements() {
        ensureLoggedIn();

        // Header title
        onView(withId(R.id.tv_header_title)).check(matches(isDisplayed()));
        onView(withText("Discover")).check(matches(isDisplayed()));

        // Filter button
        onView(withId(R.id.btn_filter)).check(matches(isDisplayed()));

        // Search button
        onView(withId(R.id.btn_search)).check(matches(isDisplayed()));

        // FAB create button
        onView(withId(R.id.fab_create)).check(matches(isDisplayed()));

        // See all button
        onView(withId(R.id.btn_see_all)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_see_all)).check(matches(isEnabled()));
    }

    /**
     * Tests RecyclerView display and refresh
     */
    @Test
    public void testFeedRecyclerView() {
        ensureLoggedIn();

        // RecyclerView displayed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // Swipe refresh works
        onView(withId(R.id.swipe_refresh)).perform(swipeDown());
        waitFor(5000);
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // Progress hidden after load
        onView(withId(R.id.progress_loading)).check(matches(not(isDisplayed())));
    }

    /**
     * Tests search functionality
     */
    @Test
    public void testSearchFunctionality() {
        ensureLoggedIn();

        // Open search
        onView(withId(R.id.btn_search)).perform(click());
        waitFor(500);

        // Search card visible
        onView(withId(R.id.search_card)).check(matches(isDisplayed()));

        // Enter search text
        String searchQuery = "Basketball";
        onView(withId(R.id.et_search))
                .perform(replaceText(searchQuery), closeSoftKeyboard());
        onView(withId(R.id.et_search)).check(matches(withText(searchQuery)));

        // Close search
        onView(withId(R.id.btn_search)).perform(click());
        waitFor(500);
    }

    /**
     * Tests filter dialog
     */
    @Test
    public void testFilterDialog() {
        ensureLoggedIn();

        onView(withId(R.id.btn_filter)).perform(click());
        waitFor(500);
        // Filter dialog should appear - specific assertions depend on implementation
    }

    /**
     * Tests navigation to create activity and activity detail
     */
    @Test
    public void testNavigationFlows() {
        ensureLoggedIn();

        // Navigate to create activity
        onView(withId(R.id.fab_create)).perform(click());
        waitFor(1000);
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));

        // Go back
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(1000);

        // Create an activity to click on
        apiHelper.createActivity(TestDataFactory.createBasicActivity());
        waitFor(3000);

        // Refresh
        onView(withId(R.id.swipe_refresh)).perform(swipeDown());
        waitFor(5000);

        // Click on activity to go to detail
        onView(withId(R.id.rv_activities))
                .perform(actionOnItemAtPosition(0, click()));
        waitFor(1000);

        // Should be on detail screen
        onView(withId(R.id.fab_back)).check(matches(isDisplayed()));

        // Go back to feed
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
