package com.djacoronel.basiclauncher;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

class TasksTouchHelper extends ItemTouchHelper.SimpleCallback {
    private ListAdapter listAdapter;

    TasksTouchHelper(ListAdapter listAdapter) {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT);
        this.listAdapter = listAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if(viewHolder.getAdapterPosition()!=listAdapter.tasks.size()-1
                && target.getAdapterPosition()!=listAdapter.tasks.size()-1){
            listAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        } else {
            return false;
        }
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