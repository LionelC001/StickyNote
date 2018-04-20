package com.lionel.stickynote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PaperContentDbHelper extends SQLiteOpenHelper {

    private String mTableName;
    private SQLiteDatabase mPaperContentDb;

    PaperContentDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, String tableName) {
        super(context, name, factory, version);
        mPaperContentDb = getWritableDatabase();
        mTableName = tableName;

        /*Cursor c = mPaperContentDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            StringBuilder str = new StringBuilder("tableList: \n");
            while ( !c.isAfterLast() ) {
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
                        "tbl_name = '" + mTableName + "'"
                , null);
        if (c != null) {
            if (c.getCount() == 0) {
                mPaperContentDb.execSQL("CREATE TABLE " + mTableName + " (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "item TEXT);");
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

    public void deleteTable() {
        mPaperContentDb.execSQL("DROP TABLE IF EXISTS " + mTableName);
    }

    @Override
    public synchronized void close() {
        super.close();
        mPaperContentDb.close();
    }


}
