package com.unipi.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.unipi.mobile.db.SCGDataBase;
import com.unipi.mobile.db.SCGEntryDao;
import com.unipi.mobile.entities.SCGEntry;

import java.time.LocalDateTime;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

public class SCGActivity extends AppCompatActivity implements SensorEventListener {

    private SCGEntryDao scgEntryDao;
    private SCGProcessing scgp;

    private SensorManager sm;
    private Sensor accelerometer;
    private SeekBar seekBar;

    private static final int samplingPeriodUs = 20000;  //period in microseconds
    private static final String TAG = "SCGActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        scgEntryDao = SCGDataBase.getInstance(this.getBaseContext()).scgEntryDao();
        scgp = new SCGProcessing(this, scgEntryDao, samplingPeriodUs);

        setContentView(R.layout.activity_scg);
        seekBar = this.findViewById(R.id.seekBar);
        seekBar.setProgress(50);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                scgp.setSeekBarProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setup();
    }

    SensorManager getSensorManager () {
        return this.sm;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setup(){
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if(accelerometer == null)
            finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //sm.registerListener(this, accelerometer, samplingPeriodUs);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            scgp.addSCGSample(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "Accuracy changed. New accuracy level: " + accuracy + ".");
    }

    public void startMeasurement(View view) {

        GifImageView gif = this.findViewById(R.id.gifImageView);
        gif.setVisibility(View.VISIBLE);
        Button buttonStart = this.findViewById(R.id.buttonStart);
        buttonStart.setVisibility(View.INVISIBLE);
        seekBar.setVisibility(View.INVISIBLE);
        TextView filteringCaption = this.findViewById(R.id.filteringCaption);
        filteringCaption.setVisibility(View.INVISIBLE);
        TextView lowCaption = this.findViewById(R.id.lowCaption);
        lowCaption.setVisibility(View.INVISIBLE);
        TextView highCaption = this.findViewById(R.id.highCaption);
        highCaption.setVisibility(View.INVISIBLE);
        TextView measuringCaption = this.findViewById(R.id.measuringCaption);
        measuringCaption.setVisibility(View.VISIBLE);
        TextView caption = this.findViewById(R.id.textView);
        caption.setVisibility(View.INVISIBLE);
        TextView bpmTextView = this.findViewById(R.id.textView2);
        bpmTextView.setVisibility(View.INVISIBLE);
        sm.registerListener(this, accelerometer, samplingPeriodUs);

    }

}