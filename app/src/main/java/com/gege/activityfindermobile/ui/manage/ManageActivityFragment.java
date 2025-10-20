package com.gege.activityfindermobile.ui.manage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.gege.activityfindermobile.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ManageActivityFragment extends Fragment {

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
