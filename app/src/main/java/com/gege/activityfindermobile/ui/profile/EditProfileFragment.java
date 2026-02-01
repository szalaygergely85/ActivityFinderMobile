package com.gege.activityfindermobile.ui.profile;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.dto.UserProfileUpdateRequest;
import com.gege.activityfindermobile.data.model.Category;
import com.gege.activityfindermobile.data.model.ImageUploadResponse;
import com.gege.activityfindermobile.data.model.User;
import com.gege.activityfindermobile.data.model.UserPhoto;
import com.gege.activityfindermobile.data.repository.UserPhotoRepository;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.utils.CategoryManager;
import com.gege.activityfindermobile.ui.adapters.PhotoGalleryAdapter;
import com.gege.activityfindermobile.utils.ImageLoader;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;

@AndroidEntryPoint
public class EditProfileFragment extends Fragment {

    @Inject UserRepository userRepository;

    @Inject UserPhotoRepository userPhotoRepository;

    @Inject SharedPreferencesManager prefsManager;

    @Inject CategoryManager categoryManager;

    private CircleImageView ivProfilePicture;
    private TextInputEditText etFullName, etBio;
    private ChipGroup chipGroupInterests;
    private MaterialButton btnSave, btnUploadPhoto;
    private CircularProgressIndicator progressLoading;
    private RecyclerView rvMyPhotos;
    private TextView tvPhotoCount;
    private View frameProfilePicture, layoutPhotosEmpty;

    // private MaterialAutoCompleteTextView actvCity;
    // private TextInputLayout tilCity;

    private User currentUser;
    private List<String> selectedInterests = new ArrayList<>();
    private ActivityResultLauncher<String> photoPickerLauncher;
    private PhotoGalleryAdapter photoGalleryAdapter;
    private List<UserPhoto> userPhotos = new ArrayList<>();
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private android.os.Handler debounceHandler = new android.os.Handler();
    private Runnable debounceRunnable;
    private boolean isSelectingItem = false;
    private String selectedPlaceId;
    private Double selectedLatitude;
    private Double selectedLongitude;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register photo picker for gallery
        photoPickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                uploadPhotoToGallery(uri);
                            }
                        });
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupBackButton(view);
        initPlacesClient();
        // setupCityAutocomplete();
        loadCurrentProfile();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Clean up debounce handler
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
    }

    private void initPlacesClient() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getString(R.string.google_maps_key));
        }
        placesClient = Places.createClient(requireContext());
        sessionToken = AutocompleteSessionToken.newInstance();
    }

    private void initViews(View view) {
        ivProfilePicture = view.findViewById(R.id.iv_profile_picture);
        frameProfilePicture = view.findViewById(R.id.frame_profile_picture);
        etFullName = view.findViewById(R.id.et_full_name);
        etBio = view.findViewById(R.id.et_bio);
        chipGroupInterests = view.findViewById(R.id.chip_group_interests);
        btnSave = view.findViewById(R.id.btn_save);
        progressLoading = view.findViewById(R.id.progress_loading);
        btnUploadPhoto = view.findViewById(R.id.btn_upload_photo);
        rvMyPhotos = view.findViewById(R.id.rv_my_photos);
        tvPhotoCount = view.findViewById(R.id.tv_photo_count);
        layoutPhotosEmpty = view.findViewById(R.id.layout_photos_empty);
        /*
                actvCity = view.findViewById(R.id.actv_city);
                tilCity = view.findViewById(R.id.til_city);
        */
        btnSave.setOnClickListener(v -> saveProfile());
        btnUploadPhoto.setOnClickListener(v -> openPhotoGalleryPicker());
        frameProfilePicture.setOnClickListener(v -> openPhotoGalleryPicker());
    }

    private void setupBackButton(View view) {
        view.findViewById(R.id.btn_back)
                .setOnClickListener(
                        v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }

    /*
        private void setupCityAutocomplete() {
            PlacesAutocompleteAdapter adapter = new PlacesAutocompleteAdapter(requireContext());
            actvCity.setAdapter(adapter);
            actvCity.setThreshold(2); // Minimum 2 characters before showing suggestions

            actvCity.setOnItemClickListener(
                    (parent, view, position, id) -> {
                        // Set flag to prevent text change listener from triggering
                        isSelectingItem = true;
                        String selectedCity = adapter.getItem(position);
                        String placeId = adapter.getPlaceId(position);
                        if (placeId != null) {
                            fetchPlaceDetails(placeId, selectedCity);
                        }
                        // Dismiss dropdown and clear flag after a short delay
                        actvCity.postDelayed(
                                () -> {
                                    actvCity.dismissDropDown();
                                    isSelectingItem = false;
                                },
                                100);
                    });

            actvCity.addTextChangedListener(
                    new android.text.TextWatcher() {
                        @Override
                        public void beforeTextChanged(
                                CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            // Skip if user is selecting an item
                            if (isSelectingItem) {
                                return;
                            }

                            // Remove any pending callbacks
                            if (debounceRunnable != null) {
                                debounceHandler.removeCallbacks(debounceRunnable);
                            }

                            // Only search if at least 2 characters
                            if (s.length() >= 2) {
                                // Create new runnable for debounced API call
                                debounceRunnable =
                                        () -> {
                                            adapter.fetchPredictions(s.toString());
                                            actvCity.post(() -> actvCity.showDropDown());
                                        };
                                // Wait 800ms before making the API call
                                debounceHandler.postDelayed(debounceRunnable, 800);
                            } else {
                                actvCity.dismissDropDown();
                            }
                        }

                        @Override
                        public void afterTextChanged(android.text.Editable s) {}
                    });
        }


        private void fetchPlaceDetails(String placeId, String cityName) {
            List<Place.Field> placeFields = List.of(Place.Field.LAT_LNG);
            FetchPlaceRequest request =
                    FetchPlaceRequest.builder(placeId, placeFields)
                            .setSessionToken(sessionToken)
                            .build();

            placesClient
                    .fetchPlace(request)
                    .addOnSuccessListener(
                            (FetchPlaceResponse response) -> {
                                Place place = response.getPlace();
                                if (place.getLatLng() != null) {
                                    selectedPlaceId = placeId;
                                    selectedLatitude = place.getLatLng().latitude;
                                    selectedLongitude = place.getLatLng().longitude;
                                    android.util.Log.d(
                                            "EditProfile",
                                            "Selected city: "
                                                    + cityName
                                                    + " at ("
                                                    + selectedLatitude
                                                    + ", "
                                                    + selectedLongitude
                                                    + ")");
                                }
                                // Regenerate token after successful place details fetch
                                sessionToken = AutocompleteSessionToken.newInstance();
                            })
                    .addOnFailureListener(
                            (exception) -> {
                                android.util.Log.e(
                                        "EditProfile",
                                        "Error fetching place details: " + exception.getMessage());
                                // Still regenerate token
                                sessionToken = AutocompleteSessionToken.newInstance();
                            });
        }

        private class PlacesAutocompleteAdapter extends android.widget.ArrayAdapter<String> {
            private final List<String> filteredCities = new ArrayList<>();
            private final List<String> filteredPlaceIds = new ArrayList<>();

            public PlacesAutocompleteAdapter(android.content.Context context) {
                super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
            }

            public void fetchPredictions(String query) {
                filteredCities.clear();
                filteredPlaceIds.clear();

                if (placesClient != null) {
                    fetchFromGooglePlaces(query);
                } else {
                    notifyDataSetChanged();
                }
            }

            public String getPlaceId(int position) {
                if (position < filteredPlaceIds.size()) {
                    return filteredPlaceIds.get(position);
                }
                return null;
            }

            private void fetchFromGooglePlaces(String query) {
                try {
                    // Create autocomplete request for cities only
                    FindAutocompletePredictionsRequest request =
                            FindAutocompletePredictionsRequest.builder()
                                    .setSessionToken(sessionToken)
                                    .setCountries("HU")
                                    .setTypesFilter(List.of(PlaceTypes.CITIES))
                                    .setQuery(query)
                                    .build();

                    placesClient
                            .findAutocompletePredictions(request)
                            .addOnSuccessListener(
                                    (FindAutocompletePredictionsResponse response) -> {
                                        filteredCities.clear();
                                        filteredPlaceIds.clear();
                                        response.getAutocompletePredictions()
                                                .forEach(
                                                        prediction -> {
                                                            String description =
                                                                    prediction
                                                                            .getFullText(null)
                                                                            .toString();
                                                            if (description != null
                                                                    && !description.isEmpty()) {
                                                                filteredCities.add(description);
                                                                filteredPlaceIds.add(
                                                                        prediction.getPlaceId());
                                                            }
                                                        });
                                        notifyDataSetChanged();
                                    })
                            .addOnFailureListener(
                                    exception -> {
                                        android.util.Log.e(
                                                "PlacesAdapter",
                                                "Error fetching predictions: " + exception.getMessage(),
                                                exception);
                                        notifyDataSetChanged();
                                    });
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
    */
    private void loadCurrentProfile() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return;
        }

        setLoading(true);

        userRepository.getUserById(
                userId,
                new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        setLoading(false);
                        currentUser = user;
                        displayUserData(user);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load profile: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                        requireActivity().onBackPressed();
                    }
                });
    }

    private void displayUserData(User user) {
        // Set current values
        if (user.getFullName() != null) {
            etFullName.setText(user.getFullName());
        }

        if (user.getBio() != null) {
            etBio.setText(user.getBio());
        }

        // Set city and coordinates
        if (user.getCity() != null && !user.getCity().isEmpty()) {
            // actvCity.setText(user.getCity());
            // Store existing place data if available
            selectedPlaceId = user.getPlaceId();
            selectedLatitude = user.getLatitude();
            selectedLongitude = user.getLongitude();
        }

        // Load profile image from photos if exists
        if (user.getPhotos() != null) {
            for (UserPhoto photo : user.getPhotos()) {
                if (photo.getIsProfilePicture() != null && photo.getIsProfilePicture()) {
                    ImageLoader.loadCircularProfileImage(
                            requireContext(), photo.getPhotoUrl(), ivProfilePicture);
                    break;
                }
            }
        }

        // Store current interests
        if (user.getInterests() != null) {
            selectedInterests.addAll(user.getInterests());
        }

        // Setup interest chips
        setupInterestChips();

        // Load user photos
        loadUserPhotos();
    }

    private void setupInterestChips() {
        chipGroupInterests.removeAllViews();

        // Load categories from CategoryManager (fetched from database)
        List<Category> categories = categoryManager.getCachedCategories();

        if (categories.isEmpty()) {
            // If cache is empty, refresh and try again after a short delay
            categoryManager.refreshCategories();
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                List<Category> refreshedCategories = categoryManager.getCachedCategories();
                populateInterestChips(refreshedCategories);
            }, 1000);
        } else {
            populateInterestChips(categories);
        }
    }

    private void populateInterestChips(List<Category> categories) {
        chipGroupInterests.removeAllViews();

        for (Category category : categories) {
            if (category.getName() != null && !category.getName().isEmpty()) {
                String interest = category.getName();
                Chip chip =
                        (Chip)
                                getLayoutInflater()
                                        .inflate(
                                                R.layout.chip_interest_item, chipGroupInterests, false);
                chip.setText(interest);
                chip.setCheckable(true);
                chip.setChecked(selectedInterests.contains(interest));

                chip.setOnCheckedChangeListener(
                        (buttonView, isChecked) -> {
                            if (isChecked) {
                                if (!selectedInterests.contains(interest)) {
                                    selectedInterests.add(interest);
                                }
                            } else {
                                selectedInterests.remove(interest);
                            }
                        });

                chipGroupInterests.addView(chip);
            }
        }
    }

    private void saveProfile() {
        // Validate input
        String fullName =
                etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String bio = etBio.getText() != null ? etBio.getText().toString().trim() : "";
        // String city = actvCity.getText() != null ? actvCity.getText().toString().trim() : "";

        if (fullName.isEmpty()) {
            etFullName.setError("Name cannot be empty");
            etFullName.requestFocus();
            return;
        }

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Profile image is now managed through the photos upload
        updateProfileData(userId, fullName, bio, null);
    }

    private void updateProfileData(Long userId, String fullName, String bio, String imageUrl) {
        // Create update request
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName(fullName);
        request.setBio(bio.isEmpty() ? null : bio);
        request.setPlaceId(selectedPlaceId);
        request.setLatitude(selectedLatitude);
        request.setLongitude(selectedLongitude);
        request.setInterests(selectedInterests.isEmpty() ? null : selectedInterests);
        request.setProfileImageUrl(imageUrl);

        userRepository.updateUserProfile(
                userId,
                userId,
                request,
                new ApiCallback<User>() {
                    @Override
                    public void onSuccess(User updatedUser) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Profile updated successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // Navigate back to profile
                        requireActivity().onBackPressed();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to update profile: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private File getFileFromUri(Uri uri) {
        try {
            ContentResolver contentResolver = requireContext().getContentResolver();
            String mimeType = contentResolver.getType(uri);
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension == null) extension = "jpg";

            // Create a temporary file
            File tempFile =
                    File.createTempFile(
                            "profile_image_", "." + extension, requireContext().getCacheDir());

            // Copy URI content to file
            InputStream inputStream = contentResolver.openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setLoading(boolean loading) {
        if (loading) {
            progressLoading.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
            btnUploadPhoto.setEnabled(false);
            etFullName.setEnabled(false);
            etBio.setEnabled(false);
            chipGroupInterests.setEnabled(false);
        } else {
            progressLoading.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            btnUploadPhoto.setEnabled(true);
            etFullName.setEnabled(true);
            etBio.setEnabled(true);
            chipGroupInterests.setEnabled(true);
        }
    }

    private void loadUserPhotos() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            setLoading(false);
            return;
        }

        userPhotoRepository.getMyPhotos(
                new ApiCallback<List<UserPhoto>>() {
                    @Override
                    public void onSuccess(List<UserPhoto> photos) {
                        setLoading(false);
                        userPhotos = photos;
                        displayPhotos(photos);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        layoutPhotosEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void loadUserPhotosAndSetFirst() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            setLoading(false);
            return;
        }

        userPhotoRepository.getMyPhotos(
                new ApiCallback<List<UserPhoto>>() {
                    @Override
                    public void onSuccess(List<UserPhoto> photos) {
                        userPhotos = photos;
                        displayPhotos(photos);

                        // Automatically set first photo as profile picture
                        if (photos != null && !photos.isEmpty()) {
                            setPhotoAsProfile(photos.get(0).getId());
                        } else {
                            setLoading(false);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        layoutPhotosEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void displayPhotos(List<UserPhoto> photos) {
        int photoCount = photos != null ? photos.size() : 0;
        tvPhotoCount.setText(photoCount + "/6");
        btnUploadPhoto.setEnabled(photoCount < 6);

        setupPhotoAdapter(photos);
    }

    private void setupPhotoAdapter(List<UserPhoto> photos) {
        int photoCount = photos != null ? photos.size() : 0;
        boolean showAddButton = photoCount < 6; // Show add button if less than 6 photos

        photoGalleryAdapter =
                new PhotoGalleryAdapter(
                        photos,
                        new PhotoGalleryAdapter.OnPhotoActionListener() {
                            @Override
                            public void onSetAsProfile(UserPhoto photo) {
                                setPhotoAsProfile(photo);
                            }

                            @Override
                            public void onDeletePhoto(UserPhoto photo) {
                                deletePhoto(photo);
                            }

                            @Override
                            public void onPhotoClick(UserPhoto photo) {
                                openPhotoViewer(photos, photos.indexOf(photo));
                            }

                            @Override
                            public void onAddPhotoClick() {
                                openPhotoGalleryPicker();
                            }
                        });
        photoGalleryAdapter.setEditMode(true);
        photoGalleryAdapter.setShowAddButton(showAddButton);
        rvMyPhotos.setAdapter(photoGalleryAdapter);
    }

    private void openPhotoGalleryPicker() {
        photoPickerLauncher.launch("image/*");
    }

    private void uploadPhotoToGallery(Uri photoUri) {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = getFileFromUri(photoUri);
        if (photoFile == null) {
            Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if this is the first photo
        boolean isFirstPhoto = userPhotos.isEmpty();

        setLoading(true);
        userPhotoRepository.uploadPhoto(
                photoFile,
                new ApiCallback<ImageUploadResponse>() {
                    @Override
                    public void onSuccess(ImageUploadResponse uploadResponse) {
                        Toast.makeText(
                                        requireContext(),
                                        "Photo uploaded successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // If this is the first photo, reload photos and automatically set it as
                        // profile picture
                        if (isFirstPhoto) {
                            loadUserPhotosAndSetFirst();
                        } else {
                            // Reload photos from backend to get complete data
                            // loadUserPhotos() will call setLoading(false) when done
                            loadUserPhotos();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to upload photo: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void setPhotoAsProfile(UserPhoto photo) {
        setPhotoAsProfile(photo.getId());
    }

    private void setPhotoAsProfile(Long photoId) {
        setLoading(true);
        userPhotoRepository.setPhotoAsProfile(
                photoId,
                new ApiCallback<UserPhoto>() {
                    @Override
                    public void onSuccess(UserPhoto updatedPhoto) {
                        setLoading(false);
                        // Update local list - set all to false first
                        for (UserPhoto p : userPhotos) {
                            p.setIsProfilePicture(false);
                        }
                        // Find and update the specific photo in the list
                        for (UserPhoto p : userPhotos) {
                            if (p.getId().equals(updatedPhoto.getId())) {
                                p.setIsProfilePicture(true);
                                break;
                            }
                        }
                        if (photoGalleryAdapter != null) {
                            photoGalleryAdapter.notifyDataSetChanged();
                        }

                        // Update profile picture display at top
                        ImageLoader.loadCircularProfileImage(
                                requireContext(), updatedPhoto.getPhotoUrl(), ivProfilePicture);

                        Toast.makeText(requireContext(), "Profile picture set!", Toast.LENGTH_SHORT)
                                .show();

                        // Reload photos to get updated data
                        loadUserPhotos();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to set profile picture: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                        // Still reload photos
                        loadUserPhotos();
                    }
                });
    }

    private void deletePhoto(UserPhoto photo) {
        // Check if the photo being deleted is the profile picture
        boolean wasProfilePicture =
                photo.getIsProfilePicture() != null && photo.getIsProfilePicture();

        setLoading(true);
        userPhotoRepository.deletePhoto(
                photo.getId(),
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        userPhotos.remove(photo);
                        displayPhotos(userPhotos);
                        Toast.makeText(
                                        requireContext(),
                                        "Photo deleted successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // If the deleted photo was the profile picture and there are remaining
                        // photos,
                        // automatically set the first one as the new profile picture
                        if (wasProfilePicture && !userPhotos.isEmpty()) {
                            setPhotoAsProfile(userPhotos.get(0).getId());
                        } else {
                            setLoading(false);
                            // If no photos left, reset profile picture to default
                            if (userPhotos.isEmpty()) {
                                ivProfilePicture.setImageResource(R.drawable.ic_person);
                            }
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to delete photo: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void openPhotoViewer(List<UserPhoto> photos, int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("photos", new ArrayList<>(photos));
        bundle.putInt("position", position);
        bundle.putBoolean("editMode", true);
        Navigation.findNavController(requireView())
                .navigate(R.id.action_editProfileFragment_to_photoViewerFragment, bundle);
    }
}
