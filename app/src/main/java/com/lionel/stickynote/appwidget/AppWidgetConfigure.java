package com.lionel.stickynote.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.lionel.stickynote.R;
import com.lionel.stickynote.adapter.AppWidgetSelectPaperAdapter;
import com.lionel.stickynote.fieldclass.PaperProperty;
import com.lionel.stickynote.preference.PreferencesUtil;

import java.util.List;

public class AppWidgetConfigure extends AppCompatActivity {

    private int appWidgetId;
    private AppWidgetSelectPaperAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);

        setContentView(R.layout.layout_app_widget_configure);

        initWidgetInfo();
        initToolbar();
        initRecyclerView();
        initBtnConfirm();
    }

    private void initWidgetInfo() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new AppWidgetSelectPaperAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        List<PaperProperty> listPaperProperty = PreferencesUtil.getListPaperProperty();
        adapter.setData(listPaperProperty);
    }


    private void initBtnConfirm() {
        Button btnConfirm = findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(view -> onUpdateAppWidget(adapter.getSelectedPosition()));
    }

    private void onUpdateAppWidget(int selectedPosition) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        AppWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId, selectedPosition);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
