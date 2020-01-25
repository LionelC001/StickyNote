package com.lionel.stickynote.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

import com.lionel.stickynote.R;
import com.lionel.stickynote.activities.PaperContentActivity;
import com.lionel.stickynote.fieldclass.PaperProperty;
import com.lionel.stickynote.preference.PreferencesUtil;

import static com.lionel.stickynote.PubConstant.KEY_PAPER_ID;

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
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_app_widget);

            // set listView adapter on appWidget
            Intent intentRemoteViewsService = new Intent(context, AppWidgetRemoteViewsService.class);
            remoteViews.setRemoteAdapter(R.id.listViewAppWidget, intentRemoteViewsService);

            // set appWidget's attribute
            remoteViews.setTextViewText(R.id.txtViewTitleAppWidget, paperProperty.getTitle());
            remoteViews.setInt(R.id.rootViewAppWidget, "setBackgroundColor", Color.parseColor(paperProperty.getBackgroundColor()));

            // set intent to PaperContentActivity from appWidget
            Intent intentPaperContent = new Intent(context, PaperContentActivity.class);
            intentPaperContent.putExtra(KEY_PAPER_ID, paperProperty.getPaperId());
            PendingIntent pendingIntentPaperContent = PendingIntent.getActivity(context, 0, intentPaperContent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.rootViewAppWidget, pendingIntentPaperContent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.listViewAppWidget);
        }
    }
}
