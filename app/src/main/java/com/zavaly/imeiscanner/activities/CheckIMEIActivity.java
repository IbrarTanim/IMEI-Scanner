package com.zavaly.imeiscanner.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.zavaly.imeiscanner.R;
import com.zavaly.imeiscanner.adapters.CheckRecyclerAdapter;
import com.zavaly.imeiscanner.dto.AppRoomDatabase;
import com.zavaly.imeiscanner.dto.entities.ImeiCache;
import com.zavaly.imeiscanner.utils.CheckImeiUtil;
import com.zavaly.imeiscanner.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class CheckIMEIActivity extends AppCompatActivity {

    private static final String DESTINATION_ADDRESS = "16002";
    private CheckIMEIActivity activity;
    private AppRoomDatabase appRoomDatabase;

    //View Variable
    private MaterialButton startCheckingBTN, stopCheckingBTN;
    private MaterialTextView totalImeiTV, checkTV;
    private RecyclerView checkedRV;
    private ProgressBar progressBar;

    //variable
    private List<ImeiCache> imeiList;
    private int checkedCounter = 0;
    private int loopCounter = 0;
    private int totalIMEI = 0;
    List<String> imeiStringList;
    List<ImeiCache> imeiCacheList;

    //launcher
    private ActivityResultLauncher<String[]> commonResultLauncher;

    //pref manager
    private PrefManager prefManager;

    //timer
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_imeiactivity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        activity = this;
        imeiList =
                new ArrayList<>();
        imeiStringList =
                new ArrayList<>();
        imeiCacheList =
                new ArrayList<>();
        appRoomDatabase =
                AppRoomDatabase.getINSTANCE(activity);
        prefManager =
                new PrefManager(activity);
        prefManager.saveReceivedIMEI("");
        commonResultLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {

                        stopTimer();
                        looper();

                    }
                });

        //initialize
        startCheckingBTN = findViewById(R.id.start_checking_btn);
        stopCheckingBTN = findViewById(R.id.stop_checking_btn);
        totalImeiTV = findViewById(R.id.total_imei_tv);
        checkTV = findViewById(R.id.checked_tv);
        checkedRV = findViewById(R.id.check_rv);
        progressBar = findViewById(R.id.progressBar);

    }

    @Override
    protected void onResume() {
        super.onResume();

        imeiList = appRoomDatabase.imeiCacheDao().getImeiList(false);

        if (imeiList.isEmpty()) {

            startCheckingBTN.setEnabled(false);

        } else {

            startCheckingBTN.setEnabled(true);

            int listSize = imeiList.size();
            totalIMEI = listSize;
            totalImeiTV.setText(String.valueOf(totalIMEI));

        }

        startCheckingBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                looper();

            }
        });

        stopCheckingBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stopTimer();

                if (stopCheckingBTN.getVisibility() == View.VISIBLE){

                    stopCheckingBTN.setVisibility(View.GONE);

                }

                if (startCheckingBTN.getVisibility() == View.GONE){

                    startCheckingBTN.setVisibility(View.VISIBLE);

                }

                if (progressBar.getVisibility() == View.VISIBLE){

                    progressBar.setVisibility(View.GONE);


                }

                Intent intentHome = new Intent(activity, MainActivity.class);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentHome);

            }
        });

    }

    private void sendSMS(String imei) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            String[] trackerPerms = new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS};
            commonResultLauncher.launch(trackerPerms);
        } else {
            String SMS_MESSAGE = String.format("KYD %s", imei);
            String SERVICE_CENTER_ADDRESS = null;

            PendingIntent sentIntent = null, deliveryIntent = null;
            // Use SmsManager.
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage
                    (DESTINATION_ADDRESS, SERVICE_CENTER_ADDRESS, SMS_MESSAGE,
                            sentIntent, deliveryIntent);

            //Toast.makeText(this, "SMS Sent for " + imei, Toast.LENGTH_SHORT).show();

            if (stopCheckingBTN.getVisibility() == View.GONE){

                stopCheckingBTN.setVisibility(View.VISIBLE);

            }

            if (progressBar.getVisibility() == View.GONE){

                progressBar.setVisibility(View.VISIBLE);


            }

            if (startCheckingBTN.getVisibility() == View.VISIBLE){

                startCheckingBTN.setVisibility(View.GONE);

            }

            loopCounter = loopCounter + 1;
        }
    }


    private void looper() {

        if (imeiList != null) {


            if (loopCounter < imeiList.size()) {

                CheckImeiUtil.PRESENT_IMEI = imeiList.get(loopCounter).getImeiNumber();


                sendSMS(CheckImeiUtil.PRESENT_IMEI);


                timer = new CountDownTimer(2000, 1000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {

                        if (prefManager.getIMEI().equals(CheckImeiUtil.PRESENT_IMEI)) {

                            if (loopCounter == imeiList.size()){
                                CheckImeiUtil.PREVIOUS_IMEI = CheckImeiUtil.PRESENT_IMEI;
                                checkedCounter = checkedCounter + 1;
                                checkTV.setText(String.valueOf(checkedCounter));
                                totalIMEI = totalIMEI - 1;
                                totalImeiTV.setText(String.valueOf(totalIMEI));
                                imeiStringList.add(prefManager.getIMEI());

                                String checkingMessage = CheckImeiUtil.PRESENT_IMEI_MESSAGE;
                                //Log.d("Checking-----", String.valueOf(checkingMessage.length()));

                                if (checkingMessage.length() < 60){

                                    //Toasty.success(activity, "IMEI Found").show();
                                    //update db
                                    ImeiCache imeiCache = new ImeiCache(prefManager.getIMEI(), true, true);
                                    imeiCacheList.add(imeiCache);
                                    appRoomDatabase.imeiCacheDao().updateDB(true, true, prefManager.getIMEI());

                                }else {

                                    //Toasty.error(activity, "IMEI Not Found").show();
                                    //update db
                                    ImeiCache imeiCache = new ImeiCache(prefManager.getIMEI(), true, false);
                                    imeiCacheList.add(imeiCache);
                                    appRoomDatabase.imeiCacheDao().updateDB(true, false, prefManager.getIMEI());


                                }


                                if (!imeiCacheList.isEmpty()){
                                    CheckRecyclerAdapter adapter = new CheckRecyclerAdapter(activity, imeiCacheList);
                                    LinearLayoutManager manager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
                                    checkedRV.setLayoutManager(manager);
                                    checkedRV.setAdapter(adapter);
                                }

                                Toasty.success(activity, "Finished checking!", Toast.LENGTH_SHORT).show();

                                stopTimer();

                                if (stopCheckingBTN.getVisibility() == View.VISIBLE){

                                    stopCheckingBTN.setVisibility(View.GONE);

                                }

                                if (startCheckingBTN.getVisibility() == View.GONE){

                                    startCheckingBTN.setVisibility(View.VISIBLE);

                                }

                                if (progressBar.getVisibility() == View.VISIBLE){

                                    progressBar.setVisibility(View.GONE);

                                }

                            }else {

                                CheckImeiUtil.PREVIOUS_IMEI = CheckImeiUtil.PRESENT_IMEI;
                                checkedCounter = checkedCounter + 1;
                                checkTV.setText(String.valueOf(checkedCounter));
                                totalIMEI = totalIMEI - 1;
                                totalImeiTV.setText(String.valueOf(totalIMEI));
                                imeiStringList.add(prefManager.getIMEI());

                                String checkingMessage = CheckImeiUtil.PRESENT_IMEI_MESSAGE;
                                //Log.e("Checking-----", checkingMessage);
                                //Log.d("Checking-----", String.valueOf(checkingMessage.length()));
                                if (checkingMessage.length() < 60){

                                    //Toasty.success(activity, "IMEI Found").show();
                                    //update db
                                    ImeiCache imeiCache = new ImeiCache(prefManager.getIMEI(), true, true);
                                    imeiCacheList.add(imeiCache);
                                    appRoomDatabase.imeiCacheDao().updateDB(true, true, prefManager.getIMEI());

                                }else {

                                    //Toasty.error(activity, "IMEI Not Found").show();
                                    //update db
                                    ImeiCache imeiCache = new ImeiCache(prefManager.getIMEI(), true, false);
                                    imeiCacheList.add(imeiCache);
                                    appRoomDatabase.imeiCacheDao().updateDB(true, false, prefManager.getIMEI());

                                }

                                if (!imeiCacheList.isEmpty()){
                                    CheckRecyclerAdapter adapter = new CheckRecyclerAdapter(activity, imeiCacheList);
                                    LinearLayoutManager manager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
                                    checkedRV.setLayoutManager(manager);
                                    checkedRV.setAdapter(adapter);
                                }
                                looper();
                            }


                        } else {

                            timer.start();

                        }

                    }
                }.start();


            } else {

                Toasty.info(this, "Finished Checking.", Toast.LENGTH_SHORT).show();

            }


        }

    }

    private void stopTimer(){
        if (timer != null){
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopTimer();

        if (stopCheckingBTN.getVisibility() == View.VISIBLE){

            stopCheckingBTN.setVisibility(View.GONE);

        }

        if (startCheckingBTN.getVisibility() == View.GONE){

            startCheckingBTN.setVisibility(View.VISIBLE);

        }

        if (progressBar.getVisibility() == View.VISIBLE){

            progressBar.setVisibility(View.GONE);


        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTimer();

        if (stopCheckingBTN.getVisibility() == View.VISIBLE){

            stopCheckingBTN.setVisibility(View.GONE);

        }

        if (startCheckingBTN.getVisibility() == View.GONE){

            startCheckingBTN.setVisibility(View.VISIBLE);

        }

        if (progressBar.getVisibility() == View.VISIBLE){

            progressBar.setVisibility(View.GONE);


        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intentHome = new Intent(activity, MainActivity.class);
        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentHome);

    }
}