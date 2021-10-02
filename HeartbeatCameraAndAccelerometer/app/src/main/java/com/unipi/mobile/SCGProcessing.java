package com.unipi.mobile;

import android.graphics.Color;
import android.hardware.SensorEvent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.github.psambit9791.jdsp.filter.Butterworth;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.unipi.mobile.db.SCGEntryDao;
import com.unipi.mobile.entities.SCGEntry;

import pl.droidsonroids.gif.GifImageView;

public class SCGProcessing implements Serializable {

    private final SCGEntryDao scgEntryDao;

    private final int samplingPeriodUs;
    private final double samplingFreq;
    private double cutoffFreq = 17.0;
    private static final double MIN_DIST = 0.35;

    private final SortedMap<Long, Double> collected_scg_sample_Z = new TreeMap<>();
    private final List<Double> scg_samples_const_sampling = new ArrayList<>();
    private final SortedMap<Double, Double> scg_filtered_sample_map = new TreeMap<>();
    private final SortedMap<Double, Double> scg_energy_signal = new TreeMap<>();
    private final List<Double> peaks = new ArrayList<>();
    private double threshold;
    private int bpm;

    private final SCGActivity scgActivity;

    private static final long TEN_SECONDS_IN_NANOS = 10000000000L;
    private static final String TAG = "SCGProcessing";

    SCGProcessing(SCGActivity scgActivity, SCGEntryDao scgEntryDao, int samplingPeriodUs) {
        this.scgActivity = scgActivity;
        this.scgEntryDao = scgEntryDao;
        this.samplingPeriodUs = samplingPeriodUs;
        this.samplingFreq = (1.0 / samplingPeriodUs) * 1000000;  //measured in Hz
    }

    public void setSeekBarProgress(int seekBarProgress) {
        this.cutoffFreq = 12.0 + seekBarProgress * 0.10;
        Log.i(TAG, "" + cutoffFreq);
    }

    void addSCGSample(SensorEvent se) {

        Long timestamp = se.timestamp;
        //double acc_x = se.values[0];
        //double acc_y = se.values[1];
        double acc_z = se.values[2];

        //double magnitude = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);
        collected_scg_sample_Z.put(timestamp, acc_z);

        // after a 10 second window, process the data
        if (collected_scg_sample_Z.lastKey() - collected_scg_sample_Z.firstKey() > TEN_SECONDS_IN_NANOS) {
            // start processing data
            processData();

            // stop measurements
            this.scgActivity.getSensorManager().unregisterListener(this.scgActivity);

            // clear data structures
            finishUp();
        }

    }

    private void processData() {

        // get values sampled at constant intervals through interpolation
        interpolate();

        // filter the data using a 4th order Butterworth filter
        filter();

        // find peaks in the graph
        findPeaks();

        computeAvgInterBeatInterval();

        // insert the value into the database
        insertInDataBase();

        // plot the data
        plot();
    }

    private void interpolate() {

        int size = collected_scg_sample_Z.size();
        double[] x_arr = new double[size];
        double[] y_arr = new double[size];

        int i = 0;
        for (SortedMap.Entry<Long, Double> entry : collected_scg_sample_Z.entrySet()) {
            x_arr[i] = entry.getKey();
            y_arr[i] = entry.getValue();
            i++;
        }

        LinearInterpolator interpolator = new LinearInterpolator();
        PolynomialSplineFunction splineFunction = interpolator.interpolate(x_arr, y_arr);

        Long firstTimestamp = collected_scg_sample_Z.firstKey();
        Long lastTimestamp = collected_scg_sample_Z.lastKey();
        long incrementNs = (long) (samplingPeriodUs * 1000);
        for (Long currentTimestamp = firstTimestamp; currentTimestamp < lastTimestamp; currentTimestamp += incrementNs) {
            double value = splineFunction.value(currentTimestamp);
            scg_samples_const_sampling.add(value);
        }

    }

    private void filter() {

        int size = scg_samples_const_sampling.size();
        double[] signal = new double[size];
        for (int j = 0; j < size; j++) {
            signal[j] = scg_samples_const_sampling.get(j);
        }

        Butterworth butterworth = new Butterworth(signal, samplingFreq);
        double[] output = butterworth.highPassFilter(4, cutoffFreq);

        for (int j = 0; j < output.length; j++) {
            double normalizedTime = ((double) j * ((double) samplingPeriodUs / (double) 1000000));
            scg_filtered_sample_map.put(normalizedTime, output[j]);
        }

    }

    private void findPeaks() {

        computeThreshold();
        normalize();

        Double[] times = scg_energy_signal.keySet().toArray(new Double[0]);

        for (int i = 0; i < times.length - 1; i++) {
            if (scg_energy_signal.get(times[i]) >= 0 && scg_energy_signal.get(times[i + 1]) < 0) {
                if (peaks.isEmpty() || (times[i] - peaks.get(peaks.size() - 1)) > MIN_DIST)
                    peaks.add(times[i]);
            }
        }
        Log.i(TAG, String.valueOf(peaks.size()));
    }

    private void computeAvgInterBeatInterval() {

        double sum = 0;
        for (int i = 0; i < peaks.size() - 1; i++) {
            sum += peaks.get(i + 1) - peaks.get(i);
        }
        double avgBeatInterval = sum / peaks.size();
        bpm = (int) Math.round((60.0 / avgBeatInterval));

        Log.i(TAG, "Heart rate: " + bpm);
    }

    private void plot() {
        GraphView scg_graph = this.scgActivity.findViewById(R.id.scg_graph);
        DataPoint[] dataPointsSCG = new DataPoint[scg_filtered_sample_map.size()];
        DataPoint[] dataPointsPeaks = new DataPoint[peaks.size()];

        int i = 0, j = 0;
        for (SortedMap.Entry<Double, Double> entry : scg_filtered_sample_map.entrySet()) {
            double x = entry.getKey();
            double y = entry.getValue();
            dataPointsSCG[i] = new DataPoint(x, y);
            if (peaks.contains(x)) {
                dataPointsPeaks[j] = new DataPoint(x, y);
                j++;
            }
            i++;
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPointsSCG);
        PointsGraphSeries<DataPoint> peaks = new PointsGraphSeries<>(dataPointsPeaks);
        peaks.setColor(Color.rgb(255, 0,  0));
        peaks.setSize(5);
        scg_graph.removeAllSeries();
        scg_graph.addSeries(series);
        scg_graph.addSeries(peaks);
    }

    private void computeThreshold () {

        double sum = 0;
        for (SortedMap.Entry<Double, Double> entry : scg_filtered_sample_map.entrySet()) {
            this.scg_energy_signal.put(entry.getKey(), entry.getValue() * entry.getValue());
            sum += entry.getValue() * entry.getValue();
        }
        this.threshold = (sum / scg_energy_signal.size()) * 2.5;
        Log.i(TAG, String.valueOf(this.threshold));

    }

    private void normalize() {
        for (SortedMap.Entry<Double, Double> entry : scg_energy_signal.entrySet()) {
            Double key = entry.getKey();
            Double normalizedValue = entry.getValue() - threshold;
            scg_energy_signal.put(key, normalizedValue);
        }
    }

    private void insertInDataBase() {

        LocalDateTime dateTime = LocalDateTime.now();
        SCGEntry entry = new SCGEntry(dateTime, this.bpm);
        scgEntryDao.insertAll(entry);

    }

    private void finishUp() {

        GraphView scg_graph = this.scgActivity.findViewById(R.id.scg_graph);
        scg_graph.setVisibility(View.VISIBLE);
        TextView plotCaption = this.scgActivity.findViewById(R.id.plotCaption);
        plotCaption.setVisibility(View.VISIBLE);
        TextView titleInstructions = this.scgActivity.findViewById(R.id.titleInstruction);
        titleInstructions.setVisibility(View.INVISIBLE);
        TextView instructions = this.scgActivity.findViewById(R.id.instructions);
        instructions.setVisibility(View.INVISIBLE);
        GifImageView gif = this.scgActivity.findViewById(R.id.gifImageView);
        gif.setVisibility(View.INVISIBLE);
        Button buttonStart = this.scgActivity.findViewById(R.id.buttonStart);
        buttonStart.setVisibility(View.VISIBLE);
        SeekBar seekBar = this.scgActivity.findViewById(R.id.seekBar);
        seekBar.setVisibility(View.VISIBLE);
        TextView filteringCaption = this.scgActivity.findViewById(R.id.filteringCaption);
        filteringCaption.setVisibility(View.VISIBLE);
        TextView lowCaption = this.scgActivity.findViewById(R.id.lowCaption);
        lowCaption.setVisibility(View.VISIBLE);
        TextView highCaption = this.scgActivity.findViewById(R.id.highCaption);
        highCaption.setVisibility(View.VISIBLE);
        TextView measuringCaption = this.scgActivity.findViewById(R.id.measuringCaption);
        measuringCaption.setVisibility(View.INVISIBLE);
        TextView caption = this.scgActivity.findViewById(R.id.textView);
        caption.setVisibility(View.VISIBLE);
        TextView bpmTextView = this.scgActivity.findViewById(R.id.textView2);
        String bpmResult = bpm + " BPM";
        bpmTextView.setText(bpmResult);
        bpmTextView.setVisibility(View.VISIBLE);

        collected_scg_sample_Z.clear();
        scg_samples_const_sampling.clear();
        scg_filtered_sample_map.clear();
        scg_energy_signal.clear();
        peaks.clear();

    }

}
