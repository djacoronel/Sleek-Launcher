package com.djacoronel.basiclauncher;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TabFragment2 extends Fragment {

    private List<AppDetail> apps;
    private Context mContext;
    RecyclerView grid;

    public TabFragment2(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_fragment2, container, false);
        mContext  = getActivity();

        grid = (RecyclerView) rootView.findViewById(R.id.app_grid);

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
        intentFilter.addDataScheme("package");
        mContext.registerReceiver(br, intentFilter);

        return rootView;
    }

    public class AppDetail{
        CharSequence label, name;
        Drawable icon;
    }



    private void loadApps() {
        PackageManager manager = mContext.getPackageManager();
        DbHelper dbHelper = new DbHelper(mContext);
        ArrayList<String> hidden = dbHelper.getHiddenList();
        apps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);

        for(ResolveInfo ri : availableActivities) {
            AppDetail app = new AppDetail();
            app.label = ri.loadLabel(manager);
            app.name = ri.activityInfo.packageName;
            app.icon = ri.activityInfo.loadIcon(manager);
            if(!hidden.contains(app.label.toString()))
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
        GridAdapter adapter = new GridAdapter(apps, mContext);
        grid.setLayoutManager(new GridLayoutManager(mContext,4));
        grid.setAdapter(adapter);
    }
}
