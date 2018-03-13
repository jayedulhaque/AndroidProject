package com.example.asus.smartattendance;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class FacultyActivity extends AppCompatActivity {
    Button btnImport;
    Button btnSelectFile;
    EditText etSelectFile;
    TextView tView;
    static String filePath = "No File Selected";
    String demo = "";
    BroadcastReceiver receiver;
    BluetoothAdapter bluetoothAdapter;
    IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty);
        btnImport = (Button) findViewById(R.id.btnImport);
        btnSelectFile = (Button) findViewById(R.id.btnSelectFile);
        etSelectFile = (EditText) findViewById(R.id.etFileLocation);
        tView = (TextView) findViewById(R.id.tvDemo);
        etSelectFile.setText(filePath);
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    if (bluetoothAdapter.getState() == bluetoothAdapter.STATE_ON) {
                        Toast.makeText(context, "Bluetooth mode on", Toast.LENGTH_LONG).show();
                        Intent intent1 = new Intent(FacultyActivity.this, MyService.class);
                        startService(intent1);

                    } else if (bluetoothAdapter.getState() == bluetoothAdapter.STATE_OFF) {
                        Toast.makeText(context, "Bluetoth must be enabled to continue", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FacultyActivity.this, FileChooser.class);
                startActivity(intent);
            }
        });
        btnImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File fileDirectory = new File(Environment.getExternalStorageDirectory(), "Attendance");
                if (etSelectFile.getText() == null || etSelectFile.getText().toString().equals("") || etSelectFile.getText().toString().equals("No File Selected")) {
                    tView.setText("Please select a file to import");
                } else {
                    if (fileDirectory.exists()) {
                        tView.setText("Already Imported");
                    } else {
                        if (!fileDirectory.mkdirs()) {
                            tView.setText("file not imported");
                        } else {

                            File source = new File(etSelectFile.getText().toString());
                            File destination = new File(fileDirectory.getPath(), "attendance.xls");
                            try {
                                FileUtils.copyFile(source, destination);
                            } catch (IOException e) {
                                tView.setText("not copied");
                                e.printStackTrace();
                            }
                            tView.setText("file imported");

                        }
                    }
                }
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
