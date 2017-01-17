package com.github.sdw8001.scheduleview.view.layout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;

/**
 * Created by sdw80 on 2016-11-30.
 *
 */

public class CheckableHeaderView extends CheckableLinearLayout {

    private final Rect mBounds = new Rect();
    private TreeNode<ScheduleHeader> headerNode;
    private TextView contents;

    public CheckableHeaderView(Context context) {
        this(context, null);
    }

    public CheckableHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        contents = new TextView(context);
        contents.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(contents, params);
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
        return contents;
    }

    public TreeNode<ScheduleHeader> getHeaderNode() {
        return headerNode;
    }

    public void setHeaderNode(TreeNode<ScheduleHeader> headerNode) {
        this.headerNode = headerNode;
    }
}
