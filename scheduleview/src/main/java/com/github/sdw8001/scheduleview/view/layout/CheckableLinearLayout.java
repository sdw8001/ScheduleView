package com.github.sdw8001.scheduleview.view.layout;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;

import com.github.sdw8001.scheduleview.view.UsableStrokeDrawable;

/**
 * Created by sdw80 on 2016-11-17.
 * Checkable LinearLayout
 */

public class CheckableLinearLayout extends LinearLayout implements Checkable {
    private final Rect mBounds = new Rect();
    private boolean mChecked = false;
    private boolean mCheckable = true;
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    @ColorInt
    private int mBackgroundColor = Color.LTGRAY;
    private OnCheckedChangeListener mOnCheckedChangeListener;

    public CheckableLinearLayout(Context context) {
        this(context, null);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackground(getCheckableStateDrawable(new ColorDrawable(mBackgroundColor)));
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        MarginLayoutParams params = new LayoutParams();
        setLayoutParams(new LinearLayout.LayoutParams(params));
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

    /**********************/
    /**   Handle clicks  **/
    /**********************/
    @Override
    public boolean performClick() {
        if (mCheckable) {
            toggle();
            return super.performClick();
        } else {
            return false;
        }
    }

    // 시작 위치를 저장을 위한 변수 
    private float mLastMotionX = 0;
    private float mLastMotionY = 0;
    //  마우스 move 로 일정범위 벗어나면 취소하기 위한  값
    private int mTouchSlop;

    // long click 을  위한  변수들 
    private boolean mHasPerformedLongPress;
    private CheckForLongPress mPendingCheckForLongPress;
    private Handler mHandler;

    // Long Click을 처리할  Runnable 입니다. 
    class CheckForLongPress implements Runnable {
        public void run() {
            if (performLongClick()) {
                mHasPerformedLongPress = true;
            }
        }
    }

    // Long Click 처리 설정을 위한 함수 
    private void postCheckForLongClick(int delayOffset) {
        mHasPerformedLongPress = false;

        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }

        if (mHandler == null)
            mHandler = new Handler();

        mHandler.postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout() - delayOffset);
        // 여기서  시스템의  getLongPressTimeout() 후에 message 수행하게 합니다.  
        // 추가 delay가 필요한 경우를 위해서  파라미터로 조절가능하게 합니다.
    }

    /**
     * Remove the longpress detection timer.
     * 중간에 취소하는 용도입니다.
     */
    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            mHandler.removeCallbacks(mPendingCheckForLongPress);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        boolean returnValue = true;
        final float x = event.getRawX();
        final float y = event.getRawY();
        final int deltaX = Math.abs((int) (mLastMotionX - x));
        final int deltaY = Math.abs((int) (mLastMotionY - y));
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = event.getRawX();
                mLastMotionY = event.getRawY();    // 시작 위치 저장

                mHasPerformedLongPress = false;
                postCheckForLongClick(0);   //  Long click message 설정

                this.setPressed(true);
                returnValue = true;
                break;
            case MotionEvent.ACTION_MOVE:

                // 일정 범위 벗어나면  취소함
                if (deltaX >= mTouchSlop || deltaY >= mTouchSlop) {
                    if (!mHasPerformedLongPress) {
                        // This is a tap, so remove the longpress check
                        removeLongPressCallback();
                    }
                }
                returnValue = true;
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!mHasPerformedLongPress) {
                    // This is a tap, so remove the longpress check
                    removeLongPressCallback();
                }
                this.setPressed(false);
                returnValue = true;
                break;
            case MotionEvent.ACTION_UP:
                if (!mHasPerformedLongPress) {
                    //  Long Click을 처리되지 않았으면 제거함.
                    removeLongPressCallback();

                    // Short Click 처리 루틴을 여기에 넣으면 됩니다. 
                    if (deltaX < mTouchSlop && deltaY < mTouchSlop) {
                        performClick();
                    }
                    this.setPressed(false);
                    returnValue = true;
                }
                break;
        }
        return returnValue; // false;
    }

    @Override
    public boolean performLongClick() {
        //  실제 Long Click 처리하는 부분을 여기 둡니다.
        return super.performClick();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return onTouchEvent(ev);
    }

    /**************************/
    /**      Checkable       **/
    /**************************/
    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mCheckable && mChecked != checked) {
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
    public float getElevation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return super.getElevation();
        } else {
            return ViewCompat.getElevation(this);
        }
    }

    @Override
    public void setElevation(float elevation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.setElevation(elevation);
        } else {
            ViewCompat.setElevation(this, elevation);
        }
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(@ColorInt int backgroundColor) {
        if (this.mBackgroundColor != backgroundColor) {
            this.mBackgroundColor = backgroundColor;
            // setChecked(!mChecked) 를 두번 호출해줌으로 Background 초기화 가 잘 안되는 부분 해결. 근본적인 원인을 알면 좀 더 좋은 방법을 찾을 수 있을것 같은데..
            setBackground(getCheckableStateDrawable(new ColorDrawable(mBackgroundColor)));
            setChecked(!mChecked);
            setChecked(!mChecked);
        }
    }

    public static StateListDrawable getCheckableStateDrawable(ColorDrawable color) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.setEnterFadeDuration(100);
        drawable.setExitFadeDuration(100);

        // checked
        ColorDrawable checkedColorDrawable = new ColorDrawable(color.getColor());
        ColorDrawable uncheckedColorDrawable = new ColorDrawable(color.getColor());
        drawable.addState(new int[]{android.R.attr.state_checked}, checkedColorDrawable);

        // pressed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.addState(new int[]{android.R.attr.state_pressed}, generateRippleDrawable(color.getColor(), new Rect()));
        } else {
            ColorDrawable pressedColorDrawable = (ColorDrawable) color.mutate();
            pressedColorDrawable.setColor(getColorWithAlpha(pressedColorDrawable.getColor(), 0.75f));
            drawable.addState(new int[]{android.R.attr.state_pressed}, pressedColorDrawable);
        }

        // unchecked
        uncheckedColorDrawable.setColor(getColorWithAlpha(uncheckedColorDrawable.getColor(), 0.15f));
        drawable.addState(new int[]{-android.R.attr.state_checked}, uncheckedColorDrawable);
        return drawable;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static Drawable generateRippleDrawable(final int color, Rect bounds) {
        ColorStateList list = ColorStateList.valueOf(color);
        Drawable mask = generateRoundRectDrawable(Color.WHITE, false);
        RippleDrawable rippleDrawable = new RippleDrawable(list, null, mask);
//        API 21
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            rippleDrawable.setBounds(bounds);
        }

//        API 22. Technically harmless to leave on for API 21 and 23, but not worth risking for 23+
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
            int center = (bounds.left + bounds.right) / 2;
            rippleDrawable.setHotspotBounds(center, bounds.top, center, bounds.bottom);
        }

        return rippleDrawable;
    }

    private static Drawable generateRoundRectDrawable(final int color, boolean useStroke) {
        float[] outerRadius = new float[]{6, 6, 6, 6, 6, 6, 6, 6};
        UsableStrokeDrawable drawable = new UsableStrokeDrawable(new RoundRectShape(outerRadius, null, null));
        drawable.setFillColor(color);
        drawable.setUseStroke(useStroke);
        return drawable;
    }

    public static int getColorWithAlpha(int color, float ratio) {
        int newColor;
//        int alpha = Math.round(Color.alpha(color) * ratio);
//        int r = Color.red(color);
//        int g = Color.green(color);
//        int b = Color.blue(color);
        int colorMax = 255;
        int alpha = Color.alpha(color);
        int red = (colorMax - Color.red(color));
        int green = (colorMax - Color.green(color));
        int blue = (colorMax - Color.blue(color));
        int r = Color.red(color) + Math.round(red * (1 - ratio));
        int g = Color.green(color) + Math.round(green * (1 - ratio));
        int b = Color.blue(color) + Math.round(blue * (1 - ratio));
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    /**************************/
    /**   Drawable States    **/
    /**************************/
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

        Drawable drawable = getBackground();
        if (drawable != null) {
            int[] myDrawableState = getDrawableState();
            drawable.setState(myDrawableState);
            invalidate();
        }
    }

    /**************************/
    /**   State persistency  **/
    /**************************/
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
            return "CheckableLinearLayout.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + checked + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        // Force our ancestor class to save its state
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.checked = isChecked();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());
        setChecked(ss.checked);
        requestLayout();
    }

    public boolean isCheckable() {
        return mCheckable;
    }

    public void setCheckable(boolean checkable) {
        this.mCheckable = checkable;
    }

    public void setMargin(int left, int top, int right, int bottom) {
        if (getLayoutParams() instanceof MarginLayoutParams) {
            ((MarginLayoutParams) getLayoutParams()).setMargins(dpToPx(left), dpToPx(top), dpToPx(right), dpToPx(bottom));
        }
    }

    // getter & setter 메서드
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
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
     *
     * @param dp DP
     * @return PX
     */
    private int dpToPx(int dp) {
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return dp;
    }

    /**
     * px 값을 dp 값으로 반환
     *
     * @param px PX
     * @return DP
     */
    private int pxToDp(int px) {
//        return (int) (px / getContext().getResources().getDisplayMetrics().density);
        return px;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(CheckableLinearLayout checkableView, boolean checked);
    }

    public static class LayoutParams extends MarginLayoutParams {

        /**
         * {@inheritDoc}
         */
        @SuppressWarnings("ResourceType")
        public LayoutParams() {
            super(MATCH_PARENT, MATCH_PARENT);
        }
    }
}