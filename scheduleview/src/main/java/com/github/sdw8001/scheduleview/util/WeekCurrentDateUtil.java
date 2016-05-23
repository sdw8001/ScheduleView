package com.github.sdw8001.scheduleview.util;

import android.content.Context;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDateTime;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-05-17.
 */
public class WeekCurrentDateUtil {
    private static WeekCurrentDateUtil instance;
    private LocalDateTime weekStartDate, selectedDate;

    public static synchronized WeekCurrentDateUtil getInstance() {
        if (instance == null)
            instance = new WeekCurrentDateUtil();
        return instance;
    }

    /**
     * Get the day difference in the selected day and the first day in the week
     *
     * @param dayName
     */
    public static int getDateGap(String dayName) {
        Log.d("dayname", dayName);

        if (dayName.equals("mon")) {
            return 1;
        } else if (dayName.equals("tue")) {
            return 2;
        } else if (dayName.equals("wed")) {
            return 3;
        } else if (dayName.equals("thu")) {
            return 4;
        } else if (dayName.equals("fri")) {
            return 5;
        } else if (dayName.equals("sat")) {
            return 6;
        } else {
            return 0;
        }
    }

    /**
     * Initial calculation of the week
     *
     * @param mContext
     */
    public void calculate(Context mContext) {
        //Initializing JodaTime
        JodaTimeAndroid.init(mContext);

        //Initializing Start with current month
        final LocalDateTime currentDateTime = new LocalDateTime();
        setStartDate(currentDateTime.getYear(), currentDateTime.getMonthOfYear(), currentDateTime.getDayOfMonth());
        int weekGap = WeekCurrentDateUtil.getDateGap(currentDateTime.dayOfWeek().getAsText().substring(0, 3).toLowerCase());
        if (weekGap != 0) {
            //if the current date is not the first day of the week the rest of days is added

            //Calendar set to the current date
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -weekGap);

            //now the date is weekGap days back
            Log.i("weekGap", "" + calendar.getTime().toString());
            LocalDateTime ldt = LocalDateTime.fromCalendarFields(calendar);
            setStartDate(ldt.getYear(), ldt.getMonthOfYear(), ldt.getDayOfMonth());
        }
    }

    /**
     * Set The Start day (week)from calender
     */
    private void setStartDate(int year, int month, int day) {
        weekStartDate = new LocalDateTime(year, month, day, 0, 0, 0);
        selectedDate = weekStartDate;
    }

    public LocalDateTime getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDateTime weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public LocalDateTime getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDateTime selectedDate) {
        this.selectedDate = selectedDate;
    }
}
