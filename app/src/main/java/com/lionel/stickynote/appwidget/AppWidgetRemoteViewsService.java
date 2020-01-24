package com.lionel.stickynote.appwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.lionel.stickynote.MyApplication;

public class AppWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new com.lionel.stickynote.appwidget.RemoteViewsFactory(MyApplication.getContext());
    }
}