package com.github.sdw8001.scheduleview.view.layout;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-11-11.
 * Checkable ScheduleCardView. (CardView Style)
 */

public class ScheduleCellView extends CheckableLinearLayout {

    private final Rect mBounds = new Rect();
    private TextView mContents;
    private TreeNode<ScheduleHeader> mHeaderNode;
    private Calendar mTimeStart;
    private Calendar mTimeEnd;

    public ScheduleCellView(Context context) {
        this(context, null);
    }

    public ScheduleCellView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContents = new TextView(context);
        addView(mContents);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // 위치이동이 있다면 mBounds 값을 바꿔준다.
        if (left != mBounds.left || top != mBounds.top || right != mBounds.right || bottom != mBounds.bottom)
            mBounds.set(left, top, right, bottom);
    }

    public Rect getBounds() {
        return mBounds;
    }

    public TextView getContents() {
        return mContents;
    }

    public TreeNode<ScheduleHeader> getHeaderNode() {
        return mHeaderNode;
    }

    public void setHeaderNode(TreeNode<ScheduleHeader> headerNode) {
        this.mHeaderNode = headerNode;
    }

    public Calendar getTimeStart() {
        return mTimeStart;
    }

    public void setTimeStart(Calendar timeStart) {
        this.mTimeStart = (Calendar) timeStart.clone();
    }

    public Calendar getTimeEnd() {
        return mTimeEnd;
    }

    public void setTimeEnd(Calendar timeEnd) {
        this.mTimeEnd = (Calendar) timeEnd.clone();
    }
}