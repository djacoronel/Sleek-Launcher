package com.djacoronel.basiclauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private List<AppDetail> apps;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        loadApps();
        loadAppGrid();

        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadApps();
                loadAppGrid();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("package");
        this.registerReceiver(br, intentFilter);

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

        boolean showHidden = preferences.getBoolean("showHidden", false);

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);

        for (ResolveInfo ri : availableActivities) {
            AppDetail app = new AppDetail();
            app.label = ri.loadLabel(manager);
            app.name = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(manager);
            if (!hidden.contains(app.label.toString()) || showHidden)
                apps.add(app);
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
        GridAdapter adapter = new GridAdapter(apps, this);
        grid.setLayoutManager(new GridLayoutManager(this, 4));
        grid.setAdapter(adapter);
    }

    public void openSettings() {
        Intent I = new Intent(this, Settings.class);
        startActivityForResult(I, 1);
    }

    public void iconLongClick(final GridAdapter adapter, final int position) {
        final DbHelper dbHelper = new DbHelper(this);
        ArrayList<String> hidden = dbHelper.getHiddenList();
        final boolean showHidden = preferences.getBoolean("showHidden", false);

        AlertDialog.Builder dBuilder = new AlertDialog.Builder(this)
                .setTitle("Options")
                .setMessage("Uninstall or hide the app?")
                .setCancelable(true)
                .setPositiveButton("Uninstall", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Uri packageUri = Uri.parse("package:" + apps.get(position).name.toString());
                        Intent uninstallIntent =
                                new Intent(Intent.ACTION_DELETE, packageUri);
                        startActivity(uninstallIntent);
                    }
                });

        if (hidden.contains(apps.get(position).label.toString())) {
            dBuilder.setNegativeButton("Unhide", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dbHelper.removeFromHidden(apps.get(position).label.toString());
                    Toast.makeText(MainActivity.this, "App removed from hidden",
                            Toast.LENGTH_LONG).show();
                }
            }).create().show();
        } else {
            dBuilder.setNegativeButton("Hide", new DialogInterface.OnClickListener() {
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
            }).create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        loadApps();
        loadAppGrid();
    }
}
