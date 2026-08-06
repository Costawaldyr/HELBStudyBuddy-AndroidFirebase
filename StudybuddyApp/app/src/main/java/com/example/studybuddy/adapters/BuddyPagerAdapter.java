package com.example.studybuddy.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.studybuddy.fragments.IABuddyFragment;
import com.example.studybuddy.fragments.MatchFragment;

/**
 * Pager adapter for the main buddy discovery screen.
 * Manages switching between the AI Buddy discovery and Human Match discovery fragments.
 */
public class BuddyPagerAdapter extends FragmentStateAdapter
{
    private static final int TAB_IA_BUDDY = 0;
    private static final int TAB_MATCH = 1;
    private static final int TOTAL_TABS = 2;

    /**
     * Initializes the pager adapter with the host activity.
     *
     * @param fragmentActivity The activity hosting the ViewPager2.
     */
    public BuddyPagerAdapter(@NonNull FragmentActivity fragmentActivity)
    {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position)
    {
        switch (position)
        {
            case TAB_IA_BUDDY:
                return new IABuddyFragment();
            case TAB_MATCH:
                return new MatchFragment();
            default:
                return new IABuddyFragment();
        }
    }

    @Override
    public int getItemCount()
    {
        return TOTAL_TABS;
    }
}
