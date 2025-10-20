package com.gege.activityfindermobile.ui.main;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject SharedPreferencesManager prefsManager;

    private NavController navController;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupNavigation();
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        // If user is not logged in, navigate to login screen
        if (!prefsManager.isLoggedIn()) {
            navController.navigate(R.id.loginFragment);
        }
    }

    private void setupNavigation() {
        // Get NavHostFragment
        NavHostFragment navHostFragment =
                (NavHostFragment)
                        getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        // Setup Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Hide bottom nav on certain screens
        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {
                    if (destination.getId() == R.id.loginFragment
                            || destination.getId() == R.id.registerFragment
                            || destination.getId() == R.id.profileSetupFragment
                            || destination.getId() == R.id.activityDetailFragment
                            || destination.getId() == R.id.createActivityFragment
                            || destination.getId() == R.id.userProfileFragment) {
                        bottomNavigationView.setVisibility(View.GONE);
                    } else {
                        bottomNavigationView.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
