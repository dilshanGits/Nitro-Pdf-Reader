package com.example.vivek.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.example.vivek.fragments.BookmarksFragment;
import com.example.vivek.fragments.TableContentsFragment;

public class ContentsPagerAdapter extends FragmentPagerAdapter {
    private String pdfPath;

    public int getCount() {
        return 2;
    }

    public ContentsPagerAdapter(FragmentManager fragmentManager, String str) {
        super(fragmentManager);
        this.pdfPath = str;
    }

    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                return TableContentsFragment.newInstance(this.pdfPath);
            case 1:
                return BookmarksFragment.newInstance(this.pdfPath);
            default:
                return null;
        }
    }
}
