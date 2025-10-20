package com.gege.activityfindermobile.ui.detail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.ExpressInterestRequest;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.ui.adapters.ParticipantAdapter;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivityDetailFragment extends Fragment {

    @Inject ParticipantRepository participantRepository;

    @Inject SharedPreferencesManager prefsManager;

    private Long activityId;
    private Long creatorId;
    private MaterialButton btnExpressInterest;
    private MaterialButton btnManage;
    private CircularProgressIndicator progressLoading;
    private RecyclerView rvParticipants;
    private TextView tvNoParticipants;
    private ParticipantAdapter participantAdapter;
    private View cardCreator;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_detail, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload participants when returning to this screen
        loadParticipants();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        btnExpressInterest = view.findViewById(R.id.btn_express_interest);
        btnManage = view.findViewById(R.id.btn_manage);
        progressLoading = view.findViewById(R.id.progress_loading);
        rvParticipants = view.findViewById(R.id.rv_participants);
        tvNoParticipants = view.findViewById(R.id.tv_no_participants);
        cardCreator = view.findViewById(R.id.card_creator);

        // Setup participant adapter with click listener
        participantAdapter =
                new ParticipantAdapter(
                        participant -> {
                            navigateToUserProfile(participant.getUserId());
                        });
        rvParticipants.setAdapter(participantAdapter);

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            activityId = args.getLong("activityId", 0L);
            creatorId = args.getLong("creatorId", 0L);
            displayActivityData(view, args);
        } else {
            // Fallback to test data if no arguments
            displayTestData(view);
        }

        // Setup join button
        btnExpressInterest.setOnClickListener(v -> expressInterest());

        // Setup manage button
        btnManage.setOnClickListener(v -> navigateToManageActivity());

        // Setup creator card click
        cardCreator.setOnClickListener(v -> navigateToUserProfile(creatorId));

        // Check if current user is the creator
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId != null && currentUserId.equals(creatorId)) {
            // Show manage button for creator
            btnManage.setVisibility(View.VISIBLE);
            // Hide join button for creator
            btnExpressInterest.setVisibility(View.GONE);
        } else {
            // Check if user has already joined this activity
            checkUserParticipationStatus();
        }

        // Load participants from API
        loadParticipants();
    }

    private void displayActivityData(View view, Bundle args) {
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDescription = view.findViewById(R.id.tv_description);
        TextView tvDate = view.findViewById(R.id.tv_date);
        TextView tvTime = view.findViewById(R.id.tv_time);
        TextView tvLocation = view.findViewById(R.id.tv_location);
        TextView tvSpots = view.findViewById(R.id.tv_spots);
        TextView tvCreatorName = view.findViewById(R.id.tv_creator_name);
        TextView tvCreatorRating = view.findViewById(R.id.tv_creator_rating);
        Chip chipCategory = view.findViewById(R.id.chip_category);
        Chip badgeTrending = view.findViewById(R.id.badge_trending);

        tvTitle.setText(args.getString("title", ""));
        tvDescription.setText(args.getString("description", ""));
        tvDate.setText(args.getString("date", ""));
        tvTime.setText(args.getString("time", ""));
        tvLocation.setText(args.getString("location", ""));

        int availableSpots = args.getInt("availableSpots", 0);
        int totalSpots = args.getInt("totalSpots", 0);
        int currentParticipants = totalSpots - availableSpots;
        tvSpots.setText(currentParticipants + " / " + totalSpots + " joined");

        tvCreatorName.setText(args.getString("creatorName", ""));
        double rating = args.getDouble("creatorRating", 0.0);
        tvCreatorRating.setText(String.format("%.1f", rating) + " (15 reviews)");

        chipCategory.setText(args.getString("category", ""));
        badgeTrending.setVisibility(args.getBoolean("trending", false) ? View.VISIBLE : View.GONE);
    }

    private void displayTestData(View view) {
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDescription = view.findViewById(R.id.tv_description);
        TextView tvDate = view.findViewById(R.id.tv_date);
        TextView tvTime = view.findViewById(R.id.tv_time);
        TextView tvLocation = view.findViewById(R.id.tv_location);
        TextView tvSpots = view.findViewById(R.id.tv_spots);
        TextView tvCreatorName = view.findViewById(R.id.tv_creator_name);
        TextView tvCreatorRating = view.findViewById(R.id.tv_creator_rating);
        Chip chipCategory = view.findViewById(R.id.chip_category);
        Chip badgeTrending = view.findViewById(R.id.badge_trending);

        tvTitle.setText("Weekend Hiking Adventure");
        tvDescription.setText(
                "Join us for an amazing hiking experience in the beautiful mountains. We'll explore"
                    + " scenic trails, enjoy breathtaking views, and connect with nature. Perfect"
                    + " for outdoor enthusiasts!");
        tvDate.setText("November 15, 2025");
        tvTime.setText("08:00 AM");
        tvLocation.setText("Mountain View Park");
        tvSpots.setText("5 of 10 available");
        tvCreatorName.setText("Sarah Johnson");
        tvCreatorRating.setText("4.8 (15 reviews)");
        chipCategory.setText("Sports");
        badgeTrending.setVisibility(View.VISIBLE);
    }

    private void loadParticipants() {
        if (activityId == null || activityId == 0L) {
            return;
        }

        participantRepository.getActivityParticipants(
                activityId,
                new ApiCallback<List<Participant>>() {
                    @Override
                    public void onSuccess(List<Participant> participants) {
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
                            participantAdapter.setParticipants(confirmedParticipants);
                            rvParticipants.setVisibility(View.VISIBLE);
                            tvNoParticipants.setVisibility(View.GONE);
                        } else {
                            rvParticipants.setVisibility(View.GONE);
                            tvNoParticipants.setVisibility(View.VISIBLE);
                        }

                        // Update participant count display
                        TextView tvSpots =
                                getView() != null ? getView().findViewById(R.id.tv_spots) : null;
                        if (tvSpots != null && getArguments() != null) {
                            int totalSpots = getArguments().getInt("totalSpots", 0);
                            int currentParticipants = confirmedParticipants.size();
                            tvSpots.setText(currentParticipants + " / " + totalSpots + " joined");
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Silently fail, show empty state
                        rvParticipants.setVisibility(View.GONE);
                        tvNoParticipants.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void expressInterest() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "Please login to join activities", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (activityId == null || activityId == 0L) {
            Toast.makeText(requireContext(), "Invalid activity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        setLoading(true);

        // Create request (not a friend by default)
        ExpressInterestRequest request = new ExpressInterestRequest(false);

        // Call API
        participantRepository.expressInterest(
                activityId,
                userId,
                request,
                new ApiCallback<Participant>() {
                    @Override
                    public void onSuccess(Participant participant) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Successfully joined the activity!",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // Update button to show joined state
                        btnExpressInterest.setText("Joined");
                        btnExpressInterest.setEnabled(false);
                        btnExpressInterest.setIcon(
                                requireContext().getDrawable(R.drawable.ic_check_circle));
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);

                        // Show user-friendly message based on error type
                        if (errorMessage.contains("already joined")
                                || errorMessage.contains("already expressed interest")) {
                            Toast.makeText(
                                            requireContext(),
                                            "You've already joined this activity!",
                                            Toast.LENGTH_LONG)
                                    .show();

                            // Update button to show joined state
                            btnExpressInterest.setText("Already Joined");
                            btnExpressInterest.setEnabled(false);
                            btnExpressInterest.setIcon(
                                    requireContext().getDrawable(R.drawable.ic_check_circle));
                        } else {
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnExpressInterest.setEnabled(false);
            progressLoading.setVisibility(View.VISIBLE);
        } else {
            btnExpressInterest.setEnabled(true);
            progressLoading.setVisibility(View.GONE);
        }
    }

    private void checkUserParticipationStatus() {
        Long userId = prefsManager.getUserId();
        if (userId == null || activityId == null || activityId == 0L) {
            return;
        }

        // Get user's participations to check if they've already joined
        participantRepository.getMyParticipations(
                userId,
                new ApiCallback<List<Participant>>() {
                    @Override
                    public void onSuccess(List<Participant> participants) {
                        // Check if any participation matches this activity
                        for (Participant participant : participants) {
                            if (participant.getActivityId() != null
                                    && participant.getActivityId().equals(activityId)) {
                                // User has already joined this activity
                                updateButtonToJoinedState();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Silently fail - user can still try to join
                        Log.d(
                                "ActivityDetailFragment",
                                "Failed to check participation status: " + errorMessage);
                    }
                });
    }

    private void updateButtonToJoinedState() {
        btnExpressInterest.setText("Leave Activity");
        btnExpressInterest.setEnabled(true);
        btnExpressInterest.setBackgroundTintList(
                getResources().getColorStateList(R.color.error, null));
        btnExpressInterest.setIcon(requireContext().getDrawable(R.drawable.ic_close));

        // Change click listener to leave instead of join
        btnExpressInterest.setOnClickListener(v -> leaveActivity());
    }

    private void navigateToManageActivity() {
        Bundle bundle = new Bundle();
        bundle.putLong("activityId", activityId);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(
                R.id.action_activityDetailFragment_to_manageActivityFragment, bundle);
    }

    private void navigateToUserProfile(Long userId) {
        if (userId == null || userId == 0L) {
            Toast.makeText(requireContext(), "User profile not available", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putLong("userId", userId);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_activityDetailFragment_to_userProfileFragment, bundle);
    }

    private void leaveActivity() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (activityId == null || activityId == 0L) {
            Toast.makeText(requireContext(), "Invalid activity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Leave Activity")
                .setMessage("Are you sure you want to leave this activity?")
                .setPositiveButton(
                        "Leave",
                        (dialog, which) -> {
                            performLeaveActivity(userId);
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLeaveActivity(Long userId) {
        setLoading(true);

        participantRepository.leaveActivity(
                activityId,
                userId,
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "You have left this activity",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // Reset button to join state
                        btnExpressInterest.setText(R.string.express_interest);
                        btnExpressInterest.setEnabled(true);
                        btnExpressInterest.setBackgroundTintList(
                                getResources().getColorStateList(R.color.primary, null));
                        btnExpressInterest.setIcon(
                                requireContext().getDrawable(R.drawable.ic_check_circle));
                        btnExpressInterest.setOnClickListener(v -> expressInterest());

                        // Reload participants to update the list
                        loadParticipants();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to leave activity: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }
}
