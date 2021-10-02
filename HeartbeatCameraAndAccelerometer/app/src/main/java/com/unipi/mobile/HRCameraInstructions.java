package com.unipi.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HRCameraInstructions extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrcamera_instructions2);

    }
    public void onBackPressed() {
        Intent i = new Intent(HRCameraInstructions.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void startPPG(View view) {
        Intent intent = new Intent(this, HRCameraComputer.class);
        startActivity(intent);
    }


}