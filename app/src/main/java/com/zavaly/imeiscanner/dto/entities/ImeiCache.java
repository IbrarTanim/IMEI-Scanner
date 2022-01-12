package com.zavaly.imeiscanner.dto.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "imei_cache")
public class ImeiCache {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "imei_number")
    String imeiNumber;

    @ColumnInfo(name = "is_sms_got")
    boolean isSmsGot;

    @ColumnInfo(name = "sms_result")
    boolean smsResult;

    public ImeiCache(@NonNull String imeiNumber, boolean isSmsGot, boolean smsResult) {
        this.imeiNumber = imeiNumber;
        this.isSmsGot = isSmsGot;
        this.smsResult = smsResult;
    }

    @NonNull
    public String getImeiNumber() {
        return imeiNumber;
    }

    public void setImeiNumber(@NonNull String imeiNumber) {
        this.imeiNumber = imeiNumber;
    }

    public boolean isSmsGot() {
        return isSmsGot;
    }

    public void setSmsGot(boolean smsGot) {
        isSmsGot = smsGot;
    }

    public boolean isSmsResult() {
        return smsResult;
    }

    public void setSmsResult(boolean smsResult) {
        this.smsResult = smsResult;
    }
}
