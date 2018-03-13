package com.example.asus.smartattendance;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.NumberToTextConverter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.sql.Timestamp;
import java.util.Date;;


public class MyService extends Service {
    boolean isRunning = false;
    Thread backgroundThread;
    final String filePath = "/storage/sdcard0/Attendance/attendance.xls";
    ArrayList<DeviceInfo> idList;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<String> pairedDevices;
    ArrayList<BluetoothDevice> devices;
    IntentFilter filter;
    BroadcastReceiver receiver;
    File file;

    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        backgroundThread.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
//        isRunning = false;
//        backgroundThread.stop();
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        inIt();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "No Bluetooth Detected", Toast.LENGTH_LONG).show();
        }
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private Runnable myTask = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                startDiscovery();
                if (!idList.isEmpty()) {
                    setMacAddress();
                    giveAttendance();
                }
            }
        }
    };

    private void inIt() {
        try {
            idList = new ArrayList<DeviceInfo>();
            file = new File(filePath);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            pairedDevices = new ArrayList<String>();
            devices = new ArrayList<BluetoothDevice>();
            backgroundThread = new Thread(myTask);
            filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        DeviceInfo deviceInfo = new DeviceInfo();
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        devices.add(device);
                        deviceInfo.setName(device.getName());
                        deviceInfo.setAdress(device.getAddress());
                        idList.add(deviceInfo);
                    } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                    } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                    }
                }
            };
            registerReceiver(receiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            registerReceiver(receiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, filter);
        } catch (Exception e) {

        }
    }

    private void startDiscovery() {
        bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery();
    }


    private void setMacAddress() {
        FileOutputStream os = null;
        try {

            FileInputStream myInput;
            POIFSFileSystem myFileSystem;
            HSSFWorkbook myWorkBook;
            HSSFSheet mySheet;
            if (!file.exists()) {
                Toast.makeText(getApplicationContext(), "Directory or File not Found Shutting down program", Toast.LENGTH_LONG)
                        .show();
            }
            myInput = new FileInputStream(file);
            myFileSystem = new POIFSFileSystem(myInput);
            myWorkBook = new HSSFWorkbook(myFileSystem);
            os = new FileOutputStream(file);
            mySheet = myWorkBook.getSheetAt(0);
            Iterator rowIter = mySheet.rowIterator();
            while (rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                if (myRow.getRowNum() == 0) {
                    HSSFCell c = myRow.getCell(3);
                    CellStyle cs = myWorkBook.createCellStyle();
                    cs.setFillForegroundColor(HSSFColor.LIME.index);
                    cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                    if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK) {
                        c = myRow.createCell(3);
                        c.setCellValue("MacAddress");
                        continue;
                    } else
                        continue;
                }
                HSSFCell myCell = myRow.getCell(2);
                HSSFCell c = myRow.getCell(3);
                String s = "";
                if (myCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                    s = NumberToTextConverter.toText(myCell.getNumericCellValue());
                }
                if (myCell.getCellType() == Cell.CELL_TYPE_STRING) {
                    s = myCell.toString().trim();
                }
                if (s.equals("")) {
                    continue;
                }

                for (DeviceInfo dinfo : idList) {
                    if (dinfo.getName() != null) {
                        if (dinfo.getName().equals(s)) {
                            if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK) {
                                c = myRow.createCell(3);
                                c.setCellValue(dinfo.getAdress());
                                break;
                            }
                        }
                    }
                }

            }
            myWorkBook.write(os);
        } catch (Exception e) {
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private void giveAttendance() {
        FileOutputStream os = null;
        try {
            boolean flag = false;
            FileInputStream myInput;
            POIFSFileSystem myFileSystem;
            HSSFWorkbook myWorkBook;
            HSSFSheet mySheet;
            file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(getApplicationContext(), "Directory or File not Found Shutting down program", Toast.LENGTH_LONG)
                        .show();
            }
            myInput = new FileInputStream(file);
            myFileSystem = new POIFSFileSystem(myInput);
            myWorkBook = new HSSFWorkbook(myFileSystem);
            mySheet = myWorkBook.getSheetAt(0);
            Iterator rowIter = mySheet.rowIterator();
            int i = 0;
            while (rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                if (myRow.getRowNum() == 0) {
                    Iterator cellIter = myRow.cellIterator();
                    CellStyle cs = myWorkBook.createCellStyle();
                    cs.setFillForegroundColor(HSSFColor.LIME.index);
                    cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
                    while (cellIter.hasNext()) {
                        i++;
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        if (myCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {

                        }
                        if (myCell.getCellType() == Cell.CELL_TYPE_STRING) {
                            if (myCell.toString().equals(getCurrentDate())) {
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (!flag) {
                        HSSFCell c = myRow.getCell(i);
                        if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK) {
                            c = myRow.createCell(i);
                            c.setCellValue(getCurrentDate());
                        }
                    }

                } else {
                    HSSFCell myCell = myRow.getCell(3);
                    if (myCell == null || myCell.getCellType() == Cell.CELL_TYPE_BLANK) {
                        continue;
                    }
                    HSSFCell c = myRow.getCell(i);
                    for (DeviceInfo dinfo : idList) {
                        if (dinfo.getAdress().equals(myCell.toString())) {
                            if (c == null || c.getCellType() == Cell.CELL_TYPE_BLANK) {
                                c = myRow.createCell(i);
                                c.setCellValue("P");
                                break;
                            }
                        }
                    }
                }

            }
            myWorkBook.write(os);
        } catch (Exception e) {
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    private String getCurrentDate() {
        String format = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date gmtTime = new Date(sdf.format(new Date().getTime()));
        Timestamp date = new Timestamp(gmtTime.getTime());
        return date.toString();
    }
}
