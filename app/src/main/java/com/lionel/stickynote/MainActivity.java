package com.lionel.stickynote;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lionel.stickynote.customview.Paper;
import com.lionel.stickynote.fieldclass.PaperProperty;
import com.lionel.stickynote.sqliteopenhelper.PaperContentDbHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static com.lionel.stickynote.PaperContentActivity.DB_NAME;


public class MainActivity extends AppCompatActivity implements Paper.DeletePaperInterface, Paper.OpenPaperContent {
    private ConstraintLayout mRootView;
    private ArrayList<PaperProperty> mPaperPropertyArrayList; // store Paper object
    private int mChildViewCount, mPaperId;
    private boolean isReenter;
    public static double iDeleteRegionX, iDeleteRegionY;
    private ImageView mImgViewTrashCan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_main);

        mRootView = findViewById(R.id.mainRootView);
        mChildViewCount = mRootView.getChildCount();
        mPaperPropertyArrayList = new ArrayList<>();

        setupDeleteRegion();
        setupTransition();
        showIntroPages();
    }

    private void showIntroPages() {
        // if app is first time launched, then show the intro pages
        SharedPreferences sharedPreferences = getSharedPreferences("FirstTimeUser", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isFirstTime", true)) {
            startActivity(new Intent(this, IntroPageActivity.class));
            sharedPreferences.edit().putBoolean("isFirstTime", false).apply();
        }
    }

    private void setupDeleteRegion() {
        // define delete region's X & Y
        mImgViewTrashCan = findViewById(R.id.imgViewTrashCan);
        mImgViewTrashCan.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                iDeleteRegionX = mImgViewTrashCan.getX() + mImgViewTrashCan.getWidth() * 0.85;
                iDeleteRegionY = mImgViewTrashCan.getY() + mImgViewTrashCan.getHeight() * 0.15;
                mImgViewTrashCan.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setupTransition() {
        Transition transition1 = TransitionInflater.from(this).inflateTransition(R.transition.transition1);
        Transition transition2 = TransitionInflater.from(this).inflateTransition(R.transition.transition2);
        getWindow().setExitTransition(transition1);
        getWindow().setReenterTransition(transition2);
        getWindow().setAllowEnterTransitionOverlap(true);
    }

    @Override
    public void onEnterAnimationComplete() {
        // exit animation would call this method, too. have to prevent it.
        if (isReenter) {
            setupPapers();
            isReenter = false;
        }
        super.onEnterAnimationComplete();
    }

    private void setupPapers() {
        // to avoid there is view and data remain
        mRootView.removeViews(2, mRootView.getChildCount() - 2);
        mPaperPropertyArrayList.clear();

        // read Paper Property from sharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("PaperPropertyData", MODE_PRIVATE);
        String json = sharedPreferences.getString("PaperProperty", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PaperProperty>>() {
            }.getType();
            mPaperPropertyArrayList = gson.fromJson(json, type);
        }
        // get Paper id from sharedPreferences
        if (mPaperId == 0) mPaperId = sharedPreferences.getInt("PaperId", 0);

        // setup papers
        for (int i = 0; i < mPaperPropertyArrayList.size(); i++) {
            PaperProperty pp = mPaperPropertyArrayList.get(i);
            Paper paper = new Paper(this, pp);
            mRootView.addView(paper);
        }
    }

    public void AddPaper(View view) {
        PaperProperty mPp = new PaperProperty(mPaperId, null,
                new String[]{null, null, null, null},
                "#AF626262",
                new float[]{0, 0});
        Paper paper = new Paper(this, mPp);
        mPaperPropertyArrayList.add(mPp);
        mRootView.addView(paper);
        mPaperId++;

        mChildViewCount = mRootView.getChildCount();
    }

    @Override
    public void deletePaper(Paper paper, PaperProperty pp) {
        mRootView.removeView(paper);
        mPaperPropertyArrayList.remove(pp); // delete this paper's property from the list array
        PaperContentDbHelper paperContentDbHelper = new PaperContentDbHelper(getApplicationContext(),
                DB_NAME, null, 1, "Paper" + pp.getPaperId());
        paperContentDbHelper.deleteTable();
        paperContentDbHelper.close();

        mChildViewCount = mRootView.getChildCount();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void openContent(PaperProperty pp) {
        Intent intent = new Intent(this, PaperContentActivity.class);
        // passing this Paper's Id,
        // then PaperContentActivity can save/load content into/from SharedPreference with specific name like "IdXItemO"
        // passing Property to ContentActivity,
        // so can save title and Top 4 content to the specific paper property.
        Bundle bundle = new Bundle();
        bundle.putSerializable("PaperProperty", pp);
        bundle.putSerializable("PaperPropertyList", mPaperPropertyArrayList);
        intent.putExtras(bundle);
        startActivityForResult(intent, 100, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // only use setupPapers() in onStart() in the following situations: first time to startup app or the paper was disappeared
        if (mChildViewCount == 2 || mRootView.getChildCount() != mChildViewCount) {
            setupPapers();
            mChildViewCount = mRootView.getChildCount();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // save Paper Property to sharedPreferences
        SharedPreferences.Editor sharedPreferences =
                getSharedPreferences("PaperPropertyData", MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(mPaperPropertyArrayList);
        sharedPreferences.putString("PaperProperty", json);

        // to save Paper Id is how much to add now
        sharedPreferences.putInt("PaperId", mPaperId);
        sharedPreferences.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isReenter = true;
        super.onActivityResult(requestCode, resultCode, data);
    }
}