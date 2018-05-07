package com.djacoronel.sleeklauncher.iconutils

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.djacoronel.sleeklauncher.R
import kotlinx.android.synthetic.main.app_detail.view.*

class IconPickerAdapter(
        private val icons: List<String>,
        private val iconPack: String,
        private val mContext: Context
) : RecyclerView.Adapter<IconPickerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(icon: Drawable) {
            itemView.item_app_icon.setImageDrawable(icon)
            itemView.setOnClickListener { (mContext as IconsActivity).setIconAsResult(icons[adapterPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.app_detail, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val icManager = IconPackManager(mContext)
        val icon = icManager.loadDrawable(icons[position], iconPack)
        icon?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int = icons.size
}
