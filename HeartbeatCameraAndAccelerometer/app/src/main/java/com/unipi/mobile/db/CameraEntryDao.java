package com.unipi.mobile.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.unipi.mobile.entities.CameraEntry;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface CameraEntryDao {

    @Query("SELECT * FROM CameraEntry")
    List<CameraEntry> getAll();

    @Query("SELECT * FROM CameraEntry WHERE dateTime BETWEEN :dayStart AND :dayEnd")
    List<CameraEntry> getByDate(LocalDateTime dayStart, LocalDateTime dayEnd);

    @Query("DELETE FROM CameraEntry")
    void deleteAll();

    @Insert
    void insertAll(CameraEntry ...cameraEntries);

    @Query("DELETE FROM CameraEntry WHERE dateTime = :day")
    void deleteByDate(LocalDateTime day);

}
