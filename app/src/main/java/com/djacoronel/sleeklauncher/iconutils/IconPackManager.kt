package com.djacoronel.sleeklauncher.iconutils


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.drawable.Drawable
import com.djacoronel.sleeklauncher.data.model.IconPrefs
import com.djacoronel.sleeklauncher.home.MainActivity

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

class IconPackManager(private val mContext: Context) {

    val availableIconPacks: HashMap<String, String>
        get() {
            val iconPacks = HashMap<String, String>()
            iconPacks.put("Default", "")

            val rInfos = iconPackRInfos
            for (ri in rInfos) {
                val packageName = ri.activityInfo.packageName
                val label = ri.loadLabel(mContext.packageManager) as String
                iconPacks.put(label, packageName)
            }

            return iconPacks
        }

    private val iconPackRInfos: ArrayList<ResolveInfo>
        get() {
            val pm = mContext.packageManager
            val novaThemes = pm.queryIntentActivities(Intent("com.teslacoilsw.launcher.THEME"), PackageManager.GET_META_DATA)
            val adwThemes = pm.queryIntentActivities(Intent("org.adw.launcher.THEMES"), PackageManager.GET_META_DATA)
            val goThemes = pm.queryIntentActivities(Intent("com.gau.go.launcherex.theme"), PackageManager.GET_META_DATA)

            val rInfos = ArrayList<ResolveInfo>()
            rInfos.addAll(novaThemes)
            rInfos.addAll(adwThemes)
            rInfos.addAll(goThemes)
            return rInfos
        }

    fun loadDrawable(drawableName: String, packageName: String): Drawable? {
        val pm = mContext.packageManager

        return try {
            val iconPackRes = pm.getResourcesForApplication(packageName)
            val id = iconPackRes!!.getIdentifier(drawableName, "drawable", packageName)
            iconPackRes.getDrawable(id)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    fun getAllIcons(packageName: String): List<String> {
        val icons = ArrayList<String>()
        val pm = mContext.packageManager

        try {
            // load app drawable xml from the icon pack package
            val iconPackRes = pm.getResourcesForApplication(packageName)
            val appFilterId = iconPackRes.getIdentifier("drawable", "xml", packageName)
            val xpp = iconPackRes.getXml(appFilterId)

            if (xpp != null) {
                var eventType = xpp.eventType

                // Iterate through app filter xml and store all drawables
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && xpp.name == "item") {
                        val drawableName = xpp.getAttributeValue(0)
                        icons.add(drawableName)
                    }
                    eventType = xpp.next()
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return icons
    }

    fun load(packageName: String): HashMap<String, String> {
        val mPackagesDrawables = HashMap<String, String>()
        val pm = mContext.packageManager

        try {
            // load app filter xml from the icon pack package
            val iconPackRes = pm.getResourcesForApplication(packageName)
            val appFilterId = iconPackRes.getIdentifier("appfilter", "xml", packageName)
            val xpp = iconPackRes.getXml(appFilterId)

            if (xpp != null) {
                var eventType = xpp.eventType

                // Iterate through app filter xml and store component-drawable mappings
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && xpp.name == "item") {
                        var componentName = xpp.getAttributeValue(0)
                        val drawableName = xpp.getAttributeValue(1)

                        val start = componentName.indexOf("{") + 1
                        val end = if (componentName.contains("/")) componentName.indexOf("/")
                        else componentName.indexOf("}")

                        if (start != -1 && end != -1) componentName = componentName.substring(start, end)

                        mPackagesDrawables.put(componentName, drawableName)
                    }
                    eventType = xpp.next()
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return mPackagesDrawables
    }

    fun getAppIcon(appName: String): Drawable? {
        val selectedIconPack = (mContext as MainActivity).preferences.getString("iconPack", "")
        val iconPrefs = mContext.iconPrefsDao.getIconPrefs(appName)

        // get custom icon
        if (iconPrefs != null && iconPrefs.iconName != IconPrefs.NO_CUSTOM_ICON) {
            val iconInfo = iconPrefs.iconName
            val split = iconInfo.split("/")
            val iconDrawable = split[0]
            val iconPackage = split[1]
            return loadDrawable(iconDrawable, iconPackage)
        } else if (selectedIconPack != "") {
            val icPackComponents = load(selectedIconPack)

            if (icPackComponents[appName] != null && loadDrawable(icPackComponents[appName]!!, selectedIconPack) != null) {
                return loadDrawable(icPackComponents[appName]!!, selectedIconPack)
            } else {
                try {
                    return mContext.packageManager.getApplicationIcon(appName)
                } catch (e: PackageManager.NameNotFoundException) {
                    e.printStackTrace()
                }

                return null
            }
        } else {
            try {
                return mContext.packageManager.getApplicationIcon(appName)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return null
        }// get themed icon if available
    }
}
