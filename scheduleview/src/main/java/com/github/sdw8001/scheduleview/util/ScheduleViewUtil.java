package com.github.sdw8001.scheduleview.util;

import com.github.sdw8001.scheduleview.event.ScheduleViewEvent;
import com.github.sdw8001.scheduleview.header.Header;
import com.github.sdw8001.scheduleview.view.ScheduleView;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-04-19.
 */
public class ScheduleViewUtil {


    /////////////////////////////////////////////////////////////////
    //
    //      Helper methods.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * 동일한 일자 인지 비교하여 동일여부를 반환합니다.
     * @param dayOne The first day.
     * @param dayTwo The second day.
     * @return 동일한 일자이면 true 반환.
     */
    public static boolean isSameDay(Calendar dayOne, Calendar dayTwo) {
        return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR) && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * <pre>
     * Event1 과 Event2 가 동일한 Header 에 해당 되는지 여부를 비교하여 반환합니다.
     * </pre>
     *
     * @param event1 The first key.
     * @param event2 The second key.
     * @param viewMode ViewMode.
     *
     * @return 동일하면 true 반환.
     */
    public static boolean isEventSameHeader(ScheduleViewEvent event1, ScheduleViewEvent event2, int viewMode) {
        if(event1 == null || event2 == null)
            return false;

        switch (viewMode) {
            case ScheduleView.VIEW_PARENT:
                if (event1.getParentHeaderKey().equals(event2.getParentHeaderKey()))
                    return true;
                break;
            case ScheduleView.VIEW_CHILD:
                if (event1.getParentHeaderKey().equals(event2.getParentHeaderKey()) && event1.getHeaderKey().equals(event2.getHeaderKey()))
                    return true;
                break;
        }

        return false;
    }

    /**
     * <pre>
     * Event 가 Header 에 해당 되는지 여부를 비교하여 반환합니다.
     * </pre>
     *
     * @param event ScheduleView Event.
     * @param header ScheduleView Header.
     * @param viewMode ViewMode.
     *
     * @return 동일하면 true 반환.
     */
    public static boolean isEventSameHeader(ScheduleViewEvent event, Header header, int viewMode) {
        if(event == null || header == null)
            return false;

        switch (viewMode) {
            case ScheduleView.VIEW_PARENT:
                if (event.getParentHeaderKey().equals(header.getHeaderKey()))
                    return true;
                break;
            case ScheduleView.VIEW_CHILD:
                if (event.getParentHeaderKey().equals(header.getParentHeaderKey()) && event.getHeaderKey().equals(header.getHeaderKey()))
                    return true;
                break;
        }

        return false;
    }

    /**
     * <pre>
     * Returns a calendar instance at the start of this day
     * </pre>
     *
     * @return the calendar instance
     */
    public static Calendar today(){
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }

    /**
     * <pre>
     * Returns a calendar instance at the start of this day
     * </pre>
     *
     * @return the calendar instance
     */
    public static Calendar resetDay(Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}