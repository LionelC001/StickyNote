package com.lionel.stickynote;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import java.util.ArrayList;

public class PaperContentActivity extends AppCompatActivity {

    private ArrayList<String> mContentItemList;
    private EditText mEdtContentTitle;
    private RecyclerContentListAdapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_content);

        mContentItemList = new ArrayList<>();
        mEdtContentTitle = findViewById(R.id.edtContentTitle);
        setupToolbar();
    }

    private void setupToolbar() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d("<<<", "homeAdUp");
                break;
            case R.id.menuItemColor:
                Log.d("<<<", "Palette");

                setStatusBarColor();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setStatusBarColor() {
        Window window = getWindow();
// clear FLAG_TRANSLUCENT_STATUS flag:
      /*  window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);*/
// finally change the color
        window.setStatusBarColor(Color.parseColor("#55550000"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // load array list form SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("PaperContentData", MODE_PRIVATE);
        if (sharedPreferences.getString("item0", null) != null) {
            mContentItemList.clear();
            for (int i = 0; i < sharedPreferences.getInt("size", 0); i++) {
                mContentItemList.add(sharedPreferences.getString("item" + i, null));
            }
        }
        // load title form SharedPreferences
        mEdtContentTitle.setText(sharedPreferences.getString("title", null));
        setupRecyclerView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // save array list to SharedPreferences
        SharedPreferences.Editor sharedPreferences =
                getSharedPreferences("PaperContentData", MODE_PRIVATE).edit();
        sharedPreferences.putInt("size", mContentItemList.size());
        for (int i = 0; i < mContentItemList.size(); i++) {
            sharedPreferences.putString("item" + i, mContentItemList.get(i));
        }
        //save title to SharedPreferences
        sharedPreferences.putString("title", mEdtContentTitle.getText().toString());
        sharedPreferences.apply();
    }


    private void setupRecyclerView() {
        RecyclerView mContentListView = findViewById(R.id.recyclerContentList);
        mRecyclerAdapter = new RecyclerContentListAdapter(mContentItemList);
        mContentListView.setAdapter(mRecyclerAdapter);
        mContentListView.setLayoutManager(new LinearLayoutManager(this));
    }

    public void addItem(View view) {
        mContentItemList.add("");
        mRecyclerAdapter.notifyDataSetChanged();
    }
}
