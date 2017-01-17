package com.github.sdw8001.scheduleview.interpreter;

import java.util.Calendar;

/**
 * Created by sdw80 on 2017-01-10.
 * Time Interpreter
 */

public interface TimeInterpreter {
    String interpretYearMonthDay(Calendar calendar);
    String interpretTime(Calendar calendar);
}
