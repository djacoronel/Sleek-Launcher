package com.djacoronel.basiclauncher;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class TasksTouchHelper extends ItemTouchHelper.SimpleCallback {
    private ListAdapter listAdapter;

    public TasksTouchHelper(ListAdapter listAdapter){
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.listAdapter = listAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        listAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listAdapter.remove(viewHolder.getAdapterPosition());
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder holder) {
        int position = holder.getAdapterPosition();
        return !listAdapter.tasks.get(position).getItemType().equals("normal")? 0 : super.getSwipeDirs(recyclerView, holder);
    }
}