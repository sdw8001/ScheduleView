package com.github.sdw8001.scheduleview.header;

import android.graphics.RectF;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-05-03.
 */
public class GroupHeader extends Header implements Serializable {
    private List<Header> subHeaders;
    private RectF rectF;

    public RectF getRectF() {
        return rectF;
    }

    public void setRectF(RectF rectF) {
        this.rectF = rectF;
    }

    public GroupHeader(Calendar calendar, String parentHeaderName, String parentHeaderKey, String headerName, String headerKey, List<Header> subHeaders) {
        super(calendar, parentHeaderName, parentHeaderKey, headerKey, headerName);
        this.subHeaders = subHeaders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GroupHeader that = (GroupHeader) o;

        return subHeaders != null ? subHeaders.equals(that.subHeaders) : that.subHeaders == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (subHeaders != null ? subHeaders.hashCode() : 0);
        return result;
    }

    public List<Header> getSubHeaders() {
        return subHeaders;
    }

    public void setSubHeaders(List<Header> subHeaders) {
        this.subHeaders = subHeaders;
    }
}
