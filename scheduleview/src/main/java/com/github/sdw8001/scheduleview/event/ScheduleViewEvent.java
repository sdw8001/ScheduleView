package com.github.sdw8001.scheduleview.event;

import android.graphics.Bitmap;

import com.github.sdw8001.scheduleview.util.ScheduleViewUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-04-26.
 */
public class ScheduleViewEvent {

    /**
     * Event 의 분류 기준을 KEY 로 설정하는 FLAG
     */
    public static final int SPLIT_USING_KEY = 1;

    /**
     * Event 의 분류 기준을 TIME 로 설정하는 FLAG
     */
    public static final int SPLIT_USING_TIME = 2;

    private String key;
    private String parentHeaderKey;
    private String headerKey;
    private int splitUsing;
    private Calendar mStartTime;
    private Calendar mEndTime;
    private int mBackgroundColor;
    private int mTypeColor;
    private String mTypeDetail;
    private int mTypeDetailForeColor;
    private int mTypeDetailBackColor;

    public ScheduleViewEvent() {

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getParentHeaderKey() {
        return parentHeaderKey;
    }

    public void setParentHeaderKey(String parentHeaderKey) {
        this.parentHeaderKey = parentHeaderKey;
    }

    public String getHeaderKey() {
        return headerKey;
    }

    public void setHeaderKey(String headerKey) {
        this.headerKey = headerKey;
    }

    public int getSplitUsing() {
        return splitUsing;
    }

    public void setSplitUsing(int splitUsing) {
        this.splitUsing = splitUsing;
    }

    public Calendar getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Calendar mStartTime) {
        this.mStartTime = mStartTime;
    }

    public Calendar getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Calendar mEndTime) {
        this.mEndTime = mEndTime;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
    }

    public int getTypeColor() {
        return mTypeColor;
    }

    public void setTypeColor(int mTypeColor) {
        this.mTypeColor = mTypeColor;
    }

    public int getTypeDetailBackColor() {
        return mTypeDetailBackColor;
    }

    public void setTypeDetailBackColor(int mTypeDetailBackColor) {
        this.mTypeDetailBackColor = mTypeDetailBackColor;
    }

    public int getTypeDetailForeColor() {
        return mTypeDetailForeColor;
    }

    public void setTypeDetailForeColor(int mTypeDetailForeColor) {
        this.mTypeDetailForeColor = mTypeDetailForeColor;
    }

    public String getTypeDetail() {
        return mTypeDetail;
    }

    public void setTypeDetail(String mTypeDetail) {
        this.mTypeDetail = mTypeDetail;
    }

    /***
     * 이 Event 의 StartTime 과 EndTime 이 같은 날이 아닌 경우 Event 를 날짜별로 분할하여 List 로 반환합니다.(일자별 표시할때 다른일자로 Display 하기 위해 사용.)
     * @return
     */
    public List<ScheduleViewEvent> splitScheduleViewEvents() {
        // ScheduleViewEvents 에 대해 하루를 기준으로 ScheduleViewEvent 를 분할한다.
        List<ScheduleViewEvent> events = new ArrayList<ScheduleViewEvent>();

        // 다음 날의 첫 번째 밀리 초는 여전히 같은 날입니다. (이것에 대한 이벤트를 분할 할 필요 없음).
        Calendar endTime = (Calendar) this.getEndTime().clone();
        endTime.add(Calendar.MILLISECOND, -1);
        if (!ScheduleViewUtil.isSameDay(this.getStartTime(), endTime)) {
            endTime = (Calendar) this.getStartTime().clone();
            endTime.set(Calendar.HOUR_OF_DAY, 23);
            endTime.set(Calendar.MINUTE, 59);

            ScheduleViewEvent event1 = new ScheduleViewEvent();

            event1.setStartTime(this.getStartTime());
            event1.setEndTime(endTime);
            event1.setKey(this.getKey());
            event1.setHeaderKey(this.getHeaderKey());
            event1.setParentHeaderKey(this.getParentHeaderKey());
            event1.setSplitUsing(this.getSplitUsing());
            event1.setTypeColor(this.getTypeColor());
            event1.setTypeDetail(this.getTypeDetail());
            event1.setTypeDetailForeColor(this.getTypeDetailForeColor());
            event1.setTypeDetailBackColor(this.getTypeDetailBackColor());

            events.add(event1);

            // Add other days.
            Calendar otherDay = (Calendar) this.getStartTime().clone();
            otherDay.add(Calendar.DATE, 1);
            while (!ScheduleViewUtil.isSameDay(otherDay, this.getEndTime())) {
                Calendar overDay = (Calendar) otherDay.clone();
                overDay.set(Calendar.HOUR_OF_DAY, 0);
                overDay.set(Calendar.MINUTE, 0);

                Calendar endOfOverDay = (Calendar) overDay.clone();
                endOfOverDay.set(Calendar.HOUR_OF_DAY, 23);
                endOfOverDay.set(Calendar.MINUTE, 59);

                ScheduleViewEvent eventMore = new ScheduleViewEvent();

                eventMore.setStartTime(overDay);
                eventMore.setEndTime(endOfOverDay);
                eventMore.setKey(this.getKey());
                eventMore.setHeaderKey(this.getHeaderKey());
                eventMore.setParentHeaderKey(this.getParentHeaderKey());
                eventMore.setSplitUsing(this.getSplitUsing());
                eventMore.setTypeColor(this.getTypeColor());
                eventMore.setTypeDetail(this.getTypeDetail());
                eventMore.setTypeDetailForeColor(this.getTypeDetailForeColor());
                eventMore.setTypeDetailBackColor(this.getTypeDetailBackColor());

                events.add(eventMore);

                // Add next day.
                otherDay.add(Calendar.DATE, 1);
            }

            // Add last day.
            Calendar startTime = (Calendar) this.getEndTime().clone();
            startTime.set(Calendar.HOUR_OF_DAY, 0);
            startTime.set(Calendar.MINUTE, 0);

            ScheduleViewEvent event2 = new ScheduleViewEvent();

            event2.setStartTime(startTime);
            event2.setEndTime(this.getEndTime());
            event2.setKey(this.getKey());
            event2.setHeaderKey(this.getHeaderKey());
            event2.setParentHeaderKey(this.getParentHeaderKey());
            event2.setSplitUsing(this.getSplitUsing());
            event2.setTypeColor(this.getTypeColor());
            event2.setTypeDetail(this.getTypeDetail());
            event2.setTypeDetailForeColor(this.getTypeDetailForeColor());
            event2.setTypeDetailBackColor(this.getTypeDetailBackColor());

            events.add(event2);
        } else {
            events.add(this);
        }

        return events;
    }
}
