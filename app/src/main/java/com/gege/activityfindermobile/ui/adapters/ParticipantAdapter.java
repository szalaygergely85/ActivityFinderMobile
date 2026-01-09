package com.gege.activityfindermobile.ui.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.model.Participant;
import com.gege.activityfindermobile.utils.DateUtil;
import com.gege.activityfindermobile.utils.ImageLoader;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {

    private List<Participant> participants = new ArrayList<>();
    private OnParticipantClickListener listener;
    private OnReviewClickListener reviewListener;
    private OnRemoveClickListener removeListener;
    private Long activityId;
    private String activityDate;
    private Long currentUserId;
    private Long creatorId;

    private final Owner owner;

    public enum Owner {
        ParticipantsTabFragment,
        ActivityDetailFragment
    }

    public interface OnParticipantClickListener {
        void onParticipantClick(Participant participant);
    }

    public interface OnReviewClickListener {
        void onReviewClick(Participant participant, Long activityId);
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(Participant participant);
    }

    public ParticipantAdapter(Owner participantsTabFragment) {
        this(null, participantsTabFragment);
    }

    public ParticipantAdapter(OnParticipantClickListener listener, Owner owner) {
        this.listener = listener;
        this.owner = owner;
    }

    public boolean isParticpantTabFragment() {
        return owner == Owner.ParticipantsTabFragment;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
        notifyDataSetChanged();
    }

    public void setReviewListener(OnReviewClickListener reviewListener) {
        this.reviewListener = reviewListener;
    }

    public void setRemoveListener(OnRemoveClickListener removeListener) {
        this.removeListener = removeListener;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public void setActivityDate(String activityDate) {
        this.activityDate = activityDate;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public void removeParticipant(Long participantId) {
        for (int i = 0; i < participants.size(); i++) {
            if (participants.get(i).getId().equals(participantId)) {
                participants.remove(i);
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
                        .inflate(R.layout.item_participant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Participant participant = participants.get(position);
        holder.bind(
                participant,
                listener,
                reviewListener,
                removeListener,
                activityId,
                activityDate,
                currentUserId,
                creatorId,
                owner);
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView ivUserAvatar;
        TextView tvUserName, tvUserRating;
        Chip chipStatus;
        MaterialButton btnReview;
        MaterialButton btnRemove;
        View cardParticipant;

        View divParticipant;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardParticipant = itemView.findViewById(R.id.card_participant);
            ivUserAvatar = itemView.findViewById(R.id.iv_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserRating = itemView.findViewById(R.id.tv_user_rating);
            chipStatus = itemView.findViewById(R.id.chip_status);
            btnReview = itemView.findViewById(R.id.btn_review);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            divParticipant = itemView.findViewById(R.id.div_participant);
        }

        void bind(
                Participant participant,
                OnParticipantClickListener listener,
                OnReviewClickListener reviewListener,
                OnRemoveClickListener removeListener,
                Long activityId,
                String activityDate,
                Long currentUserId,
                Long creatorId,
                Owner owner) {
            // Load user avatar
            ImageLoader.loadCircularProfileImage(
                    itemView.getContext(), participant.getUserAvatar(), ivUserAvatar);

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

            // Set status with visual styling
            String status = participant.getStatus();
            if (status != null) {
                chipStatus.setVisibility(View.VISIBLE);

                switch (status) {
                    case "PENDING":
                    case "INTERESTED":
                        chipStatus.setText("Pending");
                        chipStatus.setChipBackgroundColorResource(R.color.warning);
                        chipStatus.setTextColor(
                                itemView.getContext().getResources().getColor(R.color.white, null));
                        break;
                    case "ACCEPTED":
                        chipStatus.setText("Accepted");
                        chipStatus.setChipBackgroundColorResource(R.color.success);
                        chipStatus.setTextColor(
                                itemView.getContext().getResources().getColor(R.color.white, null));
                        break;
                    case "JOINED":
                        chipStatus.setText("Joined");
                        chipStatus.setChipBackgroundColorResource(R.color.primary);
                        chipStatus.setTextColor(
                                itemView.getContext().getResources().getColor(R.color.white, null));
                        break;
                    default:
                        chipStatus.setText(status);
                        chipStatus.setChipBackgroundColorResource(R.color.text_secondary);
                        chipStatus.setTextColor(
                                itemView.getContext().getResources().getColor(R.color.white, null));
                        break;
                }
            } else {
                chipStatus.setVisibility(View.GONE);
            }

            // Show review button only for:
            // 1. Joined participants (JOINED or ACCEPTED status)
            // 2. Activity is expired
            // 3. Not reviewing yourself
            boolean isActivityExpired = isActivityExpired(activityDate);
            boolean isCurrentUser =
                    currentUserId != null && currentUserId.equals(participant.getUserId());
            boolean isParticipantTabFragment =
                    owner == ParticipantAdapter.Owner.ParticipantsTabFragment;

            if (("JOINED".equals(status) || "ACCEPTED".equals(status))
                    && isParticipantTabFragment
                    && isActivityExpired
                    && !isCurrentUser) {
                btnReview.setVisibility(View.VISIBLE);
                divParticipant.setVisibility(View.VISIBLE);
                btnReview.setOnClickListener(
                        v -> {
                            Log.d(
                                    "ParticipantAdapter",
                                    "Review button clicked - reviewListener: "
                                            + (reviewListener != null)
                                            + ", activityId: "
                                            + activityId);
                            if (reviewListener != null && activityId != null) {
                                Log.d(
                                        "ParticipantAdapter",
                                        "Calling onReviewClick for user: "
                                                + participant.getUserName());
                                reviewListener.onReviewClick(participant, activityId);
                            } else {
                                Log.e(
                                        "ParticipantAdapter",
                                        "ReviewListener or activityId is null!");
                            }
                        });
            } else {
                divParticipant.setVisibility(View.GONE);
                btnReview.setVisibility(View.GONE);
            }

            // Show remove button only for activity creator and not for themselves
            boolean isCreator =
                    creatorId != null && currentUserId != null && creatorId.equals(currentUserId);
            boolean isCurrentUserParticipant =
                    currentUserId != null && currentUserId.equals(participant.getUserId());

            if (isCreator
                    && !isCurrentUserParticipant
                    && ("ACCEPTED".equals(status) || "JOINED".equals(status))
                    && isParticipantTabFragment) {
                btnRemove.setVisibility(View.VISIBLE);
                divParticipant.setVisibility(View.VISIBLE);
                btnRemove.setOnClickListener(
                        v -> {
                            if (removeListener != null) {
                                removeListener.onRemoveClick(participant);
                            }
                        });
            } else {
                btnRemove.setVisibility(View.GONE);
            }

            // Set click listener
            if (listener != null) {
                cardParticipant.setOnClickListener(v -> listener.onParticipantClick(participant));
            }
        }

        private boolean isActivityExpired(String dateStr) {
            return DateUtil.isDisplayDateExpired(dateStr);
        }
    }
}
