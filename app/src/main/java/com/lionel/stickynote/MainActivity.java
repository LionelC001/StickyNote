package com.lionel.stickynote;

import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements Paper.DeletePaperInterface {

    private ConstraintLayout mRootView;
    private ArrayList<PaperProperty> paperPropertyArrayList; // store Paper object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRootView = findViewById(R.id.rootView);
        paperPropertyArrayList = new ArrayList<>();
    }

    public void AddPaper(View view) {
        PaperProperty pp = new PaperProperty("TITLE",
                new String[]{"this is one", "this is two", "this is three", "this is four"},
                "#55555500",
                new float[]{0, 0});
        Paper paper = new Paper(this, pp);
        paperPropertyArrayList.add(pp);
        mRootView.addView(paper);
    }

    @Override
    public void deletePaper(Paper paper, PaperProperty pp) {
        mRootView.removeView(paper);
        paperPropertyArrayList.remove(pp); // delete this paper's property from the list array
    }

    @Override
    protected void onStop() {
        super.onStop();
        // save Paper Property to sharedPreferences
        SharedPreferences.Editor sharedPreferences =
                getSharedPreferences("PaperPropertyData", MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(paperPropertyArrayList);
        sharedPreferences.putString("PaperProperty", json);
        sharedPreferences.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // read Paper Property from sharedPreferences
        if (paperPropertyArrayList.size() == 0) {
            SharedPreferences sharedPreferences = getSharedPreferences("PaperPropertyData", MODE_PRIVATE);
            String json = sharedPreferences.getString("PaperProperty", null);
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<PaperProperty>>() {
                }.getType();
                paperPropertyArrayList = gson.fromJson(json, type);

                for (int i = 0; i < paperPropertyArrayList.size(); i++) {
                    PaperProperty pp = paperPropertyArrayList.get(i);
                    Paper paper = new Paper(this, pp);
                    mRootView.addView(paper);
                }
            }
        }
    }
}
