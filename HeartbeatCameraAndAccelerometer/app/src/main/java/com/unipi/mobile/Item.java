package com.unipi.mobile;

import android.content.Context;

public class Item {

    private final String time;
    private final String type;
    private final String bpm;
    private final Integer year;
    private final Integer month;
    private final Integer day;
    private Context context;

    public Item(String time, String type, String bpm, Integer year, Integer month, Integer day, Context context) {
        this.time = time;
        this.type = type;
        this.bpm = bpm;
        this.year = year;
        this.month = month;
        this.day = day;
        this.context = context;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getBpm() {
        return bpm;
    }

    public Integer getYear() {
        return year;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getDay() {
        return day;
    }

    public Context getContext() {return context;}

}
