package com.gege.activityfindermobile.ui.myactivities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.data.repository.ActivityRepository;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.ui.adapters.ActivityAdapter;
import com.gege.activityfindermobile.utils.DateUtil;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.gege.activityfindermobile.utils.UiUtil;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MyActivitiesFragment extends Fragment {

    @Inject ActivityRepository activityRepository;

    @Inject ParticipantRepository participantRepository;

    @Inject SharedPreferencesManager prefsManager;

    @Inject com.gege.activityfindermobile.utils.CategoryManager categoryManager;

    private RecyclerView rvActivities;
    private ActivityAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private CircularProgressIndicator progressLoading;
    private View layoutEmpty;
    private MaterialButton btnCreateFirst;
    private MaterialSwitch switchShowExpired;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabCreate;
    private List<Activity> allActivities = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_activities, container, false);
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
        btnCreateFirst = view.findViewById(R.id.btn_create_first);
        switchShowExpired = view.findViewById(R.id.switch_show_expired);
        fabCreate = view.findViewById(R.id.fab_create);

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

        // Use GridLayoutManager for tablets (span count from resources)
        int spanCount = getResources().getInteger(R.integer.feed_span_count);
        rvActivities.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));

        // Restore switch state from preferences
        boolean showExpired = prefsManager.getBoolean("show_expired_activities", true);
        switchShowExpired.setChecked(showExpired);

        // Switch listener
        switchShowExpired.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    prefsManager.saveBoolean("show_expired_activities", isChecked);
                    filterActivities();
                });

        // Swipe refresh
        swipeRefresh.setOnRefreshListener(
                () -> {
                    loadMyActivities();
                });

        // Create first activity button
        btnCreateFirst.setOnClickListener(
                v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_nav_my_activities_to_createActivityFragment);
                });

        // FAB Create activity button
        fabCreate.setOnClickListener(
                v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_nav_my_activities_to_createActivityFragment);
                });

        // Load user's activities
        loadMyActivities();
    }

    private void loadMyActivities() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            UiUtil.showToast(requireContext(), "User not logged in");
            showEmptyView();
            return;
        }

        setLoading(true);

        activityRepository.getMyActivities(
                userId,
                new ApiCallback<List<Activity>>() {
                    @Override
                    public void onSuccess(List<Activity> activities) {
                        setLoading(false);
                        swipeRefresh.setRefreshing(false);

                        if (activities != null) {
                            allActivities = activities;
                            filterActivities();
                        } else {
                            allActivities = new ArrayList<>();
                            adapter.setActivities(new ArrayList<>());
                            showEmptyView();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        swipeRefresh.setRefreshing(false);
                        UiUtil.showToast(
                                requireContext(), "Failed to load activities: " + errorMessage);

                        // Show empty view on error
                        allActivities = new ArrayList<>();
                        adapter.setActivities(new ArrayList<>());
                        showEmptyView();
                    }
                });
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

    private void filterActivities() {
        if (allActivities == null || allActivities.isEmpty()) {
            adapter.setActivities(new ArrayList<>());
            showEmptyView();
            return;
        }

        boolean showExpired = switchShowExpired.isChecked();
        List<Activity> filteredActivities = new ArrayList<>();

        for (Activity activity : allActivities) {
            String status = activity.getStatus();
            String activityDateStr = activity.getActivityDate();

            // Determine if activity is in the past using DateUtil
            boolean isPast = DateUtil.isPast(activityDateStr);

            boolean isExpired =
                    isPast
                            || (status != null
                                    && (status.equalsIgnoreCase("EXPIRED")
                                            || status.equalsIgnoreCase("CANCELLED")
                                            || status.equalsIgnoreCase("COMPLETED")
                                            || status.equalsIgnoreCase("CLOSED")));

            // Show all if "include expired" is on, otherwise only show upcoming/active
            if (showExpired || !isExpired) {
                filteredActivities.add(activity);
            }
        }

        if (filteredActivities.isEmpty()) {
            adapter.setActivities(new ArrayList<>());
            showEmptyView();
        } else {
            adapter.setActivities(filteredActivities);
            showContent();
        }
    }

    private void navigateToDetail(Activity activity) {
        Bundle bundle = new Bundle();
        bundle.putLong("activityId", activity.getId() != null ? activity.getId() : 0L);
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
                activity.getCreatorName() != null ? activity.getCreatorName() : "You");
        bundle.putLong("creatorId", activity.getCreatorId() != null ? activity.getCreatorId() : 0L);
        bundle.putDouble(
                "creatorRating",
                activity.getCreatorRating() != null ? activity.getCreatorRating() : 0.0);
        bundle.putDouble("latitude", activity.getLatitude() != null ? activity.getLatitude() : 0.0);
        bundle.putDouble(
                "longitude", activity.getLongitude() != null ? activity.getLongitude() : 0.0);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_my_activities_to_activityDetailFragment, bundle);
    }
}
