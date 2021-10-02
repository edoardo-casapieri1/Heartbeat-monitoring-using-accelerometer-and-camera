package com.unipi.mobile.db;

import com.unipi.mobile.entities.SCGEntry;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface SCGEntryDao {

    @Query("SELECT * FROM SCGEntry")
    List<SCGEntry> getAll();

    @Query("SELECT * FROM SCGEntry WHERE dateTime BETWEEN :dayStart AND :dayEnd")
    List<SCGEntry> getByDate(LocalDateTime dayStart, LocalDateTime dayEnd);

    @Query("DELETE FROM SCGEntry")
    void deleteAll();

    @Query("DELETE FROM SCGEntry WHERE dateTime = :day")
    void deleteByDate(LocalDateTime day);

    @Insert
    void insertAll(SCGEntry ...scgEntries);

    @Delete
    void delete(SCGEntry cameraEntry);
}
