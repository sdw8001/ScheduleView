package com.github.sdw8001.scheduleview.loader;

import com.github.sdw8001.scheduleview.event.ScheduleViewEvent;

import java.util.List;

/**
 * Created by sdw80 on 2016-04-26.
 */
public class ScheduleLoader implements ScheduleViewLoader {

    private ScheduleLoadListener mOnScheduleLoadListener;

    public ScheduleLoadListener getOnScheduleLoadListener() {
        return mOnScheduleLoadListener;
    }

    public void setOnScheduleLoadListener(ScheduleLoadListener listener) {
        this.mOnScheduleLoadListener = listener;
    }

    public ScheduleLoader(ScheduleLoadListener listener) {
        this.mOnScheduleLoadListener = listener;
    }

    @Override
    public List<? extends ScheduleViewEvent> onLoad() {
        return mOnScheduleLoadListener.onScheduleLoad();
    }

    public interface ScheduleLoadListener {
        /**
         * ScheduleView 데이터 로드 Listener 입니다. onScheduleLoad() 를 통해 Events 를 Load 합니다.
         * @return ScheduleViewEvent 목록을 반환합니다.
         */
        List<? extends ScheduleViewEvent> onScheduleLoad();
    }
}
