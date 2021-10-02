package com.unipi.mobile;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.unipi.mobile.db.CameraDataBase;
import com.unipi.mobile.db.CameraEntryDao;
import com.unipi.mobile.db.Converters;
import com.unipi.mobile.db.SCGDataBase;
import com.unipi.mobile.db.SCGEntryDao;
import com.unipi.mobile.entities.CameraEntry;
import com.unipi.mobile.entities.SCGEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ItemAdapter extends ArrayAdapter<Item> {

    private SCGEntryDao scgDao;
    private HistoryActivity historyActivity;
    private CameraEntryDao cameraDao;
    private static final String TAG = "ItemAdapter";
    public ItemAdapter(Context context, ArrayList<Item> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Item item = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }
        // Lookup view for data population
        TextView bpm = (TextView) convertView.findViewById(R.id.bpm);
        TextView time = (TextView) convertView.findViewById(R.id.time);
        TextView type = (TextView) convertView.findViewById(R.id.type);
        // Populate the data into the template view using the data object
        bpm.setText(item.getBpm());
        time.setText(item.getTime());
        type.setText(item.getType());
        cameraDao = CameraDataBase.getInstance(item.getContext()).cameraEntryDao();
        scgDao = SCGDataBase.getInstance(item.getContext()).scgEntryDao();

        Button btn = (Button)convertView.findViewById(R.id.DeleteItem);
        btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(type.getText() == "SCG") {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss.SSS");
                    LocalDateTime dateTime = LocalDateTime.parse(item.getYear() + "-" + item.getMonth() + "-" + item.getDay() + " " + time.getText(), formatter);
                    scgDao.deleteByDate(dateTime);
                    //historyActivity.recreate();
                }
                else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss.SSS");
                    LocalDateTime dateTime = LocalDateTime.parse(item.getYear() + "-" + item.getMonth() + "-" + item.getDay() + " " + time.getText(), formatter);
                    cameraDao.deleteByDate(dateTime);
                    //historyActivity.recreate();
                }
            }
        });

        // Return the completed view to render on screen
        return convertView;
    }

}
