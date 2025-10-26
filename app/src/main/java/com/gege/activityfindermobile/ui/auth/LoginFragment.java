package com.gege.activityfindermobile.ui.auth;

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

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.dto.LoginRequest;
import com.gege.activityfindermobile.data.dto.LoginResponse;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    @Inject UserRepository userRepository;

    @Inject SharedPreferencesManager prefsManager;

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private CircularProgressIndicator progressLoading;
    private TextView tvSignUp, tvForgotPassword;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        tilEmail = view.findViewById(R.id.til_email);
        tilPassword = view.findViewById(R.id.til_password);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        progressLoading = view.findViewById(R.id.progress_loading);
        tvSignUp = view.findViewById(R.id.tv_sign_up);
        tvForgotPassword = view.findViewById(R.id.tv_forgot_password);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvSignUp.setOnClickListener(
                v -> {
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.action_loginFragment_to_registerFragment);
                });

        tvForgotPassword.setOnClickListener(
                v -> {
                    Toast.makeText(
                                    requireContext(),
                                    "Forgot password feature coming soon!",
                                    Toast.LENGTH_SHORT)
                            .show();
                });
    }

    private void attemptLogin() {
        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
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

        // Show loading
        setLoading(true);

        // Create login request
        LoginRequest request = new LoginRequest(email, password);

        // Call API
        userRepository.loginUser(
                request,
                new ApiCallback<LoginResponse>() {
                    @Override
                    public void onSuccess(LoginResponse loginResponse) {
                        setLoading(false);

                        // Check if response data is valid
                        if (loginResponse == null || loginResponse.getUserId() == null) {
                            Toast.makeText(
                                            requireContext(),
                                            "Login failed: Invalid response data",
                                            Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }

                        // Check if token is present
                        if (loginResponse.getAccessToken() == null
                                || loginResponse.getAccessToken().isEmpty()) {
                            Toast.makeText(
                                            requireContext(),
                                            "Login failed: No authentication token received",
                                            Toast.LENGTH_LONG)
                                    .show();
                            return;
                        }

                        Long userId = loginResponse.getUserId();
                        String token = loginResponse.getAccessToken();
                        String refreshToken = loginResponse.getRefreshToken();
                        String fullName = loginResponse.getFullName();

                        // Save user session with JWT tokens (access + refresh)
                        prefsManager.saveUserSession(userId, token, refreshToken);

                        Toast.makeText(
                                        requireContext(),
                                        "Welcome back, " + fullName + "!",
                                        Toast.LENGTH_SHORT)
                                .show();

                        // Navigate to main screen
                        NavController navController = Navigation.findNavController(requireView());
                        navController.navigate(R.id.action_loginFragment_to_nav_feed);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        Toast.makeText(
                                        requireContext(),
                                        "Login failed: " + errorMessage,
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnLogin.setEnabled(false);
            progressLoading.setVisibility(View.VISIBLE);
            etEmail.setEnabled(false);
            etPassword.setEnabled(false);
        } else {
            btnLogin.setEnabled(true);
            progressLoading.setVisibility(View.GONE);
            etEmail.setEnabled(true);
            etPassword.setEnabled(true);
        }
    }
}
