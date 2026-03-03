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
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.repository.UserRepository;
import com.gege.activityfindermobile.utils.UiUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {

    @Inject UserRepository userRepository;

    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private MaterialButton btnSend;
    private CircularProgressIndicator progressLoading;
    private TextView tvBackToLogin;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilEmail = view.findViewById(R.id.til_email);
        etEmail = view.findViewById(R.id.et_email);
        btnSend = view.findViewById(R.id.btn_send);
        progressLoading = view.findViewById(R.id.progress_loading);
        tvBackToLogin = view.findViewById(R.id.tv_back_to_login);

        btnSend.setOnClickListener(v -> attemptSend());

        tvBackToLogin.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.popBackStack();
        });
    }

    private void attemptSend() {
        tilEmail.setError(null);

        String email = etEmail.getText().toString().trim();

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

        setLoading(true);

        userRepository.forgotPassword(
                email,
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        setLoading(false);
                        UiUtil.showLongToast(
                                requireContext(),
                                "If an account with that email exists, a reset link has been sent.");
                        NavController navController = Navigation.findNavController(requireView());
                        navController.popBackStack();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        setLoading(false);
                        UiUtil.showLongToast(requireContext(), "Network error. Please try again.");
                    }
                });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            btnSend.setEnabled(false);
            progressLoading.setVisibility(View.VISIBLE);
            etEmail.setEnabled(false);
        } else {
            btnSend.setEnabled(true);
            progressLoading.setVisibility(View.GONE);
            etEmail.setEnabled(true);
        }
    }
}
