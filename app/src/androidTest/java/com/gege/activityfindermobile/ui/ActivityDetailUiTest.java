package com.gege.activityfindermobile.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
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
import com.gege.activityfindermobile.data.model.Activity;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Functional UI tests for the Activity Detail screen.
 * Tests activity information display, participant interaction, and messaging.
 * Requires a logged-in user and an existing activity.
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
    private List<Long> createdActivityIds;

    @Before
    public void setUp() {
        apiHelper = new TestApiHelper();
        createdActivityIds = new ArrayList<>();

        // Create a test user and login
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
        }

        // Create a test activity via API
        createTestActivity();

        // Login via UI
        loginViaUi(testEmail, testPassword);
        waitFor(2000);

        // Navigate to activity detail
        navigateToActivityDetail();
    }

    @After
    public void tearDown() {
        // Clean up activities first
        for (Long activityId : createdActivityIds) {
            try {
                apiHelper.deleteActivity(activityId, testUserId);
            } catch (Exception e) {
                // Ignore
            }
        }

        // Clean up user
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

    private void createTestActivity() {
        Activity activity = apiHelper.createActivity(
                TestDataFactory.createBasicActivity()
        );
        if (activity != null) {
            createdActivityIds.add(activity.getId());
        }
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

        // Verify login succeeded
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    private void navigateToActivityDetail() {
        waitFor(2000);
        // Click on first activity in the list
        onView(withId(R.id.rv_activities))
                .perform(actionOnItemAtPosition(0, click()));
        waitFor(1000);

        // Verify we're on activity detail screen
        onView(withId(R.id.fab_back)).check(matches(isDisplayed()));
    }

    // ==================== HEADER TESTS ====================

    @Test
    public void activityDetail_backButtonDisplayed() {
        onView(withId(R.id.fab_back)).check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_heroImageDisplayed() {
        onView(withId(R.id.iv_activity_hero))
                .check(matches(isDisplayed()));
    }

    // ==================== ACTIVITY INFO TESTS ====================

    @Test
    public void activityDetail_titleDisplayed() {
        onView(withId(R.id.tv_title))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_descriptionDisplayed() {
        onView(withId(R.id.tv_description_full))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_dateDisplayed() {
        onView(withId(R.id.tv_date))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_timeDisplayed() {
        onView(withId(R.id.tv_time))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_locationDisplayed() {
        onView(withId(R.id.tv_location))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_spotsInfoDisplayed() {
        onView(withId(R.id.tv_spots))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_categoryDisplayed() {
        onView(withId(R.id.tv_activity_category))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== CREATOR INFO TESTS ====================

    @Test
    public void activityDetail_creatorCardDisplayed() {
        onView(withId(R.id.card_creator))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_creatorNameDisplayed() {
        onView(withId(R.id.tv_creator_name))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_creatorAvatarDisplayed() {
        onView(withId(R.id.iv_creator_avatar))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== MAP PREVIEW TESTS ====================

    @Test
    public void activityDetail_mapContainerDisplayed() {
        onView(withId(R.id.map_container))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_getDirectionsButtonDisplayed() {
        onView(withId(R.id.btn_get_directions))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== SECTION HEADER TESTS ====================

    @Test
    public void activityDetail_hostSectionHeaderDisplayed() {
        onView(withText("Host"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_aboutSectionHeaderDisplayed() {
        onView(withText("About this activity"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_locationSectionHeaderDisplayed() {
        onView(withText("Location"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void activityDetail_participantsSectionHeaderDisplayed() {
        onView(withText("Participants"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== NAVIGATION TESTS ====================

    @Test
    public void activityDetail_backButtonNavigatesBack() {
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
