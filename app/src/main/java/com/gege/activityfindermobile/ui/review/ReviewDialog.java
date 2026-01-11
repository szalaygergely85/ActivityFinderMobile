package com.gege.activityfindermobile.ui.review;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.dto.ReviewRequest;
import com.gege.activityfindermobile.data.model.Review;
import com.gege.activityfindermobile.data.repository.ReviewRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReviewDialog extends DialogFragment {

    @Inject ReviewRepository reviewRepository;
    @Inject SharedPreferencesManager prefsManager;

    private static final String ARG_USER_ID = "user_id";
    private static final String ARG_USER_NAME = "user_name";
    private static final String ARG_ACTIVITY_ID = "activity_id";

    private Long userId;
    private String userName;
    private Long activityId;

    private ImageView star1, star2, star3, star4, star5;
    private int selectedRating = 0;
    private TextInputEditText commentInput;

    private OnReviewSubmittedListener listener;

    public interface OnReviewSubmittedListener {
        void onReviewSubmitted();
    }

    public static ReviewDialog newInstance(Long userId, String userName, Long activityId) {
        ReviewDialog dialog = new ReviewDialog();
        Bundle args = new Bundle();
        args.putLong(ARG_USER_ID, userId);
        args.putString(ARG_USER_NAME, userName);
        args.putLong(ARG_ACTIVITY_ID, activityId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getLong(ARG_USER_ID);
            userName = getArguments().getString(ARG_USER_NAME);
            activityId = getArguments().getLong(ARG_ACTIVITY_ID);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_review, null);

        // Find star ImageViews
        star1 = view.findViewById(R.id.star1);
        star2 = view.findViewById(R.id.star2);
        star3 = view.findViewById(R.id.star3);
        star4 = view.findViewById(R.id.star4);
        star5 = view.findViewById(R.id.star5);

        commentInput = view.findViewById(R.id.commentInput);
        MaterialButton cancelButton = view.findViewById(R.id.cancelButton);
        MaterialButton submitButton = view.findViewById(R.id.submitButton);
        ImageView closeButton = view.findViewById(R.id.closeButton);

        // Setup star click listeners
        setupStarListeners();

        closeButton.setOnClickListener(v -> dismiss());
        cancelButton.setOnClickListener(v -> dismiss());
        submitButton.setOnClickListener(v -> submitReview());

        dialog.setContentView(view);
        return dialog;
    }

    private void setupStarListeners() {
        star1.setOnClickListener(v -> setRating(1));
        star2.setOnClickListener(v -> setRating(2));
        star3.setOnClickListener(v -> setRating(3));
        star4.setOnClickListener(v -> setRating(4));
        star5.setOnClickListener(v -> setRating(5));
    }

    private void setRating(int rating) {
        selectedRating = rating;
        updateStarDisplay();
    }

    private void updateStarDisplay() {
        // Update star images based on selected rating
        star1.setImageResource(
                selectedRating >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        star2.setImageResource(
                selectedRating >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        star3.setImageResource(
                selectedRating >= 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        star4.setImageResource(
                selectedRating >= 4 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        star5.setImageResource(
                selectedRating >= 5 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
    }

    private void submitReview() {
        int rating = selectedRating;
        String comment = commentInput.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(requireContext(), "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(requireContext(), "Please add a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        Long currentUserId = prefsManager.getUserId();
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d(
                "ReviewDialog",
                "Submitting review - currentUserId: "
                        + currentUserId
                        + ", reviewedUserId: "
                        + userId
                        + ", activityId: "
                        + activityId
                        + ", rating: "
                        + rating
                        + ", comment: "
                        + comment);

        ReviewRequest request = new ReviewRequest(userId, activityId, rating, comment);

        reviewRepository.createReview(
                currentUserId,
                request,
                new ApiCallback<Review>() {
                    @Override
                    public void onSuccess(Review review) {
                        Toast.makeText(
                                        requireContext(),
                                        "Review submitted successfully",
                                        Toast.LENGTH_SHORT)
                                .show();
                        if (listener != null) {
                            listener.onReviewSubmitted();
                        }
                        dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        android.util.Log.e("ReviewDialog", "Error submitting review: " + error);
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setOnReviewSubmittedListener(OnReviewSubmittedListener listener) {
        this.listener = listener;
    }
}
