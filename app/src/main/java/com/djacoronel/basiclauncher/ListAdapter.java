package com.djacoronel.basiclauncher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private ArrayList<Task> tasks;
    private Context mContext;

    ListAdapter(ArrayList<Task> tasks, MethodCaller listener, Context context) {
        this.tasks = tasks;
        this.listener = listener;
        mContext = context;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView cName, eName, cDuration, eDuration;
        ConstraintLayout collapsed, expanded;

        ViewHolder(View itemView) {
            super(itemView);

            cName = (TextView) itemView.findViewById(R.id.itemName);
            eName = (TextView) itemView.findViewById(R.id.itemNameEx);

            cDuration = (TextView) itemView.findViewById(R.id.itemDuration);
            eDuration = (TextView) itemView.findViewById(R.id.itemDurationEx);

            collapsed = (ConstraintLayout) itemView.findViewById(R.id.collapsed_item);
            expanded = (ConstraintLayout) itemView.findViewById(R.id.expanded_item);
        }
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ListAdapter.ViewHolder holder, int position) {
        Task task = tasks.get(position);

        if(task.getItemType().equals("normal")) {
            holder.cName.setText(task.getName());
            holder.cDuration.setText(task.getDuration());
            holder.eName.setText(task.getName());
            holder.eDuration.setText(task.getDuration());

            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (holder.collapsed.getVisibility() != View.GONE) {
                                holder.collapsed.setVisibility(View.GONE);
                                holder.expanded.setVisibility(View.VISIBLE);
                            } else {
                                holder.collapsed.setVisibility(View.VISIBLE);
                                holder.expanded.setVisibility(View.GONE);
                            }
                        }
                    }
            );

            holder.collapsed.setVisibility(View.VISIBLE);
            holder.expanded.setVisibility(View.GONE);

        } else if(task.getItemType().equals("input")){
            holder.cName.setText("Task Name");
            holder.cDuration.setText("Duration");
            holder.eName.setText("Task Name");
            holder.eDuration.setText("Duration");

            holder.itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tasks.remove(tasks.size()-1);
                            notifyItemRemoved(tasks.size());
                            listener.addTask();
                        }
                    }
            );

            holder.collapsed.setVisibility(View.VISIBLE);
            holder.expanded.setVisibility(View.GONE);
        } else {
            holder.collapsed.setVisibility(View.GONE);
            holder.expanded.setVisibility(View.VISIBLE);
        }
    }

    MethodCaller listener;

    interface MethodCaller {
        void addTask();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }
}
