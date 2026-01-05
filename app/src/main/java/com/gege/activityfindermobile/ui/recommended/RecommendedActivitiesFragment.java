package com.gege.activityfindermobile.ui.recommended;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.data.repository.ActivityRepository;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.ui.adapters.ActivityAdapter;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/** Fragment for displaying recommended activities based on user's interests */
@AndroidEntryPoint
public class RecommendedActivitiesFragment extends Fragment {

    @Inject ActivityRepository activityRepository;

    @Inject ParticipantRepository participantRepository;

    @Inject SharedPreferencesManager prefsManager;

    @Inject com.gege.activityfindermobile.utils.CategoryManager categoryManager;

    private ActivityAdapter activityAdapter;
    private RecyclerView recyclerViewActivities;
    private ProgressBar progressBar;
    private TextView textViewEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabCreateActivity;

    @Inject
    public RecommendedActivitiesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommended_activities, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get repository from Hilt

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFAB();
        loadRecommendedActivities();
    }

    private void initializeViews(View view) {
        recyclerViewActivities = view.findViewById(R.id.recycler_view_recommended);
        progressBar = view.findViewById(R.id.progress_bar_recommended);
        textViewEmptyState = view.findViewById(R.id.text_view_empty_state_recommended);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_recommended);
        fabCreateActivity = view.findViewById(R.id.fab_create_activity_recommended);
    }

    private void setupRecyclerView() {
        recyclerViewActivities.setLayoutManager(new LinearLayoutManager(getContext()));
        Long currentUserId = prefsManager.getUserId();
        activityAdapter =
                new ActivityAdapter(
                        activity -> navigateToActivityDetail(activity),
                        participantRepository,
                        currentUserId,
                        categoryManager);
        recyclerViewActivities.setAdapter(activityAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    loadRecommendedActivities();
                });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.accent);
    }

    private void setupFAB() {
        fabCreateActivity.setOnClickListener(
                v -> {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_nav_recommended_to_createActivity);
                });
    }

    private void loadRecommendedActivities() {
        progressBar.setVisibility(View.VISIBLE);
        textViewEmptyState.setVisibility(View.GONE);

        activityRepository.getRecommendedActivities(
                new ApiCallback<List<Activity>>() {
                    @Override
                    public void onSuccess(List<Activity> activities) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);

                        if (activities == null || activities.isEmpty()) {
                            showEmptyState();
                        } else {
                            textViewEmptyState.setVisibility(View.GONE);
                            activityAdapter.setActivities(activities);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        showErrorState(error);
                    }
                });
    }

    private void showEmptyState() {
        textViewEmptyState.setVisibility(View.VISIBLE);
        textViewEmptyState.setText(R.string.no_recommended_activities);
        activityAdapter.setActivities(null);
    }

    private void showErrorState(String error) {
        textViewEmptyState.setVisibility(View.VISIBLE);
        textViewEmptyState.setText("Error: " + error);
        activityAdapter.setActivities(null);
    }

    private void navigateToActivityDetail(Activity activity) {
        Bundle bundle = new Bundle();
        bundle.putLong("activityId", activity.getId());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_nav_recommended_to_activityDetail, bundle);
    }
}
