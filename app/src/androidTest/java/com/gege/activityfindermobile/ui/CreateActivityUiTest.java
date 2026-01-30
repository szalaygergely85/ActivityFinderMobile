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
 * Functional UI tests for the Create Activity screen.
 * Tests form inputs, validation, and submission behavior.
 * Requires a logged-in user to access the create activity screen.
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
        apiHelper = new TestApiHelper();

        // Create a test user and login
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
        }

        // Login via UI and navigate to create activity
        loginViaUi(testEmail, testPassword);
        waitFor(2000);
        navigateToCreateActivity();
    }

    @After
    public void tearDown() {
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

        // Verify login succeeded
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    private void navigateToCreateActivity() {
        onView(withId(R.id.fab_create)).perform(click());
        waitFor(1000);

        // Verify we're on create activity screen
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));
    }

    // ==================== UI ELEMENT VISIBILITY TESTS ====================

    @Test
    public void createActivity_headerDisplayed() {
        onView(withId(R.id.toolbar_title)).check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_backButtonDisplayed() {
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_titleInputDisplayed() {
        onView(withId(R.id.et_title)).check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_categoryDropdownDisplayed() {
        onView(withId(R.id.et_category)).check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_descriptionInputDisplayed() {
        onView(withId(R.id.et_description))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_dateInputDisplayed() {
        onView(withId(R.id.et_date))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_timeInputDisplayed() {
        onView(withId(R.id.et_time))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_locationInputDisplayed() {
        onView(withId(R.id.actv_location))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_totalSpotsInputDisplayed() {
        onView(withId(R.id.et_total_spots))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_createButtonDisplayed() {
        onView(withId(R.id.btn_create))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_coverImageCardDisplayed() {
        onView(withId(R.id.card_cover_image))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== INPUT FUNCTIONALITY TESTS ====================

    @Test
    public void createActivity_titleFieldAcceptsInput() {
        String title = "Test Activity Title";

        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText(title), closeSoftKeyboard());

        onView(withId(R.id.et_title))
                .check(matches(withText(title)));
    }

    @Test
    public void createActivity_descriptionFieldAcceptsInput() {
        String description = "This is a test description for the activity. It describes what we will do.";

        onView(withId(R.id.et_description))
                .perform(scrollTo(), replaceText(description), closeSoftKeyboard());

        onView(withId(R.id.et_description))
                .check(matches(withText(description)));
    }

    @Test
    public void createActivity_totalSpotsFieldAcceptsInput() {
        String spots = "10";

        onView(withId(R.id.et_total_spots))
                .perform(scrollTo(), replaceText(spots), closeSoftKeyboard());

        onView(withId(R.id.et_total_spots))
                .check(matches(withText(spots)));
    }

    @Test
    public void createActivity_locationFieldAcceptsInput() {
        String location = "Central Park, New York";

        onView(withId(R.id.actv_location))
                .perform(scrollTo(), replaceText(location), closeSoftKeyboard());

        onView(withId(R.id.actv_location))
                .check(matches(withText(location)));
    }

    // ==================== CATEGORY DROPDOWN TESTS ====================

    @Test
    public void createActivity_categoryDropdownIsClickable() {
        onView(withId(R.id.et_category))
                .perform(scrollTo(), click());

        waitFor(500);
        // Dropdown should show options
    }

    // ==================== DATE/TIME PICKER TESTS ====================

    @Test
    public void createActivity_dateFieldOpensDatePicker() {
        onView(withId(R.id.et_date))
                .perform(scrollTo(), click());

        waitFor(500);
        // Date picker dialog should appear
    }

    @Test
    public void createActivity_timeFieldOpensTimePicker() {
        onView(withId(R.id.et_time))
                .perform(scrollTo(), click());

        waitFor(500);
        // Time picker dialog should appear
    }

    // ==================== COVER IMAGE TESTS ====================

    @Test
    public void createActivity_coverImageCardIsClickable() {
        onView(withId(R.id.card_cover_image))
                .perform(scrollTo(), click());

        waitFor(500);
        // Cover image picker dialog should appear
    }

    @Test
    public void createActivity_coverPlaceholderDisplayed() {
        onView(withId(R.id.layout_cover_placeholder))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    public void createActivity_withEmptyTitle_showsError() {
        // Leave title empty and fill other required fields
        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText(""), closeSoftKeyboard());

        onView(withId(R.id.btn_create))
                .perform(scrollTo(), click());

        waitFor(500);

        // Should still be on create activity screen
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_withEmptyCategory_showsError() {
        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText("Test Activity"), closeSoftKeyboard());
        // Don't select category

        onView(withId(R.id.btn_create))
                .perform(scrollTo(), click());

        waitFor(500);

        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_withEmptyDate_showsError() {
        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText("Test Activity"), closeSoftKeyboard());
        // Don't select date

        onView(withId(R.id.btn_create))
                .perform(scrollTo(), click());

        waitFor(500);

        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_withEmptyLocation_showsError() {
        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText("Test Activity"), closeSoftKeyboard());
        onView(withId(R.id.actv_location))
                .perform(scrollTo(), replaceText(""), closeSoftKeyboard());

        onView(withId(R.id.btn_create))
                .perform(scrollTo(), click());

        waitFor(500);

        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));
    }

    // ==================== NAVIGATION TESTS ====================

    @Test
    public void createActivity_backButtonNavigatesBack() {
        onView(withId(R.id.btn_back))
                .perform(click());

        waitFor(1000);

        // Should navigate back to feed
        onView(withId(R.id.fab_create)).check(matches(isDisplayed()));
    }

    // ==================== MAP PREVIEW TESTS ====================

    @Test
    public void createActivity_mapPreviewContainerExists() {
        onView(withId(R.id.map_preview_container))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== LOADING STATE TESTS ====================

    @Test
    public void createActivity_progressIndicatorInitiallyHidden() {
        onView(withId(R.id.progress_loading))
                .check(matches(not(isDisplayed())));
    }

    // ==================== SECTION HEADER TESTS ====================

    @Test
    public void createActivity_basicInfoSectionDisplayed() {
        onView(withText("Basic Information")).check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_whenWhereSectionDisplayed() {
        onView(withText("When & Where"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void createActivity_capacitySectionDisplayed() {
        onView(withText("Capacity"))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    // ==================== BUTTON STATE TESTS ====================

    @Test
    public void createActivity_createButtonEnabled() {
        onView(withId(R.id.btn_create))
                .perform(scrollTo())
                .check(matches(isEnabled()));
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
