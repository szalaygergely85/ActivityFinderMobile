package com.gege.activityfindermobile.ui.feed;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FeedFragment extends Fragment {

    @Inject ActivityRepository activityRepository;

    @Inject ParticipantRepository participantRepository;

    @Inject com.gege.activityfindermobile.utils.SharedPreferencesManager prefsManager;

    @Inject com.gege.activityfindermobile.utils.CategoryManager categoryManager;

    private RecyclerView rvActivities;
    private ActivityAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private CircularProgressIndicator progressLoading;
    private View layoutEmpty;
    private TextView tvEmptyIcon;
    private TextView tvEmptyTitle;
    private TextView tvEmptyMessage;
    private com.google.android.material.button.MaterialButton btnOpenSettings;
    private com.google.android.material.textfield.TextInputEditText etSearch;
    private com.google.android.material.card.MaterialCardView searchCard;
    private com.google.android.material.button.MaterialButton btnSearch;
    private com.google.android.material.button.MaterialButton btnFilter;
    private ChipGroup chipGroupFilters;
    private android.os.Handler debounceHandler = new android.os.Handler();
    private Runnable debounceRunnable;

    // Location variables
    private LocationManager locationManager;
    private boolean useNearbyFilter = false;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;
    private float nearbyRadiusKm = 250f;

    // Search and filter variables
    private String currentSearchQuery = "";
    private boolean showTrendingOnly = false;
    private List<Activity> allActivities = new ArrayList<>();
    private Integer maxDistanceKm = 250; // Default 250 km max distance
    private String selectedActivityType = null; // null = all types (used by both category chips and general filter)

    // Permission launcher
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup permission launcher
        locationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> {
                            Boolean fineLocationGranted =
                                    result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                            Boolean coarseLocationGranted =
                                    result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                            if ((fineLocationGranted != null && fineLocationGranted)
                                    || (coarseLocationGranted != null && coarseLocationGranted)) {
                                // Permission granted, acquire location
                                acquireUserLocation();
                            } else {
                                // Permission denied
                                setLoading(false);
                                showEmptyViewForLocationPermission();
                            }
                        });
    }

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

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            AppBarLayout appBar = v.findViewById(R.id.app_bar);
            if (appBar != null) {
                appBar.setPadding(
                        0,
                        systemBars.top,
                        0,
                        0
                );
            }

            return insets;
        });

        rvActivities = view.findViewById(R.id.rv_activities);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressLoading = view.findViewById(R.id.progress_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        tvEmptyIcon = view.findViewById(R.id.tv_empty_icon);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_title);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        btnOpenSettings = view.findViewById(R.id.btn_open_settings);
        etSearch = view.findViewById(R.id.et_search);
        searchCard = view.findViewById(R.id.search_card);
        btnSearch = view.findViewById(R.id.btn_search);
        btnFilter = view.findViewById(R.id.btn_filter);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        ExtendedFloatingActionButton fabCreate = view.findViewById(R.id.fab_create);

        // Initialize location manager
        locationManager = new LocationManager(requireContext());

        // Setup open settings button
        btnOpenSettings.setOnClickListener(v -> openAppSettings());

        // Check and request location permission
        checkAndRequestLocationPermission();

        // Setup search button to toggle search card
        btnSearch.setOnClickListener(
                v -> {
                    if (searchCard.getVisibility() == View.GONE) {
                        searchCard.setVisibility(View.VISIBLE);
                        etSearch.requestFocus();
                        // Show keyboard
                        android.view.inputmethod.InputMethodManager imm =
                                (android.view.inputmethod.InputMethodManager)
                                        requireContext()
                                                .getSystemService(
                                                        android.content.Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(etSearch, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    } else {
                        searchCard.setVisibility(View.GONE);
                        etSearch.clearFocus();
                        // Hide keyboard
                        android.view.inputmethod.InputMethodManager imm =
                                (android.view.inputmethod.InputMethodManager)
                                        requireContext()
                                                .getSystemService(
                                                        android.content.Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                    }
                });

        // Setup filter button to show general filter dialog
        btnFilter.setOnClickListener(v -> showGeneralFilterDialog());

        // Setup search bar
        setupSearchBar();

        // Setup filter chips
        setupFilterChips();

        // Setup adapter with ParticipantRepository for accurate counts and current user ID
        Long currentUserId = prefsManager.getUserId();
        adapter =
                new ActivityAdapter(
                        activity -> {
                            navigateToDetail(activity);
                        },
                        participantRepository,
                        currentUserId,
                        categoryManager);
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

        // Check if location is available
        if (userLatitude != 0.0 && userLongitude != 0.0) {
            loadNearbyActivities();
        } else {
            // Wait for location to be acquired
            setLoading(false);
            swipeRefresh.setRefreshing(false);
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
                            allActivities = activities;
                            applyFiltersAndSearch();
                            showContent();
                            Toast.makeText(
                                            requireContext(),
                                            "Found " + activities.size() + " activities nearby",
                                            Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            allActivities = new ArrayList<>();
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
                        // loadActivitiesFromApi();
                    }
                });
    }

    /** Check and request location permission */
    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            acquireUserLocation();
        } else {
            // Request permission
            locationPermissionLauncher.launch(
                    new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    });
        }
    }

    /** Acquire user location on startup */
    private void acquireUserLocation() {
        locationManager.getCurrentLocation(
                requireContext(),
                new LocationManager.LocationCallback() {
                    @Override
                    public void onLocationReceived(double latitude, double longitude) {
                        userLatitude = latitude;
                        userLongitude = longitude;
                        // Load activities once location is acquired
                        loadActivitiesFromApi();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(
                                        requireContext(),
                                        "Unable to get location: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                        setLoading(false);
                        showEmptyViewForLocationPermission();
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
            swipeRefresh.setVisibility(View.VISIBLE);
            rvActivities.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
            android.util.Log.d("FeedFragment", "showContent() called - RV visibility: " + rvActivities.getVisibility() +
                    ", RV child count: " + rvActivities.getChildCount() +
                    ", Adapter item count: " + (adapter != null ? adapter.getItemCount() : "null adapter"));
        }
    }

    private void showEmptyView() {
        if (rvActivities != null && layoutEmpty != null) {
            rvActivities.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);

            // Reset to default empty state
            tvEmptyIcon.setText("🔍");
            tvEmptyTitle.setText(R.string.no_activities);
            tvEmptyMessage.setVisibility(View.GONE);
            btnOpenSettings.setVisibility(View.GONE);
        }
    }

    private void showEmptyViewForLocationPermission() {
        if (rvActivities != null && layoutEmpty != null) {
            rvActivities.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);

            // Show location permission required state
            tvEmptyIcon.setText("📍");
            tvEmptyTitle.setText(R.string.location_permission_required_title);
            tvEmptyMessage.setText(R.string.location_permission_required_message);
            tvEmptyMessage.setVisibility(View.VISIBLE);
            btnOpenSettings.setVisibility(View.VISIBLE);
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
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

    private void setupSearchBar() {
        etSearch.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // Remove any pending callbacks
                        if (debounceRunnable != null) {
                            debounceHandler.removeCallbacks(debounceRunnable);
                        }

                        // Create new runnable for debounced search
                        debounceRunnable =
                                () -> {
                                    currentSearchQuery = s.toString().trim();
                                    applyFiltersAndSearch();
                                };
                        // Wait 300ms before doing the search
                        debounceHandler.postDelayed(debounceRunnable, 300);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {}
                });
    }

    private void setupFilterChips() {
        chipGroupFilters.setOnCheckedStateChangeListener(
                (group, checkedIds) -> {
                    if (checkedIds.isEmpty()) {
                        // Reset all filters
                        selectedActivityType = null;
                        showTrendingOnly = false;
                        useNearbyFilter = false;
                        applyFiltersAndSearch();
                        return;
                    }

                    int checkedId = checkedIds.get(0);

                    if (checkedId == R.id.chip_all) {
                        selectedActivityType = null;
                        showTrendingOnly = false;
                        disableNearbyFilter();
                    } else if (checkedId == R.id.chip_trending) {
                        showTrendingOnly = true;
                        selectedActivityType = null;
                        disableNearbyFilter();
                    } else if (checkedId == R.id.chip_upcoming) {
                        showTrendingOnly = false;
                        selectedActivityType = null;
                        disableNearbyFilter();
                    } else if (checkedId == R.id.chip_sports) {
                        selectedActivityType = "Sports";
                        showTrendingOnly = false;
                        disableNearbyFilter();
                    } else if (checkedId == R.id.chip_music) {
                        selectedActivityType = "Music";
                        showTrendingOnly = false;
                        disableNearbyFilter();
                    } else if (checkedId == R.id.chip_art) {
                        selectedActivityType = "Art";
                        showTrendingOnly = false;
                        disableNearbyFilter();
                    } else if (checkedId == R.id.chip_social) {
                        selectedActivityType = "Social";
                        showTrendingOnly = false;
                        disableNearbyFilter();
                    } else if (checkedId == R.id.chip_outdoor) {
                        selectedActivityType = "Outdoor";
                        showTrendingOnly = false;
                        disableNearbyFilter();
                    } else if (checkedId == R.id.chip_food) {
                        selectedActivityType = "Food";
                        showTrendingOnly = false;
                        disableNearbyFilter();
                    } else if (checkedId == R.id.chip_nearby) {
                        selectedActivityType = null;
                        showTrendingOnly = false;
                        enableNearbyFilter(nearbyRadiusKm);
                        return; // Don't apply filters yet - wait for location
                    }

                    applyFiltersAndSearch();
                });
    }

    private void applyFiltersAndSearch() {
        List<Activity> filtered = new ArrayList<>(allActivities);

        // Apply trending filter
        if (showTrendingOnly) {
            filtered =
                    filtered.stream()
                            .filter(
                                    activity ->
                                            activity.getTrending() != null
                                                    && activity.getTrending())
                            .collect(Collectors.toList());
        }

        // Apply search query
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            filtered =
                    filtered.stream()
                            .filter(
                                    activity -> {
                                        String title =
                                                activity.getTitle() != null
                                                        ? activity.getTitle().toLowerCase()
                                                        : "";
                                        String description =
                                                activity.getDescription() != null
                                                        ? activity.getDescription().toLowerCase()
                                                        : "";
                                        String location =
                                                activity.getLocation() != null
                                                        ? activity.getLocation().toLowerCase()
                                                        : "";
                                        String category =
                                                activity.getCategory() != null
                                                        ? activity.getCategory().toLowerCase()
                                                        : "";
                                        return title.contains(query)
                                                || description.contains(query)
                                                || location.contains(query)
                                                || category.contains(query);
                                    })
                            .collect(Collectors.toList());
        }

        // Apply distance filter
        if (maxDistanceKm != null) {
            filtered =
                    filtered.stream()
                            .filter(
                                    activity -> {
                                        if (activity.getDistance() == null) {
                                            return true; // Include activities without distance info
                                        }
                                        return activity.getDistance() <= maxDistanceKm;
                                    })
                            .collect(Collectors.toList());
        }

        // Apply activity type filter
        if (selectedActivityType != null && !selectedActivityType.isEmpty()) {
            filtered =
                    filtered.stream()
                            .filter(
                                    activity ->
                                            selectedActivityType.equalsIgnoreCase(
                                                    activity.getCategory()))
                            .collect(Collectors.toList());
        }

        // Update adapter
        adapter.setActivities(filtered);
        android.util.Log.d("FeedFragment", "applyFiltersAndSearch: filtered size = " + filtered.size());

        if (filtered.isEmpty()) {
            showEmptyView();
        } else {
            showContent();
        }
    }

    private void showGeneralFilterDialog() {
        View dialogView =
                getLayoutInflater().inflate(R.layout.dialog_general_filter, null);

        // Get dialog views
        ChipGroup chipGroupDistance = dialogView.findViewById(R.id.chip_group_distance);
        ChipGroup chipGroupType = dialogView.findViewById(R.id.chip_group_type);

        // Set current selections
        if (maxDistanceKm == 5) {
            chipGroupDistance.check(R.id.chip_distance_5);
        } else if (maxDistanceKm == 10) {
            chipGroupDistance.check(R.id.chip_distance_10);
        } else if (maxDistanceKm == 25) {
            chipGroupDistance.check(R.id.chip_distance_25);
        } else if (maxDistanceKm == 50) {
            chipGroupDistance.check(R.id.chip_distance_50);
        } else if (maxDistanceKm == 100) {
            chipGroupDistance.check(R.id.chip_distance_100);
        } else {
            chipGroupDistance.check(R.id.chip_distance_250);
        }

        if (selectedActivityType == null) {
            chipGroupType.check(R.id.chip_type_all);
        } else {
            switch (selectedActivityType) {
                case "Sports":
                    chipGroupType.check(R.id.chip_type_sports);
                    break;
                case "Social":
                    chipGroupType.check(R.id.chip_type_social);
                    break;
                case "Outdoor":
                    chipGroupType.check(R.id.chip_type_outdoor);
                    break;
                case "Food":
                    chipGroupType.check(R.id.chip_type_food);
                    break;
                case "Travel":
                    chipGroupType.check(R.id.chip_type_travel);
                    break;
                case "Photography":
                    chipGroupType.check(R.id.chip_type_photography);
                    break;
                case "Music":
                    chipGroupType.check(R.id.chip_type_music);
                    break;
                case "Art":
                    chipGroupType.check(R.id.chip_type_art);
                    break;
                case "Gaming":
                    chipGroupType.check(R.id.chip_type_gaming);
                    break;
                case "Fitness":
                    chipGroupType.check(R.id.chip_type_fitness);
                    break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Filters")
                .setView(dialogView)
                .setPositiveButton(
                        "Apply",
                        (dialog, which) -> {
                            // Get selected distance
                            int selectedDistanceId = chipGroupDistance.getCheckedChipId();
                            if (selectedDistanceId == R.id.chip_distance_5) {
                                maxDistanceKm = 5;
                            } else if (selectedDistanceId == R.id.chip_distance_10) {
                                maxDistanceKm = 10;
                            } else if (selectedDistanceId == R.id.chip_distance_25) {
                                maxDistanceKm = 25;
                            } else if (selectedDistanceId == R.id.chip_distance_50) {
                                maxDistanceKm = 50;
                            } else if (selectedDistanceId == R.id.chip_distance_100) {
                                maxDistanceKm = 100;
                            } else if (selectedDistanceId == R.id.chip_distance_250) {
                                maxDistanceKm = 250;
                            }

                            // Get selected activity type
                            int selectedTypeId = chipGroupType.getCheckedChipId();
                            if (selectedTypeId == R.id.chip_type_all) {
                                selectedActivityType = null;
                            } else if (selectedTypeId == R.id.chip_type_sports) {
                                selectedActivityType = "Sports";
                            } else if (selectedTypeId == R.id.chip_type_social) {
                                selectedActivityType = "Social";
                            } else if (selectedTypeId == R.id.chip_type_outdoor) {
                                selectedActivityType = "Outdoor";
                            } else if (selectedTypeId == R.id.chip_type_food) {
                                selectedActivityType = "Food";
                            } else if (selectedTypeId == R.id.chip_type_travel) {
                                selectedActivityType = "Travel";
                            } else if (selectedTypeId == R.id.chip_type_photography) {
                                selectedActivityType = "Photography";
                            } else if (selectedTypeId == R.id.chip_type_music) {
                                selectedActivityType = "Music";
                            } else if (selectedTypeId == R.id.chip_type_art) {
                                selectedActivityType = "Art";
                            } else if (selectedTypeId == R.id.chip_type_gaming) {
                                selectedActivityType = "Gaming";
                            } else if (selectedTypeId == R.id.chip_type_fitness) {
                                selectedActivityType = "Fitness";
                            }

                            // Sync the main category chips with the selected filter
                            syncCategoryChips();

                            applyFiltersAndSearch();
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void syncCategoryChips() {
        // Update the main category chips to match the selectedActivityType
        if (selectedActivityType == null) {
            chipGroupFilters.check(R.id.chip_all);
        } else {
            switch (selectedActivityType) {
                case "Sports":
                    chipGroupFilters.check(R.id.chip_sports);
                    break;
                case "Music":
                    chipGroupFilters.check(R.id.chip_music);
                    break;
                case "Art":
                    chipGroupFilters.check(R.id.chip_art);
                    break;
                case "Social":
                    chipGroupFilters.check(R.id.chip_social);
                    break;
                case "Outdoor":
                    chipGroupFilters.check(R.id.chip_outdoor);
                    break;
                case "Food":
                    chipGroupFilters.check(R.id.chip_food);
                    break;
                default:
                    // For types not in main chips (Travel, Photography, Gaming, Fitness)
                    // Keep current selection or default to All
                    chipGroupFilters.clearCheck();
                    break;
            }
        }
    }
}
