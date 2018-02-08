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

    private var apps: MutableList<AppDetail>? = null

    @Inject
    lateinit var iconPrefsDao: IconPrefsDao
    @Inject
    lateinit var preferences: SharedPreferences

    private lateinit var adapter: GridAdapter
    private lateinit var dialogIcon: ImageView

    private val installedAppsInfo: List<ResolveInfo>
        get() {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            return packageManager.queryIntentActivities(intent, 0)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_main)

        loadBackground()
        setupBackgroundRefreshing()
        loadApps()
        loadAppGrid()
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
        val manager = this.packageManager

        apps = ArrayList()

        val showHidden = preferences.getBoolean("showHidden", false)

        val availableActivities = installedAppsInfo

        for (ri in availableActivities) {
            val name = ri.activityInfo.packageName
            var label = ri.loadLabel(manager) as String
            var icon: Drawable?

            val iconPrefs = iconPrefsDao.getIconPrefs(name)

            icon = getAppIcon(name)

            if (iconPrefs != null) {
                if (iconPrefs.label != IconPrefs.NO_CUSTOM_LABEL) {
                    label = iconPrefs.label
                }
                if (!iconPrefs.isHidden || showHidden) {
                    val app = AppDetail(label, name, icon!!)
                    apps!!.add(app)
                }
            } else {
                val app = AppDetail(label, name, icon!!)
                apps!!.add(app)
            }
        }

        Collections.sort(apps!!) { a1, a2 -> a1.label.compareTo(a2.label, ignoreCase = true) }
    }

    private fun getAppIcon(appName: String): Drawable? {
        val icManager = IconPackManager(this)
        val selectedIconPack = preferences.getString("iconPack", "")
        val iconPrefs = iconPrefsDao.getIconPrefs(appName)

        // get custom icon
        if (iconPrefs != null && iconPrefs.iconName != IconPrefs.NO_CUSTOM_ICON) {
            val iconInfo = iconPrefs.iconName
            val split = iconInfo.split("/")
            val iconDrawable = split[0]
            val iconPackage = split[1]
            return icManager.loadDrawable(iconDrawable, iconPackage)
        } else if (selectedIconPack != "") {
            val icPackComponents = icManager.load(selectedIconPack)

            if (icPackComponents[appName] != null && icManager.loadDrawable(icPackComponents[appName]!!, selectedIconPack) != null) {
                return icManager.loadDrawable(icPackComponents[appName]!!, selectedIconPack)
            } else {
                try {
                    return packageManager.getApplicationIcon(appName)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

                return null
            }
        } else {
            try {
                return packageManager.getApplicationIcon(appName)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return null
        }// get themed icon if available
    }

    fun loadAppGrid() {
        adapter = GridAdapter(apps!!, this)
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
                loadAppGrid()
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
            adapter.removeApp(app)
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

    fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(intent, 1)
    }

    fun iconLongClick(app: AppDetail) {
        val showHidden = preferences.getBoolean("showHidden", false)

        dialogIcon = ImageView(this)
        val bitmap = (app.icon as BitmapDrawable).bitmap
        val icon = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 200, 200, true))
        dialogIcon.setImageDrawable(icon)
        dialogIcon.setOnClickListener { showIconPackList(app) }

        val longClickAlert = alert {
            title = "Options"
            message = "Tap icon to change it."
            isCancelable = true
            customView = dialogIcon
            negativeButton("Uninstall") {
                val packageUri = Uri.parse("package:" + app.name)
                val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
                startActivity(uninstallIntent)
            }
        }

        val iconPrefs = iconPrefsDao.getIconPrefs(app.name)

        if (iconPrefs == null) {
            longClickAlert.positiveButton("Hide") {
                val newIconPrefs = IconPrefs(app.name)
                newIconPrefs.isHidden = true
                iconPrefsDao.addIconPrefs(newIconPrefs)

                toast("App marked as hidden!")

                if (!showHidden) {
                    adapter.removeApp(app)
                }
            }
        } else {
            if (iconPrefs.isHidden) {
                longClickAlert.positiveButton("Unhide") {
                    iconPrefs.isHidden = false
                    iconPrefsDao.updateIconPrefs(iconPrefs)

                    toast("App removed from hidden!")
                }
            } else {
                longClickAlert.positiveButton("Hide") {
                    iconPrefs.isHidden = true
                    iconPrefsDao.updateIconPrefs(iconPrefs)

                    toast("App marked as hidden!")

                    if (!showHidden) {
                        adapter.removeApp(app)
                    }
                }
            }
        }

        longClickAlert.show()
    }

    private fun showIconPackList(app: AppDetail) {
        val icManager = IconPackManager(this)
        val iconPacks = icManager.availableIconPacks
        val iconPackNames = iconPacks.keys.toList()

        selector("Icon Packs", iconPackNames, { _, index ->
            if (iconPackNames[index] == "Default") {
                setDefaultIcon(app)
            } else {
                val selectedIconPack = iconPackNames[index]
                launchIconPickerGridForSelectedPack(app.name,selectedIconPack)
            }
        })
    }

    private fun setDefaultIcon(app: AppDetail) {
        val iconPrefs = iconPrefsDao.getIconPrefs(app.name)

        iconPrefs?.let {
            iconPrefsDao.deleteIconPrefs(it)
        }

        try {
            val icon = packageManager.getApplicationIcon(app.name)
            app.icon = icon
            adapter.notifyDataSetChanged()

            val bitmap = (icon as BitmapDrawable).bitmap
            val dialogDrawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 200, 200, true))
            dialogIcon.setImageDrawable(dialogDrawable)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun launchIconPickerGridForSelectedPack(appName: String, selectedIconPack: String) {
        val intent = Intent(baseContext, IconsActivity::class.java)
        intent.putExtra("iconpack", selectedIconPack)
        intent.putExtra("appName", appName)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1) {
            loadApps()
            loadAppGrid()
            loadBackground()
        } else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val customIcon = it.getStringExtra("customicon")
                    val appName = it.getStringExtra("appName")
                    changeIcon(appName, customIcon)
                }
            }
        }
    }

    fun changeIcon(appName: String, customIcon: String) {
        val iconProp = customIcon.split("/")
        val icon = IconPackManager(this).loadDrawable(iconProp[0], iconProp[1])
        val app = adapter.getApp(appName)

        app.icon = icon!!
        adapter.notifyDataSetChanged()

        val bitmap = (icon as BitmapDrawable).bitmap
        val dialogDrawable = BitmapDrawable(resources, Bitmap.createScaledBitmap(bitmap, 200, 200, true))
        dialogIcon.setImageDrawable(dialogDrawable)
    }

    override fun onBackPressed() {}
}
