package com.example.asus.smartattendance;


import android.app.ListActivity;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FileChooser extends ListActivity {
    private File direction;
    private  FileArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        direction=new File(Environment.getExternalStorageDirectory().getPath());
        fill(direction);
    }

    private void fill(File f) {
        File[] dirs = f.listFiles();
        this.setTitle("Direction actual:" + f.getName());
        List<Option> dir = new ArrayList<Option>();
        List<Option> fls = new ArrayList<Option>();
        try {
            for (File ff : dirs) {
                if (ff.isDirectory()) {
                    dir.add(new Option(ff.getName(), "Folder", ff.getAbsolutePath()));
                } else {
                    fls.add(new Option(ff.getName(), "File Size" + ff.length(), ff.getAbsolutePath()));
                }

            }
        } catch(Exception e){

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if(!f.getName().equalsIgnoreCase("sdcard")){
            dir.add(0,new Option("..","Parent Directory",f.getParent()));
        }
        adapter=new FileArrayAdapter(FileChooser.this,R.layout.activity_file_chooser,dir);
        this.setListAdapter(adapter);
    }
    protected void onListItemClick(ListView listView, View v,int position,long id){
        super.onListItemClick(listView, v,position,id);
        Option option = adapter.getItem(position);
        if(option.getData().equalsIgnoreCase("Folder")||option.getData().equalsIgnoreCase("Parent Directory")){
            direction=new File(option.getPath());
            fill(direction);
        }
        else{
            FacultyActivity.filePath=option.getPath();
            Intent intent2=new Intent(FileChooser.this, FacultyActivity.class);
            intent2.putExtra("files",option.getPath());
            startActivity(intent2);
        }
    }
}
