package com.github.sdw8001.scheduleview.loader;

import android.support.annotation.NonNull;

import com.github.sdw8001.scheduleview.event.ScheduleEvent;

import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-11-28.
 * Event Loader.
 */

public class EventLoader implements IEventLoader {

    private EventLoadListener mEventLoadListener;

    public EventLoader(@NonNull EventLoadListener eventLoadListener) {
        this.mEventLoadListener = eventLoadListener;
    }

    public EventLoadListener getEventLoadListener() {
        return mEventLoadListener;
    }

    public void setEventLoadListener(EventLoadListener eventLoadListener) {
        this.mEventLoadListener = eventLoadListener;
    }

    @Override
    public List<? extends ScheduleEvent> onLoad(Calendar calendar) {
        if (mEventLoadListener != null)
            return mEventLoadListener.onEventLoad(calendar);
        else
            return null;
    }

    public interface EventLoadListener {
        /**
         * ScheduleViewGroup 데이터 로드 Listener 입니다. onEventLoad() 를 통해 Events 를 Load 합니다.
         * @param dateCalendar Load 에 사용될 선택된 Calendar
         * @return ScheduleEvent 목록을 반환합니다.
         */
        List<? extends ScheduleEvent> onEventLoad(Calendar dateCalendar);
    }
}
