package com.gege.activityfindermobile.ui.detail;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.ExpressInterestRequest;
import com.gege.activityfindermobile.data.model.ActivityGalleryAccess;
import com.gege.activityfindermobile.data.model.ActivityMessage;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.repository.ActivityPhotoRepository;
import com.gege.activityfindermobile.data.repository.ActivityRepository;
import com.gege.activityfindermobile.data.repository.MessageRepository;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.data.repository.ReviewRepository;
import com.gege.activityfindermobile.ui.adapters.CommentAdapter;
import com.gege.activityfindermobile.ui.adapters.ParticipantAdapter;
import com.gege.activityfindermobile.ui.review.ReviewDialog;
import com.gege.activityfindermobile.utils.DateUtil;
import com.gege.activityfindermobile.utils.ImageLoader;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.gege.activityfindermobile.utils.UiUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivityDetailFragment extends Fragment implements OnMapReadyCallback {

    @Inject ActivityRepository activityRepository;

    @Inject ParticipantRepository participantRepository;

    @Inject ReviewRepository reviewRepository;

    @Inject MessageRepository messageRepository;

    @Inject ActivityPhotoRepository activityPhotoRepository;

    @Inject SharedPreferencesManager prefsManager;

    private Long activityId;
    private Long creatorId;
    private String activityTitle;
    private String activityCategory;
    private ImageView ivActivityHero;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabBack;
    private GoogleMap googleMap;
    private Double activityLatitude;
    private Double activityLongitude;
    private MaterialButton btnExpressInterest;
    private MaterialButton btnReportActivity;
    private MaterialButton btnManage;
    private MaterialButton btnEditActivity;
    private MaterialButton btnDeleteActivity;
    private MaterialButton btnViewGallery;
    private View cardGallery;
    private TextView tvPhotoCountBadge;
    private TextView tvGalleryStatus;
    private CircularProgressIndicator progressLoading;
    private RecyclerView rvParticipants;
    private TextView tvNoParticipants;
    private ParticipantAdapter participantAdapter;
    private View cardCreator;
    private TextInputLayout tilComment;
    private TextInputEditText etComment;
    private RecyclerView rvComments;
    private TextView tvNoComments;
    private TextView tvComments;
    private CommentAdapter commentAdapter;

    private MaterialCardView mcvComments;

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
        Log.d(
                "ActivityDetailFragment",
                "onResume() called - reloading activity data, participants and checking status");

        // Reload activity details from API to reflect any changes
        reloadActivityData();

        // Reload participants and check button state when returning to this screen
        loadParticipants();

        // Only check participation status if user is not the creator
        Long currentUserId = prefsManager.getUserId();

        String dateStr = getArguments() != null ? getArguments().getString("date", "") : "";
        boolean isExpired = isActivityExpired(dateStr);
        setVisibilities(isExpired, currentUserId);
    }

    private void setVisibilities(boolean isExpired, Long currentUserId) {

        if (currentUserId != null && currentUserId.equals(creatorId)) {
            Log.d("ActivityDetailFragment", "User IS the creator - showing edit/delete buttons");
            // Show edit and delete buttons for creator
            btnEditActivity.setVisibility(View.VISIBLE);
            btnEditActivity.setEnabled(true);

            // Hide join button for creator
            btnExpressInterest.setVisibility(View.GONE);
            btnExpressInterest.setEnabled(false);
            // Hide report button for creator
            btnReportActivity.setVisibility(View.GONE);
            // Hide manage button (we have edit/delete now)
            btnManage.setVisibility(View.VISIBLE);
            btnManage.setEnabled(true);

            if (isExpired) {
                btnDeleteActivity.setVisibility(View.GONE);
                btnDeleteActivity.setEnabled(false);
            } else {
                btnDeleteActivity.setVisibility(View.VISIBLE);
                btnDeleteActivity.setEnabled(true);
            }
            // Creator can see and send messages
            showCommentSection();
        } else {
            Log.d(
                    "ActivityDetailFragment",
                    "User is NOT the creator - checking participation status");
            // Hide edit and delete buttons for non-creators
            btnEditActivity.setVisibility(View.GONE);
            btnEditActivity.setEnabled(false);
            btnDeleteActivity.setVisibility(View.GONE);
            btnDeleteActivity.setEnabled(false);
            // Show report button for non-creators
            btnReportActivity.setVisibility(View.VISIBLE);
            // Hide manage button for non-creators
            btnManage.setVisibility(View.GONE);
            btnManage.setEnabled(false);
            // Hide comment section by default, will show if user is joined
            hideCommentSection();
            // Hide join button if activity is expired
            if (isExpired) {
                btnExpressInterest.setVisibility(View.GONE);
            }
            // Check if user has already joined this activity
            checkUserParticipationStatus();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(
                view,
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    // Add top padding to the back button for status bar
                    com.google.android.material.floatingactionbutton.FloatingActionButton
                            fabBackBtn = v.findViewById(R.id.fab_back);
                    if (fabBackBtn != null) {
                        ViewGroup.MarginLayoutParams params =
                                (ViewGroup.MarginLayoutParams) fabBackBtn.getLayoutParams();
                        params.topMargin = systemBars.top + 16;
                        fabBackBtn.setLayoutParams(params);
                    }

                    return insets;
                });

        // Initialize hero image and back button
        ivActivityHero = view.findViewById(R.id.iv_activity_hero);
        fabBack = view.findViewById(R.id.fab_back);
        fabBack.setOnClickListener(
                v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Initialize map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnExpressInterest = view.findViewById(R.id.btn_express_interest);
        btnReportActivity = view.findViewById(R.id.btn_report_activity);
        btnManage = view.findViewById(R.id.btn_manage);
        btnEditActivity = view.findViewById(R.id.btn_edit_activity);
        btnDeleteActivity = view.findViewById(R.id.btn_delete_activity);
        btnViewGallery = view.findViewById(R.id.btn_view_gallery);
        cardGallery = view.findViewById(R.id.card_gallery);
        tvPhotoCountBadge = view.findViewById(R.id.tv_photo_count_badge);
        tvGalleryStatus = view.findViewById(R.id.tv_gallery_status);
        progressLoading = view.findViewById(R.id.progress_loading);
        rvParticipants = view.findViewById(R.id.rv_participants);
        tvNoParticipants = view.findViewById(R.id.tv_no_participants);
        cardCreator = view.findViewById(R.id.card_creator);
        tilComment = view.findViewById(R.id.til_comment);
        etComment = view.findViewById(R.id.et_comment);
        rvComments = view.findViewById(R.id.rv_comments);
        tvNoComments = view.findViewById(R.id.tv_no_comments);
        tvComments = view.findViewById(R.id.tv_comments);
        mcvComments = view.findViewById(R.id.mvc_comments);

        // Setup creator card review button
        MaterialButton btnReviewCreator = view.findViewById(R.id.btn_review_creator);

        // Setup participant adapter with click listener
        participantAdapter =
                new ParticipantAdapter(
                        participant -> {
                            navigateToUserProfile(participant.getUserId());
                        },
                        ParticipantAdapter.Owner.ActivityDetailFragment);
        participantAdapter.setReviewListener(
                (participant, activityIdParam) -> {
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
            activityTitle = args.getString("title", "Event");
            activityCategory = args.getString("category", "");
            activityDateStr = args.getString("date", "");
            displayActivityData(view, args);
        }

        // Set activity date on participant adapter for review button visibility
        participantAdapter.setActivityDate(activityDateStr);

        // Check if activity is expired and hide join button if it is
        boolean isExpired = isActivityExpired(activityDateStr);

        // Setup join button
        btnExpressInterest.setOnClickListener(v -> expressInterest());

        // Setup report button
        btnReportActivity.setOnClickListener(v -> showReportActivityDialog());

        // Setup manage button
        btnManage.setOnClickListener(v -> navigateToManageActivity());

        // Setup edit button
        btnEditActivity.setOnClickListener(v -> navigateToEditActivity());

        // Setup delete button
        btnDeleteActivity.setOnClickListener(v -> confirmDeleteActivity());

        // Setup gallery button
        btnViewGallery.setOnClickListener(v -> navigateToGallery());

        // Setup creator card click
        cardCreator.setOnClickListener(v -> navigateToUserProfile(creatorId));

        // Check gallery access
        checkGalleryAccess();

        // Check if current user is the creator
        Long currentUserId = prefsManager.getUserId();
        Log.d(
                "ActivityDetailFragment",
                "Creator check - currentUserId: " + currentUserId + ", creatorId: " + creatorId);

        setVisibilities(isExpired, currentUserId);

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
        View cardExpiredBadge = view.findViewById(R.id.card_expired_badge);
        MaterialButton btnReviewCreator = view.findViewById(R.id.btn_review_creator);
        ImageView ivArrowCreator = view.findViewById(R.id.iv_arrow_creator);

        // Load creator avatar
        String creatorAvatar = args.getString("creatorAvatar");
        ImageLoader.loadCircularProfileImage(requireContext(), creatorAvatar, ivCreatorAvatar);

        // Load category image
        String category = args.getString("category", "");
        loadCategoryImage(category);

        // Set category text
        TextView tvActivityCategory = view.findViewById(R.id.tv_activity_category);
        tvActivityCategory.setText(category != null ? category.toUpperCase() : "");

        tvTitle.setText(args.getString("title", ""));
        tvDate.setText(args.getString("date", ""));
        tvTime.setText(args.getString("time", ""));
        tvLocation.setText(args.getString("location", ""));

        int availableSpots = args.getInt("availableSpots", 0);
        int totalSpots = args.getInt("totalSpots", 0);
        int currentParticipants = totalSpots - availableSpots;
        tvSpots.setText(currentParticipants + " / " + totalSpots);

        tvCreatorName.setText(args.getString("creatorName", ""));
        double rating = args.getDouble("creatorRating", 0.0);

        // Store location coordinates for map
        activityLatitude = args.getDouble("latitude", 0.0);
        activityLongitude = args.getDouble("longitude", 0.0);
        if (activityLatitude == 0.0 && activityLongitude == 0.0) {
            activityLatitude = null;
            activityLongitude = null;
        }

        // Update map if ready
        updateMapLocation();
        if (rating > 0) {
            tvCreatorRating.setText(String.format("%.1f", rating));
        } else {
            tvCreatorRating.setText("N/A");
        }

        // Set full description
        TextView tvDescriptionFull = view.findViewById(R.id.tv_description_full);
        String description = args.getString("description", "");
        tvDescriptionFull.setText(description);

        // Set location address
        TextView tvLocationAddress = view.findViewById(R.id.tv_location_address);
        String location = args.getString("location", "");
        tvLocationAddress.setText(location);

        // Setup Get Directions button
        TextView btnGetDirections = view.findViewById(R.id.btn_get_directions);
        btnGetDirections.setOnClickListener(v -> openMapsForDirections(location));

        // Hidden fields for compatibility
        chipCategory.setText(category);
        tvDescription.setText(description);

        // Show/hide badges

        // Check if activity is expired
        String dateStr = args.getString("date", "");
        boolean isExpired = isActivityExpired(dateStr);
        cardExpiredBadge.setVisibility(isExpired ? View.VISIBLE : View.GONE);

        // Setup review button for creator
        String creatorName = args.getString("creatorName", "");
        Long currentUserId = prefsManager.getUserId();
        boolean isCurrentUserCreator = currentUserId != null && currentUserId.equals(creatorId);

        if (isExpired && !isCurrentUserCreator) {
            btnReviewCreator.setVisibility(View.VISIBLE);
            ivArrowCreator.setVisibility(View.GONE);
            btnReviewCreator.setOnClickListener(
                    v -> {
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
                        boolean isCreator =
                                currentUserId != null && currentUserId.equals(creatorId);

                        List<Participant> displayParticipants = new ArrayList<>();
                        if (participants != null) {
                            for (Participant p : participants) {
                                String status = p.getStatus();
                                // Show ACCEPTED and JOINED to everyone
                                if ("ACCEPTED".equals(status) || "JOINED".equals(status)) {
                                    displayParticipants.add(p);
                                }
                                // Also show PENDING and INTERESTED to creator
                                else if (isCreator
                                        && ("PENDING".equals(status)
                                                || "INTERESTED".equals(status))) {
                                    displayParticipants.add(p);
                                }
                            }
                        }

                        if (!displayParticipants.isEmpty()) {
                            participantAdapter.setActivityId(activityId);
                            participantAdapter.setActivityDate(
                                    getArguments() != null
                                            ? getArguments().getString("date", "")
                                            : "");
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
                                    } else if ("PENDING".equals(status)
                                            || "INTERESTED".equals(status)) {
                                        pendingCount++;
                                    }
                                }
                            }

                            // Show pending count for creator
                            if (isCreator && pendingCount > 0) {
                                tvSpots.setText(
                                        confirmedCount
                                                + " / "
                                                + totalSpots
                                                + " joined ("
                                                + pendingCount
                                                + " pending)");
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
            UiUtil.showToast(requireContext(), "Please login to join activities");
            return;
        }

        if (activityId == null || activityId == 0L) {
            UiUtil.showToast(requireContext(), "Invalid activity");
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
                            UiUtil.showToast(
                                    requireContext(),
                                    "You've been accepted! You can now join the activity.");
                            updateButtonToAcceptedState();
                            // Don't add to participant list yet - only after JOINED
                        } else if ("JOINED".equals(status)) {
                            UiUtil.showToast(requireContext(), "Successfully joined the activity!");
                            updateButtonToJoinedState();
                            // Reload participants to show yourself in the list
                            loadParticipants();
                        } else if ("PENDING".equals(status) || "INTERESTED".equals(status)) {
                            UiUtil.showToast(
                                    requireContext(), "Join request sent! Waiting for approval.");
                            updateButtonToPendingState();
                        } else {
                            UiUtil.showToast(
                                    requireContext(), "Request submitted! Status: " + status);
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
                            UiUtil.showLongToast(
                                    requireContext(),
                                    "You've already requested to join this activity!");
                            // Re-check status to update button correctly
                            checkUserParticipationStatus();
                        } else if (errorMessage.contains("maximum")
                                || errorMessage.contains("attempt")
                                || errorMessage.contains("limit")
                                || errorMessage.contains("exceeded")) {
                            UiUtil.showLongToast(
                                    requireContext(),
                                    "You've reached the maximum application attempts (3)"
                                            + " for this activity.");
                            // Show max attempts state
                            showMaxAttemptsReachedState();
                        } else {
                            UiUtil.showLongToast(
                                    requireContext(), "Failed to join: " + errorMessage);
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
            Log.d(
                    "ActivityDetailFragment",
                    "checkUserParticipationStatus: userId or activityId is null");
            return;
        }

        Log.d(
                "ActivityDetailFragment",
                "Checking participation status for activity: " + activityId);

        // Get user's participations to check if they've already joined
        participantRepository.getMyParticipations(
                userId,
                new ApiCallback<List<Participant>>() {
                    @Override
                    public void onSuccess(List<Participant> participants) {
                        Log.d(
                                "ActivityDetailFragment",
                                "Got "
                                        + (participants != null ? participants.size() : 0)
                                        + " participations");

                        boolean found = false;
                        // Check if any participation matches this activity
                        if (participants != null) {
                            for (Participant participant : participants) {
                                if (participant.getActivityId() != null
                                        && participant.getActivityId().equals(activityId)) {
                                    found = true;
                                    String status = participant.getStatus();
                                    Log.d(
                                            "ActivityDetailFragment",
                                            "Found participation with status: " + status);

                                    // Update button based on participation status
                                    if ("ACCEPTED".equals(status)) {
                                        // Accepted but not yet joined - show leave option
                                        updateButtonToAcceptedState();
                                    } else if ("JOINED".equals(status)) {
                                        // Fully joined - show leave option
                                        updateButtonToJoinedState();
                                    } else if ("PENDING".equals(status)
                                            || "INTERESTED".equals(status)) {
                                        // Waiting for approval - show cancel option
                                        updateButtonToPendingState();
                                    } else if ("WITHDRAWN".equals(status)
                                            || "LEFT".equals(status)
                                            || "DECLINED".equals(status)) {
                                        // User previously left/withdrawn/declined - can reapply
                                        // Backend will enforce the 3-attempt limit when they try to
                                        // apply
                                        Integer attempts = participant.getApplicationAttempts();
                                        Log.d(
                                                "ActivityDetailFragment",
                                                "User previously "
                                                        + status
                                                        + " - attempts: "
                                                        + attempts
                                                        + " - can reapply");
                                        resetButtonToJoinState();
                                    }
                                    break;
                                }
                            }
                        }

                        if (!found) {
                            Log.d(
                                    "ActivityDetailFragment",
                                    "No participation found for this activity");
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Silently fail - user can still try to join
                        Log.e(
                                "ActivityDetailFragment",
                                "Failed to check participation status: " + errorMessage);
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

        Log.d(
                "ActivityDetailFragment",
                "Button reset complete - enabled: "
                        + btnExpressInterest.isEnabled()
                        + ", visibility: "
                        + (btnExpressInterest.getVisibility() == View.VISIBLE
                                ? "VISIBLE"
                                : "GONE/INVISIBLE")
                        + ", text: "
                        + btnExpressInterest.getText());

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

        Log.d(
                "ActivityDetailFragment",
                "Button set to max attempts - enabled: " + btnExpressInterest.isEnabled());

        // Hide comment section for users who reached max attempts
        hideCommentSection();
    }

    private void navigateToManageActivity() {
        Bundle bundle = new Bundle();
        bundle.putLong("activityId", activityId);
        bundle.putLong("currentUserId", prefsManager.getUserId());
        bundle.putLong("creatorId", creatorId);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(
                R.id.action_activityDetailFragment_to_manageActivityFragment, bundle);
    }

    private void navigateToEditActivity() {
        Bundle bundle = new Bundle();
        bundle.putLong("activityId", activityId);

        NavController navController = Navigation.findNavController(requireView());
        // Navigate to edit activity screen - you may need to adjust the action ID based on your
        // navigation graph
        navController.navigate(
                R.id.action_activityDetailFragment_to_createActivityFragment, bundle);
    }

    private void confirmDeleteActivity() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Activity")
                .setMessage(
                        "Are you sure you want to delete this activity? This action cannot be"
                                + " undone.")
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {
                            deleteActivity();
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteActivity() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            UiUtil.showToast(requireContext(), "User not logged in");
            return;
        }

        if (activityId == null || activityId == 0L) {
            UiUtil.showToast(requireContext(), "Invalid activity");
            return;
        }

        setLoading(true);

        // Call cancel activity API (which deletes the activity)
        activityRepository.cancelActivity(
                activityId,
                userId,
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        setLoading(false);
                        UiUtil.showToast(requireContext(), "Activity deleted successfully");

                        // Navigate back to previous screen
                        requireActivity().onBackPressed();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        UiUtil.showLongToast(
                                requireContext(), "Failed to delete activity: " + errorMessage);
                    }
                });
    }

    private void navigateToUserProfile(Long userId) {
        if (userId == null || userId == 0L) {
            UiUtil.showToast(requireContext(), "User profile not available");
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
            UiUtil.showToast(requireContext(), "User not logged in");
            return;
        }

        if (activityId == null || activityId == 0L) {
            UiUtil.showToast(requireContext(), "Invalid activity");
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
            UiUtil.showToast(requireContext(), "User not logged in");
            return;
        }

        if (activityId == null || activityId == 0L) {
            UiUtil.showToast(requireContext(), "Invalid activity");
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
                        UiUtil.showToast(
                                requireContext(), "You have withdrawn from this activity.");

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
                        UiUtil.showLongToast(
                                requireContext(), "Failed to leave activity: " + errorMessage);
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
        tvComments.setVisibility(View.GONE);
        mcvComments.setVisibility(View.GONE);
    }

    private void sendComment() {
        String commentText =
                etComment.getText() != null ? etComment.getText().toString().trim() : "";

        if (commentText.isEmpty()) {
            UiUtil.showToast(requireContext(), "Please write a message");
            return;
        }

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            UiUtil.showToast(requireContext(), "Please login to send messages");
            return;
        }

        if (activityId == null || activityId == 0L) {
            UiUtil.showToast(requireContext(), "Invalid activity");
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
                        UiUtil.showToast(requireContext(), "Message sent!");

                        // Reload comments
                        loadComments();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Re-enable input
                        etComment.setEnabled(true);
                        tilComment.setEnabled(true);

                        UiUtil.showLongToast(
                                requireContext(), "Failed to send message: " + errorMessage);
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
        return DateUtil.isDisplayDateExpired(dateStr);
    }

    private void showReviewDialog(Participant participant, Long activityIdParam) {
        ReviewDialog reviewDialog =
                ReviewDialog.newInstance(
                        participant.getUserId(), participant.getUserName(), activityIdParam);
        reviewDialog.setOnReviewSubmittedListener(
                () -> {
                    // Refresh participants list after review is submitted
                    loadParticipants();
                });
        reviewDialog.show(getChildFragmentManager(), "ReviewDialog");
    }

    private void showReportActivityDialog() {
        com.gege.activityfindermobile.ui.report.ReportDialog reportDialog =
                com.gege.activityfindermobile.ui.report.ReportDialog.newInstanceForActivity(
                        activityId);
        reportDialog.show(getChildFragmentManager(), "ReportDialog");
    }

    private void reloadActivityData() {
        if (activityId == null || activityId == 0L) {
            return;
        }

        activityRepository.getActivityById(
                activityId,
                new ApiCallback<com.gege.activityfindermobile.data.model.Activity>() {
                    @Override
                    public void onSuccess(
                            com.gege.activityfindermobile.data.model.Activity activity) {
                        // Update UI with fresh data
                        if (getView() != null) {
                            TextView tvTitle = getView().findViewById(R.id.tv_title);
                            TextView tvDescription = getView().findViewById(R.id.tv_description);
                            TextView tvDate = getView().findViewById(R.id.tv_date);
                            TextView tvTime = getView().findViewById(R.id.tv_time);
                            TextView tvLocation = getView().findViewById(R.id.tv_location);
                            TextView tvSpots = getView().findViewById(R.id.tv_spots);
                            Chip chipCategory = getView().findViewById(R.id.chip_category);

                            tvTitle.setText(activity.getTitle());
                            tvDescription.setText(activity.getDescription());
                            chipCategory.setText(activity.getCategory());
                            tvLocation.setText(activity.getLocation());

                            // Format and set date and time
                            String displayDate =
                                    DateUtil.formatToDisplayDate(activity.getActivityDate());
                            String displayTime =
                                    DateUtil.formatToDisplayTime(activity.getActivityDate());

                            if (displayDate != null) {
                                tvDate.setText(displayDate);
                            }
                            if (displayTime != null) {
                                tvTime.setText(displayTime);
                            }

                            // Update spots count
                            int totalSpots = activity.getTotalSpots();
                            int availableSpots = activity.getAvailableSpots();
                            int currentParticipants = totalSpots - availableSpots;
                            tvSpots.setText(currentParticipants + " / " + totalSpots + " joined");

                            // Store location coordinates for map
                            activityLatitude = activity.getLatitude();
                            activityLongitude = activity.getLongitude();

                            // Update map if ready
                            updateMapLocation();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(
                                "ActivityDetailFragment",
                                "Failed to reload activity data: " + errorMessage);
                        // Silently fail - keep showing the old data
                    }
                });
    }

    /**
     * Check if current user has access to the activity gallery Gallery is only accessible to
     * participants who joined, after the event has ended
     */
    private void checkGalleryAccess() {
        activityPhotoRepository.checkGalleryAccess(
                activityId,
                new ApiCallback<ActivityGalleryAccess>() {
                    @Override
                    public void onSuccess(ActivityGalleryAccess access) {
                        if (access.getHasAccess()) {
                            // User has access - show gallery card
                            cardGallery.setVisibility(View.VISIBLE);

                            // Update photo count badge
                            int photoCount =
                                    access.getPhotoCount() != null ? access.getPhotoCount() : 0;
                            String countText = photoCount == 1 ? "1 photo" : photoCount + " photos";
                            tvPhotoCountBadge.setText(countText);

                            // Update status message
                            if (access.getCanUpload()) {
                                tvGalleryStatus.setText("Share memories from this event");
                            } else {
                                tvGalleryStatus.setText(
                                        "Gallery is full ("
                                                + access.getMaxPhotos()
                                                + " photos max)");
                            }
                        } else {
                            // User doesn't have access - hide gallery card
                            cardGallery.setVisibility(View.GONE);
                            Log.d(
                                    "ActivityDetailFragment",
                                    "Gallery access denied: " + access.getReason());
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Hide gallery card on error
                        cardGallery.setVisibility(View.GONE);
                        Log.e(
                                "ActivityDetailFragment",
                                "Failed to check gallery access: " + errorMessage);
                    }
                });
    }

    /** Navigate to the activity gallery */
    private void navigateToGallery() {
        Bundle bundle = new Bundle();
        bundle.putLong("activityId", activityId);
        bundle.putString("activityTitle", activityTitle);
        bundle.putString("activityCategory", activityCategory);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(
                R.id.action_activityDetailFragment_to_activityGalleryFragment, bundle);
    }

    /** Load category image based on category name (same as feed) */
    private void loadCategoryImage(String category) {
        if (ivActivityHero == null) {
            return;
        }

        if (category == null || category.isEmpty()) {
            ivActivityHero.setImageResource(R.drawable.activity_default);
            return;
        }

        // Convert category to lowercase and replace spaces with underscores
        String imageResourceName = "activity_" + category.toLowerCase().replace(" ", "_");

        // Get resource ID
        int resourceId =
                getResources()
                        .getIdentifier(
                                imageResourceName, "drawable", requireContext().getPackageName());

        // Set image or use default if not found
        if (resourceId != 0) {
            ivActivityHero.setImageResource(resourceId);
        } else {
            ivActivityHero.setImageResource(R.drawable.activity_default);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Disable map interactions
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Update map with activity location if available
        updateMapLocation();
    }

    /** Update the map marker with the activity location */
    private void updateMapLocation() {
        if (googleMap == null || activityLatitude == null || activityLongitude == null) {
            return;
        }

        LatLng activityLocation = new LatLng(activityLatitude, activityLongitude);

        // Clear previous markers
        googleMap.clear();

        // Add marker at activity location
        googleMap.addMarker(new MarkerOptions().position(activityLocation).title(activityTitle));

        // Move camera to location with appropriate zoom
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(activityLocation, 15f));
    }

    /** Open Google Maps or any maps app for directions to the location */
    private void openMapsForDirections(String location) {
        if (location == null || location.isEmpty()) {
            UiUtil.showToast(requireContext(), "Location not available");
            return;
        }

        // Create URI for Google Maps
        android.net.Uri gmmIntentUri =
                android.net.Uri.parse("geo:0,0?q=" + android.net.Uri.encode(location));
        android.content.Intent mapIntent =
                new android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Try to launch Google Maps
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback to browser if Google Maps is not installed
            android.net.Uri webUri =
                    android.net.Uri.parse(
                            "https://www.google.com/maps/search/?api=1&query="
                                    + android.net.Uri.encode(location));
            android.content.Intent webIntent =
                    new android.content.Intent(android.content.Intent.ACTION_VIEW, webUri);
            startActivity(webIntent);
        }
    }
}
