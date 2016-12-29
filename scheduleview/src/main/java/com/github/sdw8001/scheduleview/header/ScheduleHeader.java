package com.github.sdw8001.scheduleview.header;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-11-25.
 *
 */

public class ScheduleHeader extends GroupableHeader {
    private String key;
    private String name;

    public ScheduleHeader() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
