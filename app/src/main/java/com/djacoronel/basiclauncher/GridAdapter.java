package com.djacoronel.basiclauncher;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
    private PackageManager manager;
    private List<MainActivity.AppDetail> apps;
    private Context mContext;

    GridAdapter(List<MainActivity.AppDetail> apps, Context context) {
        this.apps = apps;
        mContext = context;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.item_app_label);
            icon = (ImageView) itemView.findViewById(R.id.item_app_icon);
            itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            manager = mContext.getPackageManager();
                            Intent i = manager.getLaunchIntentForPackage(apps.get(getAdapterPosition()).name.toString());

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

                            mContext.startActivity(i, optsBundle);
                        }
                    }
            );
            itemView.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (apps.get(getAdapterPosition()).label.toString().equals("Settings")) {
                                ((MainActivity) mContext).openSettings();
                            } else {
                                ((MainActivity) mContext).iconLongClick(GridAdapter.this, getAdapterPosition());
                            }
                            return false;
                        }
                    }
            );
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.app_detail, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.icon.setImageDrawable(apps.get(position).icon);
        holder.label.setText(apps.get(position).label);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }
}
