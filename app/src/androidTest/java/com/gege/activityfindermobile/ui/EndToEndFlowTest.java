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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * End-to-end UI flow tests.
 * Tests complete user journeys through the application.
 * Tests are consolidated for efficiency.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EndToEndFlowTest {

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

    private void ensureLoggedOut() {
        // Clear SharedPreferences directly
        UiTestHelper.clearAppSharedPreferences();
        waitFor(500);
        activityRule.getScenario().recreate();
        waitFor(1000);
    }

    private void ensureLoggedIn() {
        if (isLoggedIn) {
            try {
                onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
                return;
            } catch (Exception e) {
                // Need to re-login
            }
        }

        try {
            onView(withId(R.id.et_email))
                    .perform(scrollTo(), replaceText(testEmail), closeSoftKeyboard());
            onView(withId(R.id.et_password))
                    .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());
            onView(withId(R.id.btn_login))
                    .perform(scrollTo(), click());
            waitFor(5000);
            isLoggedIn = true;
        } catch (Exception e) {
            isLoggedIn = true; // Already logged in
        }
    }

    // ==================== CONSOLIDATED TESTS ====================

    /**
     * Tests login/register navigation flow
     */
    @Test
    public void testLoginRegisterNavigation() {
        ensureLoggedOut();

        // Start on login screen
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));

        // Navigate to register
        onView(withId(R.id.tv_sign_up)).perform(scrollTo(), click());
        waitFor(1000);
        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));

        // Navigate back to login
        onView(withId(R.id.tv_sign_in)).perform(scrollTo(), click());
        waitFor(1000);
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
    }

    /**
     * Tests main app navigation flows (feed, create, profile)
     */
    @Test
    public void testMainNavigationFlows() {
        ensureLoggedIn();

        // Verify on feed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // Navigate to create activity and back
        onView(withId(R.id.fab_create)).perform(click());
        waitFor(1000);
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(1000);
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // Navigate to profile and back
        onView(withId(R.id.nav_profile)).perform(click());
        waitFor(1000);
        onView(withId(R.id.nav_feed)).perform(click());
        waitFor(1000);
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    /**
     * Tests search and filter flows
     */
    @Test
    public void testSearchAndFilterFlows() {
        ensureLoggedIn();

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
    }

    /**
     * Tests create activity form flow
     */
    @Test
    public void testCreateActivityFormFlow() {
        ensureLoggedIn();

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
    }

    /**
     * Tests activity detail navigation
     */
    @Test
    public void testActivityDetailFlow() {
        ensureLoggedIn();

        // Create an activity to view
        apiHelper.createActivity(TestDataFactory.createBasicActivity());
        waitFor(2000);

        // Refresh to see the activity
        onView(withId(R.id.swipe_refresh)).perform(swipeDown());
        waitFor(2000);

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
