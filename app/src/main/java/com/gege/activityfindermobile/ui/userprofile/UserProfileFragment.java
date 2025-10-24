package com.gege.activityfindermobile.ui.userprofile;

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

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.utils.ImageLoader;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class UserProfileFragment extends Fragment {

    @Inject UserRepository userRepository;

    private Long userId;
    private TextView tvFullName, tvBadge, tvRating, tvCompletedActivities, tvBio;
    private CircleImageView ivProfileImage;
    private ChipGroup chipGroupInterests;
    private CircularProgressIndicator progressLoading;
    private View cardBio, cardInterests;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvBadge = view.findViewById(R.id.tv_badge);
        tvRating = view.findViewById(R.id.tv_rating);
        tvCompletedActivities = view.findViewById(R.id.tv_completed_activities);
        tvBio = view.findViewById(R.id.tv_bio);
        chipGroupInterests = view.findViewById(R.id.chip_group_interests);
        progressLoading = view.findViewById(R.id.progress_loading);
        cardBio = view.findViewById(R.id.card_bio);
        cardInterests = view.findViewById(R.id.card_interests);

        // Get userId from arguments
        if (getArguments() != null) {
            userId = getArguments().getLong("userId", 0L);
        }

        if (userId != null && userId != 0L) {
            loadUserProfile();
        } else {
            Toast.makeText(requireContext(), "Invalid user ID", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }
    }

    private void loadUserProfile() {
        progressLoading.setVisibility(View.VISIBLE);

        userRepository.getUserById(
                userId,
                new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        progressLoading.setVisibility(View.GONE);
                        displayUserProfile(user);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressLoading.setVisibility(View.GONE);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load profile: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void displayUserProfile(User user) {
        Log.d("UserProfile", "Displaying user: " + user.getFullName());
        Log.d("UserProfile", "Bio: " + user.getBio());
        Log.d(
                "UserProfile",
                "Interests: "
                        + (user.getInterests() != null ? user.getInterests().size() : "null"));

        // Load profile image
        ImageLoader.loadCircularProfileImage(
                requireContext(), user.getProfileImageUrl(), ivProfileImage);

        // Set name
        if (user.getFullName() != null) {
            tvFullName.setText(user.getFullName());
        } else {
            tvFullName.setText("Unknown User");
        }

        // Set badge
        if (user.getBadge() != null && !user.getBadge().isEmpty()) {
            tvBadge.setText(user.getBadge());
            tvBadge.setVisibility(View.VISIBLE);
        }

        // Set rating
        if (user.getRating() != null) {
            tvRating.setText(String.format("%.1f", user.getRating()));
        } else {
            tvRating.setText("N/A");
        }

        // Set completed activities count
        if (user.getCompletedActivities() != null) {
            tvCompletedActivities.setText(user.getCompletedActivities() + " activities");
        } else {
            tvCompletedActivities.setText("0 activities");
        }

        // Set bio
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvBio.setText(user.getBio());
            cardBio.setVisibility(View.VISIBLE);
        } else {
            // Show placeholder or hide card
            tvBio.setText("This user hasn't added a bio yet.");
            tvBio.setTextColor(getResources().getColor(R.color.text_hint, null));
            cardBio.setVisibility(View.VISIBLE);
        }

        // Set interests
        List<String> interests = user.getInterests();
        if (interests != null && !interests.isEmpty()) {
            chipGroupInterests.removeAllViews();
            for (String interest : interests) {
                Chip chip = new Chip(requireContext());
                chip.setText(interest);
                chip.setChipBackgroundColorResource(R.color.primary_light);
                chip.setClickable(false);
                chipGroupInterests.addView(chip);
            }
            cardInterests.setVisibility(View.VISIBLE);
        } else {
            // Show placeholder chip
            chipGroupInterests.removeAllViews();
            Chip chip = new Chip(requireContext());
            chip.setText("No interests added yet");
            chip.setChipBackgroundColorResource(R.color.gray_light);
            chip.setTextColor(getResources().getColor(R.color.text_hint, null));
            chip.setClickable(false);
            chipGroupInterests.addView(chip);
            cardInterests.setVisibility(View.VISIBLE);
        }
    }
}
