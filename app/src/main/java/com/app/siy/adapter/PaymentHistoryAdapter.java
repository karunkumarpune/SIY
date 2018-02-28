package com.app.siy.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by Manish-Pc on 09/12/2017.
 */

public class PaymentHistoryAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> fragments = new ArrayList();
    ArrayList<String> tabTitles = new ArrayList();

    public PaymentHistoryAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addTab(Fragment fragment, String tabTitle){
        fragments.add(fragment);
        tabTitles.add(tabTitle);
    }


    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }
}
