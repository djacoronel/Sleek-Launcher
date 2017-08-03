package com.djacoronel.basiclauncher.iconutils;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IconPackManager {

    private Context mContext;

    public IconPackManager(Context context) {
        mContext = context;
    }

    public HashMap<String, String> getAvailableIconPacks() {
        HashMap<String, String> iconPacks = new HashMap<>();
        iconPacks.put("Default", "");

        // find apps with "Theme" intent and return build the HashMap
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> novaThemes = pm.queryIntentActivities(new Intent("com.teslacoilsw.launcher.THEME"), PackageManager.GET_META_DATA);
        List<ResolveInfo> adwThemes = pm.queryIntentActivities(new Intent("org.adw.launcher.THEMES"), PackageManager.GET_META_DATA);
        List<ResolveInfo> goThemes = pm.queryIntentActivities(new Intent("com.gau.go.launcherex.theme"), PackageManager.GET_META_DATA);

        // merge those lists
        ArrayList<ResolveInfo> rInfo = new ArrayList<>();
        rInfo.addAll(novaThemes);
        rInfo.addAll(adwThemes);
        rInfo.addAll(goThemes);

        // add list to hash map
        for (ResolveInfo ri : rInfo) {
            String packageName = ri.activityInfo.packageName;
            String label = (String) ri.loadLabel(pm);
            iconPacks.put(label, packageName);
        }
        return iconPacks;
    }

    public Drawable loadDrawable(String drawableName, String packageName) {
        PackageManager pm = mContext.getPackageManager();

        Resources iconPackRes = null;

        try {
            iconPackRes = pm.getResourcesForApplication(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        int id = iconPackRes.getIdentifier(drawableName, "drawable", packageName);
        if (id > 0) return iconPackRes.getDrawable(id, null);
        else return null;
    }

    List<String> getAllIcons(String packageName) {
        ArrayList<String> icons = new ArrayList<>();

        PackageManager pm = mContext.getPackageManager();

        try {
            XmlPullParser xpp;

            // load app drawable xml from the icon pack package
            Resources iconPackRes = pm.getResourcesForApplication(packageName);
            int appFilterId = iconPackRes.getIdentifier("drawable", "xml", packageName);
            xpp = iconPackRes.getXml(appFilterId);

            if (xpp != null) {
                int eventType = xpp.getEventType();

                // Iterate through app filter xml and store all drawables
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("item")) {

                        String drawableName = xpp.getAttributeValue(0);
                        icons.add(drawableName);

                    }
                    eventType = xpp.next();
                }
            }
        } catch (PackageManager.NameNotFoundException | XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return icons;
    }

    public HashMap<String, String> load(String packageName) {
        HashMap<String, String> mPackagesDrawables = new HashMap<>();

        PackageManager pm = mContext.getPackageManager();

        try {
            XmlPullParser xpp;

            // load app filter xml from the icon pack package
            Resources iconPackRes = pm.getResourcesForApplication(packageName);
            int appFilterId = iconPackRes.getIdentifier("appfilter", "xml", packageName);
            xpp = iconPackRes.getXml(appFilterId);

            if (xpp != null) {
                int eventType = xpp.getEventType();

                // Iterate through app filter xml and store component-drawable mappings
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("item")) {

                        String componentName = xpp.getAttributeValue(0);
                        String drawableName = xpp.getAttributeValue(1);

                        int start = componentName.indexOf("{") + 1;
                        int end = (componentName.contains("/")) ? componentName.indexOf("/") : componentName.indexOf("}");

                        if (start != -1 && end != -1)
                            componentName = componentName.substring(start, end);
                        mPackagesDrawables.put(componentName, drawableName);

                    }
                    eventType = xpp.next();
                }
            }
        } catch (PackageManager.NameNotFoundException | XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return mPackagesDrawables;
    }
}
