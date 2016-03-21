package com.ifelse.posturometer.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Bruno on 31/10/2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence mTabTitles[];
    private int mTotalTabs;
    private MainTab mMainTab;
    private SettingsTab mSettingsTab;

    // Constructor
    public ViewPagerAdapter(FragmentManager fm, CharSequence mTabTitles[], int mTotalTabs) {
        super(fm);

        this.mTabTitles = mTabTitles;
        this.mTotalTabs = mTotalTabs;

        mMainTab = new MainTab();
        mSettingsTab = new SettingsTab();
    }

    // Tab # Fragment Getter
    @Override
    public Fragment getItem(int position) {

        switch(position) {
            case 0:
                return mMainTab;

            case 1:
                return  mSettingsTab;

            default:
                return null;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabTitles[position];
    }

    @Override
    public int getCount() {
        return mTotalTabs;
    }

}

