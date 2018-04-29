package com.lionel.stickynote.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class IntroPageAdapter extends PagerAdapter{

    private final ArrayList<View> mIntroPagesList;

    public IntroPageAdapter(ArrayList<View> list) {
        mIntroPagesList = list;
    }

    @Override
    public int getCount() {
        return mIntroPagesList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mIntroPagesList.get(position));
        return mIntroPagesList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mIntroPagesList.get(position));
    }
}
