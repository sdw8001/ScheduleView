package com.github.sdw8001.scheduleview.loader;

import com.github.sdw8001.scheduleview.event.ScheduleViewEvent;

import java.util.List;

/**
 * Created by sdw80 on 2016-04-26.
 */
public interface ScheduleViewLoader {

    /**
     * Events 목록 Load
     * @return ScheduleViewEvent 목록
     */
    List<? extends ScheduleViewEvent> onLoad();
}
