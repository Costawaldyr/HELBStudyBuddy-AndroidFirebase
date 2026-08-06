package com.example.studybuddy.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.studybuddy.fragments.MatchFragment;
import com.example.studybuddy.MainActivity;
import com.example.studybuddy.R;
import com.example.studybuddy.adapters.BuddyPagerAdapter;
import com.example.studybuddy.utils.MyConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * BuddyListFragment — Manages the display of AI buddies and match candidates using a ViewPager2.
 */
public class BuddyListFragment extends Fragment
{
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BuddyPagerAdapter adapter;

    /**
     * Initializes the fragment view and sets up the ViewPager2 with tabs.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_buddy_list, container, false);

        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);

        setupViewPager();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                if (position == MyConstants.ONE)
                {
                    Fragment currentFragment = getChildFragmentManager()
                            .findFragmentByTag("f" + viewPager.getCurrentItem());

                    if (currentFragment instanceof MatchFragment)
                    {
                        ((MatchFragment) currentFragment).refreshMatches();
                    }
                }
            }
        });

        return view;
    }

    private void setupViewPager()
    {
        adapter = new BuddyPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
        {
            if (position == MyConstants.ZERO)
            {
                tab.setText(getString(R.string.tab_ia_buddies));
            }
            else if (position == MyConstants.ONE)
            {
                tab.setText(getString(R.string.tab_matches_24h));
            }
        }).attach();
    }
}
