package com.gege.activityfindermobile.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.dto.UserRegistrationRequest;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.gege.activityfindermobile.utils.UiUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {

    @Inject UserRepository userRepository;

    @Inject SharedPreferencesManager prefsManager;

    private TextInputLayout tilFullName, tilEmail, tilBirthDate, tilPassword, tilConfirmPassword;
    private TextInputEditText etFullName, etEmail, etBirthDate, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private CircularProgressIndicator progressLoading;
    private TextView tvSignIn;

    private String selectedBirthDate = null; // Format: yyyy-MM-dd

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        tilFullName = view.findViewById(R.id.til_full_name);
        tilEmail = view.findViewById(R.id.til_email);
        tilBirthDate = view.findViewById(R.id.til_birth_date);
        tilPassword = view.findViewById(R.id.til_password);
        tilConfirmPassword = view.findViewById(R.id.til_confirm_password);
        etFullName = view.findViewById(R.id.et_full_name);
        etEmail = view.findViewById(R.id.et_email);
        etBirthDate = view.findViewById(R.id.et_birth_date);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        btnRegister = view.findViewById(R.id.btn_register);
        progressLoading = view.findViewById(R.id.progress_loading);
        tvSignIn = view.findViewById(R.id.tv_sign_in);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegister());

        etBirthDate.setOnClickListener(v -> showDatePicker());

        tvSignIn.setOnClickListener(
                v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigateUp();
                });
    }

    private void showDatePicker() {
        // Set max date to 18 years ago (user must be 18+)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -18);

        // Set min date to 100 years ago
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -100);

        CalendarConstraints constraints =
                new CalendarConstraints.Builder()
                        .setEnd(maxDate.getTimeInMillis())
                        .setStart(minDate.getTimeInMillis())
                        .setValidator(DateValidatorPointBackward.before(maxDate.getTimeInMillis()))
                        .build();

        MaterialDatePicker<Long> datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Select your birth date")
                        .setSelection(maxDate.getTimeInMillis())
                        .setCalendarConstraints(constraints)
                        .build();

        datePicker.addOnPositiveButtonClickListener(
                selection -> {
                    // Format for display (user-friendly)
                    SimpleDateFormat displayFormat =
                            new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    displayFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    // Format for API (yyyy-MM-dd)
                    SimpleDateFormat apiFormat =
                            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    apiFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    Date date = new Date(selection);
                    etBirthDate.setText(displayFormat.format(date));
                    selectedBirthDate = apiFormat.format(date);

                    // Clear error if any
                    tilBirthDate.setError(null);
                });

        datePicker.show(getParentFragmentManager(), "BIRTH_DATE_PICKER");
    }

    private void attemptRegister() {
        // Clear previous errors
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilBirthDate.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (fullName.isEmpty()) {
            tilFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (fullName.length() < 2) {
            tilFullName.setError("Please enter your full name");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (selectedBirthDate == null || selectedBirthDate.isEmpty()) {
            tilBirthDate.setError("Birth date is required");
            etBirthDate.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Show loading
        setLoading(true);

        // Create registration request
        UserRegistrationRequest request =
                new UserRegistrationRequest(fullName, email, password, selectedBirthDate);

        // Call API
        userRepository.registerUser(
                request,
                new ApiCallback<LoginResponse>() {
                    @Override
                    public void onSuccess(LoginResponse loginResponse) {
                        setLoading(false);

                        // Check if response data is valid
                        if (loginResponse == null || loginResponse.getUserId() == null) {
                            UiUtil.showLongToast(
                                    requireContext(), "Registration failed: Invalid response data");
                            return;
                        }

                        // Check if token is present
                        if (loginResponse.getAccessToken() == null
                                || loginResponse.getAccessToken().isEmpty()) {
                            UiUtil.showLongToast(
                                    requireContext(),
                                    "Registration failed: No authentication token received");
                            return;
                        }

                        Long userId = loginResponse.getUserId();
                        String token = loginResponse.getAccessToken();
                        String refreshToken = loginResponse.getRefreshToken();
                        String fullName = loginResponse.getFullName();

                        // Save user session with JWT tokens (access + refresh)
                        prefsManager.saveUserSession(userId, token, refreshToken);

                        UiUtil.showToast(requireContext(), "Welcome, " + fullName + "!");

                        // Navigate to profile setup
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(
                                R.id.action_registerFragment_to_profileSetupFragment);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        UiUtil.showLongToast(
                                requireContext(), "Registration failed: " + errorMessage);
                    }
                });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnRegister.setEnabled(false);
            progressLoading.setVisibility(View.VISIBLE);
            etFullName.setEnabled(false);
            etEmail.setEnabled(false);
            etBirthDate.setEnabled(false);
            etPassword.setEnabled(false);
            etConfirmPassword.setEnabled(false);
        } else {
            btnRegister.setEnabled(true);
            progressLoading.setVisibility(View.GONE);
            etFullName.setEnabled(true);
            etEmail.setEnabled(true);
            etBirthDate.setEnabled(true);
            etPassword.setEnabled(true);
            etConfirmPassword.setEnabled(true);
        }
    }
}
