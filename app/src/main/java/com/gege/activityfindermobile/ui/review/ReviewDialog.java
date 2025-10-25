package com.gege.activityfindermobile.ui.review;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.dto.ReviewRequest;
import com.gege.activityfindermobile.data.model.Review;
import com.gege.activityfindermobile.data.repository.ReviewRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
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

    private RatingBar ratingBar;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        android.view.LayoutInflater inflater = requireActivity().getLayoutInflater();
        android.view.View view = inflater.inflate(R.layout.dialog_review, null);

        ratingBar = view.findViewById(R.id.ratingBar);
        commentInput = view.findViewById(R.id.commentInput);
        MaterialButton cancelButton = view.findViewById(R.id.cancelButton);
        MaterialButton submitButton = view.findViewById(R.id.submitButton);

        // Set title with user name
        builder.setTitle("Review " + userName);

        cancelButton.setOnClickListener(v -> dismiss());
        submitButton.setOnClickListener(v -> submitReview());

        builder.setView(view);
        return builder.create();
    }

    private void submitReview() {
        int rating = (int) ratingBar.getRating();
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

        android.util.Log.d("ReviewDialog", "Submitting review - currentUserId: " + currentUserId +
                ", reviewedUserId: " + userId + ", activityId: " + activityId +
                ", rating: " + rating + ", comment: " + comment);

        ReviewRequest request = new ReviewRequest(userId, activityId, rating, comment);

        reviewRepository.createReview(currentUserId, request, new ApiCallback<Review>() {
            @Override
            public void onSuccess(Review review) {
                Toast.makeText(requireContext(), "Review submitted successfully", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onReviewSubmitted();
                }
                dismiss();
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("ReviewDialog", "Error submitting review: " + error);
                Toast.makeText(requireContext(), "Failed to submit review: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setOnReviewSubmittedListener(OnReviewSubmittedListener listener) {
        this.listener = listener;
    }
}
