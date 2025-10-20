package com.gege.activityfindermobile.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.Participant;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    private List<Participant> participants = new ArrayList<>();
    private OnParticipantClickListener listener;

    public interface OnParticipantClickListener {
        void onParticipantClick(Participant participant);
    }

    public ParticipantAdapter() {
        this(null);
    }

    public ParticipantAdapter(OnParticipantClickListener listener) {
        this.listener = listener;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Participant participant = participants.get(position);
        holder.bind(participant, listener);
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvUserRating;
        Chip chipStatus;
        View cardParticipant;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardParticipant = itemView.findViewById(R.id.card_participant);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserRating = itemView.findViewById(R.id.tv_user_rating);
            chipStatus = itemView.findViewById(R.id.chip_status);
        }

        void bind(Participant participant, OnParticipantClickListener listener) {
            // Set user name
            if (participant.getUserName() != null) {
                tvUserName.setText(participant.getUserName());
            } else {
                tvUserName.setText("Unknown User");
            }

            // Set user rating with null check
            if (participant.getUserRating() != null) {
                tvUserRating.setText(String.format("%.1f", participant.getUserRating()));
            } else {
                tvUserRating.setText("N/A");
            }

            // Set status
            if (participant.getStatus() != null) {
                chipStatus.setText(participant.getStatus());
            } else {
                chipStatus.setText("UNKNOWN");
            }

            // Set click listener
            if (listener != null) {
                cardParticipant.setOnClickListener(v -> listener.onParticipantClick(participant));
            }
        }
    }
}
