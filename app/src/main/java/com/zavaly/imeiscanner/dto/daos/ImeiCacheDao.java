package com.zavaly.imeiscanner.dto.daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.zavaly.imeiscanner.dto.entities.ImeiCache;

import java.util.List;

@Dao
public interface ImeiCacheDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ImeiCache imeiCache);

    @Query("SELECT * FROM imei_cache WHERE is_sms_got = :isSmsGot")
    List<ImeiCache> getImeiList(boolean isSmsGot);

    @Query("UPDATE imei_cache SET is_sms_got = :isSmsGot, sms_result = :smsResult WHERE imei_number = :imeiNumber")
    void updateDB(boolean isSmsGot, boolean smsResult, String imeiNumber);

    @Query("SELECT * FROM imei_cache WHERE is_sms_got = :isSmsGot AND sms_result = :smsResult")
    List<ImeiCache> getImeiListResults(boolean isSmsGot, boolean smsResult);

    @Query("DELETE FROM imei_cache")
    void clearPreviousData();
}
