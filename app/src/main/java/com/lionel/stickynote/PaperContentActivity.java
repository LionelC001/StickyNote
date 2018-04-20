package com.lionel.stickynote;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class PaperContentActivity extends AppCompatActivity {
    public final static String DB_NAME = "PaperContent.db";

    private PaperProperty mPaperProperty;
    private ArrayList<PaperProperty> mPaperPropertyArrayList;
    private ArrayList<String> mContentItemList;
    private EditText mEdtContentTitle;
    private RecyclerContentListAdapter mRecyclerAdapter;
    private String table_name;
    private Cursor mCursor;
    private PaperContentDbHelper mPaperContentDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paper_content);
        mPaperProperty = (PaperProperty) getIntent().getExtras().getSerializable("PaperProperty");
        mPaperPropertyArrayList = (ArrayList<PaperProperty>) getIntent().getExtras().getSerializable("PaperPropertyList");
        mContentItemList = new ArrayList<>();
        mEdtContentTitle = findViewById(R.id.edtContentTitle);

        setupDB();
    }

    private void setupDB() {
        table_name = "Paper" + mPaperProperty.getPaperId();
        mPaperContentDbHelper = new PaperContentDbHelper(getApplicationContext(),
                DB_NAME, null, 1, table_name);
        mPaperContentDbHelper.createTable();
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

    @Override
    protected void onStart() {
        super.onStart();
        mCursor = mPaperContentDbHelper.query(table_name, null, null, null, null, null, null);
        if (mCursor == null) return;
        if (mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            // load array list form SQLite
            String json = mCursor.getString(mCursor.getColumnIndex("item"));
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                mContentItemList = gson.fromJson(json, type);
            }
            // load title form SQLite
            mEdtContentTitle.setText(mCursor.getString(1));
        }
        setupRecyclerView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveContentToSQLite();
        savePropertyToSP();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCursor.close();
        mPaperContentDbHelper.close();
    }

    private void saveContentToSQLite() {
        // save title and content into SQLite
        Gson gson = new Gson();
        String json = gson.toJson(mContentItemList);

        ContentValues cv = new ContentValues();
        cv.put("title", mEdtContentTitle.getText().toString());
        cv.put("item", json);
        // check whether it should insert or not
        if (mCursor.getCount() == 0) mPaperContentDbHelper.insert(table_name, null, cv);
        else mPaperContentDbHelper.update(table_name, cv, "_id=1", null);
    }

    private void savePropertyToSP() {
        // save Title and the top four items to the specific PaperProperty
        // and form the PropertyArrayList with the PaperProperty
        String[] contentTop4 = new String[4];
        for (int i = 0; i < mContentItemList.size(); i++) {
            if (!mContentItemList.get(i).equals("")) {
                contentTop4[i] = (i + 1) + ". " + mContentItemList.get(i);
            } else {
                contentTop4[i] = (i + 1) + ". ";
            }
            if (i == 3) break;
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

    public void changeColorTheme(View view) {
// clear FLAG_TRANSLUCENT_STATUS flag:
      /*  window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);*/
// finally change the color
        getWindow().setStatusBarColor(Color.parseColor("#55550000"));
        ((FloatingActionButton) findViewById(R.id.btnAddItem))
                .setColorNormal(Color.parseColor("#e6593a"));
    }
}