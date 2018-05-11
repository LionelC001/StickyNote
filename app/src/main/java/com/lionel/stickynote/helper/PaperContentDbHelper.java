package com.lionel.stickynote.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PaperContentDbHelper extends SQLiteOpenHelper {

    public final static String DB_NAME = "PaperContent.db";
    public final static String TABLE_NAME = "Papers";
    private SQLiteDatabase mPaperContentDb;


    public PaperContentDbHelper(Context context, String dbName, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dbName, factory, version);
        mPaperContentDb = getWritableDatabase();

        /*Cursor c = mPaperContentDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst()) {
            StringBuilder str = new StringBuilder("tableList: \n");
            while (!c.isAfterLast()) {
                str.append(c.getString(0)).append("\n");
                c.moveToNext();
            }
            Log.d("<<<", str.toString());
        }*/
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void createTable() {
        //create table if it is null
        Cursor c = mPaperContentDb.rawQuery(
                "SELECT DISTINCT tbl_name FROM sqlite_master WHERE " +
                        "tbl_name = '" + TABLE_NAME + "'"
                , null);
        if (c != null) {
            if (c.getCount() == 0) {
                mPaperContentDb.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "paper_name TEXT," +
                        "title TEXT," +
                        "item TEXT," +
                        "theme_index INTEGER DEFAULT 0);");
            }
            c.close();
        }
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        return mPaperContentDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
    }


    public void insert(String table, String nullColumnHack, ContentValues values) {
        mPaperContentDb.insert(table, nullColumnHack, values);
    }

    public void update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        mPaperContentDb.update(table, values, whereClause, whereArgs);
    }

    public void deletePaper(String paperName) {
        Cursor c = mPaperContentDb.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+TABLE_NAME+"'", null);
        if(c!=null) {
            if(c.getCount()>0) {
                mPaperContentDb.delete(TABLE_NAME, "paper_name=?", new String[]{paperName});
            }
            c.close();
        }
    }

    public void deleteALLPaper() {
        mPaperContentDb.delete(TABLE_NAME, null, null);
    }

    @Override
    public synchronized void close() {
        super.close();
        mPaperContentDb.close();
    }


}
