package com.gege.activityfindermobile.ui.participations;

import android.os.Bundle;
import android.util.Log;
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
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.repository.ActivityRepository;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.ui.adapters.ActivityAdapter;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ParticipationsFragment extends Fragment {

    @Inject ParticipantRepository participantRepository;

    @Inject ActivityRepository activityRepository;

    @Inject SharedPreferencesManager prefsManager;

    private RecyclerView rvParticipations;
    private ActivityAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private CircularProgressIndicator progressLoading;
    private View layoutEmpty;

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

        rvParticipations = view.findViewById(R.id.rv_participations);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressLoading = view.findViewById(R.id.progress_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        // Setup adapter
        adapter =
                new ActivityAdapter(
                        activity -> {
                            navigateToDetail(activity);
                        });
        rvParticipations.setAdapter(adapter);

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

                        if (participants == null || participants.isEmpty()) {
                            setLoading(false);
                            swipeRefresh.setRefreshing(false);
                            adapter.setActivities(new ArrayList<>());
                            showEmptyView();
                            return;
                        }

                        // Extract activities from participants (already included in response!)
                        List<Activity> activities = new ArrayList<>();
                        for (Participant participant : participants) {
                            Activity activity = participant.getActivity();
                            if (activity != null) {
                                activities.add(activity);
                            }
                        }

                        setLoading(false);
                        swipeRefresh.setRefreshing(false);

                        if (!activities.isEmpty()) {
                            adapter.setActivities(activities);
                            showContent();
                        } else {
                            showEmptyView();
                        }
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

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_participations_to_activityDetailFragment, bundle);
    }
}
