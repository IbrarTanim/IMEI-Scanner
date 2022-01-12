package com.zavaly.imeiscanner.dto;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.zavaly.imeiscanner.dto.daos.ImeiCacheDao;
import com.zavaly.imeiscanner.dto.entities.ImeiCache;

@Database(entities = {ImeiCache.class}, version = 3, exportSchema = false)
public abstract class AppRoomDatabase extends RoomDatabase {

    private static volatile AppRoomDatabase INSTANCE;

    public static AppRoomDatabase getINSTANCE(Context context) {
        if (INSTANCE == null) {
            synchronized ((AppRoomDatabase.class)) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppRoomDatabase.class, "app_rrom_db").fallbackToDestructiveMigration().allowMainThreadQueries().build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ImeiCacheDao imeiCacheDao();
}
