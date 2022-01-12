package com.zavaly.imeiscanner.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.zavaly.imeiscanner.R;
import com.zavaly.imeiscanner.adapters.ResultRecyclerAdapter;
import com.zavaly.imeiscanner.dto.AppRoomDatabase;
import com.zavaly.imeiscanner.dto.entities.ImeiCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class IMEIFoundActivity extends AppCompatActivity {

    private IMEIFoundActivity activity;
    private AppRoomDatabase appRoomDatabase;
    private List<ImeiCache> imeiList;
    private RecyclerView resultRV;
    private AppCompatImageButton downloadFoundList;

    //File Upload Requests
    private ActivityResultLauncher<String[]> commonResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_imei);
        activity = this;

        //result launcher
        commonResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {



            }
        });

        appRoomDatabase =
                AppRoomDatabase.getINSTANCE(activity);

        imeiList = appRoomDatabase.imeiCacheDao().getImeiListResults(true, true);

        if (imeiList != null) {

            resultRV = findViewById(R.id.imei_found_rv);
            downloadFoundList = findViewById(R.id.download_found_list);

            ResultRecyclerAdapter adapter = new ResultRecyclerAdapter(activity, imeiList);
            LinearLayoutManager manager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
            resultRV.setLayoutManager(manager);
            resultRV.setAdapter(adapter);

            downloadFoundList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    downloadPDF();

                }
            });

        }
    }

    private void downloadPDF() {
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

            createPDF();

        }
    }

    private void createPDF() {

        Document doc = new Document();
        try {

            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(folder, "IMEI_Found_" + System.currentTimeMillis() + ".pdf");

            FileOutputStream fOut = new FileOutputStream(file);
            PdfWriter writer = PdfWriter.getInstance(doc, fOut);

            //open the document
            doc.open();

            //create table
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{100});

            PdfPCell headerCell = new PdfPCell(new Phrase("এগুলো বিটিআরসি ডাটাবেসে পাওয়া গেছে"));
            table.addCell(headerCell);

            for (ImeiCache cache : imeiList){

                PdfPCell cell = new PdfPCell(new Phrase(cache.getImeiNumber()));
                table.addCell(cell);

            }

            doc.add(table);

        } catch(DocumentException | IOException de) {

        } finally {
            doc.close();

            Dialog dialog = new Dialog(activity);
            dialog.setContentView(R.layout.dialog_layout);
            TextView warningText = dialog.findViewById(R.id.warning_text);
            warningText.setText(getResources().getString(R.string.downloaded_warning_text));
            MaterialButton closeBTN = dialog.findViewById(R.id.cancel_btn);

            if (closeBTN.getVisibility() == View.GONE){

                closeBTN.setVisibility(View.VISIBLE);

            }

            closeBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    dialog.dismiss();

                }
            });

            dialog.show();
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