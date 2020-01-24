package com.lionel.stickynote.appwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

import com.lionel.stickynote.R;
import com.lionel.stickynote.fieldclass.PaperProperty;
import com.lionel.stickynote.preference.PreferencesUtil;

public class AppWidgetProvider extends android.appwidget.AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        int appWidgetSelectedPage = PreferencesUtil.getAppWidgetPageId();
        for (int appWidgetId : appWidgetIds) {
            if (appWidgetSelectedPage != -1) {
                updateAppWidget(context, appWidgetManager, appWidgetId, appWidgetSelectedPage);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        PreferencesUtil.setAppWidgetPageId(-1);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int selectedPageId) {
        PreferencesUtil.setAppWidgetPageId(selectedPageId);

        PaperProperty paperProperty = PreferencesUtil.getPaperProperty(selectedPageId);
        if (paperProperty != null) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_app_widget);
            Intent intent = new Intent(context, AppWidgetRemoteViewsService.class);
            views.setRemoteAdapter(R.id.listViewAppWidget, intent);

            views.setTextViewText(R.id.txtViewTitleAppWidget, paperProperty.getTitle());
            views.setInt(R.id.rootViewAppWidget, "setBackgroundColor", Color.parseColor(paperProperty.getBackgroundColor()));

            appWidgetManager.updateAppWidget(appWidgetId, views);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewAppWidget);
        }
    }
}
