package com.gege.activityfindermobile.ui.participations;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.repository.ActivityRepository;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.ui.adapters.ActivityAdapter;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ParticipationsFragment extends Fragment {

    @Inject ParticipantRepository participantRepository;

    @Inject ActivityRepository activityRepository;

    @Inject SharedPreferencesManager prefsManager;

    @Inject com.gege.activityfindermobile.utils.CategoryManager categoryManager;

    private RecyclerView rvParticipations;
    private ActivityAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private CircularProgressIndicator progressLoading;
    private View layoutEmpty;
    private TabLayout tabLayout;

    private List<Participant> allParticipations = new ArrayList<>();
    private static final int TAB_JOINED = 0;
    private static final int TAB_INTERESTED = 1;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_participations, container, false);
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

        rvParticipations = view.findViewById(R.id.rv_participations);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressLoading = view.findViewById(R.id.progress_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        tabLayout = view.findViewById(R.id.tab_layout);

        // Setup back button
        View btnBack = view.findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                NavController navController = Navigation.findNavController(requireView());
                navController.navigateUp();
            });
        }

        // Setup tabs
        tabLayout.addTab(tabLayout.newTab().setText("Joined"));
        tabLayout.addTab(tabLayout.newTab().setText("Interested"));

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
        rvParticipations.setAdapter(adapter);

        // Tab selection listener
        tabLayout.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        filterActivitiesByTab(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

        // Swipe refresh
        swipeRefresh.setOnRefreshListener(
                () -> {
                    loadParticipations();
                });

        // Load joined activities
        loadParticipations();
    }

    private void loadParticipations() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            showEmptyView();
            return;
        }

        setLoading(true);

        // First get the list of participations
        participantRepository.getMyParticipations(
                userId,
                new ApiCallback<List<Participant>>() {
                    @Override
                    public void onSuccess(List<Participant> participants) {
                        Log.d(
                                "Participations",
                                "Fetched "
                                        + (participants != null ? participants.size() : 0)
                                        + " participations");

                        setLoading(false);
                        swipeRefresh.setRefreshing(false);

                        if (participants == null || participants.isEmpty()) {
                            allParticipations = new ArrayList<>();
                            adapter.setActivities(new ArrayList<>());
                            showEmptyView();
                            return;
                        }

                        // Store all participations
                        allParticipations = participants;

                        // Filter based on selected tab
                        int selectedTab =
                                tabLayout != null ? tabLayout.getSelectedTabPosition() : TAB_JOINED;
                        filterActivitiesByTab(selectedTab);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load participations: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();

                        // Show empty view on error
                        adapter.setActivities(new ArrayList<>());
                        showEmptyView();
                    }
                });
    }

    private void filterActivitiesByTab(int tabPosition) {
        List<Activity> filteredActivities = new ArrayList<>();

        for (Participant participant : allParticipations) {
            if (participant.getActivity() == null) {
                continue;
            }

            String status = participant.getStatus();

            if (tabPosition == TAB_JOINED) {
                // Show ACCEPTED and JOINED activities
                if ("ACCEPTED".equals(status) || "JOINED".equals(status)) {
                    filteredActivities.add(participant.getActivity());
                }
            } else if (tabPosition == TAB_INTERESTED) {
                // Show PENDING and INTERESTED activities
                if ("PENDING".equals(status) || "INTERESTED".equals(status)) {
                    filteredActivities.add(participant.getActivity());
                }
            }
        }

        if (!filteredActivities.isEmpty()) {
            adapter.setActivities(filteredActivities);
            showContent();
        } else {
            adapter.setActivities(new ArrayList<>());
            showEmptyView();
        }
    }

    private void setLoading(boolean loading) {
        if (progressLoading != null) {
            progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void showContent() {
        if (rvParticipations != null && layoutEmpty != null) {
            rvParticipations.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private void showEmptyView() {
        if (rvParticipations != null && layoutEmpty != null) {
            rvParticipations.setVisibility(View.GONE);
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
        bundle.putDouble(
                "creatorRating",
                activity.getCreatorRating() != null ? activity.getCreatorRating() : 0.0);
        bundle.putDouble("latitude", activity.getLatitude() != null ? activity.getLatitude() : 0.0);
        bundle.putDouble(
                "longitude", activity.getLongitude() != null ? activity.getLongitude() : 0.0);
        bundle.putString("coverImageUrl", activity.getCoverImageUrl());

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_participations_to_activityDetailFragment, bundle);
    }
}
