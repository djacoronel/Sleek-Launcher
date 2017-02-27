package com.djacoronel.basiclauncher;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class TabFragment1 extends Fragment implements ListAdapter.MethodCaller{

    private ArrayList<Task> tasks;
    View rootView;
    Context mContext;
    RecyclerView list;
    ListAdapter adapter;

    public TabFragment1(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_fragment1, container, false);
        mContext  = getActivity();

        list = (RecyclerView) rootView.findViewById(R.id.task_list);
        loadTaskList();


        return rootView;
    }

    void loadTaskList(){
        DbHelper dbHelper = new DbHelper(mContext);
        tasks = dbHelper.getTasks(mContext);
        tasks.add(new Task("input"));
        adapter = new ListAdapter(tasks, this, mContext);

        list.setLayoutManager(new LinearLayoutManager(mContext));
        list.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new TasksTouchHelper(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(list);
    }

    @Override
    public void addTask() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mContext);
        final View mView = layoutInflaterAndroid.inflate(R.layout.input_layout, null);

        final EditText name = (EditText) mView.findViewById(R.id.itemNameIn);
        final TextView duration = (TextView) mView.findViewById(R.id.itemDurationIn);
        FloatingActionButton button = (FloatingActionButton) mView.findViewById(R.id.input_button);

        final AlertDialog addTaskDialog = new AlertDialog
                .Builder(mView.getContext())
                .setView(mView)
                .setCancelable(true)
                .create();

        addTaskDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        addTaskDialog.show();

        addTaskDialog.setOnDismissListener(
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        tasks.add(new Task("input"));
                        adapter.notifyItemInserted(tasks.size());
                    }
                }
        );

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Task task = new Task(
                        name.getText().toString(),
                        duration.getText().toString(),
                        "pending",
                        mContext);

                task.addToDb();
                tasks.add(task);
                name.setText("");
                duration.setText("Duration");
                adapter.notifyItemInserted(tasks.size());
                addTaskDialog.dismiss();
            }
        });




    }
}
