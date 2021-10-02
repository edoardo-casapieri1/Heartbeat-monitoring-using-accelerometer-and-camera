package com.unipi.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.unipi.mobile.db.CameraDataBase;
import com.unipi.mobile.db.CameraEntryDao;
import com.unipi.mobile.entities.CameraEntry;

import java.time.LocalDateTime;

public class CameraHRResult extends AppCompatActivity {
    private String user, Date;
    int HR;
    private CameraEntryDao cameraEntryDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_hrresult);
        TextView RHR = this.findViewById(R.id.HRR);

        cameraEntryDao = CameraDataBase.getInstance(this.getBaseContext()).cameraEntryDao();
    
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            HR = bundle.getInt("bpm");
            //user = bundle.getString("Usr");
            RHR.setText(getString(R.string.currentHeartRate, String.valueOf(HR)));

            ////////// Store data in db //////
            LocalDateTime dateTime = LocalDateTime.now();
            CameraEntry entry = new CameraEntry(dateTime, HR);
            cameraEntryDao.insertAll(entry);
            //////////////////////////////////

        }
}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(CameraHRResult.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
