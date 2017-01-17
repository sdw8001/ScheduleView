package com.github.sdw8001.scheduleview.view.layout;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-11-11.
 * Checkable ScheduleCardView. (CardView Style)
 */

public class ScheduleCellView extends CheckableLinearLayout implements CheckableLinearLayout.OnCheckedChangeListener {

    private final Rect mBounds = new Rect();
    private TextView txtView_Contents;
    private TreeNode<ScheduleHeader> mHeaderNode;
    private Calendar mTimeStart;
    private Calendar mTimeEnd;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public ScheduleCellView(Context context) {
        this(context, null);
    }

    public ScheduleCellView(Context context, AttributeSet attrs) {
        super(context, attrs);
        txtView_Contents = new TextView(context);
        addView(txtView_Contents);
        super.setOnCheckedChangeListener(this);
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


    public String getContents() {
        return txtView_Contents.getText().toString();
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

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }

    @Override
    public void onCheckedChanged(CheckableLinearLayout checkableView, boolean checked) {
        if (mOnCheckedChangeListener != null)
            mOnCheckedChangeListener.onCheckedChanged(this, checked);
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(ScheduleCellView checkableView, boolean checked);
    }
}