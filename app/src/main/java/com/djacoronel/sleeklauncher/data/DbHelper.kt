package com.djacoronel.sleeklauncher.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*
import java.util.*

class DbHelper(context: Context) : ManagedSQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_HIDDEN)
        db.execSQL(SQL_CREATE_CUSTOM)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_HIDDEN)
        db.execSQL(SQL_DELETE_CUSTOM)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }


    val hiddenList: ArrayList<String>
        get() {
            val db = writableDatabase
            val cursor = db.rawQuery("select * from " + TABLE_NAME, null)

            val list = ArrayList<String>()
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast) {
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LABEL))

                    list.add(name)
                    cursor.moveToNext()
                }
            }
            cursor.close()
            return list
        }

    fun addToHidden(label: String): Long {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME_LABEL, label)

        return db.insert(TABLE_NAME, null, values)
    }

    fun removeFromHidden(label: String): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COLUMN_NAME_LABEL='$label'", null) > 0
    }

    fun getCustom(label: String): Array<String> {
        val db = writableDatabase
        val cursor = db.rawQuery("select * from $TABLE_NAME_CUSTOM where label='$label'", null)
        val customInfo = arrayOf("","")
        if (cursor.moveToFirst()) {
            customInfo[0] = cursor.getString(cursor.getColumnIndex("icon"))
            customInfo[1] = cursor.getString(cursor.getColumnIndex("customlabel"))
        }

        cursor.close()
        return customInfo
    }

    fun removeAllFromCustom() {
        val db = writableDatabase
        db.execSQL(SQL_DELETE_CUSTOM)
        db.execSQL(SQL_CREATE_CUSTOM)
    }

    fun addToCustom(label: String, customicon: String, customlabel: String): Long {
        val db = writableDatabase
        db.delete(TABLE_NAME_CUSTOM, "$COLUMN_NAME_APPLABEL='$label'", null)
        val values = ContentValues()
        values.put(COLUMN_NAME_APPLABEL, label)
        values.put(COLUMN_NAME_CUSTOMLABEL, customlabel)
        values.put(COLUMN_NAME_CUSTOMICON, customicon)

        return db.insert(TABLE_NAME_CUSTOM, null, values)
    }

    fun removeFromCustom(label: String): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_NAME_CUSTOM, "$COLUMN_NAME_APPLABEL='$label'", null) > 0
    }

    companion object {

        private val DATABASE_VERSION = 2
        private val DATABASE_NAME = "basiclauncher.db"

        private val TABLE_NAME = "hiddenapps"
        private val ID = "_id"
        private val COLUMN_NAME_LABEL = "label"

        private val SQL_CREATE_HIDDEN = "CREATE TABLE " + TABLE_NAME + " (" +
                ID + " INTEGER PRIMARY KEY autoincrement," +
                COLUMN_NAME_LABEL + " TEXT)"

        private val SQL_DELETE_HIDDEN = "DROP TABLE IF EXISTS " + TABLE_NAME

        private val TABLE_NAME_CUSTOM = "customapp"
        private val ID_CUSTOM = "_id"
        private val COLUMN_NAME_APPLABEL = "label"
        private val COLUMN_NAME_CUSTOMLABEL = "customlabel"
        private val COLUMN_NAME_CUSTOMICON = "icon"

        private val SQL_CREATE_CUSTOM = "CREATE TABLE " + TABLE_NAME_CUSTOM + " (" +
                ID_CUSTOM + " INTEGER PRIMARY KEY autoincrement," +
                COLUMN_NAME_APPLABEL + " TEXT," +
                COLUMN_NAME_CUSTOMLABEL + " TEXT," +
                COLUMN_NAME_CUSTOMICON + " TEXT)"

        private val SQL_DELETE_CUSTOM = "DROP TABLE IF EXISTS " + TABLE_NAME_CUSTOM
    }
}
