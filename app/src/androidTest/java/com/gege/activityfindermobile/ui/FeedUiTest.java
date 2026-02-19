package com.gege.activityfindermobile.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
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
import com.gege.activityfindermobile.util.TestLoginHelper;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Functional UI tests for the Feed screen.
 * Each test does a hard reset and fresh login.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class FeedUiTest {

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

        // Get device location
        DeviceLocationHelper locationHelper = new DeviceLocationHelper();
        locationHelper.acquireLocationAndSetForTests();

        // Create test user
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

        // Programmatic login — no UI interaction needed
        TestLoginHelper.loginAndOpenFeed(
                activityRule.getScenario(), apiHelper, testEmail, testPassword);
        waitFor(3000);
    }

    @After
    public void tearDown() {
        // Clear session from SharedPreferences
        TestLoginHelper.clearSession();

        // Delete test user
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

    /**
     * Tests all feed UI elements and functionality
     */
    @Test
    public void testFeedScreen() {
        // Verify feed is displayed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // Header elements
        onView(withId(R.id.tv_header_title)).check(matches(isDisplayed()));
        onView(withText("Discover")).check(matches(isDisplayed()));
        onView(withId(R.id.btn_filter)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_search)).check(matches(isDisplayed()));
        onView(withId(R.id.fab_create)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_see_all)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_see_all)).check(matches(isEnabled()));

        // Progress hidden
        onView(withId(R.id.progress_loading)).check(matches(not(isDisplayed())));

        // Swipe refresh
        onView(withId(R.id.swipe_refresh)).perform(swipeDown());
        waitFor(5000);
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // Search functionality
        onView(withId(R.id.btn_search)).perform(click());
        waitFor(500);
        onView(withId(R.id.search_card)).check(matches(isDisplayed()));
        onView(withId(R.id.et_search))
                .perform(replaceText("Basketball"), closeSoftKeyboard());
        onView(withId(R.id.et_search)).check(matches(withText("Basketball")));
        onView(withId(R.id.btn_search)).perform(click());
        waitFor(500);

        // Filter dialog
        onView(withId(R.id.btn_filter)).perform(click());
        waitFor(500);
        // Dismiss filter dialog by clicking Apply
        onView(withId(R.id.applyButton)).perform(click());
        waitFor(500);

        // Navigate to create activity and back
        onView(withId(R.id.fab_create)).perform(click());
        waitFor(1000);
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(1000);
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // Create activity and view detail
        apiHelper.createActivity(TestDataFactory.createBasicActivity());
        waitFor(3000);
        onView(withId(R.id.swipe_refresh)).perform(swipeDown());
        waitFor(5000);
        onView(withId(R.id.rv_activities))
                .perform(actionOnItemAtPosition(0, click()));
        waitFor(1000);
        onView(withId(R.id.fab_back)).check(matches(isDisplayed()));
        onView(withId(R.id.fab_back)).perform(click());
        waitFor(1000);
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
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
