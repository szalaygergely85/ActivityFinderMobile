package com.gege.activityfindermobile.ui.chat;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallback;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.model.ActivityMessage;
import com.gege.activityfindermobile.data.repository.MessageRepository;
import com.gege.activityfindermobile.ui.adapters.MessageAdapter;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivityChatFragment extends Fragment {

    private static final String ARG_ACTIVITY_ID = "activity_id";
    private static final long POLL_INTERVAL = 5000; // Poll every 5 seconds

    @Inject MessageRepository messageRepository;
    @Inject SharedPreferencesManager prefsManager;

    private Long activityId;
    private RecyclerView messagesRecyclerView;
    private View emptyStateLayout;
    private TextInputEditText messageInput;
    private FloatingActionButton sendButton;

    private MessageAdapter adapter;
    private Handler pollHandler;
    private Runnable pollRunnable;
    private String lastTimestamp;

    public static ActivityChatFragment newInstance(Long activityId) {
        ActivityChatFragment fragment = new ActivityChatFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ACTIVITY_ID, activityId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            activityId = getArguments().getLong(ARG_ACTIVITY_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);

        setupRecyclerView();
        setupSendButton();
        loadMessages();
        startPolling();
    }

    private void setupRecyclerView() {
        Long currentUserId = prefsManager.getUserId();
        adapter = new MessageAdapter(currentUserId);
        adapter.setOnMessageLongClickListener(this::showDeleteMessageDialog);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(adapter);
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        messageRepository.getMessages(activityId, new ApiCallback<List<ActivityMessage>>() {
            @Override
            public void onSuccess(List<ActivityMessage> messages) {
                if (messages.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    messagesRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    messagesRecyclerView.setVisibility(View.VISIBLE);
                    adapter.setMessages(messages);
                    scrollToBottom();

                    // Update last timestamp for polling
                    if (!messages.isEmpty()) {
                        lastTimestamp = messages.get(messages.size() - 1).getCreatedAt();
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Failed to load messages: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        messageRepository.sendMessage(activityId, messageText, new ApiCallback<ActivityMessage>() {
            @Override
            public void onSuccess(ActivityMessage message) {
                messageInput.setText("");
                emptyStateLayout.setVisibility(View.GONE);
                messagesRecyclerView.setVisibility(View.VISIBLE);
                adapter.addMessage(message);
                scrollToBottom();
                lastTimestamp = message.getCreatedAt();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Failed to send message: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPolling() {
        pollHandler = new Handler(Looper.getMainLooper());
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (lastTimestamp != null) {
                    pollForNewMessages();
                }
                pollHandler.postDelayed(this, POLL_INTERVAL);
            }
        };
        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL);
    }

    private void pollForNewMessages() {
        messageRepository.getMessagesSince(activityId, lastTimestamp,
                new ApiCallback<List<ActivityMessage>>() {
                    @Override
                    public void onSuccess(List<ActivityMessage> messages) {
                        if (!messages.isEmpty()) {
                            for (ActivityMessage message : messages) {
                                adapter.addMessage(message);
                            }
                            scrollToBottom();
                            lastTimestamp = messages.get(messages.size() - 1).getCreatedAt();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        // Silent fail for polling
                    }
                });
    }

    private void showDeleteMessageDialog(ActivityMessage message) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMessage(message))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMessage(ActivityMessage message) {
        messageRepository.deleteMessage(activityId, message.getId(), new ApiCallbackVoid() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                loadMessages(); // Reload to update UI
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Failed to delete message: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            messagesRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }
}
