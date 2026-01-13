package com.gege.activityfindermobile.ui.manage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.ui.adapters.InterestedUserAdapter;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RequestsTabFragment extends Fragment {

    @Inject ParticipantRepository participantRepository;

    @Inject SharedPreferencesManager prefsManager;

    private Long activityId;
    private RecyclerView rvRequests;
    private View layoutEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private InterestedUserAdapter adapter;
    private android.widget.TextView tvSectionTitle;

    public static RequestsTabFragment newInstance(Long activityId) {
        RequestsTabFragment fragment = new RequestsTabFragment();
        Bundle args = new Bundle();
        args.putLong("activityId", activityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_requests_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            activityId = getArguments().getLong("activityId");
        }

        rvRequests = view.findViewById(R.id.rv_requests);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        tvSectionTitle = view.findViewById(R.id.tv_section_title);

        // Setup adapter
        adapter =
                new InterestedUserAdapter(
                        new InterestedUserAdapter.OnActionListener() {
                            @Override
                            public void onAccept(Participant participant) {
                                acceptParticipant(participant);
                            }

                            @Override
                            public void onDecline(Participant participant) {
                                declineParticipant(participant);
                            }
                        });
        rvRequests.setAdapter(adapter);

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadInterestedUsers);

        // Load data
        loadInterestedUsers();
    }

    private void loadInterestedUsers() {
        Long creatorId = prefsManager.getUserId();
        if (creatorId == null || activityId == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        participantRepository.getInterestedUsers(
                activityId,
                creatorId,
                new ApiCallback<List<Participant>>() {
                    @Override
                    public void onSuccess(List<Participant> participants) {
                        swipeRefresh.setRefreshing(false);
                        if (participants != null && !participants.isEmpty()) {
                            adapter.setInterestedUsers(participants);
                            updateSectionTitle(participants.size());
                            showContent();
                        } else {
                            updateSectionTitle(0);
                            showEmpty();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load requests: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                        showEmpty();
                    }
                });
    }

    private void acceptParticipant(Participant participant) {
        Long creatorId = prefsManager.getUserId();
        if (creatorId == null) return;

        participantRepository.updateParticipantStatus(
                participant.getId(),
                creatorId,
                "ACCEPTED",
                new ApiCallback<Participant>() {
                    @Override
                    public void onSuccess(Participant updatedParticipant) {
                        Toast.makeText(
                                        requireContext(),
                                        "Accepted " + participant.getUserName(),
                                        Toast.LENGTH_SHORT)
                                .show();
                        // Remove from list
                        adapter.removeParticipant(participant.getId());
                        int newCount = adapter.getItemCount();
                        updateSectionTitle(newCount);
                        if (newCount == 0) {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to accept: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void declineParticipant(Participant participant) {
        Long creatorId = prefsManager.getUserId();
        if (creatorId == null) return;

        participantRepository.updateParticipantStatus(
                participant.getId(),
                creatorId,
                "DECLINED",
                new ApiCallback<Participant>() {
                    @Override
                    public void onSuccess(Participant updatedParticipant) {
                        Toast.makeText(
                                        requireContext(),
                                        "Declined " + participant.getUserName(),
                                        Toast.LENGTH_SHORT)
                                .show();
                        // Remove from list
                        adapter.removeParticipant(participant.getId());
                        int newCount = adapter.getItemCount();
                        updateSectionTitle(newCount);
                        if (newCount == 0) {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to decline: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void showContent() {
        rvRequests.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        rvRequests.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void updateSectionTitle(int count) {
        if (tvSectionTitle != null) {
            String title = "Pending Requests";
            if (count > 0) {
                title = "Pending Requests (" + count + ")";
            }
            tvSectionTitle.setText(title);
        }
    }

    public void refresh() {
        loadInterestedUsers();
    }
}
