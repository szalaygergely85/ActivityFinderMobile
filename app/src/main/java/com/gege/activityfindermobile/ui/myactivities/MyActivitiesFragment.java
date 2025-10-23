package com.gege.activityfindermobile.ui.myactivities;

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
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
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

    private RecyclerView rvActivities;
    private ActivityAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private CircularProgressIndicator progressLoading;
    private View layoutEmpty;
    private MaterialButton btnCreateFirst;

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

        rvActivities = view.findViewById(R.id.rv_activities);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressLoading = view.findViewById(R.id.progress_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        btnCreateFirst = view.findViewById(R.id.btn_create_first);

        // Setup adapter with ParticipantRepository for accurate counts
        adapter =
                new ActivityAdapter(
                        activity -> {
                            navigateToDetail(activity);
                        }, participantRepository);
        rvActivities.setAdapter(adapter);

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

        // Load user's activities
        loadMyActivities();
    }

    private void loadMyActivities() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
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
        bundle.putDouble(
                "creatorRating",
                activity.getCreatorRating() != null ? activity.getCreatorRating() : 0.0);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_my_activities_to_activityDetailFragment, bundle);
    }
}
