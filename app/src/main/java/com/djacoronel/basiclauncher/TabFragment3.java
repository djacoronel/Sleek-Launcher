package com.djacoronel.basiclauncher;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class TabFragment3 extends Fragment {

    private Context mContext;
    LinearLayout widgetSpace;

    public TabFragment3() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_fragment3, container, false);
        mContext = getActivity();
        widgetSpace = (LinearLayout) rootView.findViewById(R.id.widget_space);

        return rootView;
    }
}