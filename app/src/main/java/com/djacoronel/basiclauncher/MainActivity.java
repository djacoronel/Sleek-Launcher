package com.djacoronel.basiclauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private List<AppDetail> apps;
    private GridView grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadApps();
        loadAppGrid();
    }

    @Override
    public void onBackPressed() {
    }

    public class AppDetail{
        CharSequence label, name;
        Drawable icon;
    }

    private void loadApps() {
        PackageManager manager = getPackageManager();
        apps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);

        for(ResolveInfo ri : availableActivities) {
            AppDetail app = new AppDetail();
            app.label = ri.loadLabel(manager);
            app.name = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(manager);
            apps.add(app);
        }

        Collections.sort(apps, new Comparator<AppDetail>() {
            @Override
            public int compare(AppDetail a1, AppDetail a2) {
                return a1.label.toString().compareToIgnoreCase(a2.label.toString());
            }
        });
    }

    public void loadAppGrid(){
        RecyclerView grid = (RecyclerView) findViewById(R.id.app_grid);
        GridAdapter adapter = new GridAdapter(apps, this);

        grid.setLayoutManager(new GridLayoutManager(this,4));
        grid.setAdapter(adapter);
    }
}
