package com.lionel.stickynote;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lionel.stickynote.adapter.IntroPageAdapter;

import java.util.ArrayList;

public class IntroPageActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private ImageView whiteDot;
    private int mDistance;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_page);

        mViewPager = findViewById(R.id.viewPager);
        setDots();
        setAdapter();
        addPageChangeListener();
    }

    private void setDots() {
        final LinearLayout ll = findViewById(R.id.dot_linlayout);
        ImageView dot1 = new ImageView(this);
        ImageView dot2 = new ImageView(this);
        ImageView dot3 = new ImageView(this);
        ImageView dot4 = new ImageView(this);
        dot1.setBackgroundResource(R.drawable.gray_dot);
        dot2.setBackgroundResource(R.drawable.gray_dot);
        dot3.setBackgroundResource(R.drawable.gray_dot);
        dot4.setBackgroundResource(R.drawable.gray_dot);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 60, 0);
        ll.addView(dot1, params);
        ll.addView(dot2, params);
        ll.addView(dot3, params);
        ll.addView(dot4, params);

        whiteDot = findViewById(R.id.white_dot);
        whiteDot.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mDistance = ll.getChildAt(1).getLeft() - ll.getChildAt(0).getLeft();
                whiteDot.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setAdapter() {
        ArrayList<View> mIntroPagesList = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(this);
        mIntroPagesList.add(inflater.inflate(R.layout.intro1, null));
        mIntroPagesList.add(inflater.inflate(R.layout.intro2, null));
        mIntroPagesList.add(inflater.inflate(R.layout.intro3, null));
        mIntroPagesList.add(inflater.inflate(R.layout.intro4, null));
        mViewPager.setAdapter(new IntroPageAdapter(mIntroPagesList));
    }

    private void addPageChangeListener() {
        final ImageView imgViewLeft = findViewById(R.id.imgViewLeft);
        final ImageView imgViewRight = findViewById(R.id.imgViewRight);
        final ImageView imgViewFin = findViewById(R.id.imgViewFin);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float marginLeft = mDistance * (position + positionOffset);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) whiteDot.getLayoutParams();
                params.leftMargin = (int) marginLeft;
                whiteDot.setLayoutParams(params);
            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                if (position == 0) imgViewLeft.setVisibility(View.GONE);
                if (position == 1) imgViewLeft.setVisibility(View.VISIBLE);
                if (position == 2) {
                    imgViewFin.setVisibility(View.GONE);
                    imgViewRight.setVisibility(View.VISIBLE);
                }
                if (position == 3) {
                    imgViewRight.setVisibility(View.GONE);
                    imgViewFin.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    public void goLeft(View view) {
        mViewPager.setCurrentItem(currentPosition - 1);
    }

    public void goRight(View view) {
        mViewPager.setCurrentItem(currentPosition + 1);

    }

    public void GoFINISH(View view) {
        finish();
    }
}
