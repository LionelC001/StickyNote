package com.lionel.stickynote;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lionel.stickynote.adapter.RecyclerContentListAdapter;
import com.lionel.stickynote.customview.ColorPickerBlock;
import com.lionel.stickynote.fieldclass.PaperProperty;
import com.lionel.stickynote.itemhelper.SimpleItemTouchHelper;
import com.lionel.stickynote.sqliteopenhelper.PaperContentDbHelper;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class PaperContentActivity extends AppCompatActivity {
    public final static String DB_NAME = "PaperContent.db";
    private int mThemeIndex;

    private PaperProperty mPaperProperty;
    private ArrayList<PaperProperty> mPaperPropertyArrayList;
    private ArrayList<String> mContentItemList;
    private EditText mEdtContentTitle;
    private RecyclerContentListAdapter mRecyclerAdapter;
    private String table_name;
    private Cursor mCursor;
    private PaperContentDbHelper mPaperContentDbHelper;
    private ColorPickerHandler mColorPickerHandler = new ColorPickerHandler(this);
    private Dialog mDialog;
    private String[][] mColorBackground = {{"#AF626262", "#AFefb2d0"}, {"#D6B1D434", "#E08B468B"}, {"#AFff7500", "#DC0450FB"}};
    private String[][] mColorForeground = {{"#AFFFFFFF", "#AF00ced1"}, {"#AFFCD517", "#AFFE7D1A"}, {"#AFfcd0ab", "#AFFF3535"}};

    // Color for StatusBar, RootView, Title, FloatingActionButtonChild, ItemBG, ItemIndex, ItemText
    private String[][] mColorTheme = {
            {"#FF5C5B5B", "#E1626262", "#FF000000", "#00FFFFFF", "#AFFFFFFF", "#FF000000", "#FF000000"},
            {"#DABA236D", "#E1efb2d0", "#DA720038", "#FF9B6C83", "#AF00ced1", "#DAA10050", "#DA620131"},
            {"#FF99C400", "#E1B1D434", "#D63F5000", "#C979A135", "#AFFCD517", "#FF5C7500", "#D63F5000"},
            {"#FF8F6D8F", "#E18B468B", "#FF000000", "#FFFFA763", "#AFFE7D1A", "#FF000000", "#FF000000"},
            {"#FFCE6D1B", "#E1FF7500", "#F47B3E0B", "#E9E0C26F", "#AFfcd0ab", "#FF763702", "#FF763702"},
            {"#FF002D92", "#E10450FB", "#FF002965", "#EEFF6D6D", "#AFFF3535", "#FF002965", "#FF002965"}};

    // allow ColorPickerBlock to call this activity to make some color set
    private static class ColorPickerHandler extends Handler {
        private final WeakReference<PaperContentActivity> mActivity;

        ColorPickerHandler(PaperContentActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) return;

            mActivity.get().setColorTheme(msg.arg1);
            mActivity.get().mDialog.cancel();
        }
    }

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
        mRecyclerAdapter = new RecyclerContentListAdapter(mContentItemList, mColorTheme[mThemeIndex][4], mColorTheme[mThemeIndex][5], mColorTheme[mThemeIndex][6]);
        RecyclerView mRecyclerViewContentList = findViewById(R.id.recyclerContentList);
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
            // load theme index form SQLite
            mThemeIndex = mCursor.getInt(mCursor.getColumnIndex("theme_index"));
        }
        //set Color
        setColorTheme(mThemeIndex);
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
        cv.put("theme_index", mThemeIndex);
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
                mPaperProperty.setBackgroundColor(mColorTheme[mThemeIndex][1]);
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

    public void setupColorPicker(View view) {
        // create color blocks with parameter colorF and ColorB
        TableLayout tableLayout = new TableLayout(this);
        for (int i = 0; i < 3; i++) {
            TableRow tableRow = new TableRow(this);
            for (int j = 0; j < 2; j++) {
                ColorPickerBlock colorPickerBlock = new ColorPickerBlock(
                        this, mColorPickerHandler, mColorBackground[i][j], mColorForeground[i][j]);
                colorPickerBlock.setLayoutParams(new TableRow.LayoutParams(
                        400,
                        300));
                tableRow.addView(colorPickerBlock);
            }
            tableLayout.addView(tableRow);
        }
        mDialog = new Dialog(this);
        mDialog.setContentView(tableLayout);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setCancelable(true);
        mDialog.show();
    }

    private void setColorTheme(int index) {
        mThemeIndex = index;
        // set StatusBar's color
        getWindow().setStatusBarColor(Color.parseColor(mColorTheme[mThemeIndex][0]));

        // set background color
        findViewById(R.id.contentRootView).setBackgroundColor(Color.parseColor(mColorTheme[mThemeIndex][1]));

        // set Title's text color
        mEdtContentTitle.setTextColor(Color.parseColor(mColorTheme[mThemeIndex][2]));

        // set FloatingActionButtonChild's color
        ((FloatingActionButton) findViewById(R.id.btnAddItem))
                .setColorNormal(Color.parseColor(mColorTheme[mThemeIndex][3]));
        ((FloatingActionButton) findViewById(R.id.btnClear))
                .setColorNormal(Color.parseColor(mColorTheme[mThemeIndex][3]));
        ((FloatingActionButton) findViewById(R.id.btnChangeColor))
                .setColorNormal(Color.parseColor(mColorTheme[mThemeIndex][3]));

        // set Item's background, text, index color
        setupRecyclerView();
    }
}