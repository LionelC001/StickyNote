package com.lionel.stickynote;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements Paper.DeletePaperInterface, Paper.OpenPaperContent {
    private ConstraintLayout mRootView;
    private ArrayList<PaperProperty> mPaperPropertyArrayList; // store Paper object
    private int mPaperId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRootView = findViewById(R.id.rootView);
        mPaperPropertyArrayList = new ArrayList<>();
    }

    public void AddPaper(View view) {
        PaperProperty mPp = new PaperProperty(mPaperId, null,
                new String[]{null, null, null, null},
                "#55555500",
                new float[]{0, 0});
        Paper paper = new Paper(this, mPp);
        mPaperPropertyArrayList.add(mPp);
        mRootView.addView(paper);
        mPaperId++;
    }

    @Override
    public void deletePaper(Paper paper, PaperProperty pp) {
        mRootView.removeView(paper);
        mPaperPropertyArrayList.remove(pp); // delete this paper's property from the list array
    }

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
        startActivity(intent);
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
    protected void onStart() {
        super.onStart();
        // to avoid there is data or views remain
        mPaperPropertyArrayList.clear();
        mRootView.removeViews(1,mRootView.getChildCount()-1);

        // read Paper Property from sharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("PaperPropertyData", MODE_PRIVATE);
        String json = sharedPreferences.getString("PaperProperty", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<PaperProperty>>() {
            }.getType();
            mPaperPropertyArrayList = gson.fromJson(json, type);
            for (int i = 0; i < mPaperPropertyArrayList.size(); i++) {
                PaperProperty pp = mPaperPropertyArrayList.get(i);
                Paper paper = new Paper(this, pp);
                mRootView.addView(paper);
            }
        }
        // get Paper id from sharedPreferences
        if (mPaperId == 0) mPaperId = sharedPreferences.getInt("PaperId", 0);
    }
}