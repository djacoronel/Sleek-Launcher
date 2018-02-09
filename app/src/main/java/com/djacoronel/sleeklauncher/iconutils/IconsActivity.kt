package com.djacoronel.sleeklauncher.iconutils

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import com.djacoronel.sleeklauncher.R
import kotlinx.android.synthetic.main.icon_grid.*
import org.jetbrains.anko.toast

class IconsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icons)
        title = intent.getStringExtra("iconPack")
        loadIconsGrid()
    }

    private fun getIconPackPackageName(iconPackName: String): String {
        val icManager = IconPackManager(this)
        val iconPacks = icManager.availableIconPacks

        return iconPacks[iconPackName]!!
    }

    private fun loadIconsGrid() {
        val icManager = IconPackManager(this)
        val icPackageName = getIconPackPackageName(intent.getStringExtra("iconPack"))
        val icons = icManager.getAllIcons(icPackageName)

        val adapter = IconPickerAdapter(icons, icPackageName, this)
        icon_grid.layoutManager = GridLayoutManager(this, 4)
        icon_grid.adapter = adapter
    }

    fun setIconAsResult(customIcon: String) {
        val packageName = getIconPackPackageName(intent.getStringExtra("iconPack"))
        val returnIntent = Intent()
        returnIntent.putExtra("customIcon", customIcon + "/" + packageName)
        returnIntent.putExtra("activityName", intent.getStringExtra("activityName"))
        toast(customIcon)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}
