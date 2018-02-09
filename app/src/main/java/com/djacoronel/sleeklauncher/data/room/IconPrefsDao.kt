package com.djacoronel.sleeklauncher.data.room

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import com.djacoronel.sleeklauncher.data.model.IconPrefs

/**
* Created by djacoronel on 2/7/18.
*/

@Dao
interface IconPrefsDao {
    @Query("SELECT * FROM iconPrefs")
    fun getAllIconPrefs(): List<IconPrefs>

    @Query("SELECT * FROM iconPrefs WHERE activity = :activity")
    fun getIconPrefs(activity: String): IconPrefs?

    @Insert(onConflict = REPLACE)
    fun addIconPrefs(iconPrefs: IconPrefs)

    @Update(onConflict = REPLACE)
    fun updateIconPrefs(iconPrefs: IconPrefs)

    @Delete
    fun deleteIconPrefs(iconPrefs: IconPrefs)

    @Query("DELETE FROM iconPrefs")
    fun resetIconPrefs()
}