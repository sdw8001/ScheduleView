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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Header header = (Header) o;

        if (parentHeaderKey != null ? !parentHeaderKey.equals(header.parentHeaderKey) : header.parentHeaderKey != null)
            return false;
        if (parentHeaderName != null ? !parentHeaderName.equals(header.parentHeaderName) : header.parentHeaderName != null)
            return false;
        if (headerKey != null ? !headerKey.equals(header.headerKey) : header.headerKey != null)
            return false;
        if (headerName != null ? !headerName.equals(header.headerName) : header.headerName != null)
            return false;
        return calendar != null ? calendar.equals(header.calendar) : header.calendar == null;
    }

    @Override
    public int hashCode() {
        int result = parentHeaderKey != null ? parentHeaderKey.hashCode() : 0;
        result = 31 * result + (parentHeaderName != null ? parentHeaderName.hashCode() : 0);
        result = 31 * result + (headerKey != null ? headerKey.hashCode() : 0);
        result = 31 * result + (headerName != null ? headerName.hashCode() : 0);
        result = 31 * result + (calendar != null ? calendar.hashCode() : 0);
        return result;
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
