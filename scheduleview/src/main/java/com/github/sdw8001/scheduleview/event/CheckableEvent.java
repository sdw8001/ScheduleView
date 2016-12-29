package com.github.sdw8001.scheduleview.event;

import android.graphics.Rect;

import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-11-21.
 * Checkable Event abstract class.
 */

public abstract class CheckableEvent {
    private TreeNode<ScheduleHeader> mHeaderNode;
    private Rect mBounds = new Rect();
    private Rect mPadding = new Rect();
    private Rect mMargin = new Rect();
    private Calendar mStartTime;
    private Calendar mEndTime;
    private int mDurationTime;

    public CheckableEvent() {
        mStartTime = Calendar.getInstance();
        mEndTime = (Calendar) mStartTime.clone();
    }

    public TreeNode<ScheduleHeader> getHeaderNode() {
        return mHeaderNode;
    }

    public void setHeaderNode(TreeNode<ScheduleHeader> headerNode) {
        this.mHeaderNode = headerNode;
    }

    public Rect getBounds() {
        return mBounds;
    }

    public Rect getMargin() {
        return mMargin;
    }

    public Rect getPadding() {
        return mPadding;
    }

    public int getDurationTime() {
        return mDurationTime;
    }

    public void setDurationTime(int durationTime) {
        this.mDurationTime = durationTime;
    }

    public Calendar getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Calendar endTime) {
        this.mEndTime = endTime;
    }

    public Calendar getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Calendar startTime) {
        this.mStartTime = startTime;
    }
}
