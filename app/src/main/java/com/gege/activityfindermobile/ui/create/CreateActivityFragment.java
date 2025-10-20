package com.gege.activityfindermobile.ui.create;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateActivityFragment extends Fragment {

    @Inject
    ActivityRepository activityRepository;

    @Inject
    SharedPreferencesManager prefsManager;

    private TextInputLayout tilTitle, tilDescription, tilCategory, tilDate, tilTime, tilLocation, tilTotalSpots, tilFriendSpots;
    private TextInputEditText etTitle, etDescription, etDate, etTime, etLocation, etTotalSpots, etFriendSpots;
    private AutoCompleteTextView etCategory;
    private MaterialButton btnCreate;
    private CircularProgressIndicator progressLoading;

    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupCategoryDropdown();
        setupListeners();
    }

    private void initViews(View view) {
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
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
        tilFriendSpots = view.findViewById(R.id.til_friend_spots);

        etTitle = view.findViewById(R.id.et_title);
        etDescription = view.findViewById(R.id.et_description);
        etCategory = view.findViewById(R.id.et_category);
        etDate = view.findViewById(R.id.et_date);
        etTime = view.findViewById(R.id.et_time);
        etLocation = view.findViewById(R.id.et_location);
        etTotalSpots = view.findViewById(R.id.et_total_spots);
        etFriendSpots = view.findViewById(R.id.et_friend_spots);

        btnCreate = view.findViewById(R.id.btn_create);
        progressLoading = view.findViewById(R.id.progress_loading);
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Sports", "Social", "Outdoor", "Food", "Travel", "Photography", "Music", "Art", "Gaming", "Fitness"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        etCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        // Date picker
        etDate.setOnClickListener(v -> showDatePicker());

        // Time picker
        etTime.setOnClickListener(v -> showTimePicker());

        // Create button
        btnCreate.setOnClickListener(v -> validateAndCreateActivity());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    etDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);

                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    etTime.setText(timeFormat.format(selectedTime.getTime()));
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                false
        );
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
        tilFriendSpots.setError(null);

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String totalSpotsStr = etTotalSpots.getText().toString().trim();
        String friendSpotsStr = etFriendSpots.getText().toString().trim();

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
        int friendSpots = 0;

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

        try {
            if (!friendSpotsStr.isEmpty()) {
                friendSpots = Integer.parseInt(friendSpotsStr);
                if (friendSpots < 0) {
                    tilFriendSpots.setError("Cannot be negative");
                    isValid = false;
                } else if (friendSpots > totalSpots) {
                    tilFriendSpots.setError("Cannot exceed total spots");
                    isValid = false;
                }
            }
        } catch (NumberFormatException e) {
            tilFriendSpots.setError("Invalid number");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create activity
        createActivity(title, description, category, date, time, location, totalSpots, friendSpots);
    }

    private void createActivity(String title, String description, String category, String date, String time, String location, int totalSpots, int friendSpots) {
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

        // Create request
        ActivityCreateRequest request = new ActivityCreateRequest(
                title,
                description,
                activityDateTime,
                location,
                totalSpots,
                friendSpots,
                category
        );

        // Call API
        activityRepository.createActivity(userId, request, new ApiCallback<Activity>() {
            @Override
            public void onSuccess(Activity activity) {
                setLoading(false);
                Toast.makeText(requireContext(), "Activity created successfully!", Toast.LENGTH_SHORT).show();

                // Navigate back to feed
                NavController navController = Navigation.findNavController(requireView());
                navController.navigateUp();
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                Toast.makeText(requireContext(), "Failed to create activity: " + errorMessage, Toast.LENGTH_LONG).show();
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
                Toast.makeText(requireContext(), "Activity date must be in the future", Toast.LENGTH_SHORT).show();
                return null;
            }

            // Format as ISO 8601: yyyy-MM-dd'T'HH:mm:ss
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
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
}
