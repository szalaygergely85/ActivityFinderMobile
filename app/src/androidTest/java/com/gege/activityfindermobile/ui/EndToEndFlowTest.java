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

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.ui.main.MainActivity;
import com.gege.activityfindermobile.util.TestApiHelper;
import com.gege.activityfindermobile.util.TestDataFactory;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

/**
 * End-to-end UI flow tests.
 * Tests complete user journeys through the application.
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
    private String testFullName;

    @Before
    public void setUp() {
        apiHelper = new TestApiHelper();
    }

    @After
    public void tearDown() {
        // Clean up test user
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
    }

    // ==================== REGISTRATION TO LOGIN FLOW ====================

    @Test
    public void flow_navigateFromLoginToRegisterAndBack() {
        // Start on login screen
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));

        // Navigate to register
        onView(withId(R.id.tv_sign_up))
                .perform(scrollTo(), click());
        waitFor(1000);

        // Verify register screen
        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));

        // Navigate back to login
        onView(withId(R.id.tv_sign_in))
                .perform(scrollTo(), click());
        waitFor(1000);

        // Verify login screen
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
    }

    // ==================== LOGIN TO FEED FLOW ====================

    @Test
    public void flow_loginAndViewFeed() {
        // Create test user via API
        setupTestUser();

        // Login via UI
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(testEmail), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.btn_login))
                .perform(scrollTo(), click());

        waitFor(3000);

        // Verify feed is displayed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
        onView(withId(R.id.fab_create)).check(matches(isDisplayed()));
    }

    // ==================== FEED TO CREATE ACTIVITY FLOW ====================

    @Test
    public void flow_navigateFromFeedToCreateActivityAndBack() {
        // Setup and login
        setupTestUser();
        loginViaUi();

        // Verify on feed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // Navigate to create activity
        onView(withId(R.id.fab_create)).perform(click());
        waitFor(1000);

        // Verify create activity screen
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));

        // Navigate back
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(1000);

        // Verify back on feed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    // ==================== CREATE ACTIVITY FLOW ====================

    @Test
    public void flow_fillCreateActivityFormAndNavigateBack() {
        // Setup and login
        setupTestUser();
        loginViaUi();

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

        // Should be back on feed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    // ==================== SEARCH FLOW ====================

    @Test
    public void flow_openAndCloseSearch() {
        // Setup and login
        setupTestUser();
        loginViaUi();

        // Open search
        onView(withId(R.id.btn_search)).perform(click());
        waitFor(500);

        // Verify search card is visible
        onView(withId(R.id.search_card)).check(matches(isDisplayed()));

        // Enter search text
        onView(withId(R.id.et_search))
                .perform(replaceText("Basketball"), closeSoftKeyboard());

        // Close search by clicking search button again
        onView(withId(R.id.btn_search)).perform(click());
        waitFor(500);
    }

    // ==================== FILTER DIALOG FLOW ====================

    @Test
    public void flow_openFilterDialog() {
        // Setup and login
        setupTestUser();
        loginViaUi();

        // Open filter dialog
        onView(withId(R.id.btn_filter)).perform(click());
        waitFor(500);

        // Filter dialog should open
        // The actual filter dialog verification depends on implementation
    }

    // ==================== ACTIVITY DETAIL FLOW ====================

    @Test
    public void flow_viewActivityDetailAndNavigateBack() {
        // Setup user and create an activity
        setupTestUser();
        createTestActivity();
        loginViaUi();

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

        // Should be back on feed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    // ==================== PROFILE FLOW ====================

    @Test
    public void flow_navigateToProfileAndBack() {
        // Setup and login
        setupTestUser();
        loginViaUi();

        // Navigate to profile
        onView(withId(R.id.nav_profile)).perform(click());
        waitFor(1000);

        // Navigate back to feed
        onView(withId(R.id.nav_feed)).perform(click());
        waitFor(1000);

        // Verify feed is displayed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    // ==================== COMPLETE USER JOURNEY ====================

    @Test
    public void flow_completeUserJourney() {
        // 1. Start at login
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));

        // 2. Navigate to register and back
        onView(withId(R.id.tv_sign_up)).perform(scrollTo(), click());
        waitFor(500);
        onView(withId(R.id.tv_sign_in)).perform(scrollTo(), click());
        waitFor(500);

        // 3. Create user and login
        setupTestUser();
        loginViaUi();

        // 4. View feed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));

        // 5. Open search
        onView(withId(R.id.btn_search)).perform(click());
        waitFor(300);
        onView(withId(R.id.btn_search)).perform(click());
        waitFor(300);

        // 6. Navigate to create activity
        onView(withId(R.id.fab_create)).perform(click());
        waitFor(500);
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(500);

        // 7. Final verification - back on feed
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    // ==================== HELPER METHODS ====================

    private void setupTestUser() {
        TestDataFactory.TestUser testUser = TestDataFactory.createTestUser("E2ETest");
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
        }
    }

    private void createTestActivity() {
        apiHelper.createActivity(TestDataFactory.createBasicActivity());
    }

    private void loginViaUi() {
        // Enter credentials and login
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(testEmail), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());
        onView(withId(R.id.btn_login))
                .perform(scrollTo(), click());
        waitFor(3000);

        // Verify login succeeded
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
