package com.djacoronel.basiclauncher;

import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
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

        private CountDownTimer timer;
        private CountUpTimer upTimer;
        private long timeRemaining;

        private void startTimer(final Task task) {
            if (task.getStatus().equals("pending")) {
                countDown(task);
            } else {
                countUp(task);
            }
        }

        private void countDown(final Task task) {
            final String HFORMAT = "%2d:%02d:%02d";
            final String MFORMAT = "%2d:%02d";

            if (timer != null)
                timer.cancel();
            timer = new CountDownTimer(task.getTimeRemaining(), 1000) {
                public void onTick(long millisUntilFinished) {
                    String duration;
                    if (TimeUnit.MILLISECONDS.toHours(millisUntilFinished) != 0) {
                        duration = "" + String.format(HFORMAT,
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                    } else {
                        duration = "" + String.format(MFORMAT,
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));
                    }
                    timeRemaining = millisUntilFinished;
                    eDuration.setText(duration);
                    task.setTimeRemaining(millisUntilFinished);
                    task.updateDb();
                }

                public void onFinish() {
                    task.setStatus("overtime");
                    countUp(task);
                }
            }.start();
        }

        private void countUp(final Task task) {
            final String HFORMAT = "%2d:%02d:%02d";
            final String MFORMAT = "%2d:%02d";
            final long startTime = task.getTimeRemaining();

            if (upTimer != null)
                upTimer.stop();
            upTimer = new CountUpTimer(1000) {
                @Override
                public void onTick(long elapsedTime) {
                    elapsedTime += startTime;

                    String overTime;
                    if (TimeUnit.MILLISECONDS.toHours(elapsedTime) != 0) {
                        overTime = "" + String.format(HFORMAT,
                                TimeUnit.MILLISECONDS.toHours(elapsedTime),
                                TimeUnit.MILLISECONDS.toMinutes(elapsedTime) - TimeUnit.HOURS.toMinutes(
                                        TimeUnit.MILLISECONDS.toHours(elapsedTime)),
                                TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));
                    } else {
                        overTime = "" + String.format(MFORMAT,
                                TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
                                TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(elapsedTime)));
                    }
                    timeRemaining = elapsedTime;
                    eDuration.setText(overTime);
                    task.setTimeRemaining(timeRemaining);
                    task.updateDb();
                }
            };
            upTimer.start();
        }

        private void stopTimer() {
            if (timer != null)
                timer.cancel();
            if (upTimer != null)
                upTimer.stop();
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

        switch (task.getItemType()) {
            case "normal":
                setCollapsed(holder, task);
                break;
            case "expanded":
                setExpanded(holder, task);
                holder.startTimer(task);
                break;
            default:
                setInput(holder, task);
                break;
        }

        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (task.getItemType()) {
                            case "normal":
                                holder.collapsed.setVisibility(View.GONE);
                                holder.expanded.setVisibility(View.VISIBLE);
                                task.setItemType("expanded");
                                task.updateDb();
                                holder.startTimer(task);
                                break;
                            case "expanded":
                                if (task.getStatus().equals("pending")) {
                                    holder.collapsed.setVisibility(View.VISIBLE);
                                    holder.expanded.setVisibility(View.GONE);
                                    task.setItemType("normal");
                                    task.updateDb();
                                    holder.stopTimer();
                                }
                                break;
                            default:
                                tasks.remove(tasks.size() - 1);
                                notifyItemRemoved(tasks.size());
                                listener.addTask();
                                break;
                        }
                    }
                }
        );


        holder.itemView.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (task.getItemType().equals("expanded")) {
                            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(100);
                            holder.stopTimer();
                            holder.eDuration.setText("Done!");
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tasks.remove(holder.getAdapterPosition()).deleteFromDb();
                                    notifyItemRemoved(holder.getAdapterPosition());
                                }
                            }, 2000);
                        }
                        return true;
                    }
                }
        );

    }

    private void setCollapsed(ViewHolder holder, Task task) {
        holder.cName.setText(task.getName());
        holder.cDuration.setText(task.getDuration());
        holder.eName.setText(task.getName());

        holder.cName.setTextColor(Color.BLACK);
        holder.cDuration.setTextColor(Color.BLACK);

        holder.collapsed.setVisibility(View.VISIBLE);
        holder.expanded.setVisibility(View.GONE);
    }

    private void setExpanded(ViewHolder holder, Task task) {
        holder.cName.setText(task.getName());
        holder.cDuration.setText(task.getDuration());
        holder.eName.setText(task.getName());

        holder.cName.setTextColor(Color.BLACK);
        holder.cDuration.setTextColor(Color.BLACK);

        holder.collapsed.setVisibility(View.GONE);
        holder.expanded.setVisibility(View.VISIBLE);
    }

    private void setInput(ViewHolder holder, Task task) {
        holder.cName.setText("Task Name");
        holder.cDuration.setText("Duration");
        holder.eName.setText("");
        holder.eDuration.setText("");

        holder.cName.setTextColor(Color.LTGRAY);
        holder.cDuration.setTextColor(Color.LTGRAY);

        holder.collapsed.setVisibility(View.VISIBLE);
        holder.expanded.setVisibility(View.GONE);
    }

    private MethodCaller listener;

    interface MethodCaller {
        void addTask();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    void remove(int position) {
        tasks.remove(position).deleteFromDb();
        notifyItemRemoved(position);
    }

    void swap(int firstPosition, int secondPosition, ViewHolder holder) {

        Task task1 = tasks.get(firstPosition);
        Task task2 = tasks.get(secondPosition);

        boolean task1IsExpanded = task1.getItemType().equals("expanded");
        boolean task2IsExpanded = task2.getItemType().equals("expanded");

        if (task1IsExpanded || task2IsExpanded)
            holder.stopTimer();

        long id1 = task1.getId();
        long id2 = task2.getId();

        task1.setId(id2);
        task2.setId(id1);

        task1.updateDb();
        task2.updateDb();

        Collections.swap(tasks, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);

        if (task1IsExpanded)
            holder.startTimer(task2);
        if (task2IsExpanded)
            holder.startTimer(task1);
    }

}
