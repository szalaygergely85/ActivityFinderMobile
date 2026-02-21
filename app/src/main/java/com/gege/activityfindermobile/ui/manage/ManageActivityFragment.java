package com.gege.activityfindermobile.ui.manage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.gege.activityfindermobile.R;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ManageActivityFragment extends Fragment {

    private Long activityId;
    private Long creatorId;
    private String activityTitle;
    private String activityStatus;
    private String activityDate;

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

        ViewCompat.setOnApplyWindowInsetsListener(
                view,
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    AppBarLayout appBar = v.findViewById(R.id.app_bar);
                    if (appBar != null) {
                        appBar.setPadding(0, systemBars.top, 0, 0);
                    }

                    return insets;
                });

        // Get activity data from arguments
        if (getArguments() != null) {
            activityId = getArguments().getLong("activityId", 0L);
            creatorId = getArguments().getLong("creatorId", 0L);
            activityTitle = getArguments().getString("activityTitle", "Manage Activity");
            activityStatus = getArguments().getString("activityStatus", "ACTIVE");
            activityDate = getArguments().getString("activityDate", "");
        }

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Set activity title and status in the custom toolbar
        android.widget.TextView tvActivityTitle = view.findViewById(R.id.tv_activity_title);
        android.widget.TextView tvActivityStatus = view.findViewById(R.id.tv_activity_status);
        if (tvActivityTitle != null) {
            tvActivityTitle.setText(activityTitle);
        }
        if (tvActivityStatus != null) {
            tvActivityStatus.setText(activityStatus);
        }

        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        // Setup ViewPager
        ManagePagerAdapter adapter = new ManagePagerAdapter(this, activityId, creatorId, activityDate);
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


    private static class ManagePagerAdapter extends FragmentStateAdapter {
        private final Long activityId;
        private final Long creatorId;
        private final String activityDate;

        public ManagePagerAdapter(@NonNull Fragment fragment, Long activityId, Long creatorId, String activityDate) {
            super(fragment);
            this.activityId = activityId;
            this.creatorId = creatorId;
            this.activityDate = activityDate;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return RequestsTabFragment.newInstance(activityId);
            } else {
                return ParticipantsTabFragment.newInstance(activityId, creatorId, activityDate);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
