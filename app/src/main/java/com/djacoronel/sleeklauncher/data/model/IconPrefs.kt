package com.djacoronel.sleeklauncher.data.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
* Created by Daniel Coronel (djacoronel) on 2/7/18.
*/

@Entity
class IconPrefs(var appName: String, var activity:String) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var isHidden: Boolean = false
    var label: String = NO_CUSTOM_LABEL
    var iconName: String = NO_CUSTOM_ICON

    companion object {
        const val NO_CUSTOM_ICON = "NO_CUSTOM_ICON"
        const val NO_CUSTOM_LABEL = "NO_CUSTOM_LABEL"
    }
}