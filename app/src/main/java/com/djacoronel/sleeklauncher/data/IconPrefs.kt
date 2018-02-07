package com.djacoronel.sleeklauncher.data

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
* Created by djacoronel on 2/7/18.
*/

@Entity
class IconPrefs(
        @PrimaryKey(autoGenerate = true)
        var id: Long,
        var appName: String,
        var isHidden: Boolean,
        var label: String,
        var iconName: String
)