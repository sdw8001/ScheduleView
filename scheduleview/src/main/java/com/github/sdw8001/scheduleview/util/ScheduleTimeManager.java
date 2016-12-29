package com.github.sdw8001.scheduleview.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-12-08.
 * 스케쥴러의 시작시간(TimeStart)과 종료시간(TimeEnd), 시간간격(TimeDuration) 정보를 관리하며 필요한 정보로 가공하여 제공한다.
 */

public class ScheduleTimeManager {

    private Calendar mTimeStart;
    private Calendar mTimeEnd;
    private int mTimeDuration;

    public ScheduleTimeManager() {
        this(0, 0, 24, 0, 60);
    }

    public ScheduleTimeManager(Calendar timeStart, Calendar timeEnd, int timeDuration) {
        this.mTimeStart = (Calendar) timeStart.clone();
        this.mTimeEnd = (Calendar) timeEnd.clone();
        this.mTimeDuration = timeDuration;
    }

    public ScheduleTimeManager(int timeStartHour, int timeStartMinute, int timeEndHour, int timeEndMinute, int timeDuration) {
        this.mTimeStart = Calendar.getInstance();
        this.mTimeEnd = (Calendar) mTimeStart.clone();

        this.mTimeStart.set(Calendar.HOUR_OF_DAY, timeStartHour);
        this.mTimeStart.set(Calendar.MINUTE, timeStartMinute);
        this.mTimeEnd.set(Calendar.HOUR_OF_DAY, timeEndHour);
        this.mTimeEnd.set(Calendar.MINUTE, timeEndMinute);
        this.mTimeDuration = timeDuration;
    }

    // getter, setter 영역
    public Calendar getTimeStart() {
        return mTimeStart;
    }

    public void setTimeStart(Calendar timeStart) {
        this.mTimeStart = (Calendar) timeStart.clone();
    }

    public Calendar getTimeEnd() {
        return mTimeEnd;
    }

    public void setTimeEnd(Calendar timeEnd) {
        this.mTimeEnd = (Calendar) timeEnd.clone();
    }

    public int getTimeDuration() {
        return mTimeDuration;
    }

    public void setTimeDuration(int timeDuration) {
        this.mTimeDuration = timeDuration;
    }


    /**
     * <pre>
     *     시작시간부터 종료시간까지 TimeDuration 으로 분할한 수를 반환합니다.
     * </pre>
     * @return 분할된 TimeCount
     */
    public int getTimeCount() {
        return getTotalMinute() / mTimeDuration;
    }

    /**
     * 시작시각을 분으로 환산하여 반환.
     * @return 시작시각 Minute
     */
    public int getTimeStartMinute() {
        return mTimeStart.get(Calendar.HOUR_OF_DAY) * 60 + mTimeStart.get(Calendar.MINUTE);
    }

    /**
     * 종료시각을 분으로 환산하여 반환.
     * @return 종료시각 Minute
     */
    public int getTimeEndMinute() {
        return mTimeEnd.get(Calendar.HOUR_OF_DAY) * 60 + mTimeEnd.get(Calendar.MINUTE);
    }

    /**
     * <pre>
     *     시작시간부터 종료시간까지 기간을 분으로 환산하여 반환합니다.
     * </pre>
     * @return 시작시간부터 종료시간까지의 총 분(Minute).
     */
    public int getTotalMinute() {
        return (int) (mTimeEnd.getTimeInMillis() - mTimeStart.getTimeInMillis()) / (1000 * 60);
    }

    /**
     * <pre>
     *     시작시간부터 종료시간까지 TimeDuration 으로 분할하여 TimeStartEnd 객체의 리스트로 만들어 반환합니다.
     * </pre>
     * @return Time Start,End 를 갖는 객체인 TimeStartEnd 의 목록
     */
    public List<TimeStartEnd> getTimeStartEndList() {
        List<TimeStartEnd> timeStartEndList = new ArrayList<>();
        int timeCount = getTimeCount();
        long timeDurationMillis = mTimeDuration * 60 * 1000;
        long timeStartInMillis = mTimeStart.getTimeInMillis();
        Calendar tempTimeStart = (Calendar) mTimeStart.clone();
        Calendar tempTimeEnd = (Calendar) tempTimeStart.clone();

        for (int i = 0; i < timeCount; i++) {
            tempTimeStart.setTimeInMillis(timeStartInMillis);
            tempTimeEnd.setTimeInMillis(timeStartInMillis + timeDurationMillis);
            timeStartEndList.add(new TimeStartEnd(tempTimeStart, tempTimeEnd));
            timeStartInMillis += timeDurationMillis;
        }

        return timeStartEndList;
    }

    public int getTimeStartIndex(Calendar targetTimeStart) {
        int index;
        long timeStartInMillis = mTimeStart.getTimeInMillis();
        long targetTimeStartInMillis = targetTimeStart.getTimeInMillis();
        index = (int) ((targetTimeStartInMillis - timeStartInMillis) / (1000 * 60) / mTimeDuration);
        return index;
    }
}
