package com.github.sdw8001.scheduleview.listener;

import org.joda.time.LocalDateTime;

/**
 * Created by sdw80 on 2016-05-17.
 */
public abstract class CalendarListener {
    /**
     * DatePicker 가 선택되었을때 Listener 에게 알립니다.
     */
    public abstract void onSelectPicker();

    /**
     * Date 가 선택되었음을 알립니다.
     *
     * @param selectedDate 선택된 Date.
     */
    public abstract void onSelectDate(LocalDateTime selectedDate);
}

