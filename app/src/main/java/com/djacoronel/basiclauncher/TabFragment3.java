package com.djacoronel.basiclauncher;


import android.app.Fragment;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class TabFragment3 extends Fragment {

    private Context mContext;
    LinearLayout widgetSpace;
    AppWidgetManager appWidgetManager;
    AppWidgetHost appWidgetHost;
    DbHelper dbHelper;

    public TabFragment3() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_fragment3, container, false);
        mContext = getActivity().getApplicationContext();

        dbHelper = new DbHelper(mContext);

        widgetSpace = (LinearLayout) rootView.findViewById(R.id.widget_space);
        appWidgetManager = AppWidgetManager.getInstance(mContext);
        appWidgetHost = new AppWidgetHost(mContext, APPWIDGET_HOST_ID);
        appWidgetHost.startListening();

        getWidgets();


        widgetSpace.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        selectWidget();
                        return false;
                    }
                }
        );

        return rootView;
    }

    void getWidgets() {
        ArrayList<String> widgets = dbHelper.getWidgets();

        for (String s : widgets) {
            int appWidgetId = Integer.parseInt(s);
            AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
            AppWidgetHostView hostView = appWidgetHost.createView(mContext, appWidgetId, appWidgetInfo);
            hostView.setAppWidget(appWidgetId, appWidgetInfo);
            // Add  it on the layout you want
            widgetSpace.addView(hostView);
        }
    }

    final int APPWIDGET_HOST_ID = 2048;
    final int REQUEST_PICK_APPWIDGET = 0;
    final int REQUEST_CREATE_APPWIDGET = 5;

    // Let user pick a widget from the list of intalled AppWidgets
    public void selectWidget() {
        int appWidgetId = this.appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    // For some reason you have to add this empty data, else it won't work
    public void addEmptyData(Intent pickIntent) {
        ArrayList<AppWidgetProviderInfo> customInfo =
                new ArrayList<>();
        pickIntent.putParcelableArrayListExtra(
                AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
        ArrayList<Bundle> customExtras = new ArrayList<>();
        pickIntent.putParcelableArrayListExtra(
                AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            } else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                createWidget(data);
            }
        } else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId =
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                appWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    // Show configuration activity of the widget picked by the user
    private void configureWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo =
                appWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent =
                    new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            createWidget(data);
        }
    }

    // Get an instance of the selected widget as a AppWidgetHostView
    public void createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        dbHelper.addWidget(appWidgetId);

        AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);

        AppWidgetHostView hostView = appWidgetHost.createView(mContext, appWidgetId, appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        // Add  it on the layout you want
        widgetSpace.addView(hostView);
    }

    // Call this when you want to remove one from your layout
    public void removeWidget(AppWidgetHostView hostView) {
        appWidgetHost.deleteAppWidgetId(hostView.getAppWidgetId());

        // Remove from your layout
        widgetSpace.removeView(hostView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        appWidgetHost.stopListening();
        appWidgetHost = null;
    }
}