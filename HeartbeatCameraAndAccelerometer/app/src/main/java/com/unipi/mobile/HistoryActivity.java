package com.unipi.mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.unipi.mobile.db.CameraDataBase;
import com.unipi.mobile.db.CameraEntryDao;
import com.unipi.mobile.db.SCGDataBase;
import com.unipi.mobile.db.SCGEntryDao;
import com.unipi.mobile.entities.CameraEntry;
import com.unipi.mobile.entities.SCGEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HistoryActivity extends AppCompatActivity {

    private SCGEntryDao scgEntryDao;
    private CameraEntryDao cameraEntryDao;
    private MaterialCalendarView calendarView;
    private final ArrayList<Item> items = new ArrayList<>();
    private ItemAdapter adapter;
    private List<SCGEntry> scgEntries;
    private List<CameraEntry> cameraEntries;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        context = this.getBaseContext();
        scgEntryDao = SCGDataBase.getInstance(context).scgEntryDao();
        cameraEntryDao = CameraDataBase.getInstance(context).cameraEntryDao();

        calendarView = this.findViewById(R.id.calendarView);

        adapter = new ItemAdapter(this, items);
        ListView listView = this.findViewById(R.id.measurementList);
        listView.setAdapter(adapter);

        markDays();

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                markDays();
            }
        });
        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {

                adapter.clear();
                int year = date.getYear();
                int month = date.getMonth();
                int day = date.getDay();
                StartEndDateTime startEndDateTime = new StartEndDateTime(year, month, day);
                LocalDateTime dateTimeStart = startEndDateTime.getStart();
                LocalDateTime dateTimeEnd = startEndDateTime.getEnd();
                List<SCGEntry> scgEntries = scgEntryDao.getByDate(dateTimeStart, dateTimeEnd);
                List<CameraEntry> cameraEntries = cameraEntryDao.getByDate(dateTimeStart, dateTimeEnd);

                for (SCGEntry entry : scgEntries) {
                    adapter.add(new Item(entry.dateTime.toLocalTime().toString(), "SCG", Integer.toString(entry.bpm), year, month, day, context));
                }

                for (CameraEntry entry : cameraEntries) {
                    adapter.add(new Item(entry.dateTime.toLocalTime().toString(), "PPG", Integer.toString(entry.bpm), year, month, day, context));
                }

                //adapter.add(new Item("prova", "prova", "prova"));

            }
        });


    }

    void markDays() {

        calendarView.removeDecorators();
        CalendarDay currentDate = calendarView.getCurrentDate();
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonth();
        StartEndDateTime startEndDateTime = new StartEndDateTime(currentYear, currentMonth);
        LocalDateTime dayStart = startEndDateTime.getStart();
        LocalDateTime dayEnd = startEndDateTime.getEnd();
        scgEntries = scgEntryDao.getByDate(dayStart, dayEnd);
        cameraEntries = cameraEntryDao.getByDate(dayStart, dayEnd);

        Set<CalendarDay> days = new HashSet<>();

        for (SCGEntry entry : scgEntries) {
            LocalDateTime dateTime = entry.getDateTime();
            int year = dateTime.getYear();
            int month = dateTime.getMonthValue();
            int day = dateTime.getDayOfMonth();
            days.add(CalendarDay.from(year, month, day));
        }

        for (CameraEntry entry : cameraEntries) {
            LocalDateTime dateTime = entry.getDateTime();
            int year = dateTime.getYear();
            int month = dateTime.getMonthValue();
            int day = dateTime.getDayOfMonth();
            days.add(CalendarDay.from(year, month, day));
        }

        int color = Color.rgb(0, 0, 255);
        calendarView.addDecorators(new EventDecorator(color, days));
    }

}