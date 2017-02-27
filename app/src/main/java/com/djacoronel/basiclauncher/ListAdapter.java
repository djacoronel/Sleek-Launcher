package com.djacoronel.basiclauncher;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

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
    public void onBindViewHolder(final ListAdapter.ViewHolder holder, final int position) {
        final Task task = tasks.get(position);

        if (task.getItemType().equals("normal")) {
            holder.cName.setText(task.getName());
            holder.cDuration.setText(task.getDuration());
            holder.eName.setText(task.getName());

            holder.cName.setTextColor(Color.BLACK);
            holder.cDuration.setTextColor(Color.BLACK);

            holder.collapsed.setVisibility(View.VISIBLE);
            holder.expanded.setVisibility(View.GONE);
        } else if (task.getItemType().equals("expanded")) {
            holder.cName.setText(task.getName());
            holder.cDuration.setText(task.getDuration());
            holder.eName.setText(task.getName());

            holder.cName.setTextColor(Color.BLACK);
            holder.cDuration.setTextColor(Color.BLACK);

            holder.collapsed.setVisibility(View.GONE);
            holder.expanded.setVisibility(View.VISIBLE);
        } else {
            holder.cName.setText("Task Name");
            holder.cDuration.setText("Duration");
            holder.eName.setText("");
            holder.eDuration.setText("");

            holder.cName.setTextColor(Color.LTGRAY);
            holder.cDuration.setTextColor(Color.LTGRAY);

            holder.collapsed.setVisibility(View.VISIBLE);
            holder.expanded.setVisibility(View.GONE);
        }

        final CountDownTimer[] timer = new CountDownTimer[1];

        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (task.getItemType().equals("normal")) {
                            holder.collapsed.setVisibility(View.GONE);
                            holder.expanded.setVisibility(View.VISIBLE);
                            final String FORMAT = "%02d:%02d:%02d";


                            timer[0] = new CountDownTimer(task.getDurationValue(), 1000) { // adjust the milli seconds here

                                public void onTick(long millisUntilFinished) {

                                    String duration = "" + String.format(FORMAT,
                                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

                                    holder.eDuration.setText(duration);
                                }

                                public void onFinish() {
                                    holder.eDuration.setText("Done!");
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            tasks.remove(position).deleteFromDb();
                                            notifyItemRemoved(position);
                                        }
                                    }, 2000);
                                }
                            }.start();

                            task.setItemType("expanded");
                        } else if (task.getItemType().equals("expanded")) {
                            holder.collapsed.setVisibility(View.VISIBLE);
                            holder.expanded.setVisibility(View.GONE);
                            task.setItemType("normal");
                            timer[0].cancel();
                        } else {
                            tasks.remove(tasks.size() - 1);
                            notifyItemRemoved(tasks.size());
                            listener.addTask();
                        }
                    }
                }
        );
        holder.itemView.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        timer[0].cancel();
                        holder.eDuration.setText("Done!");
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tasks.remove(position).deleteFromDb();
                                notifyItemRemoved(position);
                            }
                        }, 2000);
                        return true;
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

    public void swap(int firstPosition, int secondPosition) {
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
