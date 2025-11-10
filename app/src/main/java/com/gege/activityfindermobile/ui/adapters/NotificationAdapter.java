package com.gege.activityfindermobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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
        View indicatorUnread;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
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

            // Show indicator for unread notifications
            if (notification.getIsRead() != null && !notification.getIsRead()) {
                indicatorUnread.setVisibility(View.VISIBLE);
                itemView.setBackgroundColor(
                        ContextCompat.getColor(context, R.color.notification_unread_bg));
            } else {
                indicatorUnread.setVisibility(View.GONE);
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
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
