package com.gege.activityfindermobile.ui.detail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.gege.activityfindermobile.data.dto.ReviewCreateRequest;
import com.gege.activityfindermobile.data.model.ActivityMessage;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.model.Review;
import com.gege.activityfindermobile.data.repository.MessageRepository;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.data.repository.ReviewRepository;
import com.gege.activityfindermobile.ui.adapters.CommentAdapter;
import com.gege.activityfindermobile.ui.adapters.ParticipantAdapter;
import com.gege.activityfindermobile.ui.review.ReviewDialog;
import com.gege.activityfindermobile.utils.ImageLoader;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivityDetailFragment extends Fragment {

    @Inject ParticipantRepository participantRepository;

    @Inject ReviewRepository reviewRepository;

    @Inject MessageRepository messageRepository;

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
    private TextInputLayout tilComment;
    private TextInputEditText etComment;
    private RecyclerView rvComments;
    private TextView tvNoComments;
    private CommentAdapter commentAdapter;

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
        Log.d("ActivityDetailFragment", "onResume() called - reloading participants and checking status");
        // Reload participants and check button state when returning to this screen
        loadParticipants();

        // Only check participation status if user is not the creator
        Long currentUserId = prefsManager.getUserId();
        if (currentUserId != null && !currentUserId.equals(creatorId)) {
            checkUserParticipationStatus();
        } else {
            Log.d("ActivityDetailFragment", "Skipping checkUserParticipationStatus in onResume - user is creator");
        }
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
        tilComment = view.findViewById(R.id.til_comment);
        etComment = view.findViewById(R.id.et_comment);
        rvComments = view.findViewById(R.id.rv_comments);
        tvNoComments = view.findViewById(R.id.tv_no_comments);

        // Setup creator card review button
        MaterialButton btnReviewCreator = view.findViewById(R.id.btn_review_creator);

        // Setup participant adapter with click listener
        participantAdapter =
                new ParticipantAdapter(
                        participant -> {
                            navigateToUserProfile(participant.getUserId());
                        });
        participantAdapter.setReviewListener((participant, activityIdParam) -> {
            showReviewDialog(participant, activityIdParam);
        });
        participantAdapter.setActivityId(activityId);
        participantAdapter.setCurrentUserId(prefsManager.getUserId());
        participantAdapter.setCreatorId(creatorId);
        rvParticipants.setAdapter(participantAdapter);

        // Setup comment adapter
        commentAdapter = new CommentAdapter(requireContext());
        rvComments.setAdapter(commentAdapter);

        // Setup send comment button
        tilComment.setEndIconOnClickListener(v -> sendComment());

        // Get arguments
        Bundle args = getArguments();
        String activityDateStr = "";
        if (args != null) {
            activityId = args.getLong("activityId", 0L);
            creatorId = args.getLong("creatorId", 0L);
            activityDateStr = args.getString("date", "");
            displayActivityData(view, args);
        } else {
            // Fallback to test data if no arguments
            displayTestData(view);
        }

        // Set activity date on participant adapter for review button visibility
        participantAdapter.setActivityDate(activityDateStr);

        // Check if activity is expired and hide join button if it is
        boolean isExpired = isActivityExpired(activityDateStr);

        // Setup join button
        btnExpressInterest.setOnClickListener(v -> expressInterest());

        // Setup manage button
        btnManage.setOnClickListener(v -> navigateToManageActivity());

        // Setup creator card click
        cardCreator.setOnClickListener(v -> navigateToUserProfile(creatorId));

        // Check if current user is the creator
        Long currentUserId = prefsManager.getUserId();
        Log.d("ActivityDetailFragment", "Creator check - currentUserId: " + currentUserId + ", creatorId: " + creatorId);
        if (currentUserId != null && currentUserId.equals(creatorId)) {
            Log.d("ActivityDetailFragment", "User IS the creator - hiding join button, showing manage button");
            // Show manage button for creator
            btnManage.setVisibility(View.VISIBLE);
            // Hide join button for creator
            btnExpressInterest.setVisibility(View.GONE);
            // Creator can see and send messages
            showCommentSection();
        } else {
            Log.d("ActivityDetailFragment", "User is NOT the creator - checking participation status");
            // Hide comment section by default, will show if user is joined
            hideCommentSection();
            // Hide join button if activity is expired
            if (isExpired) {
                btnExpressInterest.setVisibility(View.GONE);
            }
            // Check if user has already joined this activity
            checkUserParticipationStatus();
        }

        // Load participants from API
        loadParticipants();
    }

    private void displayActivityData(View view, Bundle args) {
        de.hdodenhof.circleimageview.CircleImageView ivCreatorAvatar =
                view.findViewById(R.id.iv_creator_avatar);
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
        Chip badgeExpired = view.findViewById(R.id.badge_expired);
        MaterialButton btnReviewCreator = view.findViewById(R.id.btn_review_creator);
        ImageView ivArrowCreator = view.findViewById(R.id.iv_arrow_creator);

        // Load creator avatar
        String creatorAvatar = args.getString("creatorAvatar");
        ImageLoader.loadCircularProfileImage(requireContext(), creatorAvatar, ivCreatorAvatar);

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

        // Check if activity is expired
        String dateStr = args.getString("date", "");
        boolean isExpired = isActivityExpired(dateStr);
        badgeExpired.setVisibility(isExpired ? View.VISIBLE : View.GONE);

        // Setup review button for creator
        String creatorName = args.getString("creatorName", "");
        Long currentUserId = prefsManager.getUserId();
        boolean isCurrentUserCreator = currentUserId != null && currentUserId.equals(creatorId);

        if (isExpired && !isCurrentUserCreator) {
            btnReviewCreator.setVisibility(View.VISIBLE);
            ivArrowCreator.setVisibility(View.GONE);
            btnReviewCreator.setOnClickListener(v -> {
                // Create a fake participant object for the creator
                Participant creatorParticipant = new Participant();
                creatorParticipant.setUserId(creatorId);
                creatorParticipant.setUserName(creatorName);
                showReviewDialog(creatorParticipant, activityId);
            });
        } else {
            btnReviewCreator.setVisibility(View.GONE);
            ivArrowCreator.setVisibility(View.VISIBLE);
        }
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
                        Long currentUserId = prefsManager.getUserId();
                        boolean isCreator = currentUserId != null && currentUserId.equals(creatorId);

                        List<Participant> displayParticipants = new ArrayList<>();
                        if (participants != null) {
                            for (Participant p : participants) {
                                String status = p.getStatus();
                                // Show ACCEPTED and JOINED to everyone
                                if ("ACCEPTED".equals(status) || "JOINED".equals(status)) {
                                    displayParticipants.add(p);
                                }
                                // Also show PENDING and INTERESTED to creator
                                else if (isCreator && ("PENDING".equals(status) || "INTERESTED".equals(status))) {
                                    displayParticipants.add(p);
                                }
                            }
                        }

                        if (!displayParticipants.isEmpty()) {
                            participantAdapter.setActivityId(activityId);
                            participantAdapter.setActivityDate(
                                    getArguments() != null ? getArguments().getString("date", "") : ""
                            );
                            participantAdapter.setCurrentUserId(currentUserId);
                            participantAdapter.setCreatorId(creatorId);
                            participantAdapter.setParticipants(displayParticipants);
                            rvParticipants.setVisibility(View.VISIBLE);
                            tvNoParticipants.setVisibility(View.GONE);
                        } else {
                            rvParticipants.setVisibility(View.GONE);
                            tvNoParticipants.setVisibility(View.VISIBLE);
                        }

                        // Update participant count display (only count confirmed participants)
                        TextView tvSpots =
                                getView() != null ? getView().findViewById(R.id.tv_spots) : null;
                        if (tvSpots != null && getArguments() != null) {
                            int totalSpots = getArguments().getInt("totalSpots", 0);
                            // Count only ACCEPTED and JOINED
                            int confirmedCount = 0;
                            int pendingCount = 0;
                            if (participants != null) {
                                for (Participant p : participants) {
                                    String status = p.getStatus();
                                    if ("ACCEPTED".equals(status) || "JOINED".equals(status)) {
                                        confirmedCount++;
                                    } else if ("PENDING".equals(status) || "INTERESTED".equals(status)) {
                                        pendingCount++;
                                    }
                                }
                            }

                            // Show pending count for creator
                            if (isCreator && pendingCount > 0) {
                                tvSpots.setText(confirmedCount + " / " + totalSpots + " joined (" + pendingCount + " pending)");
                            } else {
                                tvSpots.setText(confirmedCount + " / " + totalSpots + " joined");
                            }
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

                        // Check the status returned from server
                        String status = participant.getStatus();
                        if ("ACCEPTED".equals(status)) {
                            Toast.makeText(
                                            requireContext(),
                                            "You've been accepted! You can now join the activity.",
                                            Toast.LENGTH_SHORT)
                                    .show();
                            updateButtonToAcceptedState();
                            // Don't add to participant list yet - only after JOINED
                        } else if ("JOINED".equals(status)) {
                            Toast.makeText(
                                            requireContext(),
                                            "Successfully joined the activity!",
                                            Toast.LENGTH_SHORT)
                                    .show();
                            updateButtonToJoinedState();
                            // Reload participants to show yourself in the list
                            loadParticipants();
                        } else if ("PENDING".equals(status) || "INTERESTED".equals(status)) {
                            Toast.makeText(
                                            requireContext(),
                                            "Join request sent! Waiting for approval.",
                                            Toast.LENGTH_SHORT)
                                    .show();
                            updateButtonToPendingState();
                        } else {
                            Toast.makeText(
                                            requireContext(),
                                            "Request submitted! Status: " + status,
                                            Toast.LENGTH_SHORT)
                                    .show();
                            updateButtonToPendingState();
                        }

                        // Force re-check participation status to ensure button is in correct state
                        checkUserParticipationStatus();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);

                        Log.d("ActivityDetailFragment", "Express interest error: " + errorMessage);

                        // Show user-friendly message based on error type
                        if (errorMessage.contains("already joined")
                                || errorMessage.contains("already expressed interest")
                                || errorMessage.contains("already a participant")
                                || errorMessage.contains("duplicate")
                                || errorMessage.contains("already exists")) {
                            Toast.makeText(
                                            requireContext(),
                                            "You've already requested to join this activity!",
                                            Toast.LENGTH_LONG)
                                    .show();
                            // Re-check status to update button correctly
                            checkUserParticipationStatus();
                        } else if (errorMessage.contains("maximum")
                                || errorMessage.contains("attempt")
                                || errorMessage.contains("limit")
                                || errorMessage.contains("exceeded")) {
                            Toast.makeText(
                                            requireContext(),
                                            "You've reached the maximum application attempts (3) for this activity.",
                                            Toast.LENGTH_LONG)
                                    .show();
                            // Show max attempts state
                            showMaxAttemptsReachedState();
                        } else {
                            Toast.makeText(requireContext(), "Failed to join: " + errorMessage, Toast.LENGTH_LONG)
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
            Log.d("ActivityDetailFragment", "checkUserParticipationStatus: userId or activityId is null");
            return;
        }

        Log.d("ActivityDetailFragment", "Checking participation status for activity: " + activityId);

        // Get user's participations to check if they've already joined
        participantRepository.getMyParticipations(
                userId,
                new ApiCallback<List<Participant>>() {
                    @Override
                    public void onSuccess(List<Participant> participants) {
                        Log.d("ActivityDetailFragment", "Got " + (participants != null ? participants.size() : 0) + " participations");

                        boolean found = false;
                        // Check if any participation matches this activity
                        if (participants != null) {
                            for (Participant participant : participants) {
                                if (participant.getActivityId() != null
                                        && participant.getActivityId().equals(activityId)) {
                                    found = true;
                                    String status = participant.getStatus();
                                    Log.d("ActivityDetailFragment", "Found participation with status: " + status);

                                    // Update button based on participation status
                                    if ("ACCEPTED".equals(status)) {
                                        // Accepted but not yet joined - show leave option
                                        updateButtonToAcceptedState();
                                    } else if ("JOINED".equals(status)) {
                                        // Fully joined - show leave option
                                        updateButtonToJoinedState();
                                    } else if ("PENDING".equals(status) || "INTERESTED".equals(status)) {
                                        // Waiting for approval - show cancel option
                                        updateButtonToPendingState();
                                    } else if ("WITHDRAWN".equals(status) || "LEFT".equals(status) || "DECLINED".equals(status)) {
                                        // User previously left/withdrawn/declined - can reapply
                                        // Backend will enforce the 3-attempt limit when they try to apply
                                        Integer attempts = participant.getApplicationAttempts();
                                        Log.d("ActivityDetailFragment", "User previously " + status + " - attempts: " + attempts + " - can reapply");
                                        resetButtonToJoinState();
                                    }
                                    break;
                                }
                            }
                        }

                        if (!found) {
                            Log.d("ActivityDetailFragment", "No participation found for this activity");
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Silently fail - user can still try to join
                        Log.e("ActivityDetailFragment", "Failed to check participation status: " + errorMessage);
                    }
                });
    }

    private void updateButtonToAcceptedState() {
        btnExpressInterest.setText("Leave Activity");
        btnExpressInterest.setEnabled(true);
        btnExpressInterest.setBackgroundTintList(
                getResources().getColorStateList(R.color.error, null));
        btnExpressInterest.setIcon(requireContext().getDrawable(R.drawable.ic_close));

        // Change click listener to leave
        btnExpressInterest.setOnClickListener(v -> leaveActivity());

        // Show comment section for accepted users
        showCommentSection();
    }

    private void updateButtonToJoinedState() {
        btnExpressInterest.setText("Leave Activity");
        btnExpressInterest.setEnabled(true);
        btnExpressInterest.setBackgroundTintList(
                getResources().getColorStateList(R.color.error, null));
        btnExpressInterest.setIcon(requireContext().getDrawable(R.drawable.ic_close));

        // Change click listener to leave instead of join
        btnExpressInterest.setOnClickListener(v -> leaveActivity());

        // Show comment section for joined users
        showCommentSection();
    }

    private void updateButtonToPendingState() {
        btnExpressInterest.setText("Cancel Request");
        btnExpressInterest.setEnabled(true);
        btnExpressInterest.setBackgroundTintList(
                getResources().getColorStateList(R.color.warning, null));
        btnExpressInterest.setIcon(requireContext().getDrawable(R.drawable.ic_close));

        // Change click listener to cancel request
        btnExpressInterest.setOnClickListener(v -> cancelRequest());

        // Hide comment section for pending users
        hideCommentSection();
    }

    private void resetButtonToJoinState() {
        Log.d("ActivityDetailFragment", "Resetting button to join state");
        btnExpressInterest.setText(R.string.express_interest);
        btnExpressInterest.setEnabled(true);
        btnExpressInterest.setVisibility(View.VISIBLE); // Ensure button is visible
        btnExpressInterest.setBackgroundTintList(
                getResources().getColorStateList(R.color.primary, null));

        // Clear icon or set to null - avoid icon issues
        btnExpressInterest.setIcon(null);

        // Reset click listener
        btnExpressInterest.setOnClickListener(v -> expressInterest());

        Log.d("ActivityDetailFragment", "Button reset complete - enabled: " + btnExpressInterest.isEnabled()
                + ", visibility: " + (btnExpressInterest.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE/INVISIBLE")
                + ", text: " + btnExpressInterest.getText());

        // Hide comment section for non-joined users
        hideCommentSection();
    }

    private void showMaxAttemptsReachedState() {
        Log.d("ActivityDetailFragment", "Setting button to max attempts reached state");
        btnExpressInterest.setText("Max Attempts Reached (3/3)");
        btnExpressInterest.setEnabled(false);
        btnExpressInterest.setVisibility(View.VISIBLE);
        btnExpressInterest.setBackgroundTintList(
                getResources().getColorStateList(R.color.text_secondary, null));
        btnExpressInterest.setIcon(null);

        Log.d("ActivityDetailFragment", "Button set to max attempts - enabled: " + btnExpressInterest.isEnabled());

        // Hide comment section for users who reached max attempts
        hideCommentSection();
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

    private void cancelRequest() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (activityId == null || activityId == 0L) {
            Toast.makeText(requireContext(), "Invalid activity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog for canceling request
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cancel Request")
                .setMessage("Are you sure you want to cancel your join request?")
                .setPositiveButton(
                        "Yes, Cancel",
                        (dialog, which) -> {
                            performLeaveActivity(userId);
                        })
                .setNegativeButton("No", null)
                .show();
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
                                        "You have withdrawn from this activity.",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // Hide comment section when leaving
                        hideCommentSection();

                        // Reload participants to update the list
                        loadParticipants();

                        // Re-check status to update button based on application attempts
                        checkUserParticipationStatus();
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

    private void showCommentSection() {
        tilComment.setVisibility(View.VISIBLE);
        rvComments.setVisibility(View.VISIBLE);
        // Load comments when showing the section
        loadComments();
    }

    private void hideCommentSection() {
        tilComment.setVisibility(View.GONE);
        rvComments.setVisibility(View.GONE);
        tvNoComments.setVisibility(View.GONE);
    }

    private void sendComment() {
        String commentText = etComment.getText() != null ? etComment.getText().toString().trim() : "";

        if (commentText.isEmpty()) {
            Toast.makeText(requireContext(), "Please write a message", Toast.LENGTH_SHORT).show();
            return;
        }

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "Please login to send messages", Toast.LENGTH_SHORT).show();
            return;
        }

        if (activityId == null || activityId == 0L) {
            Toast.makeText(requireContext(), "Invalid activity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable input while sending
        etComment.setEnabled(false);
        tilComment.setEnabled(false);

        // Send message to activity chat
        messageRepository.sendMessage(
                activityId,
                commentText,
                new ApiCallback<ActivityMessage>() {
                    @Override
                    public void onSuccess(ActivityMessage message) {
                        // Re-enable input
                        etComment.setEnabled(true);
                        tilComment.setEnabled(true);

                        // Clear input field
                        etComment.setText("");

                        // Show success message
                        Toast.makeText(requireContext(), "Message sent!", Toast.LENGTH_SHORT)
                                .show();

                        // Reload comments
                        loadComments();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Re-enable input
                        etComment.setEnabled(true);
                        tilComment.setEnabled(true);

                        Toast.makeText(
                                        requireContext(),
                                        "Failed to send message: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void loadComments() {
        if (activityId == null || activityId == 0L) {
            return;
        }

        messageRepository.getMessages(
                activityId,
                new ApiCallback<List<ActivityMessage>>() {
                    @Override
                    public void onSuccess(List<ActivityMessage> messages) {
                        if (messages != null && !messages.isEmpty()) {
                            commentAdapter.setComments(messages);
                            rvComments.setVisibility(View.VISIBLE);
                            tvNoComments.setVisibility(View.GONE);
                        } else {
                            rvComments.setVisibility(View.GONE);
                            tvNoComments.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Silently fail, show empty state
                        rvComments.setVisibility(View.GONE);
                        tvNoComments.setVisibility(View.VISIBLE);
                    }
                });
    }

    private boolean isActivityExpired(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }

        try {
            // Parse the date string (assuming format like "Nov 15, 2025")
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US);
            java.util.Date activityDate = sdf.parse(dateStr);
            java.util.Date today = new java.util.Date();

            // If activity date is before today, it's expired
            return activityDate != null && activityDate.before(today);
        } catch (java.text.ParseException e) {
            // If date parsing fails, assume not expired
            return false;
        }
    }

    private void showReviewDialog(Participant participant, Long activityIdParam) {
        ReviewDialog reviewDialog = ReviewDialog.newInstance(
                participant.getUserId(),
                participant.getUserName(),
                activityIdParam
        );
        reviewDialog.setOnReviewSubmittedListener(() -> {
            // Refresh participants list after review is submitted
            loadParticipants();
        });
        reviewDialog.show(getChildFragmentManager(), "ReviewDialog");
    }
}
