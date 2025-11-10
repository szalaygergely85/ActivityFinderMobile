package com.gege.activityfindermobile.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gege.activityfindermobile.R;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/** Adapter for displaying activity interests/tags as horizontal chips */
public class InterestChipsAdapter
        extends RecyclerView.Adapter<InterestChipsAdapter.InterestViewHolder> {

    private List<String> interests;
    private Context context;

    public InterestChipsAdapter(Context context, List<String> interests) {
        this.context = context;
        this.interests = interests != null ? interests : new ArrayList<>();
    }

    @NonNull
    @Override
    public InterestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(context).inflate(R.layout.item_interest_chip, parent, false);
        return new InterestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InterestViewHolder holder, int position) {
        String interest = interests.get(position);
        holder.chip.setText(interest);
    }

    @Override
    public int getItemCount() {
        return interests.size();
    }

    public void updateInterests(List<String> newInterests) {
        this.interests = newInterests != null ? newInterests : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class InterestViewHolder extends RecyclerView.ViewHolder {
        Chip chip;

        public InterestViewHolder(@NonNull View itemView) {
            super(itemView);
            chip = itemView.findViewById(R.id.chip_interest);
        }
    }
}
