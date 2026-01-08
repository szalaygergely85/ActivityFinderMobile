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

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.Notification;
import com.gege.activityfindermobile.utils.DateUtil;

import java.util.ArrayList;
import java.util.List;

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
            if (type != null
                    && (type.equals("JOIN_REQUEST")
                            || type.equals("FOLLOW")
                            || type.equals("INVITE")
                            || type.equals("PARTICIPANT_ACTION"))) {
                // TODO: Load profile picture when backend provides avatarUrl field
                // For now, show a person icon
                ivNotificationIcon.setImageResource(R.drawable.ic_person);
                ivNotificationIcon.setColorFilter(
                        ContextCompat.getColor(context, R.color.slate_400));
                ivNotificationIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                // Example of how to load profile picture when avatar URL is available:
                // if (notification.getAvatarUrl() != null &&
                // !notification.getAvatarUrl().isEmpty()) {
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
            String relativeTime = DateUtil.getRelativeTimeString(timestamp);
            return relativeTime != null && !relativeTime.isEmpty() ? relativeTime : timestamp;
        }
    }
}
