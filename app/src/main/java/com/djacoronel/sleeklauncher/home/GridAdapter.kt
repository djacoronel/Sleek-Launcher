package com.djacoronel.sleeklauncher.home

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.djacoronel.sleeklauncher.R
import com.djacoronel.sleeklauncher.data.model.AppDetail
import kotlinx.android.synthetic.main.app_detail.view.*

class GridAdapter(private val apps: List<AppDetail>, private val mContext: Context) : RecyclerView.Adapter<GridAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(app: AppDetail) {
            itemView.item_app_icon.setImageDrawable(app.icon)
            itemView.item_app_label.text = app.label

            itemView.setOnClickListener { v ->
                if (app.label == "Sleek Launcher") (mContext as MainActivity).openSettings()
                else (mContext as MainActivity).launchApp(app, v)
            }

            itemView.setOnLongClickListener {
                (mContext as MainActivity).iconLongClick(app)
                false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.app_detail, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    fun getApp(appActivityName: String): AppDetail {
        val app = apps.find { it.activity == appActivityName }
        return app!!
    }

    fun removeApp(appActivityName: String) {
        val app = apps.find { it.activity == appActivityName }
        val index = apps.indexOf(app)
        (apps as MutableList).removeAt(index)
        notifyItemRemoved(index)
    }
}
