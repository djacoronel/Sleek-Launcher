package com.djacoronel.basiclauncher.iconutils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.djacoronel.basiclauncher.R;

import java.util.List;

class IconPickerAdapter extends RecyclerView.Adapter<IconPickerAdapter.ViewHolder> {
    private List<String> icons;
    private String iconPack;
    private Context mContext;

    IconPickerAdapter(List<String> icons, String iconPack, Context context) {
        this.icons = icons;
        this.iconPack = iconPack;
        mContext = context;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;

        ViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.item_app_icon);
            itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((IconsActivity) mContext).pickIcon(icons.get(getAdapterPosition()));
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
        IconPackManager icManager = new IconPackManager(mContext);
        Drawable icon = icManager.loadDrawable(icons.get(position), iconPack);
        if (icon == null) icons.remove(position);
        else holder.icon.setImageDrawable(icon);
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }
}
