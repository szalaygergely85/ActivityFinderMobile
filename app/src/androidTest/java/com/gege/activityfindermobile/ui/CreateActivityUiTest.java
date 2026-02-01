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

import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Functional UI tests for the Create Activity screen.
 * Tests are consolidated for efficiency - login/logout happens once per class.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateActivityUiTest {

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
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("CreateActivityUiTest");
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

    private void ensureLoggedInAndOnCreateActivity() {
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
                    isLoggedIn = true;
                }
            }
        }

        // Navigate to create activity
        onView(withId(R.id.fab_create)).perform(click());
        waitFor(1000);
    }

    private void goBackToFeed() {
        try {
            onView(withId(R.id.btn_back)).perform(click());
            waitFor(500);
        } catch (Exception e) {
            // Already on feed
        }
    }

    // ==================== CONSOLIDATED TESTS ====================

    /**
     * Tests all UI elements visibility in one test
     */
    @Test
    public void testFormElementsDisplayed() {
        ensureLoggedInAndOnCreateActivity();

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
        onView(withId(R.id.layout_cover_placeholder)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.map_preview_container)).perform(scrollTo()).check(matches(isDisplayed()));

        // Section headers
        onView(withText("Basic Information")).check(matches(isDisplayed()));
        onView(withText("When & Where")).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Capacity")).perform(scrollTo()).check(matches(isDisplayed()));

        // Create button
        onView(withId(R.id.btn_create)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.btn_create)).check(matches(isEnabled()));

        // Progress hidden
        onView(withId(R.id.progress_loading)).check(matches(not(isDisplayed())));

        goBackToFeed();
    }

    /**
     * Tests form input functionality
     */
    @Test
    public void testFormInputFunctionality() {
        ensureLoggedInAndOnCreateActivity();

        // Title field
        String title = "Test Activity Title";
        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText(title), closeSoftKeyboard());
        onView(withId(R.id.et_title)).check(matches(withText(title)));

        // Description field
        String description = "This is a test description for the activity.";
        onView(withId(R.id.et_description))
                .perform(scrollTo(), replaceText(description), closeSoftKeyboard());
        onView(withId(R.id.et_description)).check(matches(withText(description)));

        // Total spots field
        String spots = "10";
        onView(withId(R.id.et_total_spots))
                .perform(scrollTo(), replaceText(spots), closeSoftKeyboard());
        onView(withId(R.id.et_total_spots)).check(matches(withText(spots)));

        // Location field
        String location = "Central Park, New York";
        onView(withId(R.id.actv_location))
                .perform(scrollTo(), replaceText(location), closeSoftKeyboard());
        onView(withId(R.id.actv_location)).check(matches(withText(location)));

        goBackToFeed();
    }

    /**
     * Tests pickers and dropdowns
     */
    @Test
    public void testPickersAndDropdowns() {
        ensureLoggedInAndOnCreateActivity();

        // Category dropdown
        onView(withId(R.id.et_category)).perform(scrollTo(), click());
        waitFor(500);

        // Date picker (just test it opens)
        onView(withId(R.id.et_date)).perform(scrollTo(), click());
        waitFor(500);

        // Time picker (just test it opens)
        onView(withId(R.id.et_time)).perform(scrollTo(), click());
        waitFor(500);

        // Cover image card
        onView(withId(R.id.card_cover_image)).perform(scrollTo(), click());
        waitFor(500);

        goBackToFeed();
    }

    /**
     * Tests form validation - empty fields
     */
    @Test
    public void testFormValidation() {
        ensureLoggedInAndOnCreateActivity();

        // Try to submit with empty title
        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.btn_create)).perform(scrollTo(), click());
        waitFor(500);
        // Should still be on create activity screen
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));

        // Try with title but no category
        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText("Test Activity"), closeSoftKeyboard());
        onView(withId(R.id.btn_create)).perform(scrollTo(), click());
        waitFor(500);
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));

        goBackToFeed();
    }

    /**
     * Tests navigation back to feed
     */
    @Test
    public void testBackNavigation() {
        ensureLoggedInAndOnCreateActivity();

        onView(withId(R.id.btn_back)).perform(click());
        waitFor(1000);

        // Should navigate back to feed
        onView(withId(R.id.fab_create)).check(matches(isDisplayed()));
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
