package com.sge.platforme_etude;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

public class Test {

    public static void main(String[] args) {
        LocalDate date1 = LocalDate.of(2026,4,12);
        LocalDateTime date2 = LocalDateTime.now();

        LocalTime date3 = LocalTime.now();
        String s = date3.toString();

        LocalDate monday = date1.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        System.out.println(monday);
//        System.out.println(date2.getHour() +":"+ date2.getMinute());
//        System.out.println(s.substring(0,5));
//        System.out.println(date1.getDayOfWeek());
    }
}
