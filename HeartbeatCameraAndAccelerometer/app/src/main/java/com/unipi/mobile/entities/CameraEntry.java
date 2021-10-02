package com.unipi.mobile.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity
public class CameraEntry {

    public CameraEntry(LocalDateTime dateTime, int bpm) {
        this.dateTime = dateTime;
        this.bpm = bpm;
    }

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "dateTime")
    public LocalDateTime dateTime;

    @ColumnInfo(name = "bpm")
    public int bpm;

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public int getBpm() {
        return bpm;
    }

}
