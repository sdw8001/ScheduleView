package com.github.sdw8001.scheduleview.view.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.sdw8001.scheduleview.event.ScheduleEvent;
import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;
import com.github.sdw8001.scheduleview.util.ScheduleViewUtil;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-11-11.
 * Checkable ScheduleCardView. (CardView Style)
 */

public class ScheduleEventView extends CardView implements Checkable {
    private static final float DEFAULT_TYPE_COLOR_WIDTH = 13;
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    private ScheduleEvent event;
    private TextView txtView_Contents;
    private CheckableLinearLayout linearLayout_Main;
    private OnCheckedChangeListener onCheckedChangeListener;
    private Paint eventTypeColorPaint;
    private Paint eventTypeDetailBackPaint;
    private Paint eventTypeDetailForePaint;
    private boolean mChecked;
    private boolean interceptTouched = false;
    private boolean useTypeColor = false;
    private int mEventRectShadowRadius = 5;
    private int mEventTextSize = 12;
    private int mEventTypeDetailTextSize = 12;
    private float typeColorWidth = DEFAULT_TYPE_COLOR_WIDTH;
    public float left;
    public float width;
    public float top;
    public float bottom;

    private float typeDetailPadding = 6;
    private float typeDetailWidth;
    private float typeDetailHeight;
    private float typeDetailX;
    private float typeDetailY;
    private float typeDetailLeft;
    private float typeDetailTop;
    private float typeDetailRight;
    private float typeDetailBottom;
    private float typeDetailCornerRadius;

    public ScheduleEventView(Context context) {
        this(context, null);
    }

    public ScheduleEventView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (this.event == null) {
            this.event = new ScheduleEvent();
            this.useTypeColor = event.getTypeColor() != event.getBackgroundColor();
        }

        // 모서리 사각형으로 만들기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setRadius(10);
        }
        this.txtView_Contents = new TextView(context);
        this.txtView_Contents.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        this.txtView_Contents.setTextSize(this.mEventTextSize);
        if (this.useTypeColor)
            this.txtView_Contents.setPadding((int) typeColorWidth, this.txtView_Contents.getPaddingTop(), this.txtView_Contents.getPaddingRight(), this.txtView_Contents.getPaddingBottom());
        else
            this.txtView_Contents.setPadding(this.txtView_Contents.getPaddingLeft(), this.txtView_Contents.getPaddingTop(), this.txtView_Contents.getPaddingRight(), this.txtView_Contents.getPaddingBottom());
        this.linearLayout_Main = new CheckableLinearLayout(context);
        this.linearLayout_Main.setBackgroundColor(this.event.getBackgroundColor());
        this.linearLayout_Main.setPadding(dpToPx(10), dpToPx(10), dpToPx(10), dpToPx(10));
        this.linearLayout_Main.addView(this.txtView_Contents);
        this.addView(this.linearLayout_Main);

        MarginLayoutParams params = new LayoutParams();
        this.setLayoutParams(new CardView.LayoutParams(params));

        // Prepare event type paint.
        this.eventTypeColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.eventTypeColorPaint.setColor(Color.rgb(174, 208, 238));

        // Prepare event TypeDetailFore paint.
        this.eventTypeDetailForePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.eventTypeDetailForePaint.setTextAlign(Paint.Align.LEFT);
        this.eventTypeDetailForePaint.setTextSize(ScheduleViewUtil.getResizedDensity(getContext(), this.mEventTypeDetailTextSize));
        this.eventTypeDetailForePaint.setShadowLayer(this.mEventRectShadowRadius / 2, 0, 0, Color.GRAY);
        this.eventTypeDetailForePaint.setFakeBoldText(true);

        // Prepare event TypeDetailBack paint.
        this.eventTypeDetailBackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.eventTypeDetailBackPaint.setShadowLayer(this.mEventRectShadowRadius, 0, 0, Color.GRAY);
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
        this.linearLayout_Main.layout(0, 0, right - left, bottom - top);

        // 위치이동이 있다면 mBounds 값을 바꿔준다.
        if (left != this.event.getBounds().left || top != this.event.getBounds().top || right != this.event.getBounds().right || bottom != this.event.getBounds().bottom) {
            this.event.getBounds().set(left, top, right, bottom);
        }
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        this.interceptTouched = !interceptTouched;
//        return !this.interceptTouched;
//    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return onTouchEvent(ev);
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

    // Checkable 구현 메서드
    @Override
    public void setChecked(boolean checked) {
        if (this.mChecked != checked) {
            this.mChecked = checked;
            refreshDrawableState();
            setCheckedRecursive(this, checked);
            if (this.onCheckedChangeListener != null)
                this.onCheckedChangeListener.onCheckedChanged(this, checked);
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
        return this.mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!this.mChecked);
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

    @Override
    public void setRadius(float radius) {
        super.setRadius(radius);
        this.typeColorWidth = radius <= 0 ? DEFAULT_TYPE_COLOR_WIDTH : radius + 3;
        this.typeDetailCornerRadius = radius / 2;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        float height = canvas.getHeight();

        // Draw TypeColor
        if (useTypeColor)
            canvas.drawRect(new RectF(getPaddingLeft(), getPaddingTop(), this.typeColorWidth, height - getPaddingBottom()), this.eventTypeColorPaint);

        // Draw TypeDetail
        if (!("G,S,R,N").contains(event.getTypeDetail()))
            canvas.drawRoundRect(new RectF(typeDetailLeft, typeDetailTop, typeDetailRight, typeDetailBottom), typeDetailCornerRadius, typeDetailCornerRadius, eventTypeDetailBackPaint);
        canvas.drawText(event.getTypeDetail(), typeDetailX, typeDetailY, eventTypeDetailForePaint);
    }

    // getter & setter 메서드
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.onCheckedChangeListener = listener;
    }

    public int getEventTextSize() {
        return mEventTextSize;
    }

    public void setEventTextSize(int eventTextSize) {
        this.mEventTextSize = eventTextSize;
        this.txtView_Contents.setTextSize(eventTextSize);
        this.setLayoutTypeDetail(this.event);
    }

    public int getEventTypeDetailTextSize() {
        return this.mEventTypeDetailTextSize;
    }

    public void setEventTypeDetailTextSize(int eventTypeDetailTextSize) {
        this.mEventTypeDetailTextSize = eventTypeDetailTextSize;
        this.eventTypeDetailForePaint.setTextSize(ScheduleViewUtil.getResizedDensity(getContext(), eventTypeDetailTextSize));
    }

    public String getContents() {
        return txtView_Contents.getText().toString();
    }

    public void setContents(String contents) {
        this.txtView_Contents.setText(contents);
    }

    public int getBackgroundColor() {
        return this.event.getBackgroundColor();
    }

    public void setBackgroundColor(int backgroundColor) {
        this.event.setBackgroundColor(backgroundColor);
        this.linearLayout_Main.setBackgroundColor(backgroundColor);
    }

    public TreeNode<ScheduleHeader> getHeaderNode() {
        return event.getHeaderNode();
    }

    public void setHeaderNode(TreeNode<ScheduleHeader> headerNode) {
        this.event.setHeaderNode(headerNode);
    }

    public ScheduleEvent getEvent() {
        return event;
    }

    public void setEvent(ScheduleEvent event) {
        this.event = event;
        this.setBackgroundColor(event.getBackgroundColor());
        this.eventTypeColorPaint.setColor(event.getTypeColor());
        this.eventTypeDetailForePaint.setColor(event.getTypeDetailForeColor());
        this.eventTypeDetailBackPaint.setColor(event.getTypeDetailBackColor());
        this.setLayoutTypeDetail(event);
        this.setContents(event.getContents());
    }

    /**
     * event 의 정보들을 이용하여 TypeDetail 의 Layout 을 정하고 그에 따른 Content 의 Layout 위치를 조정한다.
     * @param event
     */
    private void setLayoutTypeDetail(ScheduleEvent event) {
        // TypeColor 적용 유무 설정
        this.useTypeColor = event.getTypeColor() != event.getBackgroundColor();
        float textWidth = eventTypeDetailForePaint.measureText(event.getTypeDetail());
        float textHeight = -(eventTypeDetailForePaint.ascent() + eventTypeDetailForePaint.descent());
        this.typeDetailWidth = typeDetailPadding * 2 + textWidth;
        this.typeDetailHeight = typeDetailPadding * 2 + textHeight;

        // txtContents 의 PaddingLeft 변수 - TypeColor 와 TypeDetail 에 따라 결정된다
        int paddingLeft = (int) typeDetailWidth;

        // TypeDetail RoundRect 정보 설정
        this.typeDetailLeft = typeDetailPadding;
        if (this.useTypeColor) {
            paddingLeft += typeColorWidth;
            typeDetailLeft += typeColorWidth;
        }
        this.typeDetailTop = typeDetailPadding;
        this.typeDetailRight = typeDetailLeft + typeDetailWidth;
        this.typeDetailBottom = typeDetailTop + typeDetailHeight;
        this.typeDetailCornerRadius = getRadius() / 2;

        // TypeDetail Text 그려질 좌표
        this.typeDetailX = this.typeDetailLeft + typeDetailPadding;
        this.typeDetailY = this.typeDetailTop + typeDetailPadding + textHeight;

        this.txtView_Contents.setPadding(paddingLeft, this.txtView_Contents.getPaddingTop(), this.txtView_Contents.getPaddingRight(), this.txtView_Contents.getPaddingBottom());
    }

    public Calendar getTimeStart() {
        return event.getStartTime();
    }

    public void setTimeStart(Calendar timeStart) {
        this.event.setStartTime((Calendar) timeStart.clone());
    }

    public Calendar getTimeEnd() {
        return this.event.getEndTime();
    }

    public void setTimeEnd(Calendar timeEnd) {
        this.event.setEndTime((Calendar) timeEnd.clone());
    }

    public boolean isInterceptTouched() {
        return this.interceptTouched;
    }

    public void setInterceptTouched(boolean interceptTouched) {
        this.interceptTouched = interceptTouched;
    }

    public RectF getBounds() {
        return this.event.getBounds();
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
