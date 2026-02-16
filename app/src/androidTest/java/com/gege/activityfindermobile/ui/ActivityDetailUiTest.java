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
 * Functional UI tests for the Activity Detail screen.
 * Each test does a hard reset and fresh login.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ActivityDetailUiTest {

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

        // Fresh login
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(testEmail), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.btn_login))
                .perform(scrollTo(), click());
        waitFor(8000);

        // Create activity after login (so apiHelper has proper session)
        apiHelper.createActivity(TestDataFactory.createBasicActivity());
        waitFor(2000);

        // Refresh and navigate to activity detail
        onView(withId(R.id.swipe_refresh)).perform(swipeDown());
        waitFor(5000);

        // Click first activity in feed
        onView(withId(R.id.rv_activities))
                .perform(actionOnItemAtPosition(0, click()));
        waitFor(1000);
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
    public void testActivityDetailScreen() {
        // Header elements
        onView(withId(R.id.fab_back)).check(matches(isDisplayed()));
        onView(withId(R.id.iv_activity_hero)).check(matches(isDisplayed()));

        // Title and description
        onView(withId(R.id.tv_title)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.tv_description_full)).perform(scrollTo()).check(matches(isDisplayed()));

        // Date, time, location
        onView(withId(R.id.tv_date)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.tv_time)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.tv_location)).perform(scrollTo()).check(matches(isDisplayed()));

        // Spots and category
        onView(withId(R.id.tv_spots)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.tv_activity_category)).perform(scrollTo()).check(matches(isDisplayed()));

        // Creator card
        onView(withId(R.id.card_creator)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.tv_creator_name)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.iv_creator_avatar)).perform(scrollTo()).check(matches(isDisplayed()));

        // Section headers
        onView(withText("Host")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("About this activity")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Location")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Participants")).perform(scrollTo()).check(matches(isDisplayed()));

        // Map and directions
        onView(withId(R.id.map_container)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btn_get_directions)).perform(scrollTo()).check(matches(isDisplayed()));

        // Test back navigation - scroll to hero first to ensure fab_back is fully visible
        onView(withId(R.id.iv_activity_hero)).perform(scrollTo());
        waitFor(500);
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
