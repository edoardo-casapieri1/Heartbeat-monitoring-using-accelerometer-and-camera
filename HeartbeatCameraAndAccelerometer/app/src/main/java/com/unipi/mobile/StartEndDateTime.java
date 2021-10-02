package com.unipi.mobile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public final class StartEndDateTime {

    private final LocalDateTime start;
    private final LocalDateTime end;

    public StartEndDateTime(int year, int month, int day) {
        Calendar calendar =  new GregorianCalendar(year, month - 1, day);
        LocalDate localDate = LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).toLocalDate();
        start = localDate.atStartOfDay();
        end = localDate.atTime(LocalTime.MAX);
    }

    public StartEndDateTime(int year, int month) {
        Calendar calendarStart = new GregorianCalendar(year, month - 1, 1);
        int numberOfDays = calendarStart.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar calendarEnd = new GregorianCalendar(year, month - 1, numberOfDays);
        LocalDate localDateStart = LocalDateTime.ofInstant(calendarStart.toInstant(), calendarStart.getTimeZone().toZoneId()).toLocalDate();
        LocalDate localDateEnd = LocalDateTime.ofInstant(calendarEnd.toInstant(), calendarEnd.getTimeZone().toZoneId()).toLocalDate();
        start = localDateStart.atStartOfDay();
        end = localDateEnd.atTime(LocalTime.MAX);
    }

    public static void main(String[] args) {
        StartEndDateTime time = new StartEndDateTime(2021, 6);
        System.out.println("" + time.getStart() + time.getEnd());
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

}
