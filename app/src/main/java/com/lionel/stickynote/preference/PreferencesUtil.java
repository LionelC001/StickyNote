package com.lionel.stickynote.preference;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lionel.stickynote.MyApplication;
import com.lionel.stickynote.fieldclass.PaperProperty;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public final class PreferencesUtil {
    public static final String KEY_APP_WIDGET_PAGE_ID = "app_widget_page_id";
    private static final String KEY_LIST_PAPER_PROPERTY = "PaperProperty";


    private static SharedPreferences sharedPreferences;

    private static SharedPreferences getInstance() {
        if (sharedPreferences == null) {
            synchronized (PreferencesUtil.class) {
                if (sharedPreferences == null) {
                    sharedPreferences = MyApplication.getContext().getSharedPreferences("MainData", MODE_PRIVATE);
                }
            }
        }
        return sharedPreferences;
    }

    public static void setAppWidgetPageId(int selectedPageId) {
        getInstance().edit().putInt(KEY_APP_WIDGET_PAGE_ID, selectedPageId).apply();
    }

    public static int getAppWidgetPageId() {
        return getInstance().getInt(KEY_APP_WIDGET_PAGE_ID, -1);
    }

    public static List<PaperProperty> getListPaperProperty() {
        String json = sharedPreferences.getString(KEY_LIST_PAPER_PROPERTY, null);
        if (json != null) {
            Type type = new TypeToken<List<PaperProperty>>() {
            }.getType();
            return new Gson().fromJson(json, type);
        } else {
            return new ArrayList<>();
        }
    }

    public static PaperProperty getPaperProperty(int pageId) {
        List<PaperProperty> listPaperProperty = getListPaperProperty();
        for(PaperProperty paperProperty: listPaperProperty) {
            if(paperProperty.getPaperId() == pageId) {
                return paperProperty;
            }
        }

        return null;
    }
}
