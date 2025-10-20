package com.gege.activityfindermobile.ui.auth;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.dto.UserProfileUpdateRequest;
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class ProfileSetupFragment extends Fragment {

    @Inject
    UserRepository userRepository;

    @Inject
    SharedPreferencesManager prefsManager;

    private CircleImageView ivProfilePicture;
    private MaterialButton btnChoosePhoto, btnRemovePhoto, btnContinue, btnSkip;
    private TextInputEditText etBio;
    private ChipGroup chipGroupInterests;
    private CircularProgressIndicator progressLoading;

    private Uri selectedImageUri;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivProfilePicture.setImageURI(uri);
                        btnRemovePhoto.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        btnChoosePhoto = view.findViewById(R.id.btn_choose_photo);
        btnRemovePhoto = view.findViewById(R.id.btn_remove_photo);
        btnContinue = view.findViewById(R.id.btn_continue);
        btnSkip = view.findViewById(R.id.btn_skip);
        etBio = view.findViewById(R.id.et_bio);
        chipGroupInterests = view.findViewById(R.id.chip_group_interests);
        progressLoading = view.findViewById(R.id.progress_loading);
    }

    private void setupListeners() {
        btnChoosePhoto.setOnClickListener(v -> openImagePicker());

        btnRemovePhoto.setOnClickListener(v -> {
            selectedImageUri = null;
            ivProfilePicture.setImageResource(R.drawable.ic_person);
            btnRemovePhoto.setVisibility(View.GONE);
        });

        btnContinue.setOnClickListener(v -> saveProfileAndContinue());

        btnSkip.setOnClickListener(v -> navigateToFeed());
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void saveProfileAndContinue() {
        String bio = etBio.getText().toString().trim();
        List<String> interests = getSelectedInterests();

        // If nothing is filled, just skip
        if (bio.isEmpty() && interests.isEmpty() && selectedImageUri == null) {
            navigateToFeed();
            return;
        }

        // Show loading
        setLoading(true);

        // Get user ID
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show();
            navigateToFeed();
            return;
        }

        // Create update request
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        if (!bio.isEmpty()) {
            request.setBio(bio);
        }
        if (!interests.isEmpty()) {
            request.setInterests(interests);
        }
        // TODO: Upload profile image - for now we'll skip this
        // In production, you'd upload the image to server and get URL

        // Update profile
        userRepository.updateUserProfile(userId, userId, request, new ApiCallback<User>() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                navigateToFeed();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                // Even if update fails, still let them continue
                Toast.makeText(requireContext(), "Could not update profile, but you can edit it later", Toast.LENGTH_SHORT).show();
                navigateToFeed();
            }
        });
    }

    private List<String> getSelectedInterests() {
        List<String> interests = new ArrayList<>();

        for (int i = 0; i < chipGroupInterests.getChildCount(); i++) {
            View child = chipGroupInterests.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    interests.add(chip.getText().toString());
                }
            }
        }

        return interests;
    }

    private void navigateToFeed() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_profileSetupFragment_to_nav_feed);
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnContinue.setEnabled(false);
            btnSkip.setEnabled(false);
            btnChoosePhoto.setEnabled(false);
            progressLoading.setVisibility(View.VISIBLE);
        } else {
            btnContinue.setEnabled(true);
            btnSkip.setEnabled(true);
            btnChoosePhoto.setEnabled(true);
            progressLoading.setVisibility(View.GONE);
        }
    }
}
