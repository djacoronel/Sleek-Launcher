package com.djacoronel.basiclauncher;

import android.content.Context;

class Task {
    private String name, duration, status, itemType = "normal";
    private long id;
    private DbHelper dbHelper;

    Task(String itemType) {
        this.itemType = itemType;
    }

    Task(String name, String duration, String status, Context context) {
        this.name = name;
        this.duration = duration;
        this.status = status;
        dbHelper = new DbHelper(context);
    }

    public void addToDb() {
        this.id = dbHelper.addTask(this);
    }

    public void updateDb() {
        dbHelper.updateTask(this);
    }

    public void deleteFromDb() {
        dbHelper.deleteTask(id);
    }

    public long getDurationValue() {
        String split[] = duration.split(" ");

        if (split.length == 2 && split[1].contains("minutes"))
            return Integer.parseInt(split[0]) * 60000;
        else if (split.length == 2 && split[1].contains("hour"))
            return Integer.parseInt(split[0]) * 3600000;
        else
            return Integer.parseInt(split[0]) * 3600000 + Integer.parseInt(split[2]) * 60000;

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
