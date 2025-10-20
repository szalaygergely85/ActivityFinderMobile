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
import com.gege.activityfindermobile.ui.adapters.ParticipantAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ParticipantsTabFragment extends Fragment {

    @Inject ParticipantRepository participantRepository;

    private Long activityId;
    private RecyclerView rvParticipants;
    private View layoutEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private ParticipantAdapter adapter;

    public static ParticipantsTabFragment newInstance(Long activityId) {
        ParticipantsTabFragment fragment = new ParticipantsTabFragment();
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
        return inflater.inflate(R.layout.fragment_participants_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            activityId = getArguments().getLong("activityId");
        }

        rvParticipants = view.findViewById(R.id.rv_participants);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);

        // Setup adapter
        adapter = new ParticipantAdapter();
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

    public void refresh() {
        loadParticipants();
    }
}
