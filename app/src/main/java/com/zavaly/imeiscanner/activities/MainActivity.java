package com.zavaly.imeiscanner.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.zavaly.imeiscanner.R;
import com.zavaly.imeiscanner.dto.AppRoomDatabase;
import com.zavaly.imeiscanner.dto.entities.ImeiCache;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MainActivity activity;
    private List<ImeiCache> foundList = new ArrayList<>();
    private List<ImeiCache> notFoundList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        foundList = AppRoomDatabase.getINSTANCE(activity).imeiCacheDao().getImeiListResults(true, true);
        notFoundList = AppRoomDatabase.getINSTANCE(activity).imeiCacheDao().getImeiListResults(true, false);

        MaterialButton uploadFileBTN = findViewById(R.id.upload_file_btn);
        MaterialButton checkIMEIBTN = findViewById(R.id.check_imei_btn);
        MaterialButton imeiFoundBTN = findViewById(R.id.see_imei_found_btn);
        MaterialButton imeiNotFoundBTN = findViewById(R.id.see_imei_not_btn);

        uploadFileBTN.setOnClickListener(activity);
        checkIMEIBTN.setOnClickListener(activity);
        imeiFoundBTN.setOnClickListener(activity);
        imeiNotFoundBTN.setOnClickListener(activity);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.upload_file_btn:
                Intent intentFile = new Intent(activity, UploadFileActivity.class);
                intentFile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intentFile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentFile);
                break;
            case R.id.check_imei_btn:
                Intent intentImei = new Intent(activity, CheckIMEIActivity.class);
                intentImei.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intentImei.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentImei);
                break;
            case R.id.see_imei_found_btn:
                if (!foundList.isEmpty()){
                    Intent imeiFoundIntent = new Intent(activity, IMEIFoundActivity.class);
                    imeiFoundIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    imeiFoundIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(imeiFoundIntent);
                }else {

                    Toasty.warning(activity, "Check IMEI first.").show();

                }

                break;
            case R.id.see_imei_not_btn:
                if (!notFoundList.isEmpty()){
                    Intent imeiNotFountIntent = new Intent(activity, ImeiNotFoundActivity.class);
                    imeiNotFountIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    imeiNotFountIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(imeiNotFountIntent);
                }else {

                    Toasty.warning(activity, "Check IMEI first.").show();

                }

                break;
            default:
                break;

        }

    }
}