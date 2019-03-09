package com.cdps.proxifood;

import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.cdps.proxifood.R;

public class ProfilePagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private long userId;

    public ProfilePagerAdapter(Context context, FragmentManager fm, long userId) {
        super(fm);
        mContext = context;
        this.userId = userId;
    }

    // This determines the fragment for each tab
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return ProfileDetailsFragment.newInstance(userId);
        }
        else if (position == 1) {
            return ProfileCommentsFragment.newInstance(userId);
        }else {
            return ProfileDetailsFragment.newInstance(userId);
        }
    }

    // This determines the number of tabs
    @Override
    public int getCount() {
        return 2;
    }

    // This determines the title for each tab
    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return mContext.getString(R.string.tab_profile);
            case 1:
                return mContext.getString(R.string.tab_comments);
            default:
                return null;
        }
    }

}