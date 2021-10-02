package com.unipi.mobile.db;

import android.content.Context;

import com.unipi.mobile.entities.SCGEntry;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {SCGEntry.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class SCGDataBase extends RoomDatabase {

    public abstract SCGEntryDao scgEntryDao();

    private static SCGDataBase INSTANCE;

    public static SCGDataBase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SCGDataBase.class, "scg_entries")
                    .allowMainThreadQueries()
                    .build();
        }
        return INSTANCE;
    }

}
