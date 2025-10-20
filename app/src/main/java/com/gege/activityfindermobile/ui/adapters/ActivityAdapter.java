package com.gege.activityfindermobile.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.Activity;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private List<Activity> activities = new ArrayList<>();
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(Activity activity);
    }

    public ActivityAdapter(OnActivityClickListener listener) {
        this.listener = listener;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDate, tvTime, tvLocation;
        TextView tvCreatorName, tvCreatorRating, tvSpotsAvailable;
        Chip chipCategory, badgeTrending;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_activity_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvCreatorName = itemView.findViewById(R.id.tv_creator_name);
            tvCreatorRating = itemView.findViewById(R.id.tv_creator_rating);
            tvSpotsAvailable = itemView.findViewById(R.id.tv_spots_available);
            chipCategory = itemView.findViewById(R.id.chip_category);
            badgeTrending = itemView.findViewById(R.id.badge_trending);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivityClick(activities.get(getAdapterPosition()));
                }
            });
        }

        void bind(Activity activity) {
            tvTitle.setText(activity.getTitle());
            tvDescription.setText(activity.getDescription());

            // Handle date with null/empty check
            String date = activity.getDate();
            if (date != null && !date.isEmpty()) {
                tvDate.setText(date);
                tvDate.setVisibility(View.VISIBLE);
            } else {
                tvDate.setText("Date TBD");
                tvDate.setVisibility(View.VISIBLE);
            }

            // Handle time with null/empty check
            String time = activity.getTime();
            if (time != null && !time.isEmpty()) {
                tvTime.setText(time);
                tvTime.setVisibility(View.VISIBLE);
            } else {
                tvTime.setText("Time TBD");
                tvTime.setVisibility(View.VISIBLE);
            }

            tvLocation.setText(activity.getLocation());
            tvCreatorName.setText(activity.getCreatorName());

            // Handle rating with null check
            if (activity.getCreatorRating() != null) {
                tvCreatorRating.setText(String.format("%.1f", activity.getCreatorRating()));
            } else {
                tvCreatorRating.setText("N/A");
            }

            // Get current participants count from API or calculate
            int currentParticipants = activity.getParticipantsCount();
            int totalSpots = activity.getTotalSpots() != null ? activity.getTotalSpots() : 0;
            tvSpotsAvailable.setText(currentParticipants + "/" + totalSpots + " spots");

            chipCategory.setText(activity.getCategory());
            badgeTrending.setVisibility(activity.getTrending() != null && activity.getTrending() ? View.VISIBLE : View.GONE);
        }
    }
}
