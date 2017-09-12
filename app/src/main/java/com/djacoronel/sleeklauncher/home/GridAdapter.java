package com.djacoronel.sleeklauncher.home;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.djacoronel.sleeklauncher.R;

import java.util.List;

class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
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
                            ((MainActivity) mContext).launchApp(getAdapterPosition(), v);
                        }
                    }
            );
            itemView.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            // open launcher settings
                            if (apps.get(getAdapterPosition()).label.toString().equals("Settings"))
                                ((MainActivity) mContext).openSettings();
                            else
                                ((MainActivity) mContext).iconLongClick(getAdapterPosition());

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
