package com.example.vivek.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.example.vivek.fragments.DevicePdfFragment;
import com.example.vivek.fragments.RecentPdfFragment;

public class BrowsePdfPagerAdapter extends FragmentPagerAdapter {
    public int getCount() {
        return 2;
    }

    public BrowsePdfPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return new RecentPdfFragment();
            case 1:
                return new DevicePdfFragment();
            default:
                return null;
        }
    }
}
