package com.djacoronel.basiclauncher;

import android.content.Context;

class Task {
    private String name, duration, status, itemType = "normal";
    private long id, timeRemaining;
    private DbHelper dbHelper;

    Task(String itemType) {
        this.itemType = itemType;
    }

    Task(String name, String duration, String status, Context context) {
        this.name = name;
        this.duration = duration;
        this.status = status;
        timeRemaining = getDurationValue();
        dbHelper = new DbHelper(context);
    }

    void addToDb() {
        this.id = dbHelper.addTask(this);
    }

    void updateDb() {
        dbHelper.updateTask(this);
    }

    void deleteFromDb() {
        dbHelper.deleteTask(id);
    }

    private long getDurationValue() {
        String split[] = duration.split(" ");

        if (split.length == 2 && split[1].contains("minutes"))
            return Integer.parseInt(split[0]) * 60000;
        else if (split.length == 2 && split[1].contains("hour"))
            return Integer.parseInt(split[0]) * 3600000;
        else
            return Integer.parseInt(split[0]) * 3600000 + Integer.parseInt(split[2]) * 60000;

    }

    void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    long getTimeRemaining() {
        return timeRemaining;
    }

    long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    String getDuration() {
        return duration;
    }

    void setDuration(String duration) {
        this.duration = duration;
    }

    String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }

    String getItemType() {
        return itemType;
    }

    void setItemType(String itemType) {
        this.itemType = itemType;
    }
}
