package com.github.sdw8001.scheduleview.view.layout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;

import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;

import org.w3c.dom.Text;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-11-11.
 * Checkable ScheduleCardView. (CardView Style)
 */

public class ScheduleEventView extends CardView implements Checkable {
    private boolean mChecked;
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private CheckableLinearLayout linearLayout_Main;
    private TextView txtView_Contents;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    @ColorInt
    private int mBackgroundColor = Color.BLUE;
    private final RectF mBounds = new RectF();
    public float left;
    public float width;
    public float top;
    public float bottom;
    private TreeNode<ScheduleHeader> mHeaderNode;
    private Calendar mTimeStart;
    private Calendar mTimeEnd;
    private boolean mInterceptTouched = false;

    public ScheduleEventView(Context context) {
        this(context, null);
    }

    public ScheduleEventView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 모서리 사각형으로 만들기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setRadius(10);
        }
        txtView_Contents = new TextView(context);
        linearLayout_Main = new CheckableLinearLayout(context);
        linearLayout_Main.setBackgroundColor(mBackgroundColor);
        linearLayout_Main.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        linearLayout_Main.addView(txtView_Contents);
        addView(linearLayout_Main);

        MarginLayoutParams params = new LayoutParams();
        setLayoutParams(new CardView.LayoutParams(params));
    }

    @Override
    public Parcelable onSaveInstanceState() {
        // Force our ancestor class to save its state
        Parcelable superState = super.onSaveInstanceState();
        ScheduleEventView.SavedState ss = new ScheduleEventView.SavedState(superState);

        ss.checked = isChecked();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        ScheduleEventView.SavedState ss = (ScheduleEventView.SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());
        setChecked(ss.checked);
        requestLayout();
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        linearLayout_Main.layout(0, 0, right - left, bottom - top);

        // 위치이동이 있다면 mBounds 값을 바꿔준다.
        if (left != mBounds.left || top != mBounds.top || right != mBounds.right || bottom != mBounds.bottom) {
            mBounds.set(left, top, right, bottom);
        }
//        if (left != linearLayout_Main.getBounds().left || top != mBounds.top || right != linearLayout_Main.getBounds().right || bottom != linearLayout_Main.getBounds().bottom) {
//            linearLayout_Main.layout(left, top, right, bottom);
//        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mInterceptTouched = !mInterceptTouched;
        return !mInterceptTouched;
    }

    @Override
    public void setElevation(float elevation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.setElevation(elevation);
        } else {
            ViewCompat.setElevation(this, elevation);
        }
    }

    // Checkable 구현 메서드
    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
            setCheckedRecursive(this, checked);
            if (mOnCheckedChangeListener != null)
                mOnCheckedChangeListener.onCheckedChanged(this, checked);
        }
    }

    private void setCheckedRecursive(ViewGroup parent, boolean checked) {
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = parent.getChildAt(i);
            if (v instanceof Checkable) {
                ((Checkable) v).setChecked(checked);
            }

            if (v instanceof ViewGroup) {
                setCheckedRecursive((ViewGroup) v, checked);
            }
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public boolean performClick() {
        toggle();
        return super.performClick();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final Drawable drawable = getBackground();
        if (drawable != null) {
            final int[] myDrawableState = getDrawableState();
            drawable.setState(myDrawableState);
            invalidate();
        }
    }

    // getter & setter 메서드
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }

    public String getContents() {
        return txtView_Contents.getText().toString();
    }

    public void setContents(String contents) {
        this.txtView_Contents.setText(contents);
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
        linearLayout_Main.setBackgroundColor(mBackgroundColor);
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

    public boolean isInterceptTouched() {
        return mInterceptTouched;
    }

    public void setInterceptTouched(boolean interceptTouched) {
        this.mInterceptTouched = interceptTouched;
    }

    public RectF getBounds() {
        return mBounds;
    }

    public void setMargin(int left, int top, int right, int bottom) {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            ((MarginLayoutParams) getLayoutParams()).setMargins(dpToPx(left), dpToPx(top), dpToPx(right), dpToPx(bottom));
        }
    }

    public int getMarginLeft() {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            return pxToDp(((MarginLayoutParams) getLayoutParams()).leftMargin);
        } else {
            return 0;
        }
    }

    public void setMarginLeft(int marginLeft) {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            ((MarginLayoutParams) getLayoutParams()).leftMargin = dpToPx(marginLeft);
        }
    }

    public int getMarginTop() {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            return pxToDp(((MarginLayoutParams) getLayoutParams()).topMargin);
        } else {
            return 0;
        }
    }

    public void setMarginTop(int marginTop) {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            ((MarginLayoutParams) getLayoutParams()).topMargin = dpToPx(marginTop);
        }
    }

    public int getMarginRight() {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            return pxToDp(((MarginLayoutParams) getLayoutParams()).rightMargin);
        } else {
            return 0;
        }
    }

    public void setMarginRight(int marginRight) {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            ((MarginLayoutParams) getLayoutParams()).rightMargin = dpToPx(marginRight);
        }
    }

    public int getMarginBottom() {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            return pxToDp(((MarginLayoutParams) getLayoutParams()).bottomMargin);
        } else {
            return 0;
        }
    }

    public void setMarginBottom(int marginBottom) {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            ((MarginLayoutParams) getLayoutParams()).bottomMargin = dpToPx(marginBottom);
        }
    }

    /**
     * dp 값을 px 값으로 반환
     * @param dp DP
     * @return PX
     */
    private int dpToPx(int dp) {
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return dp;
    }

    /**
     * px 값을 dp 값으로 반환
     * @param px PX
     * @return DP
     */
    private int pxToDp(int px) {
//        return (int) (px / getContext().getResources().getDisplayMetrics().density);
        return px;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(ScheduleEventView checkableView, boolean checked);
    }

    public static class LayoutParams extends MarginLayoutParams {

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("ResourceType")
        public LayoutParams() {
            super(WRAP_CONTENT, WRAP_CONTENT);
        }
    }

    static class SavedState extends BaseSavedState {
        boolean checked;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            checked = (Boolean) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(checked);
        }

        @Override
        public String toString() {
            return "ScheduleCardView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + checked + "}";
        }

        public static final Parcelable.Creator<ScheduleEventView.SavedState> CREATOR
                = new Parcelable.Creator<ScheduleEventView.SavedState>() {
            public ScheduleEventView.SavedState createFromParcel(Parcel in) {
                return new ScheduleEventView.SavedState(in);
            }

            public ScheduleEventView.SavedState[] newArray(int size) {
                return new ScheduleEventView.SavedState[size];
            }
        };
    }

    /**
     * 같은 노드인지 확인하여 equal 결과 반환.
     * @param headerTreeNode1 header 1
     * @param headerTreeNode2 header 2
     * @return 노드 equal 결과
     */
    public static boolean isEventSameHeader(TreeNode<ScheduleHeader> headerTreeNode1, TreeNode<ScheduleHeader> headerTreeNode2) {
        // 노드의 부모키가 같으면서 노드의 키가 같으면 true 반환. 부모가 다르거나 노드가 다르면 false
        if (headerTreeNode1 == null || headerTreeNode2 == null)
            return false;

        if (headerTreeNode1.getParent() == null && headerTreeNode2.getParent() == null) {
            return TextUtils.equals(headerTreeNode1.getData().getKey(), headerTreeNode2.getData().getKey());
        }

        if (headerTreeNode1.getParent() != null && headerTreeNode2.getParent() != null) {
            return TextUtils.equals(headerTreeNode1.getParent().getData().getKey(), headerTreeNode2.getParent().getData().getKey())
                    && TextUtils.equals(headerTreeNode1.getData().getKey(), headerTreeNode2.getData().getKey());
        }
        return false;
    }
}
