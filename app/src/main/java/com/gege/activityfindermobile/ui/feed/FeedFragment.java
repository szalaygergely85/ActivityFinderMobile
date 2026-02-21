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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.utils.Constants;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.data.repository.ActivityRepository;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.ui.adapters.ActivityAdapter;
import com.gege.activityfindermobile.utils.CountryDetector;
import com.gege.activityfindermobile.utils.LocationManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FeedFragment extends Fragment {

    @Inject
    ActivityRepository activityRepository;

    @Inject
    ParticipantRepository participantRepository;

    @Inject
    com.gege.activityfindermobile.utils.SharedPreferencesManager prefsManager;

    @Inject
    com.gege.activityfindermobile.utils.CategoryManager categoryManager;

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
    private float nearbyRadiusKm = Constants.DEFAULT_MAX_DISTANCE;

    // Search and filter variables
    private String currentSearchQuery = "";
    private boolean showTrendingOnly = false;
    private List<Activity> allActivities = new ArrayList<>();
    private Integer maxDistanceKm = com.gege.activityfindermobile.utils.Constants.DEFAULT_MAX_DISTANCE; // Default max
                                                                                                        // distance from
                                                                                                        // constants
    private String selectedActivityType = null; // null = all types (used by both category chips and general filter)
    private String sortBy = "datetime"; // "datetime" or "distance"

    // Permission launcher
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup permission launcher
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

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
    public void onDestroyView() {
        super.onDestroyView();
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh distance unit preference when returning from settings
        if (adapter != null) {
            boolean useKilometers = prefsManager.getBoolean("distance_unit", true);
            adapter.setUseKilometers(useKilometers);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(
                view,
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    AppBarLayout appBar = v.findViewById(R.id.app_bar);
                    if (appBar != null) {
                        appBar.setPadding(0, systemBars.top, 0, 0);
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
        FloatingActionButton fabCreate = view.findViewById(R.id.fab_create);

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
                        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext()
                                .getSystemService(
                                        android.content.Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(
                                etSearch,
                                android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                    } else {
                        searchCard.setVisibility(View.GONE);
                        etSearch.clearFocus();
                        // Hide keyboard
                        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext()
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

        // Setup adapter with ParticipantRepository for accurate counts and current user
        // ID
        Long currentUserId = prefsManager.getUserId();
        adapter = new ActivityAdapter(
                activity -> {
                    navigateToDetail(activity);
                },
                participantRepository,
                currentUserId,
                categoryManager);

        // Apply user's distance unit preference (km or miles)
        boolean useKilometers = prefsManager.getBoolean("distance_unit", true);
        adapter.setUseKilometers(useKilometers);

        rvActivities.setAdapter(adapter);

        // Use GridLayoutManager for tablets (span count from resources)
        int spanCount = getResources().getInteger(R.integer.feed_span_count);
        rvActivities.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));

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
     * Enable nearby activities filter and request user location. Gets current
     * device location
     * (requires location permission) to search for nearby activities. Note: User
     * profile city is
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
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

                        // Detect and save user's country for Places API
                        CountryDetector.detectCountry(requireContext(), latitude, longitude, null);

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
            android.util.Log.d(
                    "FeedFragment",
                    "showContent() called - RV visibility: "
                            + rvActivities.getVisibility()
                            + ", RV child count: "
                            + rvActivities.getChildCount()
                            + ", Adapter item count: "
                            + (adapter != null ? adapter.getItemCount() : "null adapter"));
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
        bundle.putDouble("latitude", activity.getLatitude() != null ? activity.getLatitude() : 0.0);
        bundle.putDouble(
                "longitude", activity.getLongitude() != null ? activity.getLongitude() : 0.0);
        bundle.putString("coverImageUrl", activity.getCoverImageUrl());

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_feed_to_activityDetailFragment, bundle);
    }

    private void setupSearchBar() {
        etSearch.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // Remove any pending callbacks
                        if (debounceRunnable != null) {
                            debounceHandler.removeCallbacks(debounceRunnable);
                        }

                        // Create new runnable for debounced search
                        debounceRunnable = () -> {
                            currentSearchQuery = s.toString().trim();
                            applyFiltersAndSearch();
                        };
                        // Wait 300ms before doing the search
                        debounceHandler.postDelayed(debounceRunnable, 300);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
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
            filtered = filtered.stream()
                    .filter(
                            activity -> activity.getTrending() != null
                                    && activity.getTrending())
                    .collect(Collectors.toList());
        }

        // Apply search query
        if (!currentSearchQuery.isEmpty()) {
            String query = currentSearchQuery.toLowerCase();
            filtered = filtered.stream()
                    .filter(
                            activity -> {
                                String title = activity.getTitle() != null
                                        ? activity.getTitle().toLowerCase()
                                        : "";
                                String description = activity.getDescription() != null
                                        ? activity.getDescription().toLowerCase()
                                        : "";
                                String location = activity.getLocation() != null
                                        ? activity.getLocation().toLowerCase()
                                        : "";
                                String category = activity.getCategory() != null
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
            filtered = filtered.stream()
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
            filtered = filtered.stream()
                    .filter(
                            activity -> selectedActivityType.equalsIgnoreCase(
                                    activity.getCategory()))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        if ("distance".equals(sortBy)) {
            filtered = filtered.stream()
                    .sorted(Comparator.comparing(
                            Activity::getDistance,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        } else {
            // Sort by datetime (date + time)
            filtered = filtered.stream()
                    .sorted(Comparator.comparing(
                            (Activity a) -> {
                                String date = a.getDate() != null ? a.getDate() : "";
                                String time = a.getTime() != null ? a.getTime() : "";
                                return date + " " + time;
                            },
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
        }

        // Update adapter
        adapter.setActivities(filtered);
        android.util.Log.d(
                "FeedFragment", "applyFiltersAndSearch: filtered size = " + filtered.size());

        if (filtered.isEmpty()) {
            showEmptyView();
        } else {
            showContent();
        }
    }

    private void showGeneralFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_general_filter, null);

        // Get dialog views
        ChipGroup chipGroupSort = dialogView.findViewById(R.id.chip_group_sort);
        ChipGroup chipGroupDistance = dialogView.findViewById(R.id.chip_group_distance);
        ChipGroup chipGroupType = dialogView.findViewById(R.id.chip_group_type);
        com.google.android.material.button.MaterialButton resetButton = dialogView.findViewById(R.id.resetButton);
        com.google.android.material.button.MaterialButton applyButton = dialogView.findViewById(R.id.applyButton);

        // Populate category chips dynamically from CategoryManager
        List<com.gege.activityfindermobile.data.model.Category> categories = categoryManager.getCachedCategories();

        // Create a map to store category chips by name for easy selection tracking
        java.util.Map<String, com.google.android.material.chip.Chip> categoryChipMap = new java.util.HashMap<>();

        for (com.gege.activityfindermobile.data.model.Category category : categories) {
            if (category.getIsActive() != null && category.getIsActive()) {
                com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) getLayoutInflater()
                        .inflate(
                                R.layout.chip_interest_item,
                                chipGroupType,
                                false);
                chip.setText(category.getName());
                chip.setCheckable(true);

                // Set icon if available
                String iconName = category.getIcon();
                if (iconName != null && !iconName.isEmpty()) {
                    int iconResId = getResources()
                            .getIdentifier(
                                    iconName, "drawable", requireContext().getPackageName());
                    if (iconResId != 0) {
                        chip.setChipIcon(getResources().getDrawable(iconResId, null));
                        chip.setChipIconTint(
                                android.content.res.ColorStateList.valueOf(
                                        getResources().getColor(R.color.white, null)));
                    }
                }

                chipGroupType.addView(chip);
                categoryChipMap.put(category.getName(), chip);
            }
        }

        // Set current sort selection
        if ("distance".equals(sortBy)) {
            chipGroupSort.check(R.id.chip_sort_distance);
        } else {
            chipGroupSort.check(R.id.chip_sort_datetime);
        }

        // Set current distance selection
        if (maxDistanceKm == 5) {
            chipGroupDistance.check(R.id.chip_distance_5);
        } else if (maxDistanceKm == 10) {
            chipGroupDistance.check(R.id.chip_distance_10);
        } else if (maxDistanceKm == 25) {
            chipGroupDistance.check(R.id.chip_distance_25);
        } else if (maxDistanceKm == 50) {
            chipGroupDistance.check(R.id.chip_distance_50);
        } else {
            // Default to "Anywhere" for 250km or any other value
            chipGroupDistance.check(R.id.chip_distance_anywhere);
        }

        // Set current category selection
        if (selectedActivityType != null && categoryChipMap.containsKey(selectedActivityType)) {
            categoryChipMap.get(selectedActivityType).setChecked(true);
        }

        // Create bottom sheet dialog
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                requireContext());
        dialog.setContentView(dialogView);

        // Reset button handler
        resetButton.setOnClickListener(
                v -> {
                    // Reset sort to datetime
                    chipGroupSort.check(R.id.chip_sort_datetime);
                    sortBy = "datetime";

                    // Reset distance to default
                    chipGroupDistance.check(R.id.chip_distance_anywhere);
                    maxDistanceKm = com.gege.activityfindermobile.utils.Constants.DEFAULT_MAX_DISTANCE;

                    // Clear category selection
                    chipGroupType.clearCheck();
                    selectedActivityType = null;

                    // Apply filters and close
                    syncCategoryChips();
                    applyFiltersAndSearch();
                    dialog.dismiss();
                });

        // Apply button handler
        applyButton.setOnClickListener(
                v -> {
                    // Get selected sort option
                    int selectedSortId = chipGroupSort.getCheckedChipId();
                    if (selectedSortId == R.id.chip_sort_distance) {
                        sortBy = "distance";
                    } else {
                        sortBy = "datetime";
                    }

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
                    } else if (selectedDistanceId == R.id.chip_distance_anywhere) {
                        maxDistanceKm = Constants.DEFAULT_MAX_DISTANCE;
                    }

                    // Get selected category
                    selectedActivityType = null;
                    for (java.util.Map.Entry<String, com.google.android.material.chip.Chip> entry : categoryChipMap
                            .entrySet()) {
                        if (entry.getValue().isChecked()) {
                            selectedActivityType = entry.getKey();
                            break;
                        }
                    }

                    // Sync the main category chips with the selected filter
                    syncCategoryChips();

                    applyFiltersAndSearch();
                    dialog.dismiss();
                });

        dialog.show();
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
