package com.unipi.mobile.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.unipi.mobile.entities.CameraEntry;

@Database(entities = {CameraEntry.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class CameraDataBase extends RoomDatabase {

    public abstract CameraEntryDao cameraEntryDao();

    private static CameraDataBase INSTANCE;

    public static CameraDataBase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), CameraDataBase.class, "camera_entries")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

}
