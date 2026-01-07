package com.gege.activityfindermobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter() {}

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        ImageView ivNotificationIcon;
        View indicatorUnread;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            ivNotificationIcon = itemView.findViewById(R.id.iv_notification_icon);
            indicatorUnread = itemView.findViewById(R.id.indicator_unread);

            itemView.setOnClickListener(
                    v -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && listener != null) {
                            listener.onNotificationClick(notifications.get(position));
                        }
                    });
        }

        void bind(Notification notification) {
            Context context = itemView.getContext();

            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            tvTime.setText(formatTime(notification.getCreatedAt()));

            // Set icon based on notification type
            setNotificationIcon(context, notification);

            // Show indicator for unread notifications
            if (notification.getIsRead() != null && !notification.getIsRead()) {
                indicatorUnread.setVisibility(View.VISIBLE);
            } else {
                indicatorUnread.setVisibility(View.GONE);
            }
        }

        private void setNotificationIcon(Context context, Notification notification) {
            String type = notification.getType();

            // For user-related notifications (join requests, follows, etc.), show profile picture
            // Note: When backend provides avatar URL field, use Glide to load it here
            if (type != null && (type.equals("JOIN_REQUEST") || type.equals("FOLLOW")
                    || type.equals("INVITE") || type.equals("PARTICIPANT_ACTION"))) {
                // TODO: Load profile picture when backend provides avatarUrl field
                // For now, show a person icon
                ivNotificationIcon.setImageResource(R.drawable.ic_person);
                ivNotificationIcon.setColorFilter(
                        ContextCompat.getColor(context, R.color.slate_400));
                ivNotificationIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                // Example of how to load profile picture when avatar URL is available:
                // if (notification.getAvatarUrl() != null && !notification.getAvatarUrl().isEmpty()) {
                //     Glide.with(context)
                //         .load(notification.getAvatarUrl())
                //         .circleCrop()
                //         .placeholder(R.drawable.ic_person)
                //         .into(ivNotificationIcon);
                // }
            } else {
                // For general notifications, show notification bell icon
                ivNotificationIcon.setImageResource(R.drawable.ic_notifications);
                ivNotificationIcon.setColorFilter(
                        ContextCompat.getColor(context, R.color.slate_400));
                ivNotificationIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        }

        private String formatTime(String timestamp) {
            if (timestamp == null || timestamp.isEmpty()) {
                return "";
            }

            try {
                // Parse ISO 8601 timestamp from backend
                SimpleDateFormat isoFormat =
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date date = isoFormat.parse(timestamp);

                if (date == null) {
                    return timestamp;
                }

                // Calculate time difference
                long now = System.currentTimeMillis();
                long diff = now - date.getTime();

                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                long days = TimeUnit.MILLISECONDS.toDays(diff);

                // Format as relative time
                if (seconds < 60) {
                    return "Just now";
                } else if (minutes < 60) {
                    return minutes + "m ago";
                } else if (hours < 24) {
                    return hours + "h ago";
                } else if (days < 7) {
                    return days + "d ago";
                } else {
                    // For older notifications, show the actual date
                    SimpleDateFormat displayFormat =
                            new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                    return displayFormat.format(date);
                }
            } catch (Exception e) {
                // If parsing fails, return the original timestamp
                return timestamp;
            }
        }
    }
}
