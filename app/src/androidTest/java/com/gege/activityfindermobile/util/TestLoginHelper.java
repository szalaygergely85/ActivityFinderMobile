package com.gege.activityfindermobile.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.navigation.fragment.NavHostFragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.ui.main.MainActivity;
import com.gege.activityfindermobile.utils.Constants;

/**
 * Programmatic login helper for instrumentation tests.
 *
 * Instead of driving the login UI with Espresso (slow, flaky), this helper:
 *   1. Calls the API directly via TestApiHelper to obtain a real JWT token.
 *   2. Writes the session into SharedPreferences exactly as the app's own
 *      SharedPreferencesManager.saveUserSession() does.
 *   3. Navigates directly to the feed via NavController — no activity recreate needed.
 *
 * Usage in a test class:
 *
 *   // @Before
 *   UiTestHelper.clearAppSharedPreferences();
 *   LoginResponse session = TestLoginHelper.loginAndOpenFeed(
 *           activityRule.getScenario(), apiHelper, email, password);
 *   testUserId = session.getUserId();
 *
 *   // @After
 *   TestLoginHelper.clearSession();
 *   apiHelper.deleteUser(testUserId);
 */
public class TestLoginHelper {

    /**
     * Login via API, persist the session to SharedPreferences, then navigate
     * directly to the feed via NavController (no activity recreate required).
     *
     * @param scenario  The ActivityScenario from your ActivityScenarioRule.
     * @param apiHelper A TestApiHelper instance (already constructed).
     * @param email     Credentials to log in with.
     * @param password  Credentials to log in with.
     * @return The LoginResponse from the server (contains userId, tokens, etc.).
     * @throws RuntimeException if the API login fails.
     */
    public static LoginResponse loginAndOpenFeed(
            ActivityScenario<MainActivity> scenario,
            TestApiHelper apiHelper,
            String email,
            String password) {

        LoginResponse response = apiHelper.loginWithRetry(email, password);
        if (response == null) {
            throw new RuntimeException(
                    "Programmatic login failed for: " + email
                            + " — check server connectivity and credentials");
        }

        writeSessionToPrefs(
                response.getUserId(),
                response.getAccessToken(),
                response.getRefreshToken(),
                response.getEmail());

        // Navigate directly to nav_feed via NavController on the main thread.
        // This pops loginFragment off the back stack (if present) and shows the feed,
        // avoiding any dependency on checkLoginStatus() re-running via recreate().
        scenario.onActivity(activity -> {
            NavHostFragment nhf = (NavHostFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (nhf != null) {
                // Pop back to nav_feed (the start destination), removing loginFragment
                boolean popped = nhf.getNavController().popBackStack(R.id.nav_feed, false);
                if (!popped) {
                    // Already at nav_feed, or nav_feed not in back stack — navigate there
                    nhf.getNavController().navigate(R.id.nav_feed);
                }
            }
            // Ensure bottom nav is visible (it's hidden on loginFragment)
            View bottomNav = activity.findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        });

        return response;
    }

    /**
     * Clear all session-related keys from SharedPreferences.
     * Call this in @After to leave the device in a clean state.
     */
    public static void clearSession() {
        getPrefs()
                .edit()
                .remove(Constants.KEY_USER_ID)
                .remove(Constants.KEY_USER_TOKEN)
                .remove(Constants.KEY_REFRESH_TOKEN)
                .remove(Constants.KEY_USER_EMAIL)
                .putBoolean(Constants.KEY_IS_LOGGED_IN, false)
                .commit();
    }

    // -------------------------------------------------------------------------

    /**
     * Write a session into SharedPreferences synchronously.
     * Mirrors SharedPreferencesManager.saveUserSession() exactly so the app
     * code reads the values correctly.
     */
    private static void writeSessionToPrefs(
            Long userId, String accessToken, String refreshToken, String email) {
        getPrefs()
                .edit()
                .putLong(Constants.KEY_USER_ID, userId)
                .putString(Constants.KEY_USER_TOKEN, accessToken != null ? accessToken : "")
                .putString(Constants.KEY_REFRESH_TOKEN, refreshToken != null ? refreshToken : "")
                .putString(Constants.KEY_USER_EMAIL, email != null ? email : "")
                .putBoolean(Constants.KEY_IS_LOGGED_IN, true)
                .commit();
    }

    private static SharedPreferences getPrefs() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }
}
