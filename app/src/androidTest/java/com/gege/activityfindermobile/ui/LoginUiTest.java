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

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Functional UI tests for the Login screen.
 * These tests do NOT require API/backend - they only test UI elements.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginUiTest {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
    );

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void loginScreen_emailFieldDisplayed() {
        waitFor(1000);
        onView(withId(R.id.et_email)).check(matches(isDisplayed()));
    }

    @Test
    public void loginScreen_passwordFieldDisplayed() {
        waitFor(1000);
        onView(withId(R.id.et_password)).check(matches(isDisplayed()));
    }

    @Test
    public void loginScreen_loginButtonDisplayed() {
        waitFor(1000);
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
    }

    @Test
    public void loginScreen_loginButtonEnabled() {
        waitFor(1000);
        onView(withId(R.id.btn_login)).check(matches(isEnabled()));
    }

    @Test
    public void loginScreen_signUpLinkDisplayed() {
        waitFor(1000);
        onView(withId(R.id.tv_sign_up)).check(matches(isDisplayed()));
    }

    @Test
    public void loginScreen_welcomeTextDisplayed() {
        waitFor(1000);
        onView(withText("Welcome Back")).check(matches(isDisplayed()));
    }

    @Test
    public void login_emailFieldAcceptsInput() {
        waitFor(1000);
        String testEmail = "testuser@example.com";

        onView(withId(R.id.et_email))
                .perform(scrollTo(), replaceText(testEmail), closeSoftKeyboard());

        onView(withId(R.id.et_email))
                .check(matches(withText(testEmail)));
    }

    @Test
    public void login_passwordFieldAcceptsInput() {
        waitFor(1000);
        String testPassword = "SecurePassword123";

        onView(withId(R.id.et_password))
                .perform(scrollTo(), replaceText(testPassword), closeSoftKeyboard());

        onView(withId(R.id.et_password))
                .check(matches(withText(testPassword)));
    }

    @Test
    public void login_signUpLinkNavigatesToRegister() {
        waitFor(1000);
        onView(withId(R.id.tv_sign_up))
                .perform(scrollTo(), click());

        waitFor(1000);

        // Verify registration screen is shown
        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
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
