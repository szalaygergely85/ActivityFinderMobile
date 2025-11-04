package com.gege.activityfindermobile.ui.create;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.dto.ActivityCreateRequest;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.data.repository.ActivityRepository;
import com.gege.activityfindermobile.utils.MapPickerActivity;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceTypes;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateActivityFragment extends Fragment {

    @Inject ActivityRepository activityRepository;

    @Inject SharedPreferencesManager prefsManager;

    private TextInputLayout tilTitle,
            tilDescription,
            tilCategory,
            tilDate,
            tilTime,
            tilLocation,
            tilTotalSpots;
    private TextInputEditText etTitle, etDescription, etDate, etTime, etTotalSpots;
    private MaterialAutoCompleteTextView actvLocation;
    private AutoCompleteTextView etCategory;
    private MaterialButton btnCreate;
    private CircularProgressIndicator progressLoading;

    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();

    // Location variables
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;
    private String selectedLocationName = "";
    private String selectedPlaceId = null;
    private static final int MAP_PICKER_REQUEST_CODE = 100;

    // Places API
    private PlacesClient placesClient;
    private AutocompleteSessionToken sessionToken;
    private android.os.Handler debounceHandler = new android.os.Handler();
    private Runnable debounceRunnable;
    private boolean isSelectingItem = false;

    // Edit mode variables
    private Long editActivityId = null;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        initPlacesClient();
        setupCategoryDropdown();
        setupLocationAutocomplete();
        setupListeners();

        // Check if we're in edit mode
        if (getArguments() != null) {
            editActivityId = getArguments().getLong("activityId", 0L);
            if (editActivityId != 0L) {
                isEditMode = true;
                loadActivityForEditing(editActivityId);
            }
        }
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
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(
                v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigateUp();
                });

        tilTitle = view.findViewById(R.id.til_title);
        tilDescription = view.findViewById(R.id.til_description);
        tilCategory = view.findViewById(R.id.til_category);
        tilDate = view.findViewById(R.id.til_date);
        tilTime = view.findViewById(R.id.til_time);
        tilLocation = view.findViewById(R.id.til_location);
        tilTotalSpots = view.findViewById(R.id.til_total_spots);

        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etCategory = view.findViewById(R.id.et_category);
        etDate = view.findViewById(R.id.et_date);
        etTime = view.findViewById(R.id.et_time);
        actvLocation = view.findViewById(R.id.actv_location);
        etTotalSpots = view.findViewById(R.id.et_total_spots);

        btnCreate = view.findViewById(R.id.btn_create);
        progressLoading = view.findViewById(R.id.progress_loading);
    }

    private void setupCategoryDropdown() {
        String[] categories = {
            "Sports",
            "Social",
            "Outdoor",
            "Food",
            "Travel",
            "Photography",
            "Music",
            "Art",
            "Gaming",
            "Fitness"
        };
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        etCategory.setAdapter(adapter);
    }

    private void setupLocationAutocomplete() {
        PlacesAutocompleteAdapter adapter = new PlacesAutocompleteAdapter(requireContext());
        actvLocation.setAdapter(adapter);
        actvLocation.setThreshold(2); // Minimum 2 characters before showing suggestions

        actvLocation.setOnItemClickListener((parent, view, position, id) -> {
            // Set flag to prevent text change listener from triggering
            isSelectingItem = true;

            // Cancel any pending debounce callbacks
            if (debounceRunnable != null) {
                debounceHandler.removeCallbacks(debounceRunnable);
            }

            String selectedLocation = adapter.getItem(position);
            String placeId = adapter.getPlaceId(position);
            if (placeId != null) {
                fetchPlaceDetails(placeId, selectedLocation);
            }

            // Dismiss dropdown immediately
            actvLocation.dismissDropDown();

            // Clear focus to prevent further interactions
            actvLocation.clearFocus();

            // Reset flag after a longer delay to ensure text change doesn't trigger
            actvLocation.postDelayed(() -> {
                isSelectingItem = false;
            }, 300);
        });

        actvLocation.addTextChangedListener(
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
                        if (s.length() >= 2 && !isSelectingItem) {
                            // Create new runnable for debounced API call
                            debounceRunnable = () -> {
                                adapter.fetchPredictions(s.toString());
                                actvLocation.post(() -> actvLocation.showDropDown());
                            };
                            // Wait 800ms before making the API call
                            debounceHandler.postDelayed(debounceRunnable, 800);
                        } else {
                            actvLocation.dismissDropDown();
                        }
                    }

                    @Override
                    public void afterTextChanged(android.text.Editable s) {}
                });
    }

    private void fetchPlaceDetails(String placeId, String locationName) {
        List<Place.Field> placeFields = List.of(Place.Field.LAT_LNG);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields)
                .setSessionToken(sessionToken)
                .build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener((FetchPlaceResponse response) -> {
                    Place place = response.getPlace();
                    if (place.getLatLng() != null) {
                        selectedPlaceId = placeId;
                        selectedLatitude = place.getLatLng().latitude;
                        selectedLongitude = place.getLatLng().longitude;
                        selectedLocationName = locationName;
                        android.util.Log.d("CreateActivity", "Selected location: " + locationName +
                                " at (" + selectedLatitude + ", " + selectedLongitude + ")");
                    }
                    // Regenerate token after successful place details fetch
                    sessionToken = AutocompleteSessionToken.newInstance();
                })
                .addOnFailureListener((exception) -> {
                    android.util.Log.e("CreateActivity", "Error fetching place details: " + exception.getMessage());
                    // Still regenerate token
                    sessionToken = AutocompleteSessionToken.newInstance();
                });
    }

    private void setupListeners() {
        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Time picker
        etTime.setOnClickListener(v -> showTimePicker());

        // Map picker button (end icon)
        tilLocation.setEndIconOnClickListener(v -> openMapPicker());

        // Create button
        btnCreate.setOnClickListener(v -> validateAndCreateActivity());
    }

    private void openMapPicker() {
        android.content.Intent intent =
                new android.content.Intent(requireContext(), MapPickerActivity.class);
        startActivityForResult(intent, MAP_PICKER_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MAP_PICKER_REQUEST_CODE
                && resultCode == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
            if (data != null) {
                selectedLocationName = data.getStringExtra(MapPickerActivity.EXTRA_PLACE_NAME);
                selectedLatitude = data.getDoubleExtra(MapPickerActivity.EXTRA_LATITUDE, 0.0);
                selectedLongitude = data.getDoubleExtra(MapPickerActivity.EXTRA_LONGITUDE, 0.0);
                selectedPlaceId = null; // Map picker doesn't provide placeId

                // Update UI with location name - prevent autocomplete dropdown
                isSelectingItem = true;
                actvLocation.setText(selectedLocationName);
                isSelectingItem = false;

                android.util.Log.d(
                        "CreateActivity",
                        "Location selected: "
                                + selectedLocationName
                                + " ("
                                + selectedLatitude
                                + ", "
                                + selectedLongitude
                                + ")");
            }
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        requireContext(),
                        (view, year, month, dayOfMonth) -> {
                            selectedDate.set(Calendar.YEAR, year);
                            selectedDate.set(Calendar.MONTH, month);
                            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            SimpleDateFormat dateFormat =
                                    new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                            etDate.setText(dateFormat.format(selectedDate.getTime()));
                        },
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        requireContext(),
                        (view, hourOfDay, minute) -> {
                            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            selectedTime.set(Calendar.MINUTE, minute);

                            SimpleDateFormat timeFormat =
                                    new SimpleDateFormat("hh:mm a", Locale.getDefault());
                            etTime.setText(timeFormat.format(selectedTime.getTime()));
                        },
                        selectedTime.get(Calendar.HOUR_OF_DAY),
                        selectedTime.get(Calendar.MINUTE),
                        false);
        timePickerDialog.show();
    }

    private void validateAndCreateActivity() {
        // Clear errors
        tilTitle.setError(null);
        tilDescription.setError(null);
        tilCategory.setError(null);
        tilDate.setError(null);
        tilTime.setError(null);
        tilLocation.setError(null);
        tilTotalSpots.setError(null);

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = actvLocation.getText().toString().trim();
        String totalSpotsStr = etTotalSpots.getText().toString().trim();

        // Validation
        boolean isValid = true;

        if (title.isEmpty()) {
            tilTitle.setError("Title is required");
            isValid = false;
        }

        if (description.isEmpty()) {
            tilDescription.setError("Description is required");
            isValid = false;
        }

        if (category.isEmpty()) {
            tilCategory.setError("Category is required");
            isValid = false;
        }

        if (date.isEmpty()) {
            tilDate.setError("Date is required");
            isValid = false;
        }

        if (time.isEmpty()) {
            tilTime.setError("Time is required");
            isValid = false;
        }

        if (location.isEmpty()) {
            tilLocation.setError("Location is required");
            isValid = false;
        }

        if (totalSpotsStr.isEmpty()) {
            tilTotalSpots.setError("Total spots is required");
            isValid = false;
        }

        int totalSpots = 0;

        try {
            if (!totalSpotsStr.isEmpty()) {
                totalSpots = Integer.parseInt(totalSpotsStr);
                if (totalSpots < 1) {
                    tilTotalSpots.setError("Must be at least 1");
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            tilTotalSpots.setError("Invalid number");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create or update activity based on mode
        if (isEditMode) {
            updateActivity(title, description, category, date, time, location, totalSpots);
        } else {
            createActivity(title, description, category, date, time, location, totalSpots);
        }
    }

    private void createActivity(
            String title,
            String description,
            String category,
            String date,
            String time,
            String location,
            int totalSpots) {
        // Get user ID
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Combine date and time into ISO 8601 format for backend
        String activityDateTime = combineDateAndTime();
        if (activityDateTime == null) {
            Toast.makeText(requireContext(), "Invalid date/time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        setLoading(true);

        // Create request with location data
        ActivityCreateRequest request =
                new ActivityCreateRequest(
                        title, description, activityDateTime, location, totalSpots, category);

        // Set placeId and coordinates if available
        request.setPlaceId(selectedPlaceId);
        if (selectedLatitude != 0.0 || selectedLongitude != 0.0) {
            request.setLatitude(selectedLatitude);
            request.setLongitude(selectedLongitude);
        }

        // Call API
        activityRepository.createActivity(
                userId,
                request,
                new ApiCallback<Activity>() {
                    @Override
                    public void onSuccess(Activity activity) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Activity created successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // Navigate back to feed
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigateUp();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to create activity: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private String combineDateAndTime() {
        try {
            // Create a new calendar instance to combine date and time
            Calendar combined = Calendar.getInstance();

            // Copy date from selectedDate
            combined.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
            combined.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
            combined.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));

            // Copy time from selectedTime
            combined.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY));
            combined.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE));
            combined.set(Calendar.SECOND, 0);
            combined.set(Calendar.MILLISECOND, 0);

            // Check if date is in the future
            if (combined.getTimeInMillis() <= System.currentTimeMillis()) {
                Toast.makeText(
                                requireContext(),
                                "Activity date must be in the future",
                                Toast.LENGTH_SHORT)
                        .show();
                return null;
            }

            // Format as ISO 8601: yyyy-MM-dd'T'HH:mm:ss
            SimpleDateFormat isoFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            String formatted = isoFormat.format(combined.getTime());

            // Debug log
            android.util.Log.d("CreateActivity", "Formatted date: " + formatted);

            return formatted;
        } catch (Exception e) {
            android.util.Log.e("CreateActivity", "Date format error", e);
            return null;
        }
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnCreate.setEnabled(false);
            progressLoading.setVisibility(View.VISIBLE);
        } else {
            btnCreate.setEnabled(true);
            progressLoading.setVisibility(View.GONE);
        }
    }

    private void loadActivityForEditing(Long activityId) {
        setLoading(true);

        activityRepository.getActivityById(
                activityId,
                new ApiCallback<com.gege.activityfindermobile.data.model.Activity>() {
                    @Override
                    public void onSuccess(
                            com.gege.activityfindermobile.data.model.Activity activity) {
                        setLoading(false);

                        // Populate form fields with activity data
                        etTitle.setText(activity.getTitle());
                        etDescription.setText(activity.getDescription());
                        etCategory.setText(activity.getCategory());

                        // Set flag to prevent autocomplete dropdown from showing
                        isSelectingItem = true;
                        actvLocation.setText(activity.getLocation());
                        isSelectingItem = false;

                        etTotalSpots.setText(String.valueOf(activity.getTotalSpots()));

                        // Set location data if available
                        selectedPlaceId = activity.getPlaceId();
                        if (activity.getLatitude() != null) {
                            selectedLatitude = activity.getLatitude();
                        }
                        if (activity.getLongitude() != null) {
                            selectedLongitude = activity.getLongitude();
                        }

                        // Parse and set date and time
                        try {
                            SimpleDateFormat isoFormat =
                                    new SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault());
                            java.util.Date dateTime = isoFormat.parse(activity.getActivityDate());

                            // Set date
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(dateTime);
                            selectedDate.setTime(dateTime);

                            SimpleDateFormat dateFormat =
                                    new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                            etDate.setText(dateFormat.format(dateTime));

                            // Set time
                            selectedTime.setTime(dateTime);
                            SimpleDateFormat timeFormat =
                                    new SimpleDateFormat("hh:mm a", Locale.getDefault());
                            etTime.setText(timeFormat.format(dateTime));
                        } catch (Exception e) {
                            android.util.Log.e("CreateActivity", "Error parsing activity date", e);
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to load activity: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigateUp();
                    }
                });
    }

    private void updateActivity(
            String title,
            String description,
            String category,
            String date,
            String time,
            String location,
            int totalSpots) {
        // Get user ID
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editActivityId == null || editActivityId == 0L) {
            Toast.makeText(requireContext(), "Invalid activity", Toast.LENGTH_SHORT).show();
            return;
        }

        // Combine date and time into ISO 8601 format for backend
        String activityDateTime = combineDateAndTime();
        if (activityDateTime == null) {
            Toast.makeText(requireContext(), "Invalid date/time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        setLoading(true);

        // Create request with location data
        com.gege.activityfindermobile.data.dto.ActivityCreateRequest request =
                new com.gege.activityfindermobile.data.dto.ActivityCreateRequest(
                        title, description, activityDateTime, location, totalSpots, category);

        // Set placeId and coordinates if available
        request.setPlaceId(selectedPlaceId);
        if (selectedLatitude != 0.0 || selectedLongitude != 0.0) {
            request.setLatitude(selectedLatitude);
            request.setLongitude(selectedLongitude);
        }

        // Call API
        activityRepository.updateActivity(
                editActivityId,
                userId,
                request,
                new ApiCallback<com.gege.activityfindermobile.data.model.Activity>() {
                    @Override
                    public void onSuccess(
                            com.gege.activityfindermobile.data.model.Activity activity) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Activity updated successfully!",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // Navigate back to activity detail or feed
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigateUp();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to update activity: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private class PlacesAutocompleteAdapter extends android.widget.ArrayAdapter<String> {
        private final List<String> filteredLocations = new ArrayList<>();
        private final List<String> filteredPlaceIds = new ArrayList<>();

        public PlacesAutocompleteAdapter(android.content.Context context) {
            super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        }

        public void fetchPredictions(String query) {
            filteredLocations.clear();
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
                // Create autocomplete request for all place types
                FindAutocompletePredictionsRequest request =
                        FindAutocompletePredictionsRequest.builder()
                                .setSessionToken(sessionToken)
                                .setCountries("HU")
                                .setQuery(query)
                                .build();

                placesClient
                        .findAutocompletePredictions(request)
                        .addOnSuccessListener(
                                (FindAutocompletePredictionsResponse response) -> {
                                    filteredLocations.clear();
                                    filteredPlaceIds.clear();
                                    response.getAutocompletePredictions()
                                            .forEach(
                                                    prediction -> {
                                                         String description =
                                                                prediction.getFullText(null)
                                                                        .toString();
                                                        if (description != null
                                                                && !description.isEmpty()) {
                                                            filteredLocations.add(description);
                                                            filteredPlaceIds.add(prediction.getPlaceId());
                                                        }
                                                    });
                                    notifyDataSetChanged();
                                })
                        .addOnFailureListener(
                                exception -> {
                                    android.util.Log.e(
                                            "PlacesAdapter",
                                            "Error fetching predictions: "
                                                    + exception.getMessage(),
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
            return filteredLocations.size();
        }

        @Override
        public String getItem(int position) {
            if (position < filteredLocations.size()) {
                return filteredLocations.get(position);
            }
            return null;
        }
    }
}
