package com.github.sdw8001.scheduleview.header;

import android.graphics.RectF;

import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-05-03.
 */
public class GroupHeader extends Header {
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

    public List<Header> getSubHeaders() {
        return subHeaders;
    }

    public void setSubHeaders(List<Header> subHeaders) {
        this.subHeaders = subHeaders;
    }
}
