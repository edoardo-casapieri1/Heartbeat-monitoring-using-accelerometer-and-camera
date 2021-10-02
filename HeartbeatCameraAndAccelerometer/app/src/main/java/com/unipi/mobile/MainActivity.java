package com.unipi.mobile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button HRCameraButton = this.findViewById(R.id.buttonCamera);

        HRCameraButton.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), HRCameraInstructions.class);
            startActivity(i);
            finish();
        });

    }

    public void startSCG(View view) {
        Intent intent = new Intent(this, SCGActivity.class);
        startActivity(intent);
    }

    public void startHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (arg0, arg1) -> {
                    finish();
                    System.exit(0);
                }).create().show();
    }

}