package com.gege.activityfindermobile.ui.report;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.dto.ReportRequest;
import com.gege.activityfindermobile.data.model.Report;
import com.gege.activityfindermobile.data.model.ReportType;
import com.gege.activityfindermobile.data.repository.ReportRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ReportDialog extends DialogFragment {

    @Inject ReportRepository reportRepository;

    private static final String ARG_REPORT_TYPE = "report_type";
    private static final String ARG_ACTIVITY_ID = "activity_id";
    private static final String ARG_MESSAGE_ID = "message_id";
    private static final String ARG_USER_ID = "user_id";

    private ReportType reportType;
    private Long activityId;
    private Long messageId;
    private Long userId;

    private TextInputEditText reasonInput;
    private ChipGroup reasonChipGroup;
    private MaterialButton cancelButton;
    private MaterialButton submitButton;

    private OnReportSubmittedListener listener;

    public interface OnReportSubmittedListener {
        void onReportSubmitted();
    }

    public static ReportDialog newInstanceForActivity(Long activityId) {
        ReportDialog dialog = new ReportDialog();
        Bundle args = new Bundle();
        args.putString(ARG_REPORT_TYPE, ReportType.ACTIVITY.name());
        args.putLong(ARG_ACTIVITY_ID, activityId);
        dialog.setArguments(args);
        return dialog;
    }

    public static ReportDialog newInstanceForMessage(Long messageId) {
        ReportDialog dialog = new ReportDialog();
        Bundle args = new Bundle();
        args.putString(ARG_REPORT_TYPE, ReportType.MESSAGE.name());
        args.putLong(ARG_MESSAGE_ID, messageId);
        dialog.setArguments(args);
        return dialog;
    }

    public static ReportDialog newInstanceForUser(Long userId) {
        ReportDialog dialog = new ReportDialog();
        Bundle args = new Bundle();
        args.putString(ARG_REPORT_TYPE, ReportType.USER.name());
        args.putLong(ARG_USER_ID, userId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            reportType = ReportType.valueOf(getArguments().getString(ARG_REPORT_TYPE));
            activityId =
                    getArguments().containsKey(ARG_ACTIVITY_ID)
                            ? getArguments().getLong(ARG_ACTIVITY_ID)
                            : null;
            messageId =
                    getArguments().containsKey(ARG_MESSAGE_ID)
                            ? getArguments().getLong(ARG_MESSAGE_ID)
                            : null;
            userId =
                    getArguments().containsKey(ARG_USER_ID)
                            ? getArguments().getLong(ARG_USER_ID)
                            : null;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_report, null);

        reasonInput = view.findViewById(R.id.reasonInput);
        reasonChipGroup = view.findViewById(R.id.reasonChipGroup);
        cancelButton = view.findViewById(R.id.cancelButton);
        submitButton = view.findViewById(R.id.submitButton);

        setupChips();
        setupButtons();

        builder.setView(view);
        return builder.create();
    }

    private void setupChips() {
        Chip chipSpam = reasonChipGroup.findViewById(R.id.chipSpam);
        Chip chipInappropriate = reasonChipGroup.findViewById(R.id.chipInappropriate);
        Chip chipHarassment = reasonChipGroup.findViewById(R.id.chipHarassment);
        Chip chipFalseInfo = reasonChipGroup.findViewById(R.id.chipFalseInfo);

        chipSpam.setOnClickListener(v -> reasonInput.setText("Spam"));
        chipInappropriate.setOnClickListener(v -> reasonInput.setText("Inappropriate content"));
        chipHarassment.setOnClickListener(v -> reasonInput.setText("Harassment"));
        chipFalseInfo.setOnClickListener(v -> reasonInput.setText("False information"));
    }

    private void setupButtons() {
        cancelButton.setOnClickListener(v -> dismiss());
        submitButton.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        String reason = reasonInput.getText().toString().trim();
        if (reason.isEmpty()) {
            Toast.makeText(requireContext(), "Please provide a reason", Toast.LENGTH_SHORT).show();
            return;
        }

        ReportRequest request;
        switch (reportType) {
            case ACTIVITY:
                request = ReportRequest.forActivity(activityId, reason);
                break;
            case MESSAGE:
                request = ReportRequest.forMessage(messageId, reason);
                break;
            case USER:
                request = ReportRequest.forUser(userId, reason);
                break;
            default:
                Toast.makeText(requireContext(), "Invalid report type", Toast.LENGTH_SHORT).show();
                return;
        }

        reportRepository.submitReport(
                request,
                new ApiCallback<Report>() {
                    @Override
                    public void onSuccess(Report report) {
                        Toast.makeText(
                                        requireContext(),
                                        "Report submitted successfully",
                                        Toast.LENGTH_SHORT)
                                .show();
                        if (listener != null) {
                            listener.onReportSubmitted();
                        }
                        dismiss();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to submit report: " + error,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    public void setOnReportSubmittedListener(OnReportSubmittedListener listener) {
        this.listener = listener;
    }
}
