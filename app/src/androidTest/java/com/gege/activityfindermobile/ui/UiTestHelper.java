package com.gege.activityfindermobile.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import com.gege.activityfindermobile.R;

import org.hamcrest.Matcher;

/**
 * Helper class providing common operations for UI tests.
 * Reduces code duplication across test classes.
 */
public class UiTestHelper {

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
     */
    public static void loginViaUi(String email, String password) {
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.btn_login))
                .perform(scrollTo(), click());
        waitFor(3000);
        // Verify login succeeded
        verifyFeedScreenDisplayed();
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
}
