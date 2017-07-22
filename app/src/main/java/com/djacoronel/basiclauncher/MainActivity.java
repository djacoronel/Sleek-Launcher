package com.djacoronel.basiclauncher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private List<AppDetail> apps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    class AppDetail {
        CharSequence label, name;
        Drawable icon;
    }

    private void loadApps() {
        PackageManager manager = this.getPackageManager();
        DbHelper dbHelper = new DbHelper(this);
        ArrayList<String> hidden = dbHelper.getHiddenList();
        apps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);

        for (ResolveInfo ri : availableActivities) {
            AppDetail app = new AppDetail();
            app.label = ri.loadLabel(manager);
            app.name = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(manager);
            if (!hidden.contains(app.label.toString()))
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
}
