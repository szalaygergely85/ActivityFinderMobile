package com.gege.activityfindermobile.ui.manage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.gege.activityfindermobile.R;
import com.gege.activityfindermobile.data.callback.ApiCallbackVoid;
import com.gege.activityfindermobile.data.repository.ActivityRepository;
import com.gege.activityfindermobile.utils.SharedPreferencesManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageActivityFragment extends Fragment {

    @Inject ActivityRepository activityRepository;
    @Inject SharedPreferencesManager prefsManager;

    private Long activityId;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_activity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get activity ID from arguments
        if (getArguments() != null) {
            activityId = getArguments().getLong("activityId", 0L);
        }

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        toolbar.setOnMenuItemClickListener(this::onMenuItemClick);

        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        // Setup ViewPager
        ManagePagerAdapter adapter = new ManagePagerAdapter(this, activityId);
        viewPager.setAdapter(adapter);

        // Link TabLayout and ViewPager
        new TabLayoutMediator(
                        tabLayout,
                        viewPager,
                        (tab, position) -> {
                            if (position == 0) {
                                tab.setText("Requests");
                            } else {
                                tab.setText("Participants");
                            }
                        })
                .attach();
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_activity) {
            showDeleteConfirmationDialog();
            return true;
        }
        return false;
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireActivity())
                .setTitle("Delete Activity")
                .setMessage(
                        "Are you sure you want to delete this activity? This action cannot be"
                                + " undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteActivity())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteActivity() {
        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        activityRepository.cancelActivity(
                activityId,
                userId,
                new ApiCallbackVoid() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(
                                        requireContext(),
                                        "Activity deleted successfully",
                                        Toast.LENGTH_SHORT)
                                .show();
                        // Navigate back to my activities
                        NavController navController = Navigation.findNavController(requireView());
                        navController.popBackStack();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(
                                        requireContext(),
                                        "Failed to delete activity: " + errorMessage,
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private static class ManagePagerAdapter extends FragmentStateAdapter {
        private final Long activityId;

        public ManagePagerAdapter(@NonNull Fragment fragment, Long activityId) {
            super(fragment);
            this.activityId = activityId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return RequestsTabFragment.newInstance(activityId);
            } else {
                return ParticipantsTabFragment.newInstance(activityId);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
