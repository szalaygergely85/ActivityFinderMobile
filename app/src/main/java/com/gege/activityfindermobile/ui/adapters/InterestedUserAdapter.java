package com.gege.activityfindermobile.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.utils.ImageLoader;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class InterestedUserAdapter extends RecyclerView.Adapter<InterestedUserAdapter.ViewHolder> {

    private List<Participant> interestedUsers = new ArrayList<>();
    private OnActionListener listener;

    public interface OnActionListener {
        void onAccept(Participant participant);

        void onDecline(Participant participant);
    }

    public InterestedUserAdapter(OnActionListener listener) {
        this.listener = listener;
    }

    public void setInterestedUsers(List<Participant> users) {
        this.interestedUsers = users;
        notifyDataSetChanged();
    }

    public void removeParticipant(Long participantId) {
        for (int i = 0; i < interestedUsers.size(); i++) {
            if (interestedUsers.get(i).getId().equals(participantId)) {
                interestedUsers.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_interested_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Participant participant = interestedUsers.get(position);
        holder.bind(participant, listener);
    }

    @Override
    public int getItemCount() {
        return interestedUsers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivUserAvatar;
        TextView tvUserName, tvUserRating, tvJoinedAt;
        Chip chipStatus;
        MaterialButton btnAccept, btnDecline;
        View layoutActions;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserRating = itemView.findViewById(R.id.tv_user_rating);
            tvJoinedAt = itemView.findViewById(R.id.tv_joined_at);
            chipStatus = itemView.findViewById(R.id.chip_status);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnDecline = itemView.findViewById(R.id.btn_decline);
            layoutActions = itemView.findViewById(R.id.layout_actions);
        }

        void bind(Participant participant, OnActionListener listener) {
            // Load user avatar
            ImageLoader.loadCircularProfileImage(
                    itemView.getContext(), participant.getUserAvatar(), ivUserAvatar);

            tvUserName.setText(participant.getUserName());

            // Handle rating
            if (participant.getUserRating() != null) {
                tvUserRating.setText(String.format("%.1f", participant.getUserRating()));
            } else {
                tvUserRating.setText("N/A");
            }

            // Format joined at time
            if (participant.getJoinedAt() != null) {
                tvJoinedAt.setText("Requested " + formatTimeAgo(participant.getJoinedAt()));
            } else {
                tvJoinedAt.setText("Just now");
            }

            // Show/hide action buttons based on status
            String status = participant.getStatus();
            if ("INTERESTED".equals(status)) {
                layoutActions.setVisibility(View.VISIBLE);
                chipStatus.setVisibility(View.GONE);
            } else {
                layoutActions.setVisibility(View.GONE);
                chipStatus.setVisibility(View.VISIBLE);
                chipStatus.setText(status);
            }

            // Set click listeners
            btnAccept.setOnClickListener(
                    v -> {
                        if (listener != null) {
                            listener.onAccept(participant);
                        }
                    });

            btnDecline.setOnClickListener(
                    v -> {
                        if (listener != null) {
                            listener.onDecline(participant);
                        }
                    });
        }

        private String formatTimeAgo(String timestamp) {
            // Simple time formatting - you can enhance this
            // For now, just return a simple message
            return "recently";
        }
    }
}
