package com.djacoronel.sleeklauncher.home;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.djacoronel.sleeklauncher.R;
import com.djacoronel.sleeklauncher.data.DbHelper;
import com.djacoronel.sleeklauncher.iconutils.IconPackManager;
import com.djacoronel.sleeklauncher.iconutils.IconsActivity;
import com.djacoronel.sleeklauncher.settings.SettingsActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    private List<AppDetail> apps;
    SharedPreferences preferences;
    GridAdapter adapter;

    int selectedAppPosition;
    ImageView image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        loadApps();
        loadAppGrid();

        // refresh app grid when app is installed or uninstalled
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadApps();
                loadAppGrid();
            }
        };
        this.registerReceiver(br, intentFilter);

        // make status bar and navigation bar transparent
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }


    @Override
    public void onBackPressed() {
    }


    class AppDetail {
        CharSequence label, name;
        Drawable icon;
    }


    private void loadApps() {
        PackageManager manager = this.getPackageManager();
        DbHelper dbHelper = new DbHelper(this);
        ArrayList<String> hidden = dbHelper.getHiddenList();
        apps = new ArrayList<>();

        // get settings
        boolean showHidden = preferences.getBoolean("showHidden", false);
        String selectedIconPack = preferences.getString("iconPack", "");
        IconPackManager icManager = new IconPackManager(this);
        HashMap<String, String> components = icManager.load(selectedIconPack);

        // get installed apps
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);

        for (ResolveInfo ri : availableActivities) {
            if (!hidden.contains(ri.loadLabel(manager)) || showHidden) {

                AppDetail app = new AppDetail();
                app.label = ri.loadLabel(manager);
                app.name = ri.activityInfo.packageName;

                String customInfo[] = dbHelper.getCustom((String) app.label);

                // get custom icon
                if (customInfo[0] != null) {
                    String iconInfo[] = customInfo[0].split("/");
                    app.icon = icManager.loadDrawable(iconInfo[0], iconInfo[1]);
                }
                // get themed icon if available
                else if (!selectedIconPack.equals("") && components.get(app.name) != null
                        && icManager.loadDrawable(components.get(app.name), selectedIconPack) != null)
                    app.icon = icManager.loadDrawable(components.get(app.name), selectedIconPack);
                else
                    app.icon = ri.activityInfo.loadIcon(manager);

                apps.add(app);
            }
        }

        Collections.sort(apps, new Comparator<AppDetail>() {
            @Override
            public int compare(AppDetail a1, AppDetail a2) {
                return a1.label.toString().compareToIgnoreCase(a2.label.toString());
            }
        });
    }


    public void loadAppGrid() {
        RecyclerView grid = (RecyclerView) findViewById(R.id.app_grid);
        adapter = new GridAdapter(apps, this);
        grid.setLayoutManager(new GridLayoutManager(this, 4));
        grid.setAdapter(adapter);
    }

    public void launchApp(int position, View v) {
        PackageManager manager = getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(apps.get(position).name.toString());


        // add Marshmallow opening animation
        Bundle optsBundle;
        ActivityOptions opts;
        if (Build.VERSION.SDK_INT >= 23) {
            int left = 0, top = 0;
            int width = v.getMeasuredWidth(), height = v.getMeasuredHeight();
            opts = ActivityOptions.makeClipRevealAnimation(v, left, top, width, height);
        } else {
            // Below L, we use a scale up animation
            opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        }
        optsBundle = opts != null ? opts.toBundle() : null;
        if (i != null)
            startActivity(i, optsBundle);
        else {
            Toast.makeText(MainActivity.this, "App cannot be found", Toast.LENGTH_SHORT).show();
            apps.remove(position);
            loadAppGrid();
        }
    }


    public void openSettings() {
        Intent I = new Intent(this, SettingsActivity.class);
        startActivityForResult(I, 1);
    }


    public void iconLongClick(final GridAdapter adapter, final int position) {
        final DbHelper dbHelper = new DbHelper(this);
        ArrayList<String> hidden = dbHelper.getHiddenList();
        final boolean showHidden = preferences.getBoolean("showHidden", false);

        AlertDialog.Builder dBuilder = new AlertDialog.Builder(this)
                .setTitle("Options")
                .setMessage("Touch the icon to apply theme.")
                .setCancelable(true)
                .setNegativeButton("Uninstall", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri packageUri = Uri.parse("package:" + apps.get(position).name.toString());
                        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                        startActivity(uninstallIntent);
                    }
                });


        // set hide and unhide buttons
        if (hidden.contains(apps.get(position).label.toString())) {
            dBuilder.setPositiveButton("Unhide", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dbHelper.removeFromHidden(apps.get(position).label.toString());
                    Toast.makeText(MainActivity.this, "App removed from hidden",
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            dBuilder.setPositiveButton("Hide", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dbHelper.addToHidden(apps.get(position).label.toString());
                    if (!showHidden) {
                        apps.remove(position);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "App marked as hidden, unshow marked apps in settings",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


        // add app icon to alert dialog
        image = new ImageView(this);
        Bitmap bitmap = ((BitmapDrawable) apps.get(position).icon).getBitmap();
        Drawable icon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 200, 200, true));
        image.setImageDrawable(icon);
        image.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickIcon();
                        selectedAppPosition = position;
                    }
                }
        );
        dBuilder.setView(image).create().show();
    }


    public void pickIcon() {
        IconPackManager icManager = new IconPackManager(this);
        final HashMap<String, String> iconPacks = icManager.getAvailableIconPacks();
        final String[] iconPackNames = iconPacks.keySet().toArray(new String[0]);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Icon Packs")
                .setItems(iconPackNames, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, iconPackNames[which], Toast.LENGTH_SHORT).show();

                        if (iconPackNames[which].equals("Default")) {
                            //set default icons
                            DbHelper dbHelper = new DbHelper(MainActivity.this);

                            AppDetail app = apps.get(selectedAppPosition);
                            dbHelper.removeFromCustom((String) app.label);
                            try {
                                // change icon in grid
                                Drawable icon = getPackageManager().getApplicationIcon((String) app.name);
                                app.icon = icon;
                                adapter.notifyItemChanged(selectedAppPosition);

                                // change icon in dialog
                                Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
                                icon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 200, 200, true));
                                image.setImageDrawable(icon);

                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // launch icon picker for selected pack
                            Intent intent = new Intent(getBaseContext(), IconsActivity.class);
                            intent.putExtra("iconpack", iconPacks.get(iconPackNames[which]));
                            intent.putExtra("iconpackname", iconPackNames[which]);

                            startActivityForResult(intent, 2);
                        }

                    }
                });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            loadApps();
            loadAppGrid();
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                String customIcon = data.getStringExtra("customicon");
                changeIcon(customIcon);
            }
        }
    }


    public void changeIcon(String customIcon) {
        String iconProp[] = customIcon.split("/");

        // change icon in grid
        Drawable icon = new IconPackManager(this).loadDrawable(iconProp[0], iconProp[1]);
        apps.get(selectedAppPosition).icon = icon;
        adapter.notifyItemChanged(selectedAppPosition);

        // change icon in dialog
        Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
        icon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 200, 200, true));
        image.setImageDrawable(icon);

        // save custom icon in database
        String label = (String) apps.get(selectedAppPosition).label;
        DbHelper dbHelper = new DbHelper(this);
        dbHelper.addToCustom(label, customIcon, "");
    }


    public void changeLabel() {

    }
}
