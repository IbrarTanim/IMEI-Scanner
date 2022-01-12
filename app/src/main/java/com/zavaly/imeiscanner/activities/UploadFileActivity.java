package com.zavaly.imeiscanner.activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.opencsv.CSVReader;
import com.zavaly.imeiscanner.R;
import com.zavaly.imeiscanner.dto.AppRoomDatabase;
import com.zavaly.imeiscanner.dto.entities.ImeiCache;
import com.zavaly.imeiscanner.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class UploadFileActivity extends AppCompatActivity {

    private UploadFileActivity activity;

    //File Upload Requests
    private ActivityResultLauncher<String[]> commonResultLauncher;
    private ActivityResultLauncher<Intent> fileResultLauncher;

    private List<String> imeiList = new ArrayList<>();
    private AppRoomDatabase appRoomDatabase;

    private MaterialButton uploadFileBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_file);
        activity = this;
        uploadFileBTN =
                findViewById(R.id.upload_file_btn);
        appRoomDatabase =
                AppRoomDatabase.getINSTANCE(activity);

        //result launcher
        commonResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {

                selectCSVFile();

            }
        });

        fileResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK){

                    Uri contentUri = result.getData().getData();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){

                        saveFileInternal(contentUri);

                    }else {

                        String filepath = FileUtils.getPath(activity, contentUri);

                        readFromCSV(filepath);

                    }



                }else {

                    Toasty.info(activity, "Failed to upload file.").show();

                }

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        uploadFileBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dialog = new Dialog(activity);
                dialog.setContentView(R.layout.dialog_layout);
                TextView warningText = dialog.findViewById(R.id.warning_text);
                warningText.setText(getResources().getString(R.string.upload_warning_text));
                MaterialButton enterBtn = dialog.findViewById(R.id.yes_btn);
                MaterialButton cancelBtn = dialog.findViewById(R.id.no_btn);
                LinearLayout btnLayout = dialog.findViewById(R.id.btn_layout);

                if (btnLayout.getVisibility() == View.GONE){

                    btnLayout.setVisibility(View.VISIBLE);

                }

                enterBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        selectCSVFile();

                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

    }

    private void selectCSVFile() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                String[] trackerPerms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION};
                commonResultLauncher.launch(trackerPerms);
            } else {
                String[] trackerPerms = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                commonResultLauncher.launch(trackerPerms);
            }

        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            fileResultLauncher.launch(Intent.createChooser(intent, "Open CSV File"));
        }
    }


    private void readFromCSV(String filePath){

        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String[] line;
        int counter = 0;


        try {
            while ((line = csvReader.readNext()) != null) {

                counter = counter + 1;

                if (counter == 1) {

                } else {
                    //String numbers[] = line.split(",");

                    String a = line[0];
                    imeiList.add(a);
                    /*String b = line[1];
                    System.out.println(a + " ---  " + b);*/
                }

            }

            if (imeiList != null){

                appRoomDatabase.imeiCacheDao().clearPreviousData();

                for (String imei : imeiList){

                    ImeiCache imeiCache = new ImeiCache(imei, false, false);
                    appRoomDatabase.imeiCacheDao().insert(imeiCache);

                }

                Toasty.success(activity, "Cached all data.").show();

                Intent intentHome = new Intent(activity, MainActivity.class);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intentHome.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentHome);

            }
        } catch (Exception ioException) {

            Toasty.error(activity, "Upload file failed.").show();
            //Log.e("Error---", ioException.getMessage());

        }

    }

    private void saveFileInternal(Uri uri){

        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(folder, "IMEI_" + System.currentTimeMillis() + ".csv");

        InputStream in = null;
        try {
            in = getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        byte[] buf = new byte[1024];

        try {
            int len;

            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();

            in.close();

            readFromCSV(file.getAbsolutePath());
        }catch (Exception e){

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