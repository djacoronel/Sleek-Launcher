package com.djacoronel.sleeklauncher.home

import android.app.Activity
import android.app.ActivityOptions
import android.app.WallpaperManager
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.djacoronel.sleeklauncher.R
import com.djacoronel.sleeklauncher.data.model.AppDetail
import com.djacoronel.sleeklauncher.data.model.IconPrefs
import com.djacoronel.sleeklauncher.data.room.IconPrefsDao
import com.djacoronel.sleeklauncher.iconutils.IconPackManager
import com.djacoronel.sleeklauncher.iconutils.IconsActivity
import com.djacoronel.sleeklauncher.settings.BlurBuilder
import com.djacoronel.sleeklauncher.settings.SettingsActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_grid.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.selector
import org.jetbrains.anko.toast
import java.util.*
import javax.inject.Inject

class MainActivity : Activity() {
    @Inject
    lateinit var iconPrefsDao: IconPrefsDao
    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var adapter: GridAdapter
    private lateinit var dialogIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_main)

        loadBackground()
        setupBackgroundRefreshing()
        loadApps()
        setupGridRefreshing()

        // This makes status bar and navigation bar transparent
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    private fun loadBackground() {
        val bgPref = preferences.getString("bgPref", "0,0,0,0,0")
        val argbBlur = bgPref.split(",")
        val argb = intArrayOf(argbBlur[0].toInt(), argbBlur[1].toInt(), argbBlur[2].toInt(), argbBlur[3].toInt())
        val blur = argbBlur[4].toInt()

        if (blur == 0) loadBackgroundWithColorFilter(argb)
        else loadBackgroundWithBlur(argb, blur)
    }

    private fun loadBackgroundWithColorFilter(argb: IntArray) {
        mainBackground.visibility = View.GONE
        window.decorView.setBackgroundColor(Color.argb(argb[0], argb[1], argb[2], argb[3]))
    }

    private fun loadBackgroundWithBlur(argb: IntArray, blur: Int) {
        val wallpaperDrawable = WallpaperManager.getInstance(this).drawable
        var blurredBitmap = cropWallpaper((wallpaperDrawable as BitmapDrawable).bitmap)

        val numberOfFullBlur = blur / 25
        val remainingBlur = blur % 25

        for (i in 0 until numberOfFullBlur) {
            blurredBitmap = BlurBuilder().blur(this, blurredBitmap, 25f)
        }

        if (remainingBlur != 0) {
            blurredBitmap = BlurBuilder().blur(this, blurredBitmap, remainingBlur.toFloat())
        }

        mainBackground.visibility = View.VISIBLE
        mainBackground.setColorFilter(Color.argb(argb[0], argb[1], argb[2], argb[3]))
        mainBackground.setImageDrawable(BitmapDrawable(resources, blurredBitmap))
        mainBackground.scaleType = ImageView.ScaleType.FIT_CENTER
    }

    private fun cropWallpaper(wallpaper: Bitmap): Bitmap {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        return Bitmap.createBitmap(wallpaper, 0, 0, width, height)
    }

    private fun setupBackgroundRefreshing() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_WALLPAPER_CHANGED)

        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                loadBackground()
            }
        }

        registerReceiver(br, intentFilter)
    }

    private fun loadApps() {
        val manager = packageManager
        val apps = mutableListOf<AppDetail>()
        val showHidden = preferences.getBoolean("showHidden", false)
        val availableActivities = getInstalledAppsInfo()

        for (ri in availableActivities) {
            val name = ri.activityInfo.packageName
            var label = ri.loadLabel(manager) as String

            val iconPrefs = iconPrefsDao.getIconPrefs(name)
            val icon = IconPackManager(this).getAppIcon(name)!!

            iconPrefs?.let {
                if (it.label != IconPrefs.NO_CUSTOM_LABEL) label = it.label
                if (!it.isHidden || showHidden) apps.add(AppDetail(label, name, icon))
            }
            if (iconPrefs == null) {
                val app = AppDetail(label, name, icon)
                apps.add(app)
            }
        }

        Collections.sort(apps) { a1, a2 -> a1.label.compareTo(a2.label, true) }
        loadAppGrid(apps)
    }

    private fun getInstalledAppsInfo(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
    }

    private fun loadAppGrid(apps: List<AppDetail>) {
        adapter = GridAdapter(apps, this)
        app_grid.layoutManager = GridLayoutManager(this, 4)
        app_grid.adapter = adapter
    }

    private fun setupGridRefreshing() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addDataScheme("package")
        val br = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                loadApps()
            }
        }
        this.registerReceiver(br, intentFilter)
    }

    fun launchApp(app: AppDetail, v: View) {
        val manager = packageManager
        val intent = manager.getLaunchIntentForPackage(app.name)
        val optsBundle = getMarshmallowOpeningAnimationOpts(v).toBundle()

        if (intent != null) {
            startActivity(intent, optsBundle)
        } else {
            toast("App not found! :(")
            adapter.removeApp(app.name)
        }
    }

    private fun getMarshmallowOpeningAnimationOpts(v: View): ActivityOptions {
        val left = 0
        val top = 0
        val width = v.measuredWidth
        val height = v.measuredHeight

        return if (Build.VERSION.SDK_INT >= 23) {
            // Use reveal animation if Marshmallow
            ActivityOptions.makeClipRevealAnimation(v, left, top, width, height)
        } else {
            // Use a scale up animation for lower versions
            ActivityOptions.makeScaleUpAnimation(v, left, top, width, height)
        }
    }

    fun iconLongClick(app: AppDetail) {
        dialogIcon = ImageView(this)
        val bitmap = (app.icon as BitmapDrawable).bitmap
        val icon = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 200, 200, true))
        dialogIcon.setImageDrawable(icon)
        dialogIcon.setOnClickListener { showIconPackList(app) }

        alert {
            title = "Options"
            message = "Tap icon to change it."
            isCancelable = true
            customView = dialogIcon
            negativeButton("Uninstall") {
                val packageUri = Uri.parse("package:" + app.name)
                val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
                startActivity(uninstallIntent)
            }

            val iconPrefs = iconPrefsDao.getIconPrefs(app.name)
            if (iconPrefs == null)
                positiveButton("Hide") { hideIcon(IconPrefs(app.name)) }
            else {
                if (!iconPrefs.isHidden) positiveButton("Hide") { hideIcon(iconPrefs) }
                else positiveButton("Show") { unHideIcon(iconPrefs) }
            }
        }.show()
    }

    private fun hideIcon(iconPrefs: IconPrefs) {
        iconPrefs.isHidden = true
        iconPrefsDao.addIconPrefs(iconPrefs)
        toast("App marked as hidden!")

        val showHidden = preferences.getBoolean("showHidden", false)
        if (!showHidden) adapter.removeApp(iconPrefs.appName)
    }

    private fun unHideIcon(iconPrefs: IconPrefs) {
        iconPrefs.isHidden = false
        iconPrefsDao.addIconPrefs(iconPrefs)
        toast("App removed from hidden!")
    }

    private fun showIconPackList(app: AppDetail) {
        val iconPacks = IconPackManager(this).availableIconPacks
        val iconPackNames = iconPacks.keys.toList()

        selector("Icon Packs", iconPackNames, { _, index ->
            if (iconPackNames[index] == "Default") {
                setDefaultIcon(app)
            } else {
                val selectedIconPack = iconPackNames[index]
                launchIconPickerGridForSelectedPack(app.name, selectedIconPack)
            }
        })
    }

    private fun setDefaultIcon(app: AppDetail) {
        val iconPrefs = iconPrefsDao.getIconPrefs(app.name)
        iconPrefs?.let { iconPrefsDao.deleteIconPrefs(it) }

        try {
            val icon = packageManager.getApplicationIcon(app.name)
            setIcon(app.name, icon)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun setIcon(appName: String, icon: Drawable){
        val app = adapter.getApp(appName)

        app.icon = icon
        adapter.notifyDataSetChanged()

        val bitmap = (icon as BitmapDrawable).bitmap
        val dialogDrawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 200, 200, true))
        dialogIcon.setImageDrawable(dialogDrawable)
    }

    private fun launchIconPickerGridForSelectedPack(appName: String, selectedIconPack: String) {
        val intent = Intent(baseContext, IconsActivity::class.java)
        intent.putExtra("iconPack", selectedIconPack)
        intent.putExtra("appName", appName)
        startActivityForResult(intent, 2)
    }

    fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            loadApps()
            loadBackground()
        } else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val customIcon = it.getStringExtra("customIcon")
                    val appName = it.getStringExtra("appName")
                    val iconProp = customIcon.split("/")
                    val icon = IconPackManager(this).loadDrawable(iconProp[0], iconProp[1])
                    setIcon(appName, icon!!)
                }
            }
        }
    }

    override fun onBackPressed() {}
}
