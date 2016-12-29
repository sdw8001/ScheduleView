package com.github.sdw8001.scheduleview.util;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-12-09.
 * 시작시간과 종료시간을 갖는 객체
 */

public class TimeStartEnd {
    private Calendar mTimeStart;
    private Calendar mTimeEnd;
    public TimeStartEnd(Calendar timeStart, Calendar timeEnd) {
        this.mTimeStart = (Calendar) timeStart.clone();
        this.mTimeEnd = (Calendar) timeEnd.clone();
    }

    public Calendar getTimeStart() {
        return mTimeStart;
    }

    public Calendar getTimeEnd() {
        return mTimeEnd;
    }

    public int getTimeDuration() {
        return (int) (mTimeEnd.getTimeInMillis() - mTimeStart.getTimeInMillis()) / (1000 * 60);
    }
}
