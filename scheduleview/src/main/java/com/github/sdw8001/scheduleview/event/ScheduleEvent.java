package com.github.sdw8001.scheduleview.event;

import android.graphics.Color;
import android.support.annotation.ColorInt;

/**
 * Created by sdw80 on 2016-11-28.
 *
 */

public class ScheduleEvent extends CheckableEvent {
    private String key;
    private String contents;
    private String typeDetail;
    @ColorInt
    private int backgroundColor = Color.WHITE;
    @ColorInt
    private int typeColor = Color.WHITE;
    @ColorInt
    private int typeDetailForeColor = Color.WHITE;
    @ColorInt
    private int typeDetailBackColor = Color.WHITE;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getTypeColor() {
        return typeColor;
    }

    public void setTypeColor(int typeColor) {
        this.typeColor = typeColor;
    }

    public String getTypeDetail() {
        return typeDetail;
    }

    public void setTypeDetail(String typeDetail) {
        this.typeDetail = typeDetail;
    }

    public int getTypeDetailBackColor() {
        return typeDetailBackColor;
    }

    public void setTypeDetailBackColor(int typeDetailBackColor) {
        this.typeDetailBackColor = typeDetailBackColor;
    }

    public int getTypeDetailForeColor() {
        return typeDetailForeColor;
    }

    public void setTypeDetailForeColor(int typeDetailForeColor) {
        this.typeDetailForeColor = typeDetailForeColor;
    }
}
