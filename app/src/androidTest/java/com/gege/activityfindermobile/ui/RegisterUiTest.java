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

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

/**
 * Functional UI tests for the Registration screen.
 * Tests user interface interactions and form validation behavior.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegisterUiTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    );

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void navigateToRegister() {
        // Wait for app to initialize
        waitFor(1000);

        // Navigate from login to register screen
        onView(withId(R.id.tv_sign_up)).perform(scrollTo(), click());
        waitFor(1000);

        // Verify we're on the register screen
        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
    }

    // ==================== UI ELEMENT VISIBILITY TESTS ====================

    @Test
    public void registerScreen_allElementsDisplayed() {
        onView(withId(R.id.et_full_name)).check(matches(isDisplayed()));
        onView(withId(R.id.et_email)).check(matches(isDisplayed()));
        onView(withId(R.id.et_birth_date)).check(matches(isDisplayed()));
        onView(withId(R.id.et_password)).check(matches(isDisplayed()));
        onView(withId(R.id.et_confirm_password)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
    }

    @Test
    public void registerScreen_signUpButtonEnabled() {
        onView(withId(R.id.btn_register)).check(matches(isEnabled()));
    }

    @Test
    public void registerScreen_createAccountTitleDisplayed() {
        onView(withText("Create Account")).check(matches(isDisplayed()));
    }

    @Test
    public void registerScreen_ageHelperTextDisplayed() {
        // Helper text "You must be 18 or older" should be visible
        onView(withText("You must be 18 or older")).check(matches(isDisplayed()));
    }

    @Test
    public void registerScreen_passwordHelperTextDisplayed() {
        // Helper text "Minimum 6 characters" should be visible
        onView(withText("Minimum 6 characters")).check(matches(isDisplayed()));
    }

    // ==================== INPUT VALIDATION TESTS ====================

    @Test
    public void register_withEmptyFullName_showsError() {
        // Leave full name empty, fill other fields
        onView(withId(R.id.et_full_name))
                .perform(scrollTo(), replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText("password123"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
                .perform(scrollTo(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.btn_register))
                .perform(scrollTo(), click());

        waitFor(500);

        // Should still be on register screen
        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
    }

    @Test
    public void register_withEmptyEmail_showsError() {
        onView(withId(R.id.et_full_name))
                .perform(scrollTo(), replaceText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText("password123"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
                .perform(scrollTo(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.btn_register))
                .perform(scrollTo(), click());

        waitFor(500);

        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
    }

    @Test
    public void register_withInvalidEmail_showsError() {
        onView(withId(R.id.et_full_name))
                .perform(scrollTo(), replaceText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText("not-an-email"), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText("password123"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
                .perform(scrollTo(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.btn_register))
                .perform(scrollTo(), click());

        waitFor(500);

        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
    }

    @Test
    public void register_withShortPassword_showsError() {
        onView(withId(R.id.et_full_name))
                .perform(scrollTo(), replaceText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText("123"), closeSoftKeyboard()); // Too short
        onView(withId(R.id.et_confirm_password))
                .perform(scrollTo(), replaceText("123"), closeSoftKeyboard());

        onView(withId(R.id.btn_register))
                .perform(scrollTo(), click());

        waitFor(500);

        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
    }

    @Test
    public void register_withMismatchedPasswords_showsError() {
        onView(withId(R.id.et_full_name))
                .perform(scrollTo(), replaceText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText("password123"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
                .perform(scrollTo(), replaceText("differentpassword"), closeSoftKeyboard());

        onView(withId(R.id.btn_register))
                .perform(scrollTo(), click());

        waitFor(500);

        // Should show error for mismatched passwords
        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
    }

    @Test
    public void register_withEmptyBirthDate_showsError() {
        onView(withId(R.id.et_full_name))
                .perform(scrollTo(), replaceText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText("test@example.com"), closeSoftKeyboard());
        // Don't select birth date
        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText("password123"), closeSoftKeyboard());
        onView(withId(R.id.et_confirm_password))
                .perform(scrollTo(), replaceText("password123"), closeSoftKeyboard());

        onView(withId(R.id.btn_register))
                .perform(scrollTo(), click());

        waitFor(500);

        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
    }

    // ==================== TEXT INPUT TESTS ====================

    @Test
    public void register_fullNameFieldAcceptsInput() {
        String testName = "John Doe";

        onView(withId(R.id.et_full_name))
                .perform(scrollTo(), replaceText(testName), closeSoftKeyboard());

        onView(withId(R.id.et_full_name))
                .check(matches(withText(testName)));
    }

    @Test
    public void register_emailFieldAcceptsInput() {
        String testEmail = "john.doe@example.com";

        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(testEmail), closeSoftKeyboard());

        onView(withId(R.id.et_email))
                .check(matches(withText(testEmail)));
    }

    @Test
    public void register_passwordFieldAcceptsInput() {
        String testPassword = "SecurePass123";

        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());

        onView(withId(R.id.et_password))
                .check(matches(withText(testPassword)));
    }

    @Test
    public void register_confirmPasswordFieldAcceptsInput() {
        String testPassword = "SecurePass123";

        onView(withId(R.id.et_confirm_password))
                .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());

        onView(withId(R.id.et_confirm_password))
                .check(matches(withText(testPassword)));
    }

    // ==================== DATE PICKER TESTS ====================

    @Test
    public void register_birthDateFieldIsClickable() {
        // Birth date field should be clickable to open date picker
        onView(withId(R.id.et_birth_date))
                .perform(scrollTo(), click());

        waitFor(500);

        // Date picker dialog should appear (check for any visible dialog element)
        // The specific assertion depends on the date picker implementation
    }

    // ==================== NAVIGATION TESTS ====================

    @Test
    public void register_signInLinkNavigatesToLogin() {
        // Click on "Sign in" link
        onView(withId(R.id.tv_sign_in))
                .perform(scrollTo(), click());

        waitFor(1000);

        // Verify login screen is shown
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
    }

    // ==================== LOADING STATE TESTS ====================

    @Test
    public void register_progressIndicatorInitiallyHidden() {
        onView(withId(R.id.progress_loading))
                .check(matches(not(isDisplayed())));
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
