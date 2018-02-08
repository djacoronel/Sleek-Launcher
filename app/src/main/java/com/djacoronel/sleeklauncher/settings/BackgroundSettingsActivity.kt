package com.djacoronel.sleeklauncher.settings

import android.app.Activity
import android.app.WallpaperManager
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TabHost
import com.djacoronel.sleeklauncher.R
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_background_settings.*
import kotlinx.android.synthetic.main.blur_transparency_layout.*
import kotlinx.android.synthetic.main.color_picker_layout.*
import javax.inject.Inject

class BackgroundSettingsActivity : Activity() {

    @Inject
    lateinit var sharedPref: SharedPreferences

    private var argb = intArrayOf(0, 0, 0, 0)
    private var blur = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_background_settings)

        setupTabs()
        setupApplyButton()

        setupBackgroundPreview()
        setupArgbSeekBar(transparencyBar, 0)
        setupArgbSeekBar(redBar, 1)
        setupArgbSeekBar(greenBar, 2)
        setupArgbSeekBar(blueBar, 3)
        setupBlurSeekBar(blurBar)

        loadCurrentPrefs()
    }

    private fun setupTabs() {
        tabHost.setup()

        //Tab 1
        var spec: TabHost.TabSpec = tabHost.newTabSpec("Tab One")
        spec.setContent(R.id.tab1)
        spec.setIndicator("Color")
        tabHost.addTab(spec)

        //Tab 2
        spec = tabHost.newTabSpec("Tab Two")
        spec.setContent(R.id.tab2)
        spec.setIndicator("Blur")
        tabHost.addTab(spec)
    }

    private fun setupApplyButton() {
        apply.setOnClickListener {
            savePref()
            finish()
        }
    }

    private fun setupBackgroundPreview() {
        preview.scaleType = ImageView.ScaleType.CENTER_CROP
        val wallpaperDrawable = WallpaperManager.getInstance(this).drawable
        val wallpaperBitmap = (wallpaperDrawable as BitmapDrawable).bitmap
        preview.setImageDrawable(BitmapDrawable(resources, wallpaperBitmap))
    }

    private fun setupArgbSeekBar(seekBar: SeekBar, argbIndex: Int) {
        seekBar.max = 255
        seekBar.progress = 1
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                argb[argbIndex] = progress
                preview.setColorFilter(Color.argb(argb[0], argb[1], argb[2], argb[3]))
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun setupBlurSeekBar(blurBar: SeekBar) {
        blurBar.max = 50
        blurBar.progress = 1
        blurBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                applyBlur(progress)
                blur = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun applyBlur(progress: Int) {
        val wallpaperDrawable = WallpaperManager.getInstance(this).drawable
        var blurredBitmap = (wallpaperDrawable as BitmapDrawable).bitmap

        val numberOfFullBlur = progress / 25
        val remainingBlur = progress % 25

        for (i in 0 until numberOfFullBlur)
            blurredBitmap = BlurBuilder().blur(this, blurredBitmap, 25f)

        if (remainingBlur != 0)
            blurredBitmap = BlurBuilder().blur(this, blurredBitmap, remainingBlur.toFloat())

        preview.setImageDrawable(BitmapDrawable(resources, blurredBitmap))
    }

    private fun loadCurrentPrefs() {
        val storedBgPref = sharedPref.getString("bgPref", "0,0,0,0,0")
        val splitStoredBgPref = storedBgPref.split(",")

        transparencyBar.progress = splitStoredBgPref[0].toInt()
        redBar.progress = splitStoredBgPref[1].toInt()
        greenBar.progress = splitStoredBgPref[2].toInt()
        blueBar.progress = splitStoredBgPref[3].toInt()
        blurBar.progress = splitStoredBgPref[4].toInt()
    }

    private fun savePref() {
        val editor = sharedPref.edit()
        val bgPref = "${argb[0]},${argb[1]},${argb[2]},${argb[3]},$blur"
        editor.putString("bgPref", bgPref)
        editor.apply()
    }
}