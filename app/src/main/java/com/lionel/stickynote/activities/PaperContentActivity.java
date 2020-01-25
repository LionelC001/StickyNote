package com.lionel.stickynote.activities;

import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lionel.stickynote.MyApplication;
import com.lionel.stickynote.R;
import com.lionel.stickynote.adapter.RecyclerContentListAdapter;
import com.lionel.stickynote.appwidget.AppWidgetProvider;
import com.lionel.stickynote.fieldclass.PaperProperty;
import com.lionel.stickynote.helper.PaperContentDbHelper;
import com.lionel.stickynote.helper.SimpleItemTouchHelper;
import com.lionel.stickynote.preference.PreferencesUtil;
import com.lionel.stickynote.views.ColorPickerBlock;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.lionel.stickynote.PubConstant.COLOR_THEMES;
import static com.lionel.stickynote.PubConstant.KEY_PAPER_ID;
import static com.lionel.stickynote.helper.PaperContentDbHelper.DB_NAME;
import static com.lionel.stickynote.helper.PaperContentDbHelper.TABLE_NAME;

// this class is responsible for notepaper's content, edit items and change content's theme
public class PaperContentActivity extends AppCompatActivity {

    private int mThemeIndex;

    private List<String> mContentItemList = new ArrayList<>();
    private EditText mEdtContentTitle;
    private RecyclerContentListAdapter mRecyclerAdapter;
    private String paper_name;
    private Cursor mCursor;
    private PaperContentDbHelper mPaperContentDbHelper;
    private ColorPickerHandler mColorPickerHandler = new ColorPickerHandler(this);
    private Dialog mDialog;
    private String[][] mColorBackground = {{"#AF626262", "#AFefb2d0"}, {"#D6B1D434", "#E08B468B"}, {"#AFff7500", "#DC0450FB"}};
    private String[][] mColorForeground = {{"#AFFFFFFF", "#AF00ced1"}, {"#AFFCD517", "#AFFE7D1A"}, {"#AFfcd0ab", "#AFFF3535"}};

    private int selectedPaperId;

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
        selectedPaperId = getIntent().getIntExtra(KEY_PAPER_ID, 0);
        mEdtContentTitle = findViewById(R.id.edtContentTitle);

        setupRecyclerView();
        setupDB();
        setupDataInfo();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        selectedPaperId = intent.getIntExtra(KEY_PAPER_ID, 0);

        setupDataInfo();
    }

    private void setupRecyclerView() {
        mRecyclerAdapter = new RecyclerContentListAdapter();
        RecyclerView mRecyclerViewContentList = findViewById(R.id.recyclerContentList);
        mRecyclerViewContentList.setAdapter(mRecyclerAdapter);
        mRecyclerViewContentList.setLayoutManager(new LinearLayoutManager(this));

        // let list item can swipe and drag
        SimpleItemTouchHelper simpleItemTouchHelper = new SimpleItemTouchHelper(mRecyclerAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(mRecyclerViewContentList);
    }

    private void setupDB() {
        mPaperContentDbHelper = new PaperContentDbHelper(getApplicationContext(), DB_NAME, null, 1);
        mPaperContentDbHelper.createTable();
    }

    private void setupDataInfo() {
        paper_name = "Paper" + selectedPaperId;
        mCursor = mPaperContentDbHelper.query(TABLE_NAME, null, "paper_name=?", new String[]{paper_name}, null, null, null);
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
            mEdtContentTitle.setText(mCursor.getString(mCursor.getColumnIndex("title")));
            // load theme index form SQLite
            mThemeIndex = mCursor.getInt(mCursor.getColumnIndex("theme_index"));
        }
        //set Color
        setColorTheme(mThemeIndex);
    }

    private void setColorTheme(int index) {
        mThemeIndex = index;
        // set StatusBar's color
        getWindow().setStatusBarColor(Color.parseColor(COLOR_THEMES[mThemeIndex][0]));

        // set background color
        findViewById(R.id.contentRootView).setBackgroundColor(Color.parseColor(COLOR_THEMES[mThemeIndex][1]));

        // set Title's text color
        mEdtContentTitle.setTextColor(Color.parseColor(COLOR_THEMES[mThemeIndex][2]));

        // set FloatingActionButtonChild's color
        ((FloatingActionButton) findViewById(R.id.btnAddItem))
                .setColorNormal(Color.parseColor(COLOR_THEMES[mThemeIndex][3]));
        ((FloatingActionButton) findViewById(R.id.btnClear))
                .setColorNormal(Color.parseColor(COLOR_THEMES[mThemeIndex][3]));
        ((FloatingActionButton) findViewById(R.id.btnChangeColor))
                .setColorNormal(Color.parseColor(COLOR_THEMES[mThemeIndex][3]));
        ((FloatingActionButton) findViewById(R.id.btnShare))
                .setColorNormal(Color.parseColor(COLOR_THEMES[mThemeIndex][3]));
        ((FloatingActionButton) findViewById(R.id.btnAppWidget))
                .setColorNormal(Color.parseColor(COLOR_THEMES[mThemeIndex][3]));

        // set Item's background, text, index color
        mRecyclerAdapter.setData(mContentItemList, COLOR_THEMES[mThemeIndex][4], COLOR_THEMES[mThemeIndex][5], COLOR_THEMES[mThemeIndex][6]);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveContentToSQLite();
        savePropertyToSP();
    }

    @Override
    protected void onStop() {
        super.onStop();
        updateAppWidget();
    }

    private void updateAppWidget() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(MyApplication.getContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(MyApplication.getContext(), AppWidgetProvider.class));
        Intent intent = new Intent(this, AppWidgetProvider.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(intent);
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
        cv.put("paper_name", paper_name);
        cv.put("title", mEdtContentTitle.getText().toString());
        cv.put("item", json);
        cv.put("theme_index", mThemeIndex);
        // check whether it should insert or not
        if (mCursor.getCount() == 0) mPaperContentDbHelper.insert(TABLE_NAME, null, cv);
        else mPaperContentDbHelper.update(TABLE_NAME, cv, "paper_name=?", new String[]{paper_name});
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
        List<PaperProperty> listPaperProperty = PreferencesUtil.getListPaperProperty();
        PaperProperty selectPaper = PreferencesUtil.getPaperProperty(selectedPaperId);
        selectPaper.setTitle(mEdtContentTitle.getText().toString());
        selectPaper.setContent(contentTop4);
        selectPaper.setBackgroundColor(COLOR_THEMES[mThemeIndex][1]);

        for (PaperProperty pp : listPaperProperty) {
            if (pp.getPaperId() == selectedPaperId) {
                int ppIndex = listPaperProperty.indexOf(pp);
                listPaperProperty.set(ppIndex, selectPaper);
                break;
            }
        }
        // save this PropertyArrayList into PropertySharedPreferences
        SharedPreferences.Editor sharedPreferences =
                getSharedPreferences("MainData", MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(listPaperProperty);
        sharedPreferences.putString("PaperProperty", json);
        sharedPreferences.apply();
    }


    public void addItem(View view) {
        mContentItemList.add("");
        mRecyclerAdapter.notifyDataSetChanged();
    }

    public void onClear(View view) {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure for onClear all items?")
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

    public void onShare(View view) {
        int j = 0;
        StringBuilder strList = new StringBuilder(
                "Hi, I want to do these things with you. Let's go!\n\n");
        for (int i = 0; i < mContentItemList.size(); i++) {
            if (!mContentItemList.get(i).equals("")) {
                strList.append(j + 1).append(". ").append(mContentItemList.get(i)).append("\n");
                j++;
            }
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, strList.toString());
        startActivity(Intent.createChooser(intent, "Share To-Do list"));
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

    public void setOnAppWidget(View view) {
        PreferencesUtil.setAppWidgetPageId(selectedPaperId);
        Toast.makeText(this, "Already Changed", Toast.LENGTH_SHORT).show();
        updateAppWidget();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}