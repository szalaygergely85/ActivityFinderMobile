package com.gege.activityfindermobile.ui.manage;

import android.app.AlertDialog;
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
import com.gege.activityfindermobile.ui.adapters.ParticipantAdapter;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ParticipantsTabFragment extends Fragment {

    @Inject ParticipantRepository participantRepository;

    @Inject SharedPreferencesManager prefsManager;
    private Long activityId;
    private Long creatorId;
    private RecyclerView rvParticipants;
    private View layoutEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private ParticipantAdapter adapter;

    public static ParticipantsTabFragment newInstance(Long activityId, Long creatorId) {
        ParticipantsTabFragment fragment = new ParticipantsTabFragment();
        Bundle args = new Bundle();
        args.putLong("activityId", activityId);
        args.putLong("creatorId", creatorId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_participants_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            activityId = getArguments().getLong("activityId");
            creatorId = getArguments().getLong("creatorId");
        }

        rvParticipants = view.findViewById(R.id.rv_participants);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        // Setup adapter with remove listener
        adapter = new ParticipantAdapter(ParticipantAdapter.Owner.ParticipantsTabFragment);
        adapter.setRemoveListener(
                new ParticipantAdapter.OnRemoveClickListener() {
                    @Override
                    public void onRemoveClick(Participant participant) {
                        removeParticipant(participant);
                    }
                });
        rvParticipants.setAdapter(adapter);

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadParticipants);

        // Load data
        loadParticipants();
    }

    private void loadParticipants() {
        if (activityId == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        participantRepository.getActivityParticipants(
                activityId,
                new ApiCallback<List<Participant>>() {
                    @Override
                    public void onSuccess(List<Participant> participants) {
                        swipeRefresh.setRefreshing(false);

                        // Filter to show only ACCEPTED and JOINED participants
                        List<Participant> confirmedParticipants = new ArrayList<>();
                        if (participants != null) {
                            for (Participant p : participants) {
                                String status = p.getStatus();
                                if ("ACCEPTED".equals(status) || "JOINED".equals(status)) {
                                    confirmedParticipants.add(p);
                                }
                            }
                        }

                        if (!confirmedParticipants.isEmpty()) {
                            adapter.setParticipants(confirmedParticipants);
                            adapter.setCurrentUserId(prefsManager.getUserId());
                            adapter.setCreatorId(creatorId);
                            showContent();
                        } else {
                            showEmpty();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load participants: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                        showEmpty();
                    }
                });
    }

    private void showContent() {
        rvParticipants.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        rvParticipants.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void removeParticipant(Participant participant) {
        Long creatorId = prefsManager.getUserId();
        if (creatorId == null) return;

        // Show confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Remove Participant")
                .setMessage(
                        "Are you sure you want to remove "
                                + participant.getUserName()
                                + " from this activity?")
                .setPositiveButton(
                        "Remove",
                        (dialog, which) -> {
                            participantRepository.updateParticipantStatus(
                                    participant.getId(),
                                    creatorId,
                                    "REMOVED",
                                    new ApiCallback<Participant>() {
                                        @Override
                                        public void onSuccess(Participant updatedParticipant) {
                                            Toast.makeText(
                                                            requireContext(),
                                                            "Removed " + participant.getUserName(),
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                            // Remove from list
                                            adapter.removeParticipant(participant.getId());
                                            if (adapter.getItemCount() == 0) {
                                                showEmpty();
                                            }
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            Toast.makeText(
                                                            requireContext(),
                                                            "Failed to remove: " + errorMessage,
                                                            Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    });
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void refresh() {
        loadParticipants();
    }
}
