package com.djacoronel.sleeklauncher.data.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.djacoronel.sleeklauncher.data.IconPrefs

/**
* Created by djacoronel on 2/7/18.
*/

@Database(entities = [IconPrefs::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun iconPrefsDao(): IconPrefsDao

    companion object {
        const val DATABASE_NAME = "sleek-launcher-db"
    }
}