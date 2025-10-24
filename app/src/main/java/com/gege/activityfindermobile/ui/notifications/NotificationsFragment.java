package com.gege.activityfindermobile.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.model.Notification;
import com.gege.activityfindermobile.data.repository.NotificationRepository;
import com.gege.activityfindermobile.ui.adapters.NotificationAdapter;
import com.gege.activityfindermobile.ui.main.MainActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationsFragment extends Fragment {

    @Inject NotificationRepository notificationRepository;

    private RecyclerView notificationsRecyclerView;
    private View emptyStateLayout;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton markAllReadButton;
    private Chip chipAll;
    private Chip chipUnread;

    private NotificationAdapter adapter;
    private boolean showUnreadOnly = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        markAllReadButton = view.findViewById(R.id.markAllReadButton);
        chipAll = view.findViewById(R.id.chipAll);
        chipUnread = view.findViewById(R.id.chipUnread);

        setupRecyclerView();
        setupSwipeRefresh();
        setupFilters();
        setupMarkAllRead();
        loadNotifications();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        adapter.setOnNotificationClickListener(this::handleNotificationClick);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        notificationsRecyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadNotifications);
        swipeRefresh.setColorSchemeResources(R.color.primary);
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> {
            showUnreadOnly = false;
            loadNotifications();
        });

        chipUnread.setOnClickListener(v -> {
            showUnreadOnly = true;
            loadNotifications();
        });
    }

    private void setupMarkAllRead() {
        markAllReadButton.setOnClickListener(v -> markAllNotificationsAsRead());
    }

    private void loadNotifications() {
        if (showUnreadOnly) {
            loadUnreadNotifications();
        } else {
            loadAllNotifications();
        }
    }

    private void loadAllNotifications() {
        notificationRepository.getAllNotifications(new ApiCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                swipeRefresh.setRefreshing(false);
                updateUI(notifications);
            }

            @Override
            public void onError(String error) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), "Failed to load notifications: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUnreadNotifications() {
        notificationRepository.getUnreadNotifications(new ApiCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                swipeRefresh.setRefreshing(false);
                updateUI(notifications);
            }

            @Override
            public void onError(String error) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), "Failed to load unread notifications: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(List<Notification> notifications) {
        if (notifications.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            notificationsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            notificationsRecyclerView.setVisibility(View.VISIBLE);
            adapter.setNotifications(notifications);
        }
    }

    private void handleNotificationClick(Notification notification) {
        // Mark as read
        if (!notification.getIsRead()) {
            notificationRepository.markAsRead(notification.getId(), new ApiCallbackVoid() {
                @Override
                public void onSuccess() {
                    loadNotifications(); // Refresh to update UI
                    refreshBadge(); // Update badge count
                }

                @Override
                public void onError(String error) {
                    // Silent fail
                }
            });
        }

        // Navigate to related content
        navigateToRelatedContent(notification);
    }

    private void navigateToRelatedContent(Notification notification) {
        if (notification.getActivityId() != null) {
            Bundle bundle = new Bundle();
            bundle.putLong("activityId", notification.getActivityId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_notifications_to_activityDetail, bundle);
        }
        // Add more navigation logic for participants, reviews, etc.
    }

    private void markAllNotificationsAsRead() {
        notificationRepository.markAllAsRead(new ApiCallbackVoid() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "All notifications marked as read",
                        Toast.LENGTH_SHORT).show();
                loadNotifications();
                refreshBadge(); // Update badge count
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Failed to mark all as read: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshBadge() {
        // Refresh the notification badge in MainActivity
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshNotificationBadge();
        }
    }
}
