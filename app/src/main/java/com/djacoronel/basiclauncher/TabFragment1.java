package com.djacoronel.basiclauncher;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.util.ArrayList;

public class TabFragment1 extends Fragment implements ListAdapter.MethodCaller {

    private ArrayList<Task> tasks;
    View rootView;
    Context mContext;
    RecyclerView list;
    ListAdapter adapter;

    public TabFragment1() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.tab_fragment1, container, false);
        mContext = getActivity();

        list = (RecyclerView) rootView.findViewById(R.id.task_list);
        loadTaskList();


        return rootView;
    }

    void loadTaskList() {
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


    String duration, m = "5 minutes", h = "0 hours";

    @Override
    public void addTask() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mContext);
        final View mView = layoutInflaterAndroid.inflate(R.layout.input_layout, null);

        final EditText name = (EditText) mView.findViewById(R.id.itemNameIn);
        final NumberPicker hour = (NumberPicker) mView.findViewById(R.id.hourPicker);
        final NumberPicker min = (NumberPicker) mView.findViewById(R.id.minutePicker);
        FloatingActionButton button = (FloatingActionButton) mView.findViewById(R.id.input_button);

        final String hours[] = {"0 hours", "1 hour", "2 hours", "3 hours", "4 hours", "5 hours"};
        final String mins[] = {"5 minutes", "10 minutes", "15 minutes",
                "20 minutes", "25 minutes", "30 minutes", "35 minutes",
                "40 minutes", "45 minutes", "50 minutes", "55 minutes"};

        hour.setMinValue(0);
        hour.setMaxValue(hours.length - 1);
        min.setMinValue(0);
        min.setMaxValue(mins.length - 1);

        hour.setDisplayedValues(hours);
        min.setDisplayedValues(mins);

        hour.setWrapSelectorWheel(true);
        min.setWrapSelectorWheel(true);

        setDividerColor(hour, Color.TRANSPARENT);
        setDividerColor(min, Color.TRANSPARENT);

        hour.setOnValueChangedListener(
                new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        h = hours[newVal];
                    }
                }
        );
        min.setOnValueChangedListener(
                new NumberPicker.OnValueChangeListener() {
                    @Override
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        m = mins[newVal];
                    }
                }
        );

        final AlertDialog addTaskDialog = new AlertDialog
                .Builder(mView.getContext())
                .setView(mView)
                .setCancelable(true)
                .create();

        addTaskDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        addTaskDialog.show();
        addTaskDialog.getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addTaskDialog.setOnDismissListener(
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        tasks.add(new Task("input"));
                        adapter.notifyItemInserted(tasks.size());

                        m = "5 minutes";
                        h = "0 hours";
                    }
                }
        );


        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (h.equals("0 hours"))
                            duration = m;
                        else
                            duration = h + " " + m;

                        Task task = new Task(
                                name.getText().toString(),
                                duration,
                                "pending",
                                mContext);

                        task.addToDb();
                        tasks.add(task);
                        name.setText("");
                        adapter.notifyItemInserted(tasks.size());
                        addTaskDialog.dismiss();
                    }
                });

    }

    private void setDividerColor(NumberPicker picker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
