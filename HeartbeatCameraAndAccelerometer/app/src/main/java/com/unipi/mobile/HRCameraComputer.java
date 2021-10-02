package com.unipi.mobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.ceil;

@SuppressWarnings("deprecation")
public class HRCameraComputer extends Activity {
    MediaPlayer mediaPlayerBeep;
    MediaPlayer mediaPlayerTry;

    private static final AtomicBoolean ProcessFlag = new AtomicBoolean(false);
    private Toast Toast;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private SurfaceView cameraPreview = null;
    private static SurfaceHolder cameraPreviewHolder = null;
    private static Camera camera = null;
    private static PowerManager.WakeLock wakeLock = null;
    public int HeartBeats = 0;
    public double TempAvgBeats = 0;
    private ProgressBar ProgressBarHR;
    public int progress = 0;
    public int increment = 0;
    private static long startingTime = 0;
    private double SamplingFreq;
    public ArrayList<Double> AvgGREENList = new ArrayList<Double>();
    public ArrayList<Double> AvgREDList = new ArrayList<Double>();
    public int FramesNo = 0;


    @SuppressLint("InvalidWakeLockTag")
    @RequiresApi(api = Build.VERSION_CODES.M) //change the minimum required API level to 23. Necessary as "getSystemService" is available from 23 onwards.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_camera_computer);


        mediaPlayerBeep = MediaPlayer.create(HRCameraComputer.this, R.raw.beep);
        mediaPlayerTry = MediaPlayer.create(HRCameraComputer.this, R.raw.tryagain);

        //check whether or not the user has already consented to the use of the camera.
        //If not, the access request is forwarded.
        checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);

        //Camera View Settings
        cameraPreview = findViewById(R.id.PreviewCamera);
        cameraPreviewHolder = cameraPreview.getHolder();
        cameraPreviewHolder.addCallback(cameraPreviewHolderCallback);
        cameraPreviewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // progress bar initiation
        ProgressBarHR = findViewById(R.id.ProgressBar);
        ProgressBarHR.setProgress(0);

        //Settings required to prevent the display from going dark
        PowerManager pMngr = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pMngr.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Stay on");
    }

    @Override
    public void onResume() {
        super.onResume();
        wakeLock.acquire(10*60*1000L /*10 minutes*/);
        camera = Camera.open();
        camera.setDisplayOrientation(90); //la rotazione è necessaria affinché l'immagine si veda nella direzione corretta
        startingTime = System.currentTimeMillis();
    }

    @Override
    public void onPause() {
        super.onPause();
        wakeLock.release();
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(HRCameraComputer.this, MainActivity.class);
        startActivity(i);
        finish();
    }


    //getting frames data from the camera and start the heartbeat process
    private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera cameraa) {

            if (data == null) throw new NullPointerException();
            Camera.Size size = cameraa.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            //Atomically sets the value to the given updated value if the current value == the expected value.
            if (!ProcessFlag.compareAndSet(false, true))
                return;

            int width = size.width;
            int height = size.height;
            // Given a byte array representing a yuv420sp image, determine the average amount of red in the image
            double AvgGreen = ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data.clone(), height, width, 3);
            //Given a byte array representing a yuv420sp image, determine the average amount of red in the image
            double AvgRED = ImageProcessing.decodeYUV420SPtoRedBlueGreenAvg(data.clone(), height, width, 1);


            //countes number of frames in 30 seconds
            if (AvgRED>200){
                AvgGREENList.add(AvgGreen);
                AvgREDList.add(AvgRED);
                ++FramesNo;
            }

            //To check if we got a good red intensity to process if not return to the condition and set it again until we get a good red intensity
            if (AvgRED < 200) {
                AvgREDList.clear();
                AvgGREENList.clear();
                TempAvgBeats=0;
                HeartBeats=0;
                /////////////////////
                increment = 0;
                progress = increment;
                FramesNo = 0;
                ProgressBarHR.setProgress(progress);
                ProcessFlag.set(false);

                ////
                startingTime = System.currentTimeMillis();
            }

            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startingTime) / 1000d; //to convert time to seconds
            if (totalTimeInSecs >= 30) { //when 30 seconds of measuring passes do the following " we chose 30 seconds to take half sample since 60 seconds is normally a full sample of the heart beat "

                Double[] Green = AvgGREENList.toArray(new Double[AvgGREENList.size()]);
                Double[] Red = AvgREDList.toArray(new Double[AvgREDList.size()]);

                SamplingFreq = (FramesNo / totalTimeInSecs); //calculating the sampling frequency

                double HeartRateFreq = FFT(Green, FramesNo, SamplingFreq); // send the green array and get its fft then return the amount of heartrate per second
                double BeatsXMin = (int) ceil(HeartRateFreq * 60);
                double HR1Freq = FFT(Red, FramesNo, SamplingFreq);  // send the red array and get its fft then return the amount of heartrate per second
                double bpm1 = (int) ceil(HR1Freq * 60);

                // The following code is to make sure that if the heartrate from red and green intensities are reasonable
                // take the average between them, otherwise take the green or red if one of them is good
                // TempAvgBeats=0;
                if ((BeatsXMin > 45 || BeatsXMin < 200)) {
                    if ((bpm1 > 45 || bpm1 < 200)) {

                        TempAvgBeats = (BeatsXMin + bpm1) / 2;
                    } else {
                        TempAvgBeats = BeatsXMin;
                    }
                } else if ((bpm1 > 45 || bpm1 < 200)) {
                    TempAvgBeats = bpm1;
                }

                if (TempAvgBeats < 45 || TempAvgBeats > 200) { //if the heart beat wasn't reasonable after all reset the progresspag and restart measuring
                     mediaPlayerTry.start();
                    increment = 0;
                    progress = increment;
                    ProgressBarHR.setProgress(progress);
                    Toast = Toast.makeText(getApplicationContext(), "Heart rate Measurement Failed", Toast.LENGTH_SHORT);
                    Toast.show();
                    //startingTime = System.currentTimeMillis();
                    FramesNo = 0;
                    ProcessFlag.set(false);
                    return;
                }

                HeartBeats = (int) TempAvgBeats;
            }

            if (HeartBeats != 0)
            {
                Intent i = new Intent(HRCameraComputer.this, CameraHRResult.class);
                i.putExtra("bpm", HeartBeats);
                mediaPlayerBeep.start(); // no need to call prepare(); create() does that for you
                startActivity(i);
                finish();
            }

            if (AvgRED != 0)
            {
                //increment the progresspar
                progress = increment++ / 34;
                ProgressBarHR.setProgress(progress);
            }
            //keeps taking frames tell 30 seconds
            ProcessFlag.set(false);

        }
    };

    private final SurfaceHolder.Callback cameraPreviewHolderCallback = new SurfaceHolder.Callback() {

        @SuppressLint("LongLogTag")
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(cameraPreviewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("cameraPreviewHolderCallback", "Exception: ", t);
            }
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                Log.d("surfaceChanged", "Using width=" + size.width + " height=" + size.height);
            }

            camera.setParameters(parameters);
            camera.startPreview();
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

        }

    };

    private static Camera.Size getSmallestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea < resultArea) result = size;
                }
            }
        }
        return result;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(HRCameraComputer.this, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(HRCameraComputer.this, new String[] { permission }, requestCode);
        }
    }


    public  double FFT(Double[] in, int size, double samplingFrequency) {
        double temp = 0;
        double POMP = 0;
        double frequency;
        double[] output = new double[2 * size];

        for (int i = 0; i < output.length; i++)
            output[i] = 0;

        for (int x = 0; x < size; x++) {
            output[x] = in[x];
        }

        DoubleFft1d fft = new DoubleFft1d(size);
        fft.realForward(output);

        //Usually done when Peak Peaking is to be carried out. You are interested in magnitude so it has to be taken 
        //the absolute value of the spectrum. You don't need real and imaginary parts.
        for (int x = 0; x < 2 * size; x++) {
            output[x] = Math.abs(output[x]); 
        }

        //NOTE: The spectrum is calculated over a TIME WINDOW of heartbeat. Specifically a window of 30 seconds-
        //In the spectrum calculated on a window of a periodic signal you will find the periodicity of the heartbeat.
        //Ideally in the absence of noise you will find two peaks: average red value 
        //(continuous component of the signal at frequency 0 or at least low), plus since 
        //you know that the average red value varies over time, you would certainly find a peak 
        //at a high frequency because you have a constant signal with periodic variations which 
        //impact on a signal that has periodicity. 
        //In general at low frequency you will also find noise in addition to the continuous component !
        //NOTE2: In general, the average value is found at low frequency and will have a peak much higher than the peak 
        //del at the frequency concerned. This is why the following HIGH-PASS filter is applied. 
        for (int p = 35; p < size; p++) {
            if (temp < output[p]) {
                temp = output[p];
                POMP = p;

            }
        }

        //EXAMPLE: 
        //If you take 30-second window: beat period 1 second. I have 30 beats.
        //In the spectrum you find a continuous frequency + the noise + the frequency at which the peak of the 
        //heart rate which corresponds to the periodicity of the heartbeat.

        if (POMP < 35) {
            POMP = 0;
        }
        //POMP => Frequency at which the detected peak is located. Normalised frequency that depends on the sampling frequency.
        //Cardiac Frequency: found by denormalising POMP.
        frequency = POMP * samplingFrequency / (2 * size);
        return frequency;
    }
}

