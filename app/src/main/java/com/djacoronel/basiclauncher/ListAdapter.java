package com.djacoronel.basiclauncher;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    ArrayList<Task> tasks;
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
        final Task task = tasks.get(position);

        if (task.getItemType().equals("normal")) {
            holder.cName.setText(task.getName());
            holder.cDuration.setText(task.getDuration());
            holder.eName.setText(task.getName());
            holder.eDuration.setText(task.getDuration());

            holder.collapsed.setVisibility(View.VISIBLE);
            holder.expanded.setVisibility(View.GONE);
        } else if (task.getItemType().equals("expanded")) {
            holder.cName.setText(task.getName());
            holder.cDuration.setText(task.getDuration());
            holder.eName.setText(task.getName());
            holder.eDuration.setText(task.getDuration());

            holder.collapsed.setVisibility(View.GONE);
            holder.expanded.setVisibility(View.VISIBLE);
        } else {
            holder.cName.setText("Task Name");
            holder.cDuration.setText("00:00");
            holder.eName.setText("");
            holder.eDuration.setText("");

            holder.collapsed.setVisibility(View.VISIBLE);
            holder.expanded.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (task.getItemType().equals("normal")) {
                            holder.collapsed.setVisibility(View.GONE);
                            holder.expanded.setVisibility(View.VISIBLE);
                            task.setItemType("expanded");
                        } else if (task.getItemType().equals("expanded")) {
                            holder.collapsed.setVisibility(View.VISIBLE);
                            holder.expanded.setVisibility(View.GONE);
                            task.setItemType("normal");
                        } else {
                            tasks.remove(tasks.size() - 1);
                            notifyItemRemoved(tasks.size());
                            listener.addTask();
                        }
                    }
                }
        );
    }

    MethodCaller listener;

    interface MethodCaller {
        void addTask();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void remove(int position) {
        tasks.remove(position).deleteFromDb();
        notifyItemRemoved(position);
    }

    public void swap(int firstPosition, int secondPosition){
        Task task1 = tasks.get(firstPosition);
        Task task2 = tasks.get(secondPosition);

        long id1 = task1.getId();
        long id2 = task2.getId();

        task1.setId(id2);
        task2.setId(id1);

        task1.updateDb();
        task2.updateDb();

        Collections.swap(tasks, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
    }

}
