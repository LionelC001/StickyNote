package com.lionel.stickynote.appwidget;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lionel.stickynote.R;
import com.lionel.stickynote.helper.PaperContentDbHelper;
import com.lionel.stickynote.preference.PreferencesUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.lionel.stickynote.PubConstant.COLOR_THEMES;
import static com.lionel.stickynote.helper.PaperContentDbHelper.DB_NAME;
import static com.lionel.stickynote.helper.PaperContentDbHelper.TABLE_NAME;

public class RemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private List<String> data = new ArrayList<>();
    private int themeIndex;

    public RemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        initDB();
    }

    private void initDB() {
        int selectedPaperId = PreferencesUtil.getAppWidgetPageId();
        String paperName = "Paper" + selectedPaperId;
        PaperContentDbHelper paperContentDbHelper = new PaperContentDbHelper(context, DB_NAME, null, 1);
        paperContentDbHelper.createTable();

        Cursor cursor = paperContentDbHelper.query(TABLE_NAME, null, "paper_name=?", new String[]{paperName}, null, null, null);
        if (cursor == null) return;

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            // load array list form SQLite
            String json = cursor.getString(cursor.getColumnIndex("item"));
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<String>>() {
                }.getType();
                data = gson.fromJson(json, type);
            }

            // load theme index form SQLite
            themeIndex = cursor.getInt(cursor.getColumnIndex("theme_index"));
        }
    }

    @Override
    public void onDestroy() {
        data.clear();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.item_app_widget_paper_content);
        String content = (position + 1) + ". " + data.get(position);
        remoteViews.setTextViewText(R.id.txtViewAppWidgetContent, content);
        remoteViews.setTextColor(R.id.txtViewAppWidgetContent, Color.parseColor(COLOR_THEMES[themeIndex][2]));
        remoteViews.setInt(R.id.rootViewItemAppWidgetPaperContent, "setBackgroundColor", Color.TRANSPARENT);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
