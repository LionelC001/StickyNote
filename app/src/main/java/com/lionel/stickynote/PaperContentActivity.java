package com.lionel.stickynote;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import java.util.ArrayList;

public class PaperContentActivity extends AppCompatActivity {
    private ArrayList<PaperProperty> mPaperPropertyArrayList;
    private ArrayList<String> mContentItemList;
    private EditText mEdtContentTitle;
    private RecyclerContentListAdapter mRecyclerAdapter;
    private PaperProperty mPaperProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_content);
        mPaperProperty = (PaperProperty) getIntent().getExtras().getSerializable("PaperProperty");
        mPaperPropertyArrayList = (ArrayList<PaperProperty>) getIntent().getExtras().getSerializable("PaperPropertyList");
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
        getMenuInflater().inflate(R.menu.menu_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menuItemColor:
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

        ((FloatingActionButton) findViewById(R.id.btnAddItem))
                .setColorNormal(Color.parseColor("#e6593a"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // load array list form SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("PaperContentData" + mPaperProperty.getPaperId(), MODE_PRIVATE);
        if (sharedPreferences.getString("Item0", null) != null) {
            mContentItemList.clear();
            for (int i = 0; i < sharedPreferences.getInt("size", 0); i++) {
                mContentItemList.add(sharedPreferences.getString("Item" + i, null));
            }
        }
        // load title form SharedPreferences
        mEdtContentTitle.setText(sharedPreferences.getString("title", null));
        setupRecyclerView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveContentToSP();
        savePropertyToSP();
    }

    private void saveContentToSP() {
        // save array list into ContentSharedPreferences
        SharedPreferences.Editor spContent =
                getSharedPreferences("PaperContentData" + mPaperProperty.getPaperId(), MODE_PRIVATE).edit();
        spContent.putInt("size", mContentItemList.size());
        for (int i = 0; i < mContentItemList.size(); i++) {
            spContent.putString("Item" + i, mContentItemList.get(i));
        }
        //save title to SharedPreferences
        spContent.putString("title", mEdtContentTitle.getText().toString());
        spContent.apply();
    }

    private void savePropertyToSP() {
        // save Title and the top four items to the specific PaperProperty
        // and form the PropertyArrayList with the PaperProperty
        String[] contentTop4 = new String[4];
        for (int i = 0; i < mContentItemList.size(); i++) {
            if (!mContentItemList.get(i).equals("")) {
                contentTop4[i] = (i+1) + ". " + mContentItemList.get(i);
            }
            if(i==3) break;
        }

        // update PropertyArrayList
        for (PaperProperty pp : mPaperPropertyArrayList) {
            if (pp.getPaperId() == mPaperProperty.getPaperId()) {
                int ppIndex = mPaperPropertyArrayList.indexOf(pp);
                mPaperProperty.setTitle(mEdtContentTitle.getText().toString());
                mPaperProperty.setContent(contentTop4);
                mPaperPropertyArrayList.set(ppIndex, mPaperProperty);
                break;
            }
        }
        // save this PropertyArrayList into PropertySharedPreferences
        SharedPreferences.Editor sharedPreferences =
                getSharedPreferences("PaperPropertyData", MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(mPaperPropertyArrayList);
        sharedPreferences.putString("PaperProperty", json);
        sharedPreferences.apply();
    }


    private void setupRecyclerView() {
        RecyclerView mRecyclerViewContentList = findViewById(R.id.recyclerContentList);
        mRecyclerAdapter = new RecyclerContentListAdapter(mContentItemList);
        mRecyclerViewContentList.setAdapter(mRecyclerAdapter);
        mRecyclerViewContentList.setLayoutManager(new LinearLayoutManager(this));

        // let list item can swipe and drag
        SimpleItemTouchHelper simpleItemTouchHelper = new SimpleItemTouchHelper(mRecyclerAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mRecyclerViewContentList);
    }


    public void addItem(View view) {
        mContentItemList.add("");
        mRecyclerAdapter.notifyDataSetChanged();
    }

    public void clear(View view) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure for clear all items?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContentItemList.clear();
                        mRecyclerAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}