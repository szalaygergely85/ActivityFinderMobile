package com.gege.activityfindermobile.ui.profile;

import android.os.Bundle;
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
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.ui.adapters.PhotoGalleryAdapter;
import com.gege.activityfindermobile.utils.ImageLoader;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class ProfileFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @Inject SharedPreferencesManager prefsManager;

    @Inject UserRepository userRepository;

    private CircleImageView ivProfileAvatar;
    private TextView tvName, tvEmail, tvBio, tvRatingValue, tvActivitiesCount, tvUserLocation, tvPhotoCount;
    private Chip chipBadge;
    private ChipGroup chipGroupInterests;
    private MaterialButton btnLogout, btnEditProfile, btnSetLocation;
    private CircularProgressIndicator progressLoading;
    private MaterialAutoCompleteTextView actvCity;
    private TextInputLayout tilCity;
    private View cardPhotos, layoutPhotosEmpty;
    private RecyclerView rvUserPhotos;
    private PhotoGalleryAdapter photoGalleryAdapter;

    private User currentUser;
    private GoogleApiClient googleApiClient;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initGoogleApiClient();
        setupCityAutocomplete();
        setupListeners();
        loadUserProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile when returning from edit screen
        loadUserProfile();
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    private void initGoogleApiClient() {
        if (googleApiClient != null) {
            return;
        }
        googleApiClient =
                new GoogleApiClient.Builder(requireContext())
                        .addApi(Places.GEO_DATA_API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
        // Connect immediately
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        android.util.Log.d("ProfileFragment", "GoogleApiClient connected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        android.util.Log.d("ProfileFragment", "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        android.util.Log.e(
                "ProfileFragment",
                "GoogleApiClient connection failed: " + connectionResult.getErrorMessage());
    }

    private void initViews(View view) {
        ivProfileAvatar = view.findViewById(R.id.iv_profile_avatar);
        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvBio = view.findViewById(R.id.tv_bio);
        tvRatingValue = view.findViewById(R.id.tv_rating_value);
        tvActivitiesCount = view.findViewById(R.id.tv_activities_count);
        tvUserLocation = view.findViewById(R.id.tv_user_location);
        chipBadge = view.findViewById(R.id.chip_badge);
        chipGroupInterests = view.findViewById(R.id.chip_group_interests);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnSetLocation = view.findViewById(R.id.btn_set_location);
        progressLoading = view.findViewById(R.id.progress_loading);
        tilCity = view.findViewById(R.id.til_city);
        actvCity = view.findViewById(R.id.actv_city);
        cardPhotos = view.findViewById(R.id.card_photos);
        rvUserPhotos = view.findViewById(R.id.rv_user_photos);
        layoutPhotosEmpty = view.findViewById(R.id.layout_photos_empty);
        tvPhotoCount = view.findViewById(R.id.tv_photo_count);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        btnEditProfile.setOnClickListener(v -> navigateToEditProfile());

        // Set location button
        btnSetLocation.setOnClickListener(v -> showCityDialog());
    }

    private void setupCityAutocomplete() {
        // Set up city autocomplete using old free Places API
        PlacesAutocompleteAdapter adapter = new PlacesAutocompleteAdapter(requireContext());
        actvCity.setAdapter(adapter);
        actvCity.setThreshold(1);

        // Listen for text changes to fetch predictions
        actvCity.addTextChangedListener(
                new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() > 0) {
                            adapter.fetchPredictions(s.toString());
                            actvCity.post(() -> actvCity.showDropDown());
                        } else {
                            actvCity.dismissDropDown();
                        }
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {}
                });

        // Handle city selection
        actvCity.setOnItemClickListener(
                (parent, view, position, id) -> {
                    String selectedCity = adapter.getItem(position);
                    if (selectedCity != null) {
                        updateUserCity(selectedCity);
                    }
                });
    }

    private void showCityDialog() {
        // Show a dialog with autocomplete field for city selection
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Enter Your City");

        // Create autocomplete text view
        MaterialAutoCompleteTextView actvDialog =
                new MaterialAutoCompleteTextView(requireContext());
        actvDialog.setHint("Type city name...");
        actvDialog.setMinimumHeight(48);

        // Set up adapter for the dialog
        PlacesAutocompleteAdapter adapter = new PlacesAutocompleteAdapter(requireContext());
        actvDialog.setAdapter(adapter);
        actvDialog.setThreshold(1);

        // Listen for text changes
        actvDialog.addTextChangedListener(
                new android.text.TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.length() > 0) {
                            adapter.fetchPredictions(s.toString());
                            actvDialog.post(() -> actvDialog.showDropDown());
                        } else {
                            actvDialog.dismissDropDown();
                        }
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {}
                });

        // Handle selection from dropdown
        actvDialog.setOnItemClickListener(
                (parent, view, position, id) -> {
                    String selectedCity = adapter.getItem(position);
                    if (selectedCity != null) {
                        updateUserCity(selectedCity);
                    }
                });

        // Add padding
        android.widget.FrameLayout container = new android.widget.FrameLayout(requireContext());
        android.widget.FrameLayout.LayoutParams params =
                new android.widget.FrameLayout.LayoutParams(
                        android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                        android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(24, 24, 24, 24);
        actvDialog.setLayoutParams(params);
        container.addView(actvDialog);

        builder.setView(container);
        builder.setPositiveButton(
                "Confirm",
                (dialog, which) -> {
                    String city = actvDialog.getText().toString().trim();
                    if (!city.isEmpty()) {
                        updateUserCity(city);
                    }
                });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /** City autocomplete adapter using Google Places API */
    private class PlacesAutocompleteAdapter extends android.widget.ArrayAdapter<String> {
        private final List<String> filteredCities = new ArrayList<>();

        public PlacesAutocompleteAdapter(android.content.Context context) {
            super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        }

        public void fetchPredictions(String query) {
            android.util.Log.d("PlacesAdapter", "Fetching predictions for: " + query);

            filteredCities.clear();

            // Use Google Places API if connected
            if (googleApiClient != null && googleApiClient.isConnected()) {
                android.util.Log.d("PlacesAdapter", "GoogleApiClient is connected, calling API");
                fetchFromGooglePlaces(query);
            } else {
                android.util.Log.d("PlacesAdapter", "GoogleApiClient not connected yet");
                // Show message to user that they need to wait for connection
                notifyDataSetChanged();
            }
        }

        private void fetchFromGooglePlaces(String query) {
            try {
                // Create autocomplete filter to only get cities
                AutocompleteFilter typeFilter =
                        new AutocompleteFilter.Builder()
                                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                                .build();

                android.util.Log.d("PlacesAdapter", "Calling Places API with filter: CITIES");

                // Request predictions from Google Places API
                PendingResult<AutocompletePredictionBuffer> result =
                        Places.GeoDataApi.getAutocompletePredictions(
                                googleApiClient, query, null, typeFilter);

                // Set a timeout and callback - wait up to 5 seconds
                result.setResultCallback(
                        autocompletePredictions -> {
                            if (autocompletePredictions != null) {
                                Status status = autocompletePredictions.getStatus();
                                android.util.Log.d(
                                        "PlacesAdapter",
                                        "Places API Response Status: "
                                                + status.getStatusCode()
                                                + " - "
                                                + status.getStatusMessage());

                                if (status.isSuccess()) {
                                    // Get city names from predictions
                                    for (AutocompletePrediction prediction :
                                            autocompletePredictions) {
                                        String description =
                                                prediction.getFullText(null).toString();
                                        android.util.Log.d(
                                                "PlacesAdapter", "Got prediction: " + description);
                                        if (description != null && !description.isEmpty()) {
                                            filteredCities.add(description);
                                        }
                                    }
                                    autocompletePredictions.release();
                                    android.util.Log.d(
                                            "PlacesAdapter",
                                            "Got " + filteredCities.size() + " from Google Places");
                                } else {
                                    android.util.Log.e(
                                            "PlacesAdapter",
                                            "Places API status not success. Status: "
                                                    + status.getStatusMessage());
                                }
                            } else {
                                android.util.Log.e("PlacesAdapter", "Places API returned null");
                            }
                            notifyDataSetChanged();
                        },
                        5,
                        java.util.concurrent.TimeUnit.SECONDS);
            } catch (SecurityException e) {
                android.util.Log.e(
                        "PlacesAdapter", "SecurityException in Places API: " + e.getMessage(), e);
                notifyDataSetChanged();
            } catch (Exception e) {
                android.util.Log.e(
                        "PlacesAdapter", "Exception in Places API: " + e.getMessage(), e);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return filteredCities.size();
        }

        @Override
        public String getItem(int position) {
            if (position < filteredCities.size()) {
                return filteredCities.get(position);
            }
            return null;
        }
    }

    private void updateUserCity(String city) {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressLoading.setVisibility(View.VISIBLE);

        userRepository.updateUserLocation(
                userId,
                city,
                new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        progressLoading.setVisibility(View.GONE);
                        currentUser = user;
                        displayUserLocation();
                        Toast.makeText(
                                        requireContext(),
                                        "City updated successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressLoading.setVisibility(View.GONE);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to update city: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void navigateToEditProfile() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_profile_to_editProfileFragment);
    }

    private void loadUserProfile() {
        Long userId = prefsManager.getUserId();

        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Show loading
        progressLoading.setVisibility(View.VISIBLE);

        // Fetch user profile
        userRepository.getUserById(
                userId,
                new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        progressLoading.setVisibility(View.GONE);
                        currentUser = user;
                        displayUserProfile(user);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        progressLoading.setVisibility(View.GONE);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load profile: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void displayUserProfile(User user) {
        // Basic info
        tvName.setText(user.getFullName());
        tvEmail.setText(user.getEmail());

        // Bio
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            tvBio.setText(user.getBio());
        } else {
            tvBio.setText("No bio added yet");
            tvBio.setTextColor(getResources().getColor(R.color.text_hint, null));
        }

        // Stats
        if (user.getRating() != null) {
            tvRatingValue.setText(String.format("%.1f", user.getRating()));
        } else {
            tvRatingValue.setText("N/A");
        }

        if (user.getCompletedActivities() != null) {
            tvActivitiesCount.setText(String.valueOf(user.getCompletedActivities()));
        } else {
            tvActivitiesCount.setText("0");
        }

        // Badge
        if (user.getBadge() != null && !user.getBadge().isEmpty()) {
            chipBadge.setText(user.getBadge());
            chipBadge.setVisibility(View.VISIBLE);
        } else {
            chipBadge.setVisibility(View.GONE);
        }

        // Interests
        chipGroupInterests.removeAllViews();
        List<String> interests = user.getInterests();
        if (interests != null && !interests.isEmpty()) {
            for (String interest : interests) {
                Chip chip = new Chip(requireContext());
                chip.setText(interest);
                chip.setChipBackgroundColorResource(R.color.primary_light);
                chip.setClickable(false);
                chipGroupInterests.addView(chip);
            }
        } else {
            // Show "No interests" message
            Chip chip = new Chip(requireContext());
            chip.setText("No interests added");
            chip.setChipBackgroundColorResource(R.color.gray_light);
            chip.setClickable(false);
            chipGroupInterests.addView(chip);
        }

        // Load profile image
        ImageLoader.loadCircularProfileImage(
                requireContext(), user.getProfileImageUrl(), ivProfileAvatar);

        // Display user location
        displayUserLocation();

        // Set photos
        List<UserPhoto> photos = user.getPhotos();
        if (photos != null && !photos.isEmpty()) {
            setupPhotosAdapter(photos);
            cardPhotos.setVisibility(View.VISIBLE);
        } else {
            layoutPhotosEmpty.setVisibility(View.VISIBLE);
            cardPhotos.setVisibility(View.VISIBLE);
        }
    }

    private void displayUserLocation() {
        if (currentUser != null
                && currentUser.getCity() != null
                && !currentUser.getCity().isEmpty()) {
            String locationText = "📍 " + currentUser.getCity();
            tvUserLocation.setText(locationText);
            tvUserLocation.setVisibility(View.VISIBLE);
            actvCity.setText(currentUser.getCity());
        } else {
            tvUserLocation.setText("No city set");
            tvUserLocation.setVisibility(View.VISIBLE);
            actvCity.setText("");
        }
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(
                        "Logout",
                        (dialog, which) -> {
                            performLogout();
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        // Clear user session
        prefsManager.clearUserSession();

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate to login screen
        navigateToLogin();
    }

    private void navigateToLogin() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_nav_profile_to_loginFragment);
    }

    private void setupPhotosAdapter(List<UserPhoto> photos) {
        photoGalleryAdapter =
                new PhotoGalleryAdapter(
                        photos,
                        new PhotoGalleryAdapter.OnPhotoActionListener() {
                            @Override
                            public void onSetAsProfile(UserPhoto photo) {
                                // Not available in profile view
                            }

                            @Override
                            public void onDeletePhoto(UserPhoto photo) {
                                // Not available in profile view
                            }

                            @Override
                            public void onPhotoClick(UserPhoto photo) {
                                // Open full-screen photo viewer
                                openPhotoViewer(photos, photos.indexOf(photo));
                            }
                        });
        photoGalleryAdapter.setEditMode(false);
        rvUserPhotos.setAdapter(photoGalleryAdapter);
        tvPhotoCount.setText(photos.size() + "/6");
        layoutPhotosEmpty.setVisibility(View.GONE);
    }

    private void openPhotoViewer(List<UserPhoto> photos, int position) {
        // First, check if the navigation action exists
        try {
            Bundle bundle = new Bundle();
            bundle.putSerializable("photos", new ArrayList<>(photos));
            bundle.putInt("position", position);
            bundle.putBoolean("editMode", false);

            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.photoViewerFragment, bundle);
        } catch (Exception e) {
            // If navigation fails, show a toast
            Toast.makeText(requireContext(), "Unable to open photo viewer", Toast.LENGTH_SHORT).show();
        }
    }
}
