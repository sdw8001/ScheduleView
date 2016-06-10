package com.github.sdw8001.scheduleview.header;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by sdw80 on 2016-04-22.
 */
public class Header implements Serializable {

    /**
     * Header 의 종류가 Calendar 의 Date
     */
    public static final int HEADER_USING_CALENDAR = 1;

    /**
     * Header 의 종류가 Title TEXT
     */
    public static final int HEADER_USING_TITLE = 2;

    private String parentHeaderKey;
    private String parentHeaderName;
    private String headerKey;
    private String headerName;
    private Calendar calendar;

    public Header() {
        this.headerKey = null;
        this.calendar = null;
        this.headerName = null;
    }
    public Header(Calendar calendar, String parentHeaderName, String parentHeaderKey, String headerKey, String headerName) {
        this.parentHeaderKey = parentHeaderKey;
        this.parentHeaderName = parentHeaderName;
        this.headerKey = headerKey;
        this.headerName = headerName;
        this.calendar = calendar;
    }

    public String getHeaderKey() {
        return headerKey;
    }

    public String getParentHeaderKey() {
        return parentHeaderKey;
    }

    public String getParentHeaderName() {
        return parentHeaderName;
    }

    public void setParentHeaderName(String parentHeaderName) {
        this.parentHeaderName = parentHeaderName;
    }

    public void setParentHeaderKey(String parentHeaderKey) {
        this.parentHeaderKey = parentHeaderKey;
    }

    public void setHeaderKey(String headerKey) {
        this.headerKey = headerKey;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
}
