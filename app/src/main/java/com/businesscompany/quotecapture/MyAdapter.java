package com.businesscompany.quotecapture;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public static class MyAdapter extends FragmentPagerAdapter {

    public MyAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        if (position < 1){
            return new CameraFragment();
        }
        //this else if is for when a quote has just been read via OCR
        else{
            return new MainFragment();
        }

    }
}
