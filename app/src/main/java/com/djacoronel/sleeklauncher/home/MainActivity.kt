package com.djacoronel.sleeklauncher.home

import android.app.Activity
import android.app.ActivityOptions
import android.app.WallpaperManager
import android.content.*
import android.content.Intent.EXTRA_UID
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
    @Inject
    lateinit var iconPackManager: IconPackManager

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    private fun loadBackground() {
        val bgPref = preferences.getString("bgPref", "0,0,0,0,0")
        val argbBlur = bgPref.split(",")
        val argb = intArrayOf(argbBlur[0].toInt(), argbBlur[1].toInt(), argbBlur[2].toInt(), argbBlur[3].toInt())
        val blur = argbBlur[4].toInt()

        loadBackgroundWithBlur(argb, blur)
    }

    private fun loadBackgroundWithBlur(argb: IntArray, blur: Int) {
        val wallpaperDrawable = WallpaperManager.getInstance(this).drawable

        var blurredBitmap =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) (wallpaperDrawable as BitmapDrawable).bitmap
                else cropWallpaper((wallpaperDrawable as BitmapDrawable).bitmap)

        val numberOfFullBlur = blur / 25
        val remainingBlur = blur % 25

        for (i in 0 until numberOfFullBlur) blurredBitmap = BlurBuilder().blur(this, blurredBitmap, 25f)
        if (remainingBlur != 0) blurredBitmap = BlurBuilder().blur(this, blurredBitmap, remainingBlur.toFloat())

        mainBackground.visibility = View.VISIBLE
        mainBackground.setColorFilter(Color.argb(argb[0], argb[1], argb[2], argb[3]))
        mainBackground.setImageDrawable(BitmapDrawable(resources, blurredBitmap))
        mainBackground.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    private fun cropWallpaper(wallpaper: Bitmap): Bitmap {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        val x = 0
        val y = 0
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        return if (wallpaper.width < (x + width) || wallpaper.height < (y + height))
            wallpaper
        else
            Bitmap.createBitmap(wallpaper, x, y, width, height)
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
        val showHidden = preferences.getBoolean("showHidden", false)
        val selectedIconPack = preferences.getString("iconPack", "")

        val manager = packageManager
        val availableActivities = getInstalledAppsInfo()
        val apps = mutableListOf<AppDetail>()

        for (ri in availableActivities) {
            val name = ri.activityInfo.packageName
            val activityName = ri.activityInfo.name
            val label = ri.loadLabel(manager) as String
            val icon = ri.loadIcon(manager)

            val app = AppDetail(label, name, activityName, icon)
            val iconPrefs = iconPrefsDao.getIconPrefs(activityName)

            iconPrefs?.let {
                if (!it.isHidden || showHidden) {
                    if (it.iconName != IconPrefs.NO_CUSTOM_ICON) app.icon = iconPackManager.getAppIcon(it)
                    else app.icon = iconPackManager.getAppIcon(app, selectedIconPack)

                    if (it.label != IconPrefs.NO_CUSTOM_LABEL) app.label = it.label
                    apps.add(app)
                }
            }

            if (iconPrefs == null) {
                app.icon = iconPackManager.getAppIcon(app, selectedIconPack)
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
                val uid = intent.getIntExtra(EXTRA_UID, 0)
                val name = packageManager.getNameForUid(uid)
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
            adapter.removeApp(app.activity)
        }
    }

    private fun getMarshmallowOpeningAnimationOpts(v: View): ActivityOptions {
        val left = 0
        val top = 0
        val width = v.measuredWidth
        val height = v.measuredHeight

        // Use reveal animation if Marshmallow
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ActivityOptions.makeClipRevealAnimation(v, left, top, width, height)
        else
            ActivityOptions.makeScaleUpAnimation(v, left, top, width, height)
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

            val iconPrefs = iconPrefsDao.getIconPrefs(app.activity)
            if (iconPrefs == null)
                positiveButton("Hide") { hideIcon(IconPrefs(app.name, app.activity)) }
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
        if (!showHidden) adapter.removeApp(iconPrefs.activity)
    }

    private fun unHideIcon(iconPrefs: IconPrefs) {
        iconPrefs.isHidden = false
        iconPrefsDao.addIconPrefs(iconPrefs)
        toast("App removed from hidden!")
    }

    private fun showIconPackList(app: AppDetail) {
        val iconPacks = iconPackManager.availableIconPacks
        val iconPackNames = iconPacks.keys.toList()

        selector("Icon Packs", iconPackNames, { _, index ->
            if (iconPackNames[index] == "Default") {
                setDefaultIcon(app)
            } else {
                val selectedIconPack = iconPackNames[index]
                launchIconPickerGridForSelectedPack(app.activity, selectedIconPack)
            }
        })
    }

    private fun setDefaultIcon(app: AppDetail) {
        val iconPrefs = iconPrefsDao.getIconPrefs(app.activity)
        iconPrefs?.let { iconPrefsDao.deleteIconPrefs(it) }

        try {
            val icon = packageManager.getApplicationIcon(app.name)
            setIcon(app.activity, icon)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun setIcon(activityName: String, icon: Drawable) {
        val app = adapter.getApp(activityName)

        app.icon = icon
        adapter.notifyDataSetChanged()

        val bitmap = (icon as BitmapDrawable).bitmap
        val dialogDrawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 200, 200, true))
        dialogIcon.setImageDrawable(dialogDrawable)
    }

    private fun launchIconPickerGridForSelectedPack(activityName: String, selectedIconPack: String) {
        val intent = Intent(baseContext, IconsActivity::class.java)
        intent.putExtra("iconPack", selectedIconPack)
        intent.putExtra("activityName", activityName)
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
                    val activityName = it.getStringExtra("activityName")
                    val iconProp = customIcon.split("/")
                    val icon = iconPackManager.loadDrawable(iconProp[0], iconProp[1])

                    val app = adapter.getApp(activityName)
                    val iconPrefs = IconPrefs(app.name, app.activity)
                    iconPrefs.iconName = customIcon
                    iconPrefsDao.addIconPrefs(iconPrefs)

                    setIcon(activityName, icon!!)
                }
            }
        }
    }

    override fun onBackPressed() {}
}
