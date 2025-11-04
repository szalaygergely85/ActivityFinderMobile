package com.gege.activityfindermobile.ui.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.data.repository.ActivityRepository;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.ui.adapters.ActivityAdapter;
import com.gege.activityfindermobile.utils.LocationManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FeedFragment extends Fragment {

    @Inject ActivityRepository activityRepository;

    @Inject ParticipantRepository participantRepository;

    @Inject com.gege.activityfindermobile.utils.SharedPreferencesManager prefsManager;

    private RecyclerView rvActivities;
    private ActivityAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private CircularProgressIndicator progressLoading;
    private View layoutEmpty;

    // Location variables
    private LocationManager locationManager;
    private boolean useNearbyFilter = false;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;
    private float nearbyRadiusKm = 10f;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvActivities = view.findViewById(R.id.rv_activities);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressLoading = view.findViewById(R.id.progress_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        ExtendedFloatingActionButton fabCreate = view.findViewById(R.id.fab_create);

        // Initialize location manager
        locationManager = new LocationManager(requireContext());

        // Setup adapter with ParticipantRepository for accurate counts and current user ID
        Long currentUserId = prefsManager.getUserId();
        adapter =
                new ActivityAdapter(
                        activity -> {
                            navigateToDetail(activity);
                        },
                        participantRepository,
                        currentUserId);
        rvActivities.setAdapter(adapter);

        // Swipe refresh
        swipeRefresh.setOnRefreshListener(
                () -> {
                    loadActivitiesFromApi();
                });

        // FAB click
        fabCreate.setOnClickListener(
                v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_nav_feed_to_createActivityFragment);
                });

        // Load activities from API
        loadActivitiesFromApi();
    }

    private void loadActivitiesFromApi() {
        setLoading(true);

        // If nearby filter is enabled, use location-based search
        if (useNearbyFilter && userLatitude != 0.0 && userLongitude != 0.0) {
            loadNearbyActivities();
        } else {
            // Load all upcoming activities
            activityRepository.getUpcomingActivities(
                    new ApiCallback<List<Activity>>() {
                        @Override
                        public void onSuccess(List<Activity> activities) {
                            setLoading(false);
                            swipeRefresh.setRefreshing(false);

                            if (activities != null && !activities.isEmpty()) {
                                adapter.setActivities(activities);
                                showContent();
                            } else {
                                adapter.setActivities(new ArrayList<>());
                                showEmptyView();
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            setLoading(false);
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(
                                            requireContext(),
                                            "Failed to load activities: " + errorMessage,
                                            Toast.LENGTH_SHORT)
                                    .show();

                            // Show empty view on error
                            adapter.setActivities(new ArrayList<>());
                            showEmptyView();
                        }
                    });
        }
    }

    private void loadNearbyActivities() {
        activityRepository.getNearbyActivities(
                userLatitude,
                userLongitude,
                nearbyRadiusKm,
                new ApiCallback<List<Activity>>() {
                    @Override
                    public void onSuccess(List<Activity> activities) {
                        setLoading(false);
                        swipeRefresh.setRefreshing(false);

                        if (activities != null && !activities.isEmpty()) {
                            adapter.setActivities(activities);
                            showContent();
                            Toast.makeText(
                                            requireContext(),
                                            "Found " + activities.size() + " activities nearby",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            adapter.setActivities(new ArrayList<>());
                            showEmptyView();
                            Toast.makeText(
                                            requireContext(),
                                            "No activities nearby within " + nearbyRadiusKm + "km",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load nearby activities: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();

                        adapter.setActivities(new ArrayList<>());
                        showEmptyView();
                    }
                });
    }

    /**
     * Enable nearby activities filter and request user location. Gets current device location
     * (requires location permission) to search for nearby activities. Note: User profile city is
     * stored separately for display purposes only.
     *
     * @param radiusKm Radius in kilometers for nearby search
     */
    public void enableNearbyFilter(float radiusKm) {
        nearbyRadiusKm = radiusKm;
        useNearbyFilter = true;

        // Request current location from device (location coordinates, not city)
        locationManager.getCurrentLocation(
                requireContext(),
                new LocationManager.LocationCallback() {
                    @Override
                    public void onLocationReceived(double latitude, double longitude) {
                        userLatitude = latitude;
                        userLongitude = longitude;
                        Toast.makeText(
                                        requireContext(),
                                        "Location acquired. Searching nearby activities...",
                                        Toast.LENGTH_SHORT)
                                .show();
                        loadActivitiesFromApi();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        useNearbyFilter = false;
                        Toast.makeText(
                                        requireContext(),
                                        "Unable to get location: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                        // Fall back to all activities
                        loadActivitiesFromApi();
                    }
                });
    }

    /** Disable nearby activities filter and load all activities */
    public void disableNearbyFilter() {
        useNearbyFilter = false;
        userLatitude = 0.0;
        userLongitude = 0.0;
        loadActivitiesFromApi();
    }

    private void setLoading(boolean loading) {
        if (progressLoading != null) {
            progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showContent() {
        if (rvActivities != null && layoutEmpty != null) {
            rvActivities.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmptyView() {
        if (rvActivities != null && layoutEmpty != null) {
            rvActivities.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToDetail(Activity activity) {
        Bundle bundle = new Bundle();
        bundle.putLong("activityId", activity.getId() != null ? activity.getId() : 0L);
        bundle.putLong("creatorId", activity.getCreatorId() != null ? activity.getCreatorId() : 0L);
        bundle.putString("title", activity.getTitle() != null ? activity.getTitle() : "");
        bundle.putString(
                "description", activity.getDescription() != null ? activity.getDescription() : "");
        bundle.putString("date", activity.getDate() != null ? activity.getDate() : "");
        bundle.putString("time", activity.getTime() != null ? activity.getTime() : "");
        bundle.putString("location", activity.getLocation() != null ? activity.getLocation() : "");
        bundle.putString("category", activity.getCategory() != null ? activity.getCategory() : "");
        bundle.putInt(
                "totalSpots", activity.getTotalSpots() != null ? activity.getTotalSpots() : 0);
        bundle.putInt(
                "availableSpots",
                activity.getAvailableSpots() != null ? activity.getAvailableSpots() : 0);
        bundle.putBoolean(
                "trending", activity.getTrending() != null ? activity.getTrending() : false);
        bundle.putString(
                "creatorName",
                activity.getCreatorName() != null ? activity.getCreatorName() : "Unknown");
        bundle.putString("creatorAvatar", activity.getCreatorAvatar());
        bundle.putDouble(
                "creatorRating",
                activity.getCreatorRating() != null ? activity.getCreatorRating() : 0.0);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_feed_to_activityDetailFragment, bundle);
    }
}
