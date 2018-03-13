package com.example.asus.smartattendance;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ASUS on 8/23/2016.
 */
public class FileArrayAdapter extends ArrayAdapter<Option> {
    private Context context;
    private int id;
    private List<Option> items;

    public FileArrayAdapter(Context context, int textViewResourceId, List<Option> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.id = textViewResourceId;
        this.items = objects;
    }

    public Option getItem(int i) {
        return items.get(i);
    }
    public View getView(int position, View contextView, ViewGroup parent){
        View v=contextView;
        if(v==null){
            LayoutInflater v1=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v=v1.inflate(id,null);
        }
        final Option o=items.get(position);
        if(o!=null){
            TextView tvName= (TextView) v.findViewById(R.id.tvName);
            TextView tvSize= (TextView) v.findViewById(R.id.tvSize);
            if(tvName!=null){
                tvName.setText(o.getName());
            }
            if(tvSize!=null){
                tvSize.setText(o.getData());
            }
        }
        return v;
    }
}
