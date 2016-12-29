package com.github.sdw8001.scheduleview.event;

/**
 * Created by sdw80 on 2016-11-28.
 *
 */

public class ScheduleEvent extends CheckableEvent {
    private String contents;

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }
}
