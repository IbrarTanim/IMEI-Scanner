package com.zavaly.imeiscanner.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    Context context;
    SharedPreferences sharedPreferences;

    public PrefManager(Context context) {
        this.context = context;
    }

    public void saveReceivedIMEI(String imei) {
        sharedPreferences = context.getSharedPreferences("ImeiInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("imei", imei);
        editor.apply();
    }


    public String getIMEI() {
        sharedPreferences = context.getSharedPreferences("ImeiInfo", Context.MODE_PRIVATE);
        return sharedPreferences.getString("imei", "null");
    }
}
