package com.gege.activityfindermobile.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
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
 * Functional UI tests for the Create Activity screen.
 * Each test does a hard reset and fresh login.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateActivityUiTest {

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
        UiTestHelper.clearAppSharedPreferences();

        apiHelper = new TestApiHelper();

        DeviceLocationHelper locationHelper = new DeviceLocationHelper();
        locationHelper.acquireLocationAndSetForTests();

        // 1. Create test user via API
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("CreateActivityUiTest");
        testEmail = testUser.email;
        testPassword = testUser.password;

        var createResponse = apiHelper.createUser(
                testUser.fullName, testUser.email, testUser.password, testUser.birthDate);
        if (createResponse == null) {
            throw new RuntimeException("Failed to create test user via API");
        }
        testUserId = createResponse.getUserId();

        // 2. Login via API — writes session to SharedPreferences, recreates activity on feed
        TestLoginHelper.loginAndOpenFeed(
                activityRule.getScenario(), apiHelper, testEmail, testPassword);
        waitFor(2000);

        // 3. Open create activity
        onView(withId(R.id.fab_create)).perform(click());
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
    public void testCreateActivityScreen() {
        // Header and navigation
        onView(withId(R.id.toolbar_title)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));

        // Form inputs
        onView(withId(R.id.et_title)).check(matches(isDisplayed()));
        onView(withId(R.id.et_category)).check(matches(isDisplayed()));
        onView(withId(R.id.et_description)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.et_date)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.et_time)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.actv_location)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.et_total_spots)).perform(scrollTo()).check(matches(isDisplayed()));

        // Cover image and map
        onView(withId(R.id.card_cover_image)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.map_preview_container)).perform(scrollTo()).check(matches(isDisplayed()));

        // Section headers
        onView(withText("Basic Information")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("When & Where")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Capacity")).perform(scrollTo()).check(matches(isDisplayed()));

        // Create button
        onView(withId(R.id.btn_create)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btn_create)).check(matches(isEnabled()));

        // Progress hidden
        onView(withId(R.id.progress_loading)).check(matches(not(isDisplayed())));

        // Test input fields
        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText("Test Activity"), closeSoftKeyboard());
        onView(withId(R.id.et_title)).check(matches(withText("Test Activity")));

        onView(withId(R.id.et_description))
                .perform(scrollTo(), replaceText("Test description"), closeSoftKeyboard());
        onView(withId(R.id.et_description)).check(matches(withText("Test description")));

        onView(withId(R.id.et_total_spots))
                .perform(scrollTo(), replaceText("10"), closeSoftKeyboard());
        onView(withId(R.id.et_total_spots)).check(matches(withText("10")));

        // Test back navigation
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(1000);
        onView(withId(R.id.fab_create)).check(matches(isDisplayed()));
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
