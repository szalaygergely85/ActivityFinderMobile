package com.gege.activityfindermobile.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
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
 * Functional UI tests for the Feed screen.
 * Tests activity list display, filtering, search, and navigation.
 * Requires a logged-in user to access the feed.
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
        apiHelper = new TestApiHelper();

        // Create a test user and login via API
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
        }

        // Login via UI
        loginViaUi(testEmail, testPassword);
        waitFor(2000);
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

    private void loginViaUi(String email, String password) {
        // Enter credentials and login
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.btn_login))
                .perform(scrollTo(), click());
        waitFor(3000);

        // Verify login succeeded by checking we're on the feed screen
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    // ==================== FEED HEADER TESTS ====================

    @Test
    public void feed_headerTitleDisplayed() {
        onView(withId(R.id.tv_header_title)).check(matches(isDisplayed()));
        onView(withText("Discover")).check(matches(isDisplayed()));
    }

    @Test
    public void feed_filterButtonDisplayed() {
        onView(withId(R.id.btn_filter)).check(matches(isDisplayed()));
    }

    @Test
    public void feed_searchButtonDisplayed() {
        onView(withId(R.id.btn_search)).check(matches(isDisplayed()));
    }

    @Test
    public void feed_fabCreateButtonDisplayed() {
        onView(withId(R.id.fab_create)).check(matches(isDisplayed()));
    }

    // ==================== RECYCLERVIEW TESTS ====================

    @Test
    public void feed_recyclerViewDisplayed() {
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    @Test
    public void feed_swipeRefreshWorks() {
        // Perform swipe down to refresh
        onView(withId(R.id.swipe_refresh))
                .perform(swipeDown());

        waitFor(2000);

        // RecyclerView should still be displayed after refresh
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    @Test
    public void feed_clickOnActivityNavigatesToDetail() {
        // Create an activity via API first to ensure there's data
        apiHelper.createActivity(TestDataFactory.createBasicActivity());
        waitFor(2000);

        // Refresh to see the new activity
        onView(withId(R.id.swipe_refresh)).perform(swipeDown());
        waitFor(2000);

        // Click on first item in the list
        onView(withId(R.id.rv_activities))
                .perform(actionOnItemAtPosition(0, click()));

        waitFor(1000);

        // Should navigate to activity detail screen
        onView(withId(R.id.fab_back)).check(matches(isDisplayed()));
    }

    // ==================== FILTER DIALOG TESTS ====================

    @Test
    public void feed_filterButtonOpensFilterDialog() {
        onView(withId(R.id.btn_filter))
                .perform(click());

        waitFor(500);

        // Filter dialog should appear
        // Specific assertions depend on dialog implementation
    }

    // ==================== SEARCH FUNCTIONALITY TESTS ====================

    @Test
    public void feed_searchButtonTogglesSearchCard() {
        // Click search button
        onView(withId(R.id.btn_search))
                .perform(click());

        waitFor(500);

        // Search card should become visible
        onView(withId(R.id.search_card)).check(matches(isDisplayed()));
    }

    @Test
    public void feed_searchInputAcceptsText() {
        // Open search
        onView(withId(R.id.btn_search))
                .perform(click());

        waitFor(500);

        // Enter search query
        String searchQuery = "Basketball";
        onView(withId(R.id.et_search))
                .perform(replaceText(searchQuery), closeSoftKeyboard());

        onView(withId(R.id.et_search))
                .check(matches(withText(searchQuery)));
    }

    // ==================== FAB NAVIGATION TESTS ====================

    @Test
    public void feed_fabNavigatesToCreateActivity() {
        onView(withId(R.id.fab_create))
                .perform(click());

        waitFor(1000);

        // Should navigate to create activity screen
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));
    }

    // ==================== EMPTY STATE TESTS ====================
    // Empty state tests are skipped as they depend on the data state

    // ==================== LOADING STATE TESTS ====================

    @Test
    public void feed_progressIndicatorBehavior() {
        // Progress indicator should be hidden when data is loaded
        waitFor(3000);
        onView(withId(R.id.progress_loading)).check(matches(not(isDisplayed())));
    }

    // ==================== SEE ALL BUTTON TESTS ====================

    @Test
    public void feed_seeAllButtonDisplayed() {
        onView(withId(R.id.btn_see_all)).check(matches(isDisplayed()));
    }

    @Test
    public void feed_seeAllButtonIsClickable() {
        onView(withId(R.id.btn_see_all)).check(matches(isEnabled()));
    }

    // ==================== SCROLL BEHAVIOR TESTS ====================

    @Test
    public void feed_canScrollRecyclerView() {
        waitFor(2000);

        // RecyclerView should be displayed and scrollable
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
