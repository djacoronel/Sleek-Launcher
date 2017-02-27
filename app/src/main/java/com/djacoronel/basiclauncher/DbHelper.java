package com.djacoronel.basiclauncher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "basiclauncher.db";

    private static final String TABLE_NAME = "hiddenapps";
    private static final String ID = "_id";
    private static final String COLUMN_NAME_LABEL = "label";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_LABEL + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String TABLE_NAME_TASKS = "tasks";
    private static final String ID_TASKS = "_id";
    private static final String COLUMN_NAME_TNAME = "name";
    private static final String COLUMN_NAME_TDURATION = "duration";
    private static final String COLUMN_NAME_TSTATUS = "status";

    private static final String SQL_CREATE_TASKS =
            "CREATE TABLE " + TABLE_NAME_TASKS + " (" +
                    ID_TASKS + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_TNAME + " TEXT," +
                    COLUMN_NAME_TDURATION + " TEXT," +
                    COLUMN_NAME_TSTATUS + " TEXT)";

    private static final String SQL_DELETE_TASKS =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_TASKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_TASKS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    ArrayList<Task> getTasks(Context context) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME_TASKS, null);

        ArrayList<Task> tasks = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                tasks.add(new Task(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TNAME)),
                                cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TDURATION)),
                                cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TSTATUS)),
                                context));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return tasks;
    }

    long addTask(Task task){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_TNAME, task.getName());
        values.put(COLUMN_NAME_TDURATION, task.getDuration());
        values.put(COLUMN_NAME_TSTATUS, task.getStatus());
        return db.insert(TABLE_NAME_TASKS, null, values);
    }

    void updateTask(Task task){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_TNAME, task.getName());
        values.put(COLUMN_NAME_TDURATION, task.getDuration());
        values.put(COLUMN_NAME_TSTATUS, task.getStatus());


        String selection = ID+ " LIKE ?";
        String[] selectionArgs = { "" + task.getId() };

        int count = db.update(
                TABLE_NAME_TASKS,
                values,
                selection,
                selectionArgs);
    }

    void deleteTask(long id){
        SQLiteDatabase db = getWritableDatabase();
        String selection = ID + " LIKE ?";
        String[] selectionArgs = {""+id};
        db.delete(TABLE_NAME_TASKS, selection, selectionArgs);
    }

    ArrayList<String> getHiddenList() {
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

    long addToHidden(String label) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_LABEL, label);

        return db.insert(TABLE_NAME, null, values);
    }

    void removeFromHidden(String label) {
        SQLiteDatabase db = getWritableDatabase();
        String selection = COLUMN_NAME_LABEL + " LIKE ?";
        String[] selectionArgs = {label};
        db.delete(TABLE_NAME, selection, selectionArgs);
    }
}
