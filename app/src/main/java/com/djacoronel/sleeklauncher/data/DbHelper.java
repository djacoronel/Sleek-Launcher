package com.djacoronel.sleeklauncher.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "basiclauncher.db";

    private static final String TABLE_NAME = "hiddenapps";
    private static final String ID = "_id";
    private static final String COLUMN_NAME_LABEL = "label";

    private static final String SQL_CREATE_HIDDEN =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ID + " INTEGER PRIMARY KEY autoincrement," +
                    COLUMN_NAME_LABEL + " TEXT)";

    private static final String SQL_DELETE_HIDDEN =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String TABLE_NAME_CUSTOM = "customapp";
    private static final String ID_CUSTOM = "_id";
    private static final String COLUMN_NAME_APPLABEL = "label";
    private static final String COLUMN_NAME_CUSTOMLABEL = "customlabel";
    private static final String COLUMN_NAME_CUSTOMICON = "icon";

    private static final String SQL_CREATE_CUSTOM =
            "CREATE TABLE " + TABLE_NAME_CUSTOM + " (" +
                    ID_CUSTOM + " INTEGER PRIMARY KEY autoincrement," +
                    COLUMN_NAME_APPLABEL + " TEXT," +
                    COLUMN_NAME_CUSTOMLABEL + " TEXT," +
                    COLUMN_NAME_CUSTOMICON + " TEXT)";

    private static final String SQL_DELETE_CUSTOM =
            "DROP TABLE IF EXISTS " + TABLE_NAME_CUSTOM;


    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_HIDDEN);
        db.execSQL(SQL_CREATE_CUSTOM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_HIDDEN);
        db.execSQL(SQL_DELETE_CUSTOM);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public ArrayList<String> getHiddenList() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);

        ArrayList<String> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LABEL));

                list.add(name);
                cursor.moveToNext();
            }
        }
        cursor.close();
        return list;
    }

    public long addToHidden(String label) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_LABEL, label);

        return db.insert(TABLE_NAME, null, values);
    }

    public boolean removeFromHidden(String label) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_NAME_LABEL + "='" + label + "'", null) > 0;
    }

    public String[] getCustom(String label) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME_CUSTOM + " where label='" + label + "'", null);
        String customInfo[] = new String[2];
        if (cursor.moveToFirst()) {
            customInfo[0] = cursor.getString(cursor.getColumnIndex("icon"));
            customInfo[1] = cursor.getString(cursor.getColumnIndex("customlabel"));
        }

        cursor.close();
        return customInfo;
    }

    public void removeAllFromCustom() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_CUSTOM);
        db.execSQL(SQL_CREATE_CUSTOM);
    }

    public long addToCustom(String label, String customicon, String customlabel) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME_CUSTOM, COLUMN_NAME_APPLABEL + "='" + label + "'", null);
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_APPLABEL, label);
        values.put(COLUMN_NAME_CUSTOMLABEL, customlabel);
        values.put(COLUMN_NAME_CUSTOMICON, customicon);

        return db.insert(TABLE_NAME_CUSTOM, null, values);
    }

    public boolean removeFromCustom(String label) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_NAME_CUSTOM, COLUMN_NAME_APPLABEL + "='" + label + "'", null) > 0;
    }
}
