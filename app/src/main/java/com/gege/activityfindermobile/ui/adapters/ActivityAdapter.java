package com.gege.activityfindermobile.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.model.Activity;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.data.repository.ParticipantRepository;
import com.gege.activityfindermobile.utils.CategoryManager;
import com.gege.activityfindermobile.utils.ImageLoader;
import com.google.android.material.chip.Chip;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private List<Activity> activities = new ArrayList<>();
    private OnActivityClickListener listener;
    private ParticipantRepository participantRepository;
    private Long currentUserId;
    private CategoryManager categoryManager;

    public interface OnActivityClickListener {
        void onActivityClick(Activity activity);
    }

    public ActivityAdapter(OnActivityClickListener listener) {
        this.listener = listener;
    }

    public ActivityAdapter(
            OnActivityClickListener listener, ParticipantRepository participantRepository) {
        this.listener = listener;
        this.participantRepository = participantRepository;
    }

    public ActivityAdapter(
            OnActivityClickListener listener,
            ParticipantRepository participantRepository,
            Long currentUserId) {
        this.listener = listener;
        this.participantRepository = participantRepository;
        this.currentUserId = currentUserId;
    }

    public ActivityAdapter(
            OnActivityClickListener listener,
            ParticipantRepository participantRepository,
            Long currentUserId,
            CategoryManager categoryManager) {
        this.listener = listener;
        this.participantRepository = participantRepository;
        this.currentUserId = currentUserId;
        this.categoryManager = categoryManager;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
        android.util.Log.d("ActivityAdapter", "setActivities called with " + (activities != null ? activities.size() : 0) + " items");
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_activity_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Activity activity = activities.get(position);
        android.util.Log.d("ActivityAdapter", "onBindViewHolder called for position " + position + " with activity: " + (activity != null ? activity.getTitle() : "null"));
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        int count = activities.size();
        android.util.Log.d("ActivityAdapter", "getItemCount: " + count);
        return count;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivActivityImage;
        ImageView ivCreatorAvatar;
        TextView tvTitle, tvDescription, tvDate, tvTime, tvLocation, tvDistance;
        TextView tvCreatorName, tvCreatorRating, tvSpotsAvailable, tvSpotsDisplay, tvActivityCategory;
        Chip chipCategory, chipStatus, chipExpired;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivActivityImage = itemView.findViewById(R.id.iv_activity_image);
            ivCreatorAvatar = itemView.findViewById(R.id.iv_creator_avatar);
            tvTitle = itemView.findViewById(R.id.tv_activity_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvCreatorName = itemView.findViewById(R.id.tv_creator_name);
            tvCreatorRating = itemView.findViewById(R.id.tv_creator_rating);
            tvSpotsAvailable = itemView.findViewById(R.id.tv_spots_available);
            tvSpotsDisplay = itemView.findViewById(R.id.tv_spots_display);
            tvActivityCategory = itemView.findViewById(R.id.tv_activity_category);
            chipCategory = itemView.findViewById(R.id.chip_category);
            chipStatus = itemView.findViewById(R.id.chip_status);
            chipExpired = itemView.findViewById(R.id.chip_expired);

            itemView.setOnClickListener(
                    v -> {
                        if (listener != null) {
                            listener.onActivityClick(activities.get(getAdapterPosition()));
                        }
                    });
        }

        void bind(Activity activity) {
            Context context = itemView.getContext();
            android.util.Log.d("ActivityAdapter", "bind() started for: " + activity.getTitle());
            android.util.Log.d("ActivityAdapter", "ItemView height: " + itemView.getHeight() + ", width: " + itemView.getWidth());

            // Load category background image
            loadCategoryImage(activity.getCategory(), context);

            // Load creator avatar
            ImageLoader.loadCircularProfileImage(
                    context, activity.getCreatorAvatar(), ivCreatorAvatar);

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

            // Display distance if available
            if (activity.getDistance() != null && activity.getDistance() > 0) {
                tvDistance.setText(String.format("%.1f km", activity.getDistance()));
                tvDistance.setVisibility(View.VISIBLE);
            } else {
                tvDistance.setVisibility(View.GONE);
            }

            tvCreatorName.setText(activity.getCreatorName());

            // Display creator rating if available
            displayCreatorRating(activity.getCreatorRating(), tvCreatorRating);

            // Get current participants count - fetch actual accepted count
            int totalSpots = activity.getTotalSpots() != null ? activity.getTotalSpots() : 0;

            if (participantRepository != null && activity.getId() != null) {
                // Fetch actual participants and count only accepted ones
                loadAcceptedParticipantsCount(activity.getId(), totalSpots, tvSpotsAvailable, tvSpotsDisplay);
            } else {
                // Fallback to server count
                int currentParticipants = activity.getParticipantsCount();
                String spotsText = currentParticipants + "/" + totalSpots + " spots";
                tvSpotsAvailable.setText(spotsText);
                if (tvSpotsDisplay != null) {
                    tvSpotsDisplay.setText(spotsText);
                }
            }

            // Set category with dynamic color
            String category = activity.getCategory();
            if (tvActivityCategory != null) {
                tvActivityCategory.setText(category != null ? category.toUpperCase() : "");
                setCategoryColor(tvActivityCategory, category, context);
            }
            if (chipCategory != null) {
                chipCategory.setText(category);
                setCategoryStyleAndIcon(chipCategory, category, context);
            }

            // Check if activity is expired
            boolean isExpired = isActivityExpired(activity);
            chipExpired.setVisibility(isExpired ? View.VISIBLE : View.GONE);

            // Set status chip based on user's relationship with activity
            updateStatusChip(activity, chipStatus, context);
        }

        private void updateStatusChip(Activity activity, Chip chipStatus, Context context) {
            // Hide by default
            chipStatus.setVisibility(View.GONE);

            if (currentUserId == null) {
                return;
            }

            // Check if user is the creator
            if (activity.getCreatorId() != null && activity.getCreatorId().equals(currentUserId)) {
                chipStatus.setText("My Activity");
                chipStatus.setChipBackgroundColorResource(R.color.primary);
                chipStatus.setTextColor(context.getResources().getColor(R.color.white, null));
                chipStatus.setVisibility(View.VISIBLE);
                return;
            }

            // Check participant status if repository is available
            if (participantRepository != null && activity.getId() != null) {
                loadParticipantStatus(activity.getId(), chipStatus, context);
            }
        }

        private void loadParticipantStatus(Long activityId, Chip chipStatus, Context context) {
            participantRepository.getActivityParticipants(
                    activityId,
                    new ApiCallback<List<Participant>>() {
                        @Override
                        public void onSuccess(List<Participant> participants) {
                            if (participants != null && currentUserId != null) {
                                for (Participant p : participants) {
                                    if (p.getUserId() != null
                                            && p.getUserId().equals(currentUserId)) {
                                        String status = p.getStatus();
                                        if ("ACCEPTED".equals(status) || "JOINED".equals(status)) {
                                            chipStatus.setText("Joined");
                                            chipStatus.setChipBackgroundColorResource(
                                                    R.color.success);
                                            chipStatus.setTextColor(
                                                    context.getResources()
                                                            .getColor(R.color.white, null));
                                            chipStatus.setVisibility(View.VISIBLE);
                                        } else if ("PENDING".equals(status)
                                                || "INTERESTED".equals(status)) {
                                            chipStatus.setText("Interested");
                                            chipStatus.setChipBackgroundColorResource(
                                                    R.color.warning);
                                            chipStatus.setTextColor(
                                                    context.getResources()
                                                            .getColor(R.color.white, null));
                                            chipStatus.setVisibility(View.VISIBLE);
                                        }
                                        break;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // Silently fail - don't show status chip
                        }
                    });
        }

        private void loadAcceptedParticipantsCount(
                Long activityId, int totalSpots, TextView tvSpotsAvailable, TextView tvSpotsDisplay) {
            participantRepository.getActivityParticipants(
                    activityId,
                    new ApiCallback<List<Participant>>() {
                        @Override
                        public void onSuccess(List<Participant> participants) {
                            // Count only ACCEPTED and JOINED participants
                            int acceptedCount = 0;
                            if (participants != null) {
                                for (Participant p : participants) {
                                    String status = p.getStatus();
                                    if ("ACCEPTED".equals(status) || "JOINED".equals(status)) {
                                        acceptedCount++;
                                    }
                                }
                            }
                            String spotsText = acceptedCount + "/" + totalSpots + " spots";
                            tvSpotsAvailable.setText(spotsText);
                            if (tvSpotsDisplay != null) {
                                tvSpotsDisplay.setText(spotsText);
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            // Silently fail - show 0 on error
                            String spotsText = "0/" + totalSpots + " spots";
                            tvSpotsAvailable.setText(spotsText);
                            if (tvSpotsDisplay != null) {
                                tvSpotsDisplay.setText(spotsText);
                            }
                        }
                    });
        }

        private void setCategoryColor(TextView textView, String category, Context context) {
            int colorRes;

            switch (category.toLowerCase()) {
                case "sports":
                    colorRes = R.color.category_sports;
                    break;
                case "social":
                    colorRes = R.color.category_social;
                    break;
                case "outdoor":
                    colorRes = R.color.category_outdoor;
                    break;
                case "food":
                    colorRes = R.color.category_food;
                    break;
                case "travel":
                    colorRes = R.color.category_travel;
                    break;
                case "photography":
                    colorRes = R.color.category_photography;
                    break;
                case "music":
                    colorRes = R.color.category_music;
                    break;
                case "art":
                    colorRes = R.color.category_art;
                    break;
                case "gaming":
                    colorRes = R.color.category_gaming;
                    break;
                case "fitness":
                    colorRes = R.color.category_fitness;
                    break;
                default:
                    colorRes = R.color.accent_green;
                    break;
            }

            int color = ContextCompat.getColor(context, colorRes);
            textView.setTextColor(color);
        }

        private void setCategoryStyleAndIcon(Chip chip, String category, Context context) {
            int colorRes;
            int iconRes;

            switch (category.toLowerCase()) {
                case "sports":
                    colorRes = R.color.category_sports;
                    iconRes = R.drawable.ic_sports;
                    break;
                case "social":
                    colorRes = R.color.category_social;
                    iconRes = R.drawable.ic_social;
                    break;
                case "outdoor":
                    colorRes = R.color.category_outdoor;
                    iconRes = R.drawable.ic_outdoor;
                    break;
                case "food":
                    colorRes = R.color.category_food;
                    iconRes = R.drawable.ic_food;
                    break;
                case "travel":
                    colorRes = R.color.category_travel;
                    iconRes = R.drawable.ic_travel;
                    break;
                case "photography":
                    colorRes = R.color.category_photography;
                    iconRes = R.drawable.ic_photography;
                    break;
                case "music":
                    colorRes = R.color.category_music;
                    iconRes = R.drawable.ic_music;
                    break;
                case "art":
                    colorRes = R.color.category_art;
                    iconRes = R.drawable.ic_art;
                    break;
                case "gaming":
                    colorRes = R.color.category_gaming;
                    iconRes = R.drawable.ic_gaming;
                    break;
                case "fitness":
                    colorRes = R.color.category_fitness;
                    iconRes = R.drawable.ic_fitness;
                    break;
                default:
                    colorRes = R.color.primary;
                    iconRes = R.drawable.ic_category;
                    break;
            }

            int color = ContextCompat.getColor(context, colorRes);
            chip.setChipBackgroundColor(ColorStateList.valueOf(color));
            chip.setChipIcon(ContextCompat.getDrawable(context, iconRes));
            chip.setChipIconTint(
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
        }

        private boolean isActivityExpired(Activity activity) {
            if (activity.getDate() == null) {
                return false;
            }

            try {
                // Parse the date string (assuming format like "Nov 15, 2025")
                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US);
                java.util.Date activityDate = sdf.parse(activity.getDate());
                java.util.Date today = new java.util.Date();

                // Reset time to start of day for both dates to compare just the date portion
                java.util.Calendar calActivity = java.util.Calendar.getInstance();
                calActivity.setTime(activityDate);
                calActivity.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calActivity.set(java.util.Calendar.MINUTE, 0);
                calActivity.set(java.util.Calendar.SECOND, 0);
                calActivity.set(java.util.Calendar.MILLISECOND, 0);

                java.util.Calendar calToday = java.util.Calendar.getInstance();
                calToday.setTime(today);
                calToday.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calToday.set(java.util.Calendar.MINUTE, 0);
                calToday.set(java.util.Calendar.SECOND, 0);
                calToday.set(java.util.Calendar.MILLISECOND, 0);

                // Activity is expired if the date is before today (not including today)
                return calActivity.before(calToday);
            } catch (java.text.ParseException e) {
                // If date parsing fails, assume not expired
                return false;
            }
        }

        private void loadCategoryImage(String category, Context context) {
            if (category == null || category.isEmpty()) {
                // Use default image
                android.util.Log.d("ActivityAdapter", "Category is null/empty, using default");
                ivActivityImage.setImageResource(R.drawable.activity_default);
                return;
            }

            // Get image resource name from CategoryManager if available
            String imageResourceName;
            if (categoryManager != null) {
                imageResourceName = categoryManager.getImageResourceName(category);
                android.util.Log.d("ActivityAdapter", "CategoryManager returned: " + imageResourceName + " for category: " + category);
            } else {
                // Fallback to convention-based naming
                imageResourceName =
                        "activity_" + category.toLowerCase().replaceAll("\\s+", "_");
                android.util.Log.d("ActivityAdapter", "CategoryManager null, using fallback: " + imageResourceName);
            }

            // Get resource ID from resource name
            int resourceId =
                    context.getResources()
                            .getIdentifier(imageResourceName, "drawable", context.getPackageName());

            android.util.Log.d("ActivityAdapter", "Resource ID for " + imageResourceName + ": " + resourceId);

            // Set image or use default if not found
            if (resourceId != 0) {
                ivActivityImage.setImageResource(resourceId);
            } else {
                android.util.Log.w("ActivityAdapter", "Resource not found: " + imageResourceName + ", using default");
                ivActivityImage.setImageResource(R.drawable.activity_default);
            }
        }

        private void displayCreatorRating(Double rating, TextView tvCreatorRating) {
            if (rating == null || rating == 0.0) {
                tvCreatorRating.setText("N/A");
            } else {
                tvCreatorRating.setText(String.format("%.1f", rating));
            }
        }
    }
}
