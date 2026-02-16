package com.gege.activityfindermobile.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.utils.Constants;

import org.hamcrest.Matcher;

import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.util.TestApiHelper;
import com.gege.activityfindermobile.util.TestDataFactory;

/**
 * Helper class providing common operations for UI tests.
 * Reduces code duplication across test classes.
 */
public class UiTestHelper {

    /**
     * Create a test user via API and return the user ID.
     * Throws RuntimeException if user creation fails.
     *
     * @param apiHelper The TestApiHelper instance
     * @param testUser The test user data
     * @return The created user's ID
     */
    public static Long createTestUserOrFail(TestApiHelper apiHelper, TestDataFactory.TestUser testUser) {
        LoginResponse response = apiHelper.createUser(
                testUser.fullName,
                testUser.email,
                testUser.password,
                testUser.birthDate
        );

        if (response == null) {
            throw new RuntimeException("Failed to create test user via API - check server connectivity");
        }

        // Wait for backend to process the registration
        apiHelper.waitMedium();

        return response.getUserId();
    }

    /**
     * Wait for a specified number of milliseconds.
     * Use sparingly - prefer IdlingResources for production tests.
     */
    public static void waitFor(long millis) {
        onView(ViewMatchers.isRoot()).perform(waitForAction(millis));
    }

    /**
     * Create a ViewAction that waits for a specified duration.
     */
    public static ViewAction waitForAction(final long millis) {
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

    /**
     * Login with the given credentials via UI.
     * Verifies login succeeded by checking for feed screen.
     * Waits 5 seconds for login + location acquisition + activities to load.
     */
    public static void loginViaUi(String email, String password) {
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.btn_login))
                .perform(scrollTo(), click());
        // Wait longer for login + location acquisition + activities to load
        waitFor(5000);
        // Verify login succeeded
        verifyFeedScreenDisplayed();
    }

    /**
     * Check if already logged in and skip login if so.
     * Otherwise, login with the given credentials via UI.
     */
    public static void loginOrSkipIfAlreadyLoggedIn(String email, String password) {
        // Check if we're already on the feed screen (already logged in)
        try {
            onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
            // Already logged in, no need to login again
            return;
        } catch (Exception e) {
            // Not on feed screen, need to login
        }

        loginViaUi(email, password);
    }

    /**
     * Ensure user is logged out.
     * If on feed screen (logged in), navigate to profile/settings and log out.
     */
    public static void ensureLoggedOut() {
        // Check if we're on the feed screen (logged in)
        try {
            onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
            // We're logged in, need to log out
            // Navigate to profile
            onView(withId(R.id.nav_profile)).perform(click());
            waitFor(1000);
            // Go to settings (btn_settings is in fixed header, no scroll needed)
            onView(withId(R.id.btn_settings)).perform(click());
            waitFor(1000);
            // Click logout
            onView(withText("Log Out")).perform(scrollTo(), click());
            waitFor(2000);
        } catch (Exception e) {
            // Not on feed screen, already logged out
        }
    }

    /**
     * Navigate from login to register screen.
     * Verifies register screen is displayed.
     */
    public static void navigateToRegister() {
        onView(withId(R.id.tv_sign_up)).perform(scrollTo(), click());
        waitFor(1000);
        verifyRegisterScreenDisplayed();
    }

    /**
     * Navigate from register to login screen.
     * Verifies login screen is displayed.
     */
    public static void navigateToLogin() {
        onView(withId(R.id.tv_sign_in)).perform(scrollTo(), click());
        waitFor(1000);
        verifyLoginScreenDisplayed();
    }

    /**
     * Navigate to the create activity screen from feed.
     * Verifies create activity screen is displayed.
     */
    public static void navigateToCreateActivity() {
        onView(withId(R.id.fab_create)).perform(click());
        waitFor(1000);
        verifyCreateActivityScreenDisplayed();
    }

    /**
     * Navigate to profile tab.
     */
    public static void navigateToProfile() {
        onView(withId(R.id.nav_profile)).perform(click());
        waitFor(1000);
    }

    /**
     * Navigate to feed tab.
     * Verifies feed screen is displayed.
     */
    public static void navigateToFeed() {
        onView(withId(R.id.nav_feed)).perform(click());
        waitFor(1000);
        verifyFeedScreenDisplayed();
    }

    /**
     * Navigate to notifications tab.
     */
    public static void navigateToNotifications() {
        onView(withId(R.id.notificationsFragment)).perform(click());
        waitFor(1000);
    }

    /**
     * Click the back button (for create activity screen).
     */
    public static void pressBack() {
        onView(withId(R.id.btn_back)).perform(click());
        waitFor(500);
    }

    /**
     * Click the FAB back button (for activity detail screen).
     */
    public static void pressFabBack() {
        onView(withId(R.id.fab_back)).perform(click());
        waitFor(500);
    }

    /**
     * Verify the login screen is displayed.
     */
    public static void verifyLoginScreenDisplayed() {
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
    }

    /**
     * Verify the register screen is displayed.
     */
    public static void verifyRegisterScreenDisplayed() {
        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
    }

    /**
     * Verify the feed screen is displayed.
     */
    public static void verifyFeedScreenDisplayed() {
        onView(withId(R.id.rv_activities)).check(matches(isDisplayed()));
    }

    /**
     * Verify the create activity screen is displayed.
     */
    public static void verifyCreateActivityScreenDisplayed() {
        onView(withId(R.id.btn_create)).check(matches(isDisplayed()));
    }

    /**
     * Fill in the registration form.
     */
    public static void fillRegistrationForm(String fullName, String email, String password) {
        onView(withId(R.id.et_full_name))
                .perform(scrollTo(), replaceText(fullName), closeSoftKeyboard());
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
                .perform(scrollTo(), replaceText(password), closeSoftKeyboard());
    }

    /**
     * Fill in the create activity form with basic data.
     */
    public static void fillCreateActivityForm(String title, String description, String location, String spots) {
        onView(withId(R.id.et_title))
                .perform(scrollTo(), replaceText(title), closeSoftKeyboard());
        onView(withId(R.id.et_description))
                .perform(scrollTo(), replaceText(description), closeSoftKeyboard());
        onView(withId(R.id.actv_location))
                .perform(scrollTo(), replaceText(location), closeSoftKeyboard());
        if (spots != null && !spots.isEmpty()) {
            onView(withId(R.id.et_total_spots))
                    .perform(scrollTo(), replaceText(spots), closeSoftKeyboard());
        }
    }

    /**
     * Clear a text field.
     */
    public static void clearTextField(int viewId) {
        onView(withId(viewId))
                .perform(scrollTo(), replaceText(""), closeSoftKeyboard());
    }

    /**
     * Enter text in a field.
     */
    public static void enterText(int viewId, String text) {
        onView(withId(viewId))
                .perform(scrollTo(), replaceText(text), closeSoftKeyboard());
    }

    /**
     * Click on a view with the given ID.
     */
    public static void clickView(int viewId) {
        onView(withId(viewId)).perform(click());
    }

    /**
     * Scroll to and click on a view with the given ID.
     */
    public static void scrollAndClick(int viewId) {
        onView(withId(viewId)).perform(scrollTo(), click());
    }

    /**
     * Verify a view is displayed.
     */
    public static void verifyDisplayed(int viewId) {
        onView(withId(viewId)).check(matches(isDisplayed()));
    }

    /**
     * Scroll to a view and verify it's displayed.
     */
    public static void scrollAndVerifyDisplayed(int viewId) {
        onView(withId(viewId)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    /**
     * Clear the app's SharedPreferences directly.
     * This is the most reliable way to clear session data in tests.
     * Call this in tearDown before deleting the test user.
     */
    public static void clearAppSharedPreferences() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    /**
     * Clear session data from SharedPreferences.
     * This removes only the session-related keys while preserving other preferences.
     */
    public static void clearSessionData() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(Constants.KEY_USER_ID)
                .remove(Constants.KEY_USER_TOKEN)
                .remove(Constants.KEY_REFRESH_TOKEN)
                .remove(Constants.KEY_USER_EMAIL)
                .putBoolean(Constants.KEY_IS_LOGGED_IN, false)
                .apply();
    }
}
