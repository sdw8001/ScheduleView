package com.github.sdw8001.scheduleview.interpreter;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-04-19.
 */
public interface DateTimeInterpreter {
    String interpretDate(Calendar date);
    String interpretDayOfMonth(Calendar date);
    String interpretDayOfWeek(Calendar date);
    String interpretTime(int hour);
}
