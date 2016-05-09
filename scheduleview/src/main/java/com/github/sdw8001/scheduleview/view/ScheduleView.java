package com.github.sdw8001.scheduleview.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.github.sdw8001.scheduleview.DateTimeInterpreter;
import com.github.sdw8001.scheduleview.R;
import com.github.sdw8001.scheduleview.event.ScheduleViewEvent;
import com.github.sdw8001.scheduleview.header.GroupHeader;
import com.github.sdw8001.scheduleview.header.Header;
import com.github.sdw8001.scheduleview.interpreter.HeaderInterpreter;
import com.github.sdw8001.scheduleview.loader.ScheduleLoader;
import com.github.sdw8001.scheduleview.loader.ScheduleViewLoader;
import com.github.sdw8001.scheduleview.util.ScheduleViewUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Created by sdw80 on 2016-04-21.
 */
public class ScheduleView extends View {

    private enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    public static final int VIEW_PARENT = 1;
    public static final int VIEW_CHILD = 2;

    private final Context mContext;

    private boolean mAreDimensionsInvalid = true;
    private boolean mIsZooming;
    private boolean mRefreshEvents = false;

    private Calendar mFirstVisibleDay;  // TODO: FirtVisibleDay 는 Calendar 형식이다. 날짜가아닌 다른 Object 를 List up 할 때 해당 변의 선언과 사용부분을 적절히 수정해야 한다.
    private List<EventRect> mEventRects;
    private List<ScheduleRect> mScheduleRects;
    private List<GroupHeader> mGroupHeaderItems;
    private List<? extends ScheduleViewEvent> mCurrentEvents;
    private FloatingActionButton mFloatingActionButton;
    private GroupHeader mFixedGroupHeader;

    // Time Column Text 관련 변수
    private Paint mTimeTextPaint;
    private float mTimeTextWidth;
    private float mTimeTextHeight;

    // Header Text 관련 변수
    private Paint mTodayHeaderTextPaint;
    private Paint mHeaderTextPaint;
    private Paint mHeaderBackgroundPaint;
    private float mHeaderTextHeight;
    private float mHeaderHeight;
    private float mHeaderColumnWidth;

    // Event 관련 변수
    private TextPaint mEventTextPaint;
    private int mEventTextSize = 12;
    private int mEventTextColor = Color.BLACK;
    private int mEventPadding = 8;

    // 기타 Paint 변수
    private Paint mDayBackgroundPaint;
    private Paint mHourSeparatorPaint;
    private Paint mTodayBackgroundPaint;
    private Paint mFutureBackgroundPaint;
    private Paint mPastBackgroundPaint;
    private Paint mFutureWeekendBackgroundPaint;
    private Paint mPastWeekendBackgroundPaint;
    private Paint mNowLinePaint;

    // Draw 에 사용될 Paint 관련
    private Paint mHeaderColumnBackgroundPaint;
    private Paint mEventBackgroundPaint;
    private Paint mCellFocusPaint;

    // Item 의 Width 값
    private float mWidthPerDay;

    private float mHeaderMarginBottom;
    private int mMinimumFlingVelocity = 0;
    private int mScaledTouchSlop = 0;
    private int mOverlappingEventGap = 0;
    private int mDefaultEventColor;
    private int mEventCornerRadius = 0;
    private int mCachedNumberOfVisible = 3;

    // Attributes 의 초기값과 함께 선언
    private int mColumnGap = 10;
    private int mMinHourHeight = 0; //no minimum specified (기준이 되는 base View 의 크기에 따라 동적으로 설정)
    private int mMaxHourHeight = 250;
    private int mEffectiveMinHourHeight = mMinHourHeight; //zoom out 할수 있는 최소 HourHeight
    private int mHourHeight = 50;
    private int mNewHourHeight = -1;
    private int mNumberOfVisibleDays = mCachedNumberOfVisible;
    private int mTextSize = 12;
    private int mHeaderColumnPadding = 10;
    private int mHeaderColumnTextColor = Color.BLACK;
    private int mHeaderRowTextColor = Color.BLACK;
    private int mHeaderColumnBackgroundColor = Color.WHITE;
    private int mHeaderRowPadding = 10;
    private int mHeaderRowBackgroundColor = Color.WHITE;
    private int mDayBackgroundColor = Color.rgb(245, 245, 245);
    private int mPastBackgroundColor = Color.rgb(227, 227, 227);
    private int mFutureBackgroundColor = Color.rgb(245, 245, 245);
    private int mPastWeekendBackgroundColor = 0;
    private int mFutureWeekendBackgroundColor = 0;
    private int mNowLineColor = Color.rgb(102, 102, 102);
    private int mNowLineThickness = 5;
    private int mHourSeparatorColor = Color.rgb(230, 230, 230);
    private int mTodayBackgroundColor = Color.rgb(239, 247, 254);
    private int mHourSeparatorHeight = 2;
    private int mTodayHeaderTextColor = Color.rgb(39, 137, 228);
    private int mAllDayEventHeight = 100;
    private int mScrollDuration = 250;
    private int mHeaderType = Header.HEADER_USING_CALENDAR;
    private int mEventMarginLeft = 0;
    private int mEventMarginTop = 0;
    private int mEventMarginRight = 0;
    private int mEventMarginBottom = 0;
    private float mXScrollingSpeed = 1f;
    private int mEventRectShadowRadius = 5;
    private int mHeaderRowShadowRadius = 5;
    private EventRect mFocusedEventRect = null;
    private ScheduleRect mFocusedEmptyScheduleRect = null;
    private boolean mCellFocusable = true;
    private boolean mEventRectShadowEnabled = true;
    private boolean mHeaderRowShadowEnabled = true;
    private boolean mHorizontalFlingEnabled = true;
    private boolean mVerticalFlingEnabled = true;
    private boolean mShowDistinctWeekendColor = false;
    private boolean mShowDistinctPastFutureColor = false;
    private int mViewMode = VIEW_PARENT;
    private Calendar mScrollToDay = null;
    private double mScrollToHour = -1;

    private PointF mCurrentOrigin = new PointF(0f, 0f);
    private Direction mCurrentScrollDirection = Direction.NONE;
    private Direction mCurrentFlingDirection = Direction.NONE;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetectorCompat mGestureDetector;
    private OverScroller mScroller;

    // Listeners.
    private EventClickListener mEventClickListener;
    private EventLongPressListener mEventLongPressListener;
    private EmptyViewClickListener mEmptyViewClickListener;
    private EmptyViewLongPressListener mEmptyViewLongPressListener;
    private GroupHeaderClickListener mGroupHeaderClickListener;
    private DateTimeInterpreter mDateTimeInterpreter;
    private HeaderInterpreter mHeaderInterpreter;
    private ScheduleViewLoader mScheduleViewLoader;
    private ScrollListener mScrollListener;
    private EventDrawListener mEventDrawListener;

    /////////////////////////////////////////////////////////////////
    //
    //      OnCreate init 관련 함수
    //
    /////////////////////////////////////////////////////////////////

    public ScheduleView(Context context) {
        this(context, null);
    }

    public ScheduleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScheduleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;

        // Get the attribute values (if any).
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScheduleView, 0, 0);
        try {
//            mFirstDayOfWeek = a.getInteger(R.styleable.ScheduleView_firstDayOfWeek, mFirstDayOfWeek);
            mHourHeight = a.getDimensionPixelSize(R.styleable.ScheduleView_hourHeight, mHourHeight);
            mMinHourHeight = a.getDimensionPixelSize(R.styleable.ScheduleView_minHourHeight, mMinHourHeight);
            mEffectiveMinHourHeight = mMinHourHeight;
            mMaxHourHeight = a.getDimensionPixelSize(R.styleable.ScheduleView_maxHourHeight, mMaxHourHeight);
            mTextSize = a.getDimensionPixelSize(R.styleable.ScheduleView_textSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, context.getResources().getDisplayMetrics()));
            mHeaderColumnPadding = a.getDimensionPixelSize(R.styleable.ScheduleView_headerColumnPadding, mHeaderColumnPadding);
            mColumnGap = a.getDimensionPixelSize(R.styleable.ScheduleView_columnGap, mColumnGap);
            mHeaderColumnTextColor = a.getColor(R.styleable.ScheduleView_headerColumnTextColor, mHeaderColumnTextColor);
            mHeaderRowTextColor = a.getColor(R.styleable.ScheduleView_headerRowTextColor, mHeaderRowTextColor);
            mNumberOfVisibleDays = a.getInteger(R.styleable.ScheduleView_noOfVisibleDays, mNumberOfVisibleDays);
//            mShowFirstDayOfWeekFirst = a.getBoolean(R.styleable.ScheduleView_showFirstDayOfWeekFirst, mShowFirstDayOfWeekFirst);
            mHeaderRowPadding = a.getDimensionPixelSize(R.styleable.ScheduleView_headerRowPadding, mHeaderRowPadding);
            mHeaderRowBackgroundColor = a.getColor(R.styleable.ScheduleView_headerRowBackgroundColor, mHeaderRowBackgroundColor);
            mDayBackgroundColor = a.getColor(R.styleable.ScheduleView_dayBackgroundColor, mDayBackgroundColor);
            mFutureBackgroundColor = a.getColor(R.styleable.ScheduleView_futureBackgroundColor, mFutureBackgroundColor);
            mPastBackgroundColor = a.getColor(R.styleable.ScheduleView_pastBackgroundColor, mPastBackgroundColor);
            mFutureWeekendBackgroundColor = a.getColor(R.styleable.ScheduleView_futureWeekendBackgroundColor, mFutureBackgroundColor); // If not set, use the same color as in the week
            mPastWeekendBackgroundColor = a.getColor(R.styleable.ScheduleView_pastWeekendBackgroundColor, mPastBackgroundColor);
            mNowLineColor = a.getColor(R.styleable.ScheduleView_nowLineColor, mNowLineColor);
            mNowLineThickness = a.getDimensionPixelSize(R.styleable.ScheduleView_nowLineThickness, mNowLineThickness);
            mHourSeparatorColor = a.getColor(R.styleable.ScheduleView_hourSeparatorColor, mHourSeparatorColor);
            mTodayBackgroundColor = a.getColor(R.styleable.ScheduleView_todayBackgroundColor, mTodayBackgroundColor);
            mHourSeparatorHeight = a.getDimensionPixelSize(R.styleable.ScheduleView_hourSeparatorHeight, mHourSeparatorHeight);
            mTodayHeaderTextColor = a.getColor(R.styleable.ScheduleView_todayHeaderTextColor, mTodayHeaderTextColor);
            mEventTextSize = a.getDimensionPixelSize(R.styleable.ScheduleView_eventTextSize, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mEventTextSize, context.getResources().getDisplayMetrics()));
            mEventTextColor = a.getColor(R.styleable.ScheduleView_eventTextColor, mEventTextColor);
            mEventPadding = a.getDimensionPixelSize(R.styleable.ScheduleView_eventPadding, mEventPadding);
            mHeaderColumnBackgroundColor = a.getColor(R.styleable.ScheduleView_headerColumnBackground, mHeaderColumnBackgroundColor);
//            mDayNameLength = a.getInteger(R.styleable.ScheduleView_dayNameLength, mDayNameLength);
            mOverlappingEventGap = a.getDimensionPixelSize(R.styleable.ScheduleView_overlappingEventGap, mOverlappingEventGap);
            mEventMarginLeft = a.getDimensionPixelSize(R.styleable.ScheduleView_eventMarginLeft, mEventMarginLeft);
            mEventMarginTop = a.getDimensionPixelSize(R.styleable.ScheduleView_eventMarginTop, mEventMarginTop);
            mEventMarginRight = a.getDimensionPixelSize(R.styleable.ScheduleView_eventMarginRight, mEventMarginRight);
            mEventMarginBottom = a.getDimensionPixelSize(R.styleable.ScheduleView_eventMarginBottom, mEventMarginBottom);
            mXScrollingSpeed = a.getFloat(R.styleable.ScheduleView_xScrollingSpeed, mXScrollingSpeed);
            mEventCornerRadius = a.getDimensionPixelSize(R.styleable.ScheduleView_eventCornerRadius, mEventCornerRadius);
            mShowDistinctPastFutureColor = a.getBoolean(R.styleable.ScheduleView_showDistinctPastFutureColor, mShowDistinctPastFutureColor);
            mShowDistinctWeekendColor = a.getBoolean(R.styleable.ScheduleView_showDistinctWeekendColor, mShowDistinctWeekendColor);
            mViewMode = a.getInteger(R.styleable.ScheduleView_viewMode, mViewMode);
//            mShowNowLine = a.getBoolean(R.styleable.ScheduleView_showNowLine, mShowNowLine);
            mEventRectShadowRadius = a.getDimensionPixelSize(R.styleable.ScheduleView_eventRectShadowRadius, mEventRectShadowRadius);
            mHeaderRowShadowRadius = a.getDimensionPixelSize(R.styleable.ScheduleView_headerRowShadowRadius, mHeaderRowShadowRadius);
            mCellFocusable = a.getBoolean(R.styleable.ScheduleView_cellFocusable, mCellFocusable);
            mEventRectShadowEnabled = a.getBoolean(R.styleable.ScheduleView_eventRectShadowEnabled, mEventRectShadowEnabled);
            mHeaderRowShadowEnabled = a.getBoolean(R.styleable.ScheduleView_headerRowShadowEnabled, mHeaderRowShadowEnabled);
            mHorizontalFlingEnabled = a.getBoolean(R.styleable.ScheduleView_horizontalFlingEnabled, mHorizontalFlingEnabled);
            mVerticalFlingEnabled = a.getBoolean(R.styleable.ScheduleView_verticalFlingEnabled, mVerticalFlingEnabled);
            mAllDayEventHeight = a.getDimensionPixelSize(R.styleable.ScheduleView_allDayEventHeight, mAllDayEventHeight);
            mScrollDuration = a.getInt(R.styleable.ScheduleView_scrollDuration, mScrollDuration);
            mHeaderType = a.getInteger(R.styleable.ScheduleView_headerType, mHeaderType);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        // Scrolling initialization.
        mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);
        mScroller = new OverScroller(mContext, new FastOutLinearInInterpolator());

        mMinimumFlingVelocity = ViewConfiguration.get(mContext).getScaledMinimumFlingVelocity();
        mScaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

        // Measure settings for time column.
        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTimeTextPaint.setTextSize(mTextSize);
        mTimeTextPaint.setColor(mHeaderColumnTextColor);
        Rect rect = new Rect();
        mTimeTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        mTimeTextHeight = rect.height();
        mHeaderMarginBottom = mTimeTextHeight / 2;
        initTextTimeWidth();

        // Measure settings for header row.
        mHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderTextPaint.setColor(mHeaderRowTextColor);
        mHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mHeaderTextPaint.setTextSize(mTextSize);
        mHeaderTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);
        mHeaderTextHeight = rect.height();
        mHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        // Prepare header background paint.
        mHeaderBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderBackgroundPaint.setColor(mHeaderRowBackgroundColor);
        if (mHeaderRowShadowEnabled) {
            mHeaderBackgroundPaint.setShadowLayer(mHeaderRowShadowRadius, 0, 0, Color.GRAY);
            this.setLayerType(LAYER_TYPE_SOFTWARE, mHeaderBackgroundPaint);
        }

        // Prepare day background color paint.
        mDayBackgroundPaint = new Paint();
        mDayBackgroundPaint.setColor(mDayBackgroundColor);
        mFutureBackgroundPaint = new Paint();
        mFutureBackgroundPaint.setColor(mFutureBackgroundColor);
        mPastBackgroundPaint = new Paint();
        mPastBackgroundPaint.setColor(mPastBackgroundColor);
        mFutureWeekendBackgroundPaint = new Paint();
        mFutureWeekendBackgroundPaint.setColor(mFutureWeekendBackgroundColor);
        mPastWeekendBackgroundPaint = new Paint();
        mPastWeekendBackgroundPaint.setColor(mPastWeekendBackgroundColor);

        // Prepare hour separator color paint.
        mHourSeparatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourSeparatorPaint.setStyle(Paint.Style.STROKE);
        mHourSeparatorPaint.setStrokeWidth(mHourSeparatorHeight);
        mHourSeparatorPaint.setColor(mHourSeparatorColor);

        // Prepare the "now" line color paint
        mNowLinePaint = new Paint();
        mNowLinePaint.setStrokeWidth(mNowLineThickness);
        mNowLinePaint.setColor(mNowLineColor);

        // Prepare today background color paint.
        mTodayBackgroundPaint = new Paint();
        mTodayBackgroundPaint.setColor(mTodayBackgroundColor);

        // Prepare today header text color paint.
        mTodayHeaderTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTodayHeaderTextPaint.setTextAlign(Paint.Align.CENTER);
        mTodayHeaderTextPaint.setTextSize(mTextSize);
        mTodayHeaderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTodayHeaderTextPaint.setColor(mTodayHeaderTextColor);

        // Prepare event background color.
        mEventBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEventBackgroundPaint.setColor(Color.rgb(174, 208, 238));
        if (mEventRectShadowEnabled) {
            mEventBackgroundPaint.setShadowLayer(mEventRectShadowRadius, 0, 0, Color.GRAY);
            this.setLayerType(LAYER_TYPE_SOFTWARE, mEventBackgroundPaint);
        }

        // Prepare cell focused paint.
        mCellFocusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCellFocusPaint.setColor(Color.rgb(255, 32, 32));
        mCellFocusPaint.setStyle(Paint.Style.STROKE);
        mCellFocusPaint.setStrokeWidth(3);
        mCellFocusPaint.setPathEffect(new DashPathEffect(new float[]{10, 3}, 0));

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint = new Paint();
        mHeaderColumnBackgroundPaint.setColor(mHeaderColumnBackgroundColor);

        // Prepare event text size and color.
        mEventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mEventTextPaint.setStyle(Paint.Style.FILL);
        mEventTextPaint.setColor(mEventTextColor);
        mEventTextPaint.setTextSize(mEventTextSize);

        // Set default event color.
        mDefaultEventColor = Color.parseColor("#9fc6e7");

        mScaleDetector = new ScaleGestureDetector(mContext, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                mIsZooming = false;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mIsZooming = true;
                goToNearestOrigin();
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                mNewHourHeight = Math.round(mHourHeight * detector.getScaleFactor());
                invalidate();
                return true;
            }
        });
    }

    private void initTextTimeWidth() {
        mTimeTextWidth = 0;
        for (int i = 0; i < 24; i++) {
            // Measure time string and get max width.
            String time = getDateTimeInterpreter().interpretTime(i);
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            mTimeTextWidth = Math.max(mTimeTextWidth, mTimeTextPaint.measureText(time));
        }
    }

    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            goToNearestOrigin();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Check if view is zoomed.
            if (mIsZooming)
                return true;

            switch (mCurrentScrollDirection) {
                case NONE: {
                    // Allow scrolling only in one direction.
                    if (Math.abs(distanceX) > Math.abs(distanceY)) {
                        if (distanceX > 0) {
                            mCurrentScrollDirection = Direction.LEFT;
                        } else {
                            mCurrentScrollDirection = Direction.RIGHT;
                        }
                    } else {
                        mCurrentScrollDirection = Direction.VERTICAL;
                    }
                    break;
                }
                case LEFT: {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX < -mScaledTouchSlop)) {
                        mCurrentScrollDirection = Direction.RIGHT;
                    }
                    break;
                }
                case RIGHT: {
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX > mScaledTouchSlop)) {
                        mCurrentScrollDirection = Direction.LEFT;
                    }
                    break;
                }
            }

            // Calculate the new origin after scroll.
            switch (mCurrentScrollDirection) {
                case LEFT:
                case RIGHT:
                    mCurrentOrigin.x -= distanceX * mXScrollingSpeed;
                    ViewCompat.postInvalidateOnAnimation(ScheduleView.this);
                    break;
                case VERTICAL:
                    mCurrentOrigin.y -= distanceY;
                    ViewCompat.postInvalidateOnAnimation(ScheduleView.this);
                    break;
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mIsZooming)
                return true;

            if ((mCurrentFlingDirection == Direction.LEFT && !mHorizontalFlingEnabled) ||
                    (mCurrentFlingDirection == Direction.RIGHT && !mHorizontalFlingEnabled) ||
                    (mCurrentFlingDirection == Direction.VERTICAL && !mVerticalFlingEnabled)) {
                return true;
            }

            mScroller.forceFinished(true);

            mCurrentFlingDirection = mCurrentScrollDirection;
            switch (mCurrentFlingDirection) {
                case LEFT:
                case RIGHT:
                    mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y,
                            (int) (velocityX * mXScrollingSpeed), 0,
                            Integer.MIN_VALUE, Integer.MAX_VALUE,
                            (int) -(mHourHeight * 24 + mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount() + mHeaderMarginBottom + mTimeTextHeight / 2 - getHeight()), 0);
                    break;
                case VERTICAL:
                    mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y,
                            0, (int) velocityY,
                            Integer.MIN_VALUE, Integer.MAX_VALUE,
                            (int) -(mHourHeight * 24 + mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount() + mHeaderMarginBottom + mTimeTextHeight / 2 - getHeight()), 0);
                    break;
            }

            ViewCompat.postInvalidateOnAnimation(ScheduleView.this);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // if 내부는 mEventClickListener 에 이벤트를 전달하는 역할
            if (mEventRects != null && mEventClickListener != null) {
                List<EventRect> reversedEventRects = mEventRects;

                // reversedEventRects 를 역으로 뒤집는다.
                Collections.reverse(reversedEventRects);

                // reversedEventRects 의 item 들을 반복하며
                for (EventRect event : reversedEventRects) {

                    // TouchPoint 가 EventRect 에 포함되면
                    if (event.rectF != null && event.rectF.contains(e.getX(), e.getY())) {

                        setFocusedEventRect(event);

                        // EventLongPressListener 에 이벤트를 전달하고
                        mEventClickListener.onEventClick(event.originalEvent, event.rectF);

                        // 기기에 SoundEffect Click 효과를 수행한다.
                        playSoundEffect(SoundEffectConstants.CLICK);

                        invalidate();
                        return super.onSingleTapConfirmed(e);
                    }
                }
            }

            // if 내부는 EmptyViewLongPressListener 에 이벤트를 전달하는 역할
            if (mEmptyViewClickListener != null && isContainsContentsArea(e)) {
                for (ScheduleRect scheduleRect : mScheduleRects) {

                    // TouchPoint 가 EventRect 에 포함되면
                    if (scheduleRect.rectF != null && scheduleRect.rectF.contains(e.getX(), e.getY())) {
                        playSoundEffect(SoundEffectConstants.CLICK);
                        setFocusedEmptyScheduleRect(scheduleRect);
                        mEmptyViewClickListener.onEmptyViewClicked(scheduleRect);
                        invalidate();
                        return super.onSingleTapConfirmed(e);
                    }
                }
            }

            // if 내부는 GroupHeaderClickListener 에 이벤트를 전달하는 역할
            if (mGroupHeaderClickListener != null && mGroupHeaderItems != null) {
                for (GroupHeader groupHeader : mGroupHeaderItems) {
                    // TouchPoint 가 GroupHeader 의 RectF 에 포함되면 이벤트 전달.
                    if (groupHeader.getRectF() != null && groupHeader.getRectF().contains(e.getX(), e.getY())) {
                        playSoundEffect(SoundEffectConstants.CLICK);
                        mGroupHeaderClickListener.onGroupHeaderClicked(groupHeader);
                        invalidate();
                        return  super.onSingleTapConfirmed(e);
                    }
                }
            }

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);

            // if 내부는 EventLongPressListener 에 이벤트를 전달하는 역할
            if (mEventLongPressListener != null && mEventRects != null) {
                List<EventRect> reversedEventRects = mEventRects;

                // reversedEventRects 를 역으로 뒤집는다.
                Collections.reverse(reversedEventRects);

                // reversedEventRects 의 item 들을 반복하며
                for (EventRect event : reversedEventRects) {

                    // TouchPoint 가 EventRect 에 포함되면
                    if (event.rectF != null && event.rectF.contains(e.getX(), e.getY())) {

                        // EventLongPressListener 에 이벤트를 전달하고
                        mEventLongPressListener.onEventLongPress(event.originalEvent, event.rectF);

                        // 기기에 HapticFeedback LongPress 효과를 수행한다.
                        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        return;
                    }
                }
            }

            // if 내부는 EmptyViewLongPressListener 에 이벤트를 전달하는 역할
            if (mEmptyViewLongPressListener != null && isContainsContentsArea(e)) {

                Calendar selectedTime = getTimeFromPoint(e.getX(), e.getY());
                if (selectedTime != null) {

                    // 기기에 LongPress 효과를 수행한다.(기기 진동)
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);

                    // EmptyViewLongPressListener 에 이벤트를 전달하고
                    // TODO: Empty 에 대한 Event 객체가 필요할거 같다 위치를 나타낼수 있는 객체의 생성이 필요하고 이를 ScheduleViewEvent 가 상속해서 구현하도록 구조를 바꿔야할거 같다.
//                    mEmptyViewLongPressListener.onEmptyViewLongPress(selectedTime);
                }
            }
        }
    };

    /////////////////////////////////////////////////////////////////
    //
    //      Draw 관련 함수
    //
    /////////////////////////////////////////////////////////////////

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mAreDimensionsInvalid = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the header row.
        drawHeaderRowAndEvents(canvas);

        // Draw the time column and all the axes/separators.
        drawTimeColumnAndAxes(canvas);
    }

    private void calculateHeaderHeight() {
        //Make sure the header is the right size (depends on AllDay events)
        boolean containsAllDayEvent = false;

        // if 안에서는 현재 스케쥴 화면에 보이는 Column 중 하루 종일 스케쥴이 잡혀있는 경우는 containsAllDayEvent 를 true 로 설정
        // TODO: AllDay 스케쥴 설정은 안된다고 가정하에 아래 AllDay 설정부분 삭제, AllDay 스케쥴 사용 시 아래 구현할 것.
//        if (mEventRects != null && mEventRects.size() > 0) {
//            for (int dayNumber = 0;
//                 dayNumber < mNumberOfVisibleDays;
//                 dayNumber++) {
//                Calendar day = (Calendar) getFirstVisibleDay().clone();
//                day.add(Calendar.DATE, dayNumber);
//                for (int i = 0; i < mEventRects.size(); i++) {
//                    // day 가 List<EventRect> 의 item 중 같은 날짜이면서 하루종일 EventTime 이 잡혀 있는경우
//                    if (WeekViewUtil.isSameDay(mEventRects.get(i).event.getStartTime(), day) && mEventRects.get(i).event.isAllDay()) {
//                        containsAllDayEvent = true;
//                        break;
//                    }
//                }
//                if(containsAllDayEvent){
//                    break;
//                }
//            }
//        }

        if (containsAllDayEvent) {
            mHeaderHeight = mHeaderTextHeight + (mAllDayEventHeight + mHeaderMarginBottom);
        } else {
            mHeaderHeight = mHeaderTextHeight;
        }
        if (mViewMode == VIEW_CHILD) {
            mHeaderHeight = mHeaderHeight * 2;
        }
    }

    private void drawHeaderRowAndEvents(Canvas canvas) {

        // FixedGroupHeader 가 설정된 경우 GroupHeader 의 SubHeader 수만큼 mNumberOfVisibleDays 를 Fix 시켜준다.
        if (mFixedGroupHeader != null) {
            mNumberOfVisibleDays = mFixedGroupHeader.getSubHeaders().size();
        }

        // mNumberOfVisibleDays 에 따라 분할된 컬럼의 설정가능한 폭을 계산합니다.
        mHeaderColumnWidth = mTimeTextWidth + mHeaderColumnPadding * 2;
        mWidthPerDay = getWidth() - mHeaderColumnWidth - (mColumnGap * mNumberOfVisibleDays);
        mWidthPerDay = mWidthPerDay / mNumberOfVisibleDays;

        calculateHeaderHeight(); // header 의 높이는 적당한지 확인하기위해 계산 (AllDay events 에 따라 다름)

        Calendar today = ScheduleViewUtil.today();

        if (mAreDimensionsInvalid) {
            mEffectiveMinHourHeight = Math.max(mMinHourHeight, (int) ((getHeight() - mHeaderHeight - mHeaderRowPadding * 2 * getHeaderRowCount() - mHeaderMarginBottom) / 24));

            mAreDimensionsInvalid = false;
            if (mScrollToDay != null)
//                goToDate(mScrollToDay);   // TODO: goToDate() 라는 일자별 이동 함수 관련된것을 다른 Object Header 의 형식으로 바꿔야한다

                mAreDimensionsInvalid = false;
            if (mScrollToHour >= 0)
                goToHour(mScrollToHour);

            mScrollToDay = null;
            mScrollToHour = -1;
            mAreDimensionsInvalid = false;
        }

        /*  // TODO: WeekView 처럼 날짜로 이동 시 mShowFirstDayOfWeekFirst 가 true 일때 mFirstDayOfWeek 에 설정된 요일로 이동하는 옵션 적용부분
        if (mIsFirstDraw){
            mIsFirstDraw = false;

            // ShowFirstDayOfWeekFirst 옵션이 true 일 때, FirstDayOfWeek 에 설정된 요일이 첫번째에 위치 되도록 mCurrentOrigin.x 의 값을 조정한다.
            if(mNumberOfVisibleDays >= 7 && today.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek && mShowFirstDayOfWeekFirst) {
                int difference = (today.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek);
                mCurrentOrigin.x += (mWidthPerDay + mColumnGap) * difference;
            }
        }
        */

        // 줌으로 인해 변경된 height 값을 계산한다.
        if (mNewHourHeight > 0) {
            if (mNewHourHeight < mEffectiveMinHourHeight)
                mNewHourHeight = mEffectiveMinHourHeight;
            else if (mNewHourHeight > mMaxHourHeight)
                mNewHourHeight = mMaxHourHeight;

            mCurrentOrigin.y = (mCurrentOrigin.y / mHourHeight) * mNewHourHeight;
            mHourHeight = mNewHourHeight;
            mNewHourHeight = -1;
        }

        // mCurrentOrigin.y 값이 View 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (mCurrentOrigin.y < getHeight() - mHourHeight * 24 - mHeaderHeight - mHeaderRowPadding * 2 * getHeaderRowCount() - mHeaderMarginBottom - mTimeTextHeight / 2)
            mCurrentOrigin.y = getHeight() - mHourHeight * 24 - mHeaderHeight - mHeaderRowPadding * 2 * getHeaderRowCount() - mHeaderMarginBottom - mTimeTextHeight / 2;

        // mCurrentOrigin.y 값이 View 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (mCurrentOrigin.y > 0) {
            mCurrentOrigin.y = 0;
        }

        // Scroll 된 offset 설정
        int leftDaysWithGaps = (int) -(Math.ceil(mCurrentOrigin.x / (mWidthPerDay + mColumnGap)));
        float startFromPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps + mHeaderColumnWidth;
        float startPixel = startFromPixel;

        // mCurrentOrigin.x 값이 HeaderViewList 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (mGroupHeaderItems != null && leftDaysWithGaps < 0) {
            leftDaysWithGaps = 0;
            mCurrentOrigin.x = 0;
            mScroller.forceFinished(true);
            startFromPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps + mHeaderColumnWidth;
            startPixel = startFromPixel;
        }

        // mCurrentOrigin.x 값이 HeaderViewList 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (mGroupHeaderItems != null && leftDaysWithGaps + mNumberOfVisibleDays > getHeaderItemSize() - 1) {
            leftDaysWithGaps = getHeaderItemSize() - mNumberOfVisibleDays;
            mCurrentOrigin.x = -((getHeaderItemSize() - mNumberOfVisibleDays) * (mWidthPerDay + mColumnGap));
            mScroller.forceFinished(true);
            startFromPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps + mHeaderColumnWidth;
            startPixel = startFromPixel;
        }

        // 각 일자를 iterate 하기위해 준비
//        Calendar day = (Calendar) today.clone();
//        day.add(Calendar.HOUR, 6);

        // 각 시간에 구분선을 그리기 위해 준비
        int lineCount = (int) ((getHeight() - mHeaderHeight - mHeaderRowPadding * 2 * getHeaderRowCount() - mHeaderMarginBottom) / mHourHeight) + 1;
        float[] hourLines = new float[lineCount * 4];   // drawLines 에서 float 값 4개씩 x1, y1, x2, y2 로 값을 가져와 line 을 그리기 때문에 lineCount * 4 를 한다

        // EventRect 캐시 삭제
        if (mEventRects != null) {
            for (EventRect eventRect : mEventRects) {
                eventRect.rectF = null;
            }
        }

        if (mScheduleRects != null) {
            for (ScheduleRect scheduleRect : mScheduleRects) {
                scheduleRect.rectF = null;
            }
        }

        // Events 를 Paint 하기위한 영역을 ClipRect 로 지정합니다.
        canvas.clipRect(mHeaderColumnWidth, mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount() + mHeaderMarginBottom + mTimeTextHeight / 2, getWidth(), getHeight(), Region.Op.REPLACE);

        if (mScheduleRects == null || mRefreshEvents) {

            if (mScheduleRects != null) {
                mScheduleRects.clear();
                mScheduleRects = null;
            }
            mScheduleRects = new ArrayList<>();

            // Schedule Rects 설정(Event 가 만들어 질 수있는 모든 Cell 영역 객체).
            getLoadBaseEventRect(startPixel);
        }

        for (int dayNumber = leftDaysWithGaps; dayNumber <= leftDaysWithGaps + mNumberOfVisibleDays; dayNumber++) {

            if (mGroupHeaderItems == null || dayNumber < 0 || dayNumber > getHeaderItemSize() - 1)
                break;

            // 필요한 경우 더 많은 이벤트를 가져옵니다. 사전에 3개월 이벤트를 저장합니다. 이 루프의 첫 번째 반복 인 경우에만 이벤트를 가져옵니다.
            // getMoreEvents(Calendar) 에서 GetLoadEvents() 로 변경합니다. Scroll 이벤트로 추가적으로 Load 되는 방식이 아닌 일괄 Load 방식을 사용합니다.
            if (mEventRects == null || mRefreshEvents) {
                getLoadEvents();
                mRefreshEvents = false;
            }

            // 각 Day Event Cell 에 Background 를 그린다.
            float start = (startPixel < mHeaderColumnWidth ? mHeaderColumnWidth : startPixel);
            if (mWidthPerDay + startPixel - start > 0) {
                canvas.drawRect(start, mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount() + mTimeTextHeight / 2 + mHeaderMarginBottom, startPixel + mWidthPerDay, getHeight(), mDayBackgroundPaint);
            }

            // hourLines 배열에 시간 분할선을 그리기 위한 현재 HeaderColumn 의 index 에 해당되는 x,y 좌표들을 설정한다.
            int i = 0;
            for (int hourNumber = 0; hourNumber < 24; hourNumber++) {
                float top = mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount() + mCurrentOrigin.y + mHourHeight * hourNumber + mTimeTextHeight / 2 + mHeaderMarginBottom;
                if (top > mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount() + mTimeTextHeight / 2 + mHeaderMarginBottom - mHourSeparatorHeight && top < getHeight() && startPixel + mWidthPerDay - start > 0) {
                    hourLines[i * 4] = start;
                    hourLines[i * 4 + 1] = top;
                    hourLines[i * 4 + 2] = startPixel + mWidthPerDay;
                    hourLines[i * 4 + 3] = top;
                    i++;
                }
            }

            // 시간별 구분선을 그린다
            canvas.drawLines(hourLines, mHourSeparatorPaint);

            // Empty Base Events 를 그린다.
            drawBaseEvent(dayNumber, startPixel, canvas);

            // Events 를 그린다
            drawEvents(getHeaderItem(dayNumber), startPixel, canvas);

            // ShowNowLine 속성이 true 인 경우, 오늘 날짜 현재 시간에 해당되는 Event Area 에 Line 을 그립니다. // TODO: ShowNowLine 오늘날짜에 현재시간 표시인데 일단보류
//            if (mShowNowLine && sameDay){
//                float startY = mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight/2 + mHeaderMarginBottom + mCurrentOrigin.y;
//                Calendar now = Calendar.getInstance();
//                float beforeNow = (now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE)/60.0f) * mHourHeight;
//                canvas.drawLine(start, startY + beforeNow, startPixel + mWidthPerDay, startY + beforeNow, mNowLinePaint);
//            }

            // In the next iteration, start from the next day.
            startPixel += mWidthPerDay + mColumnGap;
        }
        if (mCellFocusable && getFocusedEmptyScheduleRect() != null && getFocusedEmptyScheduleRect().rectF != null) {
            canvas.drawRoundRect(getFocusedEmptyScheduleRect().rectF, mEventCornerRadius, mEventCornerRadius, mCellFocusPaint);
        }

//        // 왼쪽 상단의 첫 Cell 배경색을 HeaderBackground 색으로 그린다.(Cell 숨김 목적)
//        canvas.clipRect(0, 0, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount(), Region.Op.REPLACE);
//        canvas.drawRect(0, 0, mTimeTextWidth + mHeaderColumnPadding * 2, mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount(), mHeaderBackgroundPaint);

        // Header 를 Paint 하기위한 영역을 ClipRect 로 지정합니다.
        canvas.clipRect(mHeaderColumnWidth, 0, getWidth(), mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount(), Region.Op.REPLACE);

        // Header Background 를 그린다.
//        canvas.drawRect(0, 0, getWidth(), mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount(), mHeaderBackgroundPaint);

        // Header Text 를 그린다.
        startPixel = startFromPixel;
        String tempGroupHeaderKey = null;
        int gap = leftDaysWithGaps;
        for (int dayNumber = leftDaysWithGaps; dayNumber <= leftDaysWithGaps + mNumberOfVisibleDays; dayNumber++) {
            if (mGroupHeaderItems == null || dayNumber < 0 || dayNumber > getHeaderItemSize() - 1)
                break;

            // 이상하게 clip y 값을 0 으로 해도 text 그릴때 mHeaderTextHeight 정도 마이너스되서 계산된다. 그래서 y 값을 임의로 mHeaderTextHeight 더해줌
            float startHeaderY = mHeaderTextHeight;

            // Header Group Text 를 그린다.
            if (mViewMode == VIEW_CHILD) {
                startHeaderY = startHeaderY + mHeaderRowPadding;
                GroupHeader groupHeader = getGroupHeaderItemToChildIndex(dayNumber);
                if (groupHeader.getHeaderKey().equals(tempGroupHeaderKey) == false) {
                    String groupName = getHeaderInterpreter().interpretHeaderColumn(groupHeader);
                    float startGroupHeaderX = startPixel + (mWidthPerDay + mColumnGap) * groupHeader.getSubHeaders().size() / 2;
                    for (GroupHeader groupHeader1 : mGroupHeaderItems) {
                        if (gap > groupHeader1.getSubHeaders().size() - 1 && mFixedGroupHeader == null)
                            gap = gap - groupHeader1.getSubHeaders().size();
                        else
                            break;
                    }
                    float left, top, right, bottom;

                    left = startPixel;
                    top = startHeaderY - mHeaderRowPadding - mHeaderTextHeight - 1;
                    right = startPixel + (mWidthPerDay + mColumnGap) * groupHeader.getSubHeaders().size() - mColumnGap;
                    bottom = startHeaderY + mHeaderRowPadding - 1;

                    groupHeader.setRectF(new RectF(left, top, right, bottom));

                    if (tempGroupHeaderKey == null) {
                        startGroupHeaderX = startGroupHeaderX - (mWidthPerDay + mColumnGap) * gap;
                        right = right - (mWidthPerDay + mColumnGap) * gap;
                    }
                    canvas.drawRect(left, top, right, bottom, mHeaderBackgroundPaint);

                    canvas.drawText(groupName, startGroupHeaderX, startHeaderY, mHeaderTextPaint);
                    tempGroupHeaderKey = groupHeader.getHeaderKey();
                }
                startHeaderY = startHeaderY + mHeaderTextHeight + mHeaderRowPadding;
            }

            // Header Text 를 그린다.
            String dayLabel = getHeaderInterpreter().interpretHeaderColumn(getHeaderItem(dayNumber));
            if (dayLabel == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null date");

            startHeaderY = startHeaderY + mHeaderRowPadding;

            float left = startPixel;
            float top = startHeaderY - mHeaderRowPadding - mHeaderTextHeight;
            float right = startPixel + mWidthPerDay;
            float bottom = startHeaderY + mHeaderRowPadding;

            canvas.drawRect(left, top, right, bottom, mHeaderBackgroundPaint);
            canvas.drawText(dayLabel, startPixel + mWidthPerDay / 2, startHeaderY, mHeaderTextPaint);
//            drawAllDayEvents(day, startPixel, canvas);
            startPixel += mWidthPerDay + mColumnGap;
        }
    }

    private int getHeaderRowCount() {
        if (mGroupHeaderItems == null)
            return 1;

        switch (mViewMode) {
            case VIEW_PARENT:
                return 1;
            case VIEW_CHILD:
                return 2;
        }
        return 1;
    }

    private GroupHeader getGroupHeaderItemToChildIndex(int childIndex) {
        if (mGroupHeaderItems == null)
            return null;

        if (mFixedGroupHeader != null) {
            return mFixedGroupHeader;
        } else {
            for (GroupHeader groupHeader : mGroupHeaderItems) {
                if (groupHeader.getSubHeaders() != null && childIndex > groupHeader.getSubHeaders().size() - 1) {
                    childIndex = childIndex - groupHeader.getSubHeaders().size();
                } else {
                    return groupHeader;
                }
            }
        }

        return null;
    }

    /**
     * <pre>
     * Header 목록을 반환합니다.
     * ViewMode 에 따라,
     * PARENT 의 목록 또는 PARENT 의 각 CHILD 의 목록들이 반환됩니다.
     * </pre>
     *
     * @return {@link Header} List 를 반환합니다.
     */
    private Header[] getHeaderItems() {
        if (mGroupHeaderItems == null)
            return null;

        switch (mViewMode) {
            case VIEW_PARENT:
                return mGroupHeaderItems.toArray(new Header[0]);
            case VIEW_CHILD:
                List<Header> headers = new ArrayList<>();
                for (GroupHeader groupHeader : mGroupHeaderItems) {
                    if (groupHeader.getSubHeaders() == null)
                        continue;

                    headers.addAll(groupHeader.getSubHeaders());
                }
                return headers.toArray(new Header[0]);
        }
        return null;
    }

    /**
     * <pre>
     * index 에 맞는 Header 를 반환합니다.
     * ViewMode 에 따라,
     * PARENT 의 Header 또는 PARENT 의 각 CHILD 의 Header 가 반환됩니다.
     * </pre>
     *
     * @param index
     * @return {@link Header} 를 반환합니다.
     */
    private Header getHeaderItem(int index) {
        if (mGroupHeaderItems == null)
            return null;

        switch (mViewMode) {
            case VIEW_PARENT:
                if (mFixedGroupHeader != null) {
                    return mFixedGroupHeader;
                } else {
                    return mGroupHeaderItems.get(index);
                }
            case VIEW_CHILD:
                if (mFixedGroupHeader != null) {
                    return mFixedGroupHeader.getSubHeaders().get(index);
                } else {
                    int itemIndex = 0;
                    for (GroupHeader groupHeader : mGroupHeaderItems) {
                        if (groupHeader.getSubHeaders() == null)
                            continue;
                        for (Header header : groupHeader.getSubHeaders()) {
                            if (itemIndex == index)
                                return header;

                            itemIndex++;
                        }
                    }
                }
                return null;
        }
        return null;
    }

    /**
     * <pre>
     * Header Item 의 Size 를 반환합니다.
     * ViewMode 에 따라,
     * PARENT 의 size 또는 PARENT 의 각 CHILD 의 size 의 합이 반환됩니다.
     * </pre>
     *
     * @return Header Item Size
     */
    private int getHeaderItemSize() {
        if (mGroupHeaderItems == null)
            return 0;

        switch (mViewMode) {
            case VIEW_PARENT:
                if (mFixedGroupHeader != null) {
                    return 1;
                } else {
                    return mGroupHeaderItems.size();
                }
            case VIEW_CHILD:
                if (mFixedGroupHeader != null) {
                    return mFixedGroupHeader.getSubHeaders().size();
                } else {
                    int size = 0;
                    for (GroupHeader groupHeader : mGroupHeaderItems) {
                        if (groupHeader.getSubHeaders() == null)
                            continue;
                        size += groupHeader.getSubHeaders().size();
                    }
                    return size;
                }
        }
        return 0;
    }


    private void drawBaseEvent(int columnIndex, float startFromPixel, Canvas canvas) {
        int columnGap = 24 * columnIndex;
        for (int i = columnGap; i < 24 + columnGap; i++) {

            // Calculate top.
            float top = mHourHeight * 24 * (mScheduleRects.get(i).startTime.get(Calendar.HOUR_OF_DAY) * 60 + mScheduleRects.get(i).startTime.get(Calendar.MINUTE)) / 1440
                    + mCurrentOrigin.y + mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount()
                    + mHeaderMarginBottom + mTimeTextHeight / 2 + mEventMarginTop;

            // Calculate bottom.
            float bottom = mHourHeight * 24 * (mScheduleRects.get(i).endTime.get(Calendar.HOUR_OF_DAY) * 60 + mScheduleRects.get(i).endTime.get(Calendar.MINUTE)) / 1440
                    + mCurrentOrigin.y + mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount()
                    + mHeaderMarginBottom + mTimeTextHeight / 2 - mEventMarginBottom;

            // Calculate left.
            float left = startFromPixel + 0F * mWidthPerDay + mEventMarginLeft;
            if (left < startFromPixel)
                left += mOverlappingEventGap;

            // Calculate right.
            float right = left + 1F * mWidthPerDay - mEventMarginRight;
            if (right < startFromPixel + mWidthPerDay)
                right -= mOverlappingEventGap;

            // Draw the event and the event name on top of it.
            if (left < right && left < getWidth() && top < getHeight() && right > mHeaderColumnWidth &&
                    bottom > mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount() + mTimeTextHeight / 2 + mHeaderMarginBottom) {
                mScheduleRects.get(i).rectF = new RectF(left, top, right, bottom);
            } else
                mScheduleRects.get(i).rectF = null;

            if (getFocusedEmptyScheduleRect() != null &&
                    getFocusedEmptyScheduleRect().headerKey == mScheduleRects.get(i).headerKey &&
                    getFocusedEmptyScheduleRect().startTime == mScheduleRects.get(i).startTime) {
                getFocusedEmptyScheduleRect().rectF = mScheduleRects.get(i).rectF;
            }
        }
    }

    /**
     * Draw all the events of a particular day.
     *
     * @param header         Header.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    private void drawEvents(Header header, float startFromPixel, Canvas canvas) {

        if (mEventRects != null && mEventRects.size() > 0) {
            for (int i = 0; i < mEventRects.size(); i++) {
                if (ScheduleViewUtil.isEventSameHeader(mEventRects.get(i).event, header, mViewMode)) {

                    // Calculate top.
                    float top = mHourHeight * 24 * mEventRects.get(i).top / 1440
                            + mCurrentOrigin.y + mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount()
                            + mHeaderMarginBottom + mTimeTextHeight / 2 + mEventMarginTop;

                    // Calculate bottom.
                    float bottom = mHourHeight * 24 * mEventRects.get(i).bottom / 1440
                            + mCurrentOrigin.y + mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount()
                            + mHeaderMarginBottom + mTimeTextHeight / 2 - (mEventMarginTop + mEventMarginBottom);

                    // Calculate left.
                    float left = startFromPixel + mEventRects.get(i).left * mWidthPerDay + mEventMarginLeft;
                    if (left < startFromPixel)
                        left += mOverlappingEventGap;

                    // Calculate right.
                    float right = left + mEventRects.get(i).width * mWidthPerDay - (mEventMarginLeft + mEventMarginRight);
                    if (right < startFromPixel + mWidthPerDay)
                        right -= mOverlappingEventGap;

                    // Draw the event and the event name on top of it.
                    if (left < right &&
                            left < getWidth() &&
                            top < getHeight() &&
                            right > mHeaderColumnWidth &&
                            bottom > mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount() + mTimeTextHeight / 2 + mHeaderMarginBottom
                            ) {
                        mEventRects.get(i).rectF = new RectF(left, top, right, bottom);
                        // TODO : Event Color 항목이 없어서 주석
                        mEventBackgroundPaint.setColor(mEventRects.get(i).event.getBackgroundColor() == 0 ? mDefaultEventColor : mEventRects.get(i).event.getBackgroundColor());
                        canvas.drawRoundRect(mEventRects.get(i).rectF, mEventCornerRadius, mEventCornerRadius, mEventBackgroundPaint);
                        if (mCellFocusable && getFocusedEventRect() != null && getFocusedEventRect().originalEvent.getKey() == mEventRects.get(i).originalEvent.getKey())
                            canvas.drawRoundRect(mEventRects.get(i).rectF, mEventCornerRadius, mEventCornerRadius, mCellFocusPaint);
                        drawEventTitle(mEventRects.get(i).event, mEventRects.get(i).rectF, canvas, top, left);
                    } else
                        mEventRects.get(i).rectF = null;
                }
            }
        }
    }

    private void drawTimeColumnAndAxes(Canvas canvas) {
        // TimeColumn Header 의 배경을 그립니다.
        canvas.drawRect(0, mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount(), mHeaderColumnWidth, getHeight(), mHeaderColumnBackgroundPaint);

        // 왼쪽의 Time Column Header 를 Paint 하기위한 영역을 ClipRect 로 지정합니다.
        canvas.clipRect(0, mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount(), mHeaderColumnWidth, getHeight(), Region.Op.REPLACE);

        // 왼쪽의 Time Column Header 에 시간 별 Text 를 그립니다.
        for (int i = 0; i < 24; i++) {
            float top = mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount() + mCurrentOrigin.y + mHourHeight * i + mHeaderMarginBottom;

            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner.
            String time = getDateTimeInterpreter().interpretTime(i);
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            if (top < getHeight())
                canvas.drawText(time, mTimeTextWidth + mHeaderColumnPadding, top + mTimeTextHeight, mTimeTextPaint);
        }
    }

    /**
     * Draw all the Allday-events of a particular day.
     *
     * @param date           The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas         The canvas to draw upon.
     */
    private void drawAllDayEvents(Calendar date, float startFromPixel, Canvas canvas) {
        // TODO: AllDayEvent 문제도 해결해야한다. 이벤트 생성부터 AllDay 이벤트는 생성할수 없도록..
        if (mEventRects != null && mEventRects.size() > 0) {
//            for (int i = 0; i < mEventRects.size(); i++) {
//                if (WeekViewUtil.isSameDay(mEventRects.get(i).event.getStartTime(), date) && mEventRects.get(i).event.isAllDay()){
//
//                    // Calculate top.
//                    float top = mHeaderRowPadding * 2 + mHeaderMarginBottom +  + mTimeTextHeight/2 + mEventMarginTop;
//
//                    // Calculate bottom.
//                    float bottom = top + mEventRects.get(i).bottom - mEventMarginBottom;
//
//                    // Calculate left.
//                    float left = startFromPixel + mEventRects.get(i).left * mWidthPerDay + mEventMarginLeft;
//                    if (left < startFromPixel)
//                        left += mOverlappingEventGap;

//                    // Calculate right.
//                    float right = left + mEventRects.get(i).width * mWidthPerDay - mEventMarginRight;
//                    if (right < startFromPixel + mWidthPerDay)
//                        right -= mOverlappingEventGap;
//
//                    // Draw the event and the event name on top of it.
//                    if (left < right &&
//                            left < getWidth() &&
//                            top < getHeight() &&
//                            right > mHeaderColumnWidth &&
//                            bottom > 0
//                            ) {
//                        mEventRects.get(i).rectF = new RectF(left, top, right, bottom);
//                        mEventBackgroundPaint.setColor(mEventRects.get(i).event.getColor() == 0 ? mDefaultEventColor : mEventRects.get(i).event.getColor());
//                        canvas.drawRoundRect(mEventRects.get(i).rectF, mEventCornerRadius, mEventCornerRadius, mEventBackgroundPaint);
//                        drawEventTitle(mEventRects.get(i).event, mEventRects.get(i).rectF, canvas, top, left);
//                    }
//                    else
//                        mEventRects.get(i).rectF = null;
//                }
//            }
        }
    }

    /**
     * Draw the name of the event on top of the event rectangle.
     *
     * @param event        The event of which the title (and location) should be drawn.
     * @param rect         The rectangle on which the text is to be drawn.
     * @param canvas       The canvas to draw upon.
     * @param originalTop  The original top position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     */
    private void drawEventTitle(ScheduleViewEvent event, RectF rect, Canvas canvas, float originalTop, float originalLeft) {
        if (rect.right - rect.left - mEventPadding * 2 < 0) return;
        if (rect.bottom - rect.top - mEventPadding * 2 < 0) return;

        int availableHeight = (int) (rect.bottom - originalTop - mEventPadding * 2);
        int availableWidth = (int) (rect.right - originalLeft - mEventPadding * 2);

        // Prepare the name of the event.
        SpannableStringBuilder bob = new SpannableStringBuilder();
        if (event.getHeaderKey() != null) {
            bob.append(event.getHeaderKey());
            bob.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, bob.length(), 0);
        }

        // onEventDraw 를 통해 사용자로부터 SpannableStringBuilder 를 공급받는다.
        if (mEventDrawListener != null)
            bob = mEventDrawListener.onEventDraw(event, mEventTextPaint, availableWidth, availableHeight);

        // Get text dimensions.
        StaticLayout textLayout = new StaticLayout(bob, mEventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

        int lineHeight = textLayout.getHeight() / textLayout.getLineCount();

        if (availableHeight >= lineHeight) {
            // Calculate available number of line counts.
            int availableLineCount = availableHeight / lineHeight;
            do {
                // Ellipsize text to fit into event rect.
                textLayout = new StaticLayout(TextUtils.ellipsize(bob, mEventTextPaint, availableLineCount * availableWidth, TextUtils.TruncateAt.END), mEventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                // Reduce line count.
                availableLineCount--;

                // Repeat until text is short enough.
            } while (textLayout.getHeight() > availableHeight);
        }

        // Draw text.
        canvas.save();
        canvas.translate(originalLeft + mEventPadding, originalTop + mEventPadding);
        textLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * Event 가 들어갈수 있는 Contents 영역에 MotionEvent 의 Point 가 속해 있는지 여부를 반환합니다.
     *
     * @param e MotionEvent
     * @return 포함되면 true, 포함되지 않으면 false
     */
    private boolean isContainsContentsArea(MotionEvent e) {
        return e.getX() > mHeaderColumnWidth && e.getY() > (mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom);
    }

    /**
     * Get the time and date where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and date at the clicked position.
     */
    private Calendar getTimeFromPoint(float x, float y) {
        int leftDaysWithGaps = (int) -(Math.ceil(mCurrentOrigin.x / (mWidthPerDay + mColumnGap)));
        float startPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps +
                mHeaderColumnWidth;
        for (int dayNumber = leftDaysWithGaps + 1;
             dayNumber <= leftDaysWithGaps + mNumberOfVisibleDays + 1;
             dayNumber++) {
            float start = (startPixel < mHeaderColumnWidth ? mHeaderColumnWidth : startPixel);
            if (mWidthPerDay + startPixel - start > 0 && x > start && x < startPixel + mWidthPerDay) {
                Calendar day = ScheduleViewUtil.today();
                day.add(Calendar.DATE, dayNumber - 1);
                float pixelsFromZero = y - mCurrentOrigin.y - mHeaderHeight
                        - mHeaderRowPadding * 2 - mTimeTextHeight / 2 - mHeaderMarginBottom;
                int hour = (int) (pixelsFromZero / mHourHeight);
                int minute = (int) (60 * (pixelsFromZero - hour * mHourHeight) / mHourHeight);
                day.add(Calendar.HOUR, hour);
                day.set(Calendar.MINUTE, minute);
                return day;
            }
            startPixel += mWidthPerDay + mColumnGap;
        }
        return null;
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Scrolling 동작관련 함수
    //
    /////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        boolean val = mGestureDetector.onTouchEvent(event);

        // mGestureDetector 체크가 끝난 후, mCurrentFlingDirection 과 mCurrentScrollDirection 의 상태를 설정한다.
        if (event.getAction() == MotionEvent.ACTION_UP && !mIsZooming && mCurrentFlingDirection == Direction.NONE) {
            if (mCurrentScrollDirection == Direction.RIGHT || mCurrentScrollDirection == Direction.LEFT) {
                goToNearestOrigin();
            }
            mCurrentScrollDirection = Direction.NONE;
        }

        return val;
    }

    private void goToNearestOrigin() {
        double leftDays = mCurrentOrigin.x / (mWidthPerDay + mColumnGap);

        if (mCurrentFlingDirection != Direction.NONE) {
            // snap to nearest day
            leftDays = Math.round(leftDays);
        } else if (mCurrentScrollDirection == Direction.LEFT) {
            // snap to last day
            leftDays = Math.floor(leftDays);
        } else if (mCurrentScrollDirection == Direction.RIGHT) {
            // snap to next day
            leftDays = Math.ceil(leftDays);
        } else {
            // snap to nearest day
            leftDays = Math.round(leftDays);
        }

        int nearestOrigin = (int) (mCurrentOrigin.x - leftDays * (mWidthPerDay + mColumnGap));

        if (nearestOrigin != 0) {
            // Stop current animation.
            mScroller.forceFinished(true);
            // Snap to date.
            mScroller.startScroll((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, -nearestOrigin, 0, (int) (Math.abs(nearestOrigin) / mWidthPerDay * mScrollDuration));
            ViewCompat.postInvalidateOnAnimation(ScheduleView.this);
        }
        // Reset scrolling and fling direction.
        mCurrentScrollDirection = mCurrentFlingDirection = Direction.NONE;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if (mScroller.isFinished()) {
            if (mCurrentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestOrigin();
            }
        } else {
            if (mCurrentFlingDirection != Direction.NONE && forceFinishScroll()) {
                goToNearestOrigin();
            } else if (mScroller.computeScrollOffset()) {
                mCurrentOrigin.y = mScroller.getCurrY();
                mCurrentOrigin.x = mScroller.getCurrX();
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }
    }

    /**
     * Scrolling 을 정지할 필요가 있는지 체크한다.
     *
     * @return true 이면 Scrolling 작업을 중지하고 End Scrolling 관련 Animation 을 실행한다.
     */
    private boolean forceFinishScroll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // current velocity only available since api 14
            return mScroller.getCurrVelocity() <= mMinimumFlingVelocity;
        } else {
            return false;
        }
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Properties 의 get or set 관련 함수
    //
    /////////////////////////////////////////////////////////////////

    public int getViewMode() {
        return mViewMode;
    }

    public void setViewMode(int viewMode, boolean refresh) {
        this.mViewMode = viewMode;
        this.setNumberOfVisibleDays(mCachedNumberOfVisible);
        if (refresh)
            notifyDatasetChanged();
    }

    public GroupHeader getFixedGroupHeader() {
        return mFixedGroupHeader;
    }

    public void setFixedGroupHeader(GroupHeader fixedGroupHeader) {
        setFixedGroupHeader(fixedGroupHeader, false);
    }

    public void setFixedGroupHeader(GroupHeader fixedGroupHeader, boolean refresh) {
        this.mFixedGroupHeader = fixedGroupHeader;
        if (fixedGroupHeader != null) {
            this.mNumberOfVisibleDays = mFixedGroupHeader.getSubHeaders().size();
        } else {
            this.mNumberOfVisibleDays = mCachedNumberOfVisible;
        }
        mCurrentOrigin.x = 0;
        mCurrentOrigin.y = 0;
        if (refresh) {
            invalidate();
        }
    }

    public EventRect getFocusedEventRect() {
        return mFocusedEventRect;
    }

    public void setFocusedEventRect(EventRect focusedEvent) {
        this.mFocusedEventRect = focusedEvent;
        this.mFocusedEmptyScheduleRect = null;
    }

    public ScheduleRect getFocusedEmptyScheduleRect() {
        return mFocusedEmptyScheduleRect;
    }

    public void setFocusedEmptyScheduleRect(ScheduleRect focusedEmptyScheduleRect) {
        this.mFocusedEmptyScheduleRect = focusedEmptyScheduleRect;
        this.mFocusedEventRect = null;
    }

    public List<GroupHeader> getGroupHeaderItems() {
        return mGroupHeaderItems;
    }

    public void setGroupHeaderItems(List<GroupHeader> groupHeaderItems) {
        this.mGroupHeaderItems = groupHeaderItems;
        this.setNumberOfVisibleDays(this.mNumberOfVisibleDays);
    }

    public FloatingActionButton getFloatingActionButton() {
        return mFloatingActionButton;
    }

    public void setFloatingActionButton(FloatingActionButton floatingActionButton) {
        this.mFloatingActionButton = floatingActionButton;
    }

    /**
     * Header 와 Time Column 의 머리글 행에 표시 할 텍스트를 제공하는 interpreter 가져옵니다.
     *
     * @return The date, time interpreter.
     */
    public DateTimeInterpreter getDateTimeInterpreter() {
        if (mDateTimeInterpreter == null) {
            mDateTimeInterpreter = new DateTimeInterpreter() {
                @Override
                public String interpretDate(Calendar date) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE M/dd", Locale.getDefault()); // LENGTH_LONG
//                        SimpleDateFormat sdf = new SimpleDateFormat("EEEEE M/dd", Locale.getDefault()); // LENGTH_SHORT
                        return sdf.format(date.getTime()).toUpperCase();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }

                @Override
                public String interpretTime(int hour) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, 0);

                    try {
                        SimpleDateFormat sdf = DateFormat.is24HourFormat(getContext()) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("hh a", Locale.getDefault());
                        return sdf.format(calendar.getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }
            };
        }
        return mDateTimeInterpreter;
    }

    /**
     * Header 와 Time Column 의 머리글 행에 표시 할 텍스트를 제공하는 interpreter 설정합니다.
     *
     * @param dateTimeInterpreter The date, time interpreter.
     */
    public void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter) {
        this.mDateTimeInterpreter = dateTimeInterpreter;

        // Refresh time column width.
        initTextTimeWidth();
    }

    /**
     * Header 와 Time Column 의 머리글 행에 표시 할 텍스트를 제공하는 interpreter 가져옵니다.
     *
     * @return The headerTitle, time interpreter.
     */
    public HeaderInterpreter getHeaderInterpreter() {
        if (mHeaderInterpreter == null) {
            mHeaderInterpreter = new HeaderInterpreter() {
                @Override
                public String interpretHeaderColumn(Header header) {
                    if (mHeaderType == Header.HEADER_USING_CALENDAR) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE M/dd", Locale.getDefault()); // LENGTH_LONG
                            return sdf.format(header.getCalendar().getTime()).toUpperCase();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "";
                        }
                    } else if (mHeaderType == Header.HEADER_USING_TITLE) {
                        return header.getHeaderName();
                    } else {
                        return "";
                    }
                }

                @Override
                public String interpretTime(int hour) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, 0);

                    try {
                        SimpleDateFormat sdf = DateFormat.is24HourFormat(getContext()) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("hh a", Locale.getDefault());
                        return sdf.format(calendar.getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }
            };
        }
        return mHeaderInterpreter;
    }

    /**
     * Header 와 Time Column 의 머리글 행에 표시 할 텍스트를 제공하는 interpreter 설정합니다.
     *
     * @param headerInterpreter The headerTitle, time interpreter.
     */
    public void setHeaderInterpreter(HeaderInterpreter headerInterpreter) {
        this.mHeaderInterpreter = headerInterpreter;

        // Refresh time column width.
        initTextTimeWidth();
    }

    /**
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    public Calendar getFirstVisibleDay() {
        return mFirstVisibleDay;
    }

    /**
     * Get the number of visible days in a week.
     *
     * @return The number of visible days in a week.
     */
    public int getNumberOfVisibleDays() {
        return mNumberOfVisibleDays;
    }

    /**
     * Set the number of visible days in a week.
     *
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    public void setNumberOfVisibleDays(int numberOfVisibleDays) {
        mCachedNumberOfVisible = numberOfVisibleDays;

        if (mGroupHeaderItems != null && getHeaderItemSize() < numberOfVisibleDays)
            this.mNumberOfVisibleDays = getHeaderItemSize();
        else
            this.mNumberOfVisibleDays = numberOfVisibleDays;
        mCurrentOrigin.x = 0;
        mCurrentOrigin.y = 0;
        invalidate();
    }

    public void setNumberOfVisibleDays(int numberOfVisibleDays, boolean refresh) {
        mCachedNumberOfVisible = numberOfVisibleDays;

        if (mGroupHeaderItems != null && getHeaderItemSize() < numberOfVisibleDays)
            this.mNumberOfVisibleDays = getHeaderItemSize();
        else
            this.mNumberOfVisibleDays = numberOfVisibleDays;
        mCurrentOrigin.x = 0;
        mCurrentOrigin.y = 0;
        if (refresh) {
            invalidate();
        }
    }

    public
    @Nullable
    ScheduleLoader.ScheduleLoadListener getScheduleLoadListener() {
        if (mScheduleViewLoader instanceof ScheduleLoader)
            return ((ScheduleLoader) mScheduleViewLoader).getOnScheduleLoadListener();
        return null;
    }

    public void setScheduleLoadListener(ScheduleLoader.ScheduleLoadListener scheduleLoadListener) {
        this.mScheduleViewLoader = new ScheduleLoader(scheduleLoadListener);
    }

    /**
     * ScheduleView 에서 EventLoader 를 가져옵니다. ViewLoader 를 확장하여 사용자정의 이벤트를 정의할 수 있습니다.
     *
     * @return Event Loader.
     */
    public ScheduleViewLoader getScheduleViewLoader() {
        return mScheduleViewLoader;
    }

    /**
     * ScheduleView 에 EventLoader 를 설정합니다. ViewLoader 를 확장하여 사용자정의 이벤트를 정의할 수 있습니다.
     *
     * @param loader Event Loader.
     */
    public void setScheduleViewLoader(ScheduleViewLoader loader) {
        this.mScheduleViewLoader = loader;
    }

    public void setOnEventClickListener(EventClickListener listener) {
        this.mEventClickListener = listener;
    }

    public EventClickListener getEventClickListener() {
        return mEventClickListener;
    }

    public EventLongPressListener getEventLongPressListener() {
        return mEventLongPressListener;
    }

    public void setEventLongPressListener(EventLongPressListener eventLongPressListener) {
        this.mEventLongPressListener = eventLongPressListener;
    }

    public void setEmptyViewClickListener(EmptyViewClickListener emptyViewClickListener) {
        this.mEmptyViewClickListener = emptyViewClickListener;
    }

    public EmptyViewClickListener getEmptyViewClickListener() {
        return mEmptyViewClickListener;
    }

    public void setEmptyViewLongPressListener(EmptyViewLongPressListener emptyViewLongPressListener) {
        this.mEmptyViewLongPressListener = emptyViewLongPressListener;
    }

    public EmptyViewLongPressListener getEmptyViewLongPressListener() {
        return mEmptyViewLongPressListener;
    }

    public GroupHeaderClickListener getGroupHeaderClickListener() {
        return mGroupHeaderClickListener;
    }

    public void setGroupHeaderClickListener(GroupHeaderClickListener mGroupHeaderClickListener) {
        this.mGroupHeaderClickListener = mGroupHeaderClickListener;
    }

    public void setScrollListener(ScrollListener scrolledListener) {
        this.mScrollListener = scrolledListener;
    }

    public ScrollListener getScrollListener() {
        return mScrollListener;
    }

    public void setEventDrawListener(EventDrawListener listener) {
        this.mEventDrawListener = listener;
    }

    public EventDrawListener getEventDrawListener() {
        return mEventDrawListener;
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Event Rect Class 및 관련 Method
    //
    /////////////////////////////////////////////////////////////////

    /**
     * A class to hold reference to the events and their visual representation. An EventRect is
     * actually the rectangle that is drawn on the calendar for a given event. There may be more
     * than one rectangle for a single event (an event that expands more than one day). In that
     * case two instances of the EventRect will be used for a single event. The given event will be
     * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
     * instance will be stored in "event".
     */
    public class EventRect {
        public ScheduleViewEvent event;
        public ScheduleViewEvent originalEvent;
        public RectF rectF;
        public float left;
        public float width;
        public float top;
        public float bottom;

        /**
         * EventRect 의 새로운 인스턴스를 생성합니다.
         * 이벤트 사각형은 실제로 해당 이벤트의 달력에 그려진 사각형입니다.
         * 하나의 이벤트 (하루 이상 확장 이벤트)에 대한 하나 이상의 사각형이있을 수 있습니다.
         * 그 경우 EventRect 두 인스턴스는 하나의 이벤트에 사용된다.
         * 주어진 이벤트는 "originalEvent"에 저장됩니다. 그러나 구형 인스턴스 직사각형 해당 이벤트는 "event"로 저장된다.
         * <p/>
         * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
         * on the calendar for a given event. There may be more than one rectangle for a single
         * event (an event that expands more than one day). In that case two instances of the
         * EventRect will be used for a single event. The given event will be stored in
         * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
         * be stored in "event".
         *
         * @param event         사각형이 나타내는 Event 인스턴스 입니다. Represents the event which this instance of rectangle represents.
         * @param originalEvent 사용자에 의해 전달된 원래 Event 입니다. The original event that was passed by the user.
         * @param rectF         사각형 The rectangle.
         */
        public EventRect(ScheduleViewEvent event, ScheduleViewEvent originalEvent, RectF rectF) {
            this.event = event;
            this.rectF = rectF;
            this.originalEvent = originalEvent;
        }
    }

    /**
     * Schedule Rect. ScheduleView 에 Event 가 만들어질수 있는 Rect 영역 개체.
     * Column 에 해당되는 Header Key 값과, 해당 Vertical 값에 해당되는 Calendar(Start, End) 와 RectF 를 갖는다.
     */
    public class ScheduleRect {
        private String headerKey;
        private RectF rectF;
        private Calendar startTime;
        private Calendar endTime;

        /**
         */
        public ScheduleRect(String headerKey, RectF rectF, Calendar startTime, Calendar endTime) {
            this.headerKey = headerKey;
            this.rectF = rectF;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public Calendar getEndTime() {
            return endTime;
        }

        public void setEndTime(Calendar endTime) {
            this.endTime = endTime;
        }

        public Calendar getStartTime() {
            return startTime;
        }

        public void setStartTime(Calendar startTime) {
            this.startTime = startTime;
        }

        public RectF getRectF() {
            return rectF;
        }

        public void setRectF(RectF rectF) {
            this.rectF = rectF;
        }

        public String getHeaderKey() {
            return headerKey;
        }

        public void setHeaderKey(String headerKey) {
            this.headerKey = headerKey;
        }
    }

    private void getLoadBaseEventRect(float startFromPixel) {

        ScheduleRect scheduleRect;
        RectF rectF;
        for (Header header : getHeaderItems()) {
            for (int i = 0; i < 24; i++) {
                Calendar startTime = ScheduleViewUtil.today();
                startTime.set(Calendar.HOUR_OF_DAY, i);
                startTime.set(Calendar.MINUTE, 0);
                startTime.set(Calendar.SECOND, 0);
                startTime.set(Calendar.MILLISECOND, 0);
                Calendar endTime = (Calendar) startTime.clone();
                endTime.set(Calendar.HOUR_OF_DAY, i + 1);

                // Calculate top.
                float top = mHourHeight * 24 * (startTime.get(Calendar.HOUR_OF_DAY) * 60 + startTime.get(Calendar.MINUTE)) / 1440
                        + mCurrentOrigin.y + mHeaderHeight + mHeaderRowPadding * 2
                        + mHeaderMarginBottom + mTimeTextHeight / 2 + mEventMarginTop;

                // Calculate bottom.
                float bottom = mHourHeight * 24 * (endTime.get(Calendar.HOUR_OF_DAY) * 60 + endTime.get(Calendar.MINUTE)) / 1440
                        + mCurrentOrigin.y + mHeaderHeight + mHeaderRowPadding * 2
                        + mHeaderMarginBottom + mTimeTextHeight / 2 - mEventMarginBottom;

                // Calculate left.
                float left = startFromPixel + 0F * mWidthPerDay + mEventMarginLeft;
                if (left < startFromPixel)
                    left += mOverlappingEventGap;

                // Calculate right.
                float right = left + 1F * mWidthPerDay - mEventMarginRight;
                if (right < startFromPixel + mWidthPerDay)
                    right -= mOverlappingEventGap;

                // Draw the event and the event name on top of it.
                if (left < right && left < getWidth() && top < getHeight() && right > mHeaderColumnWidth &&
                        bottom > mHeaderHeight + mHeaderRowPadding * 2 + mTimeTextHeight / 2 + mHeaderMarginBottom) {
                    rectF = new RectF(left, top, right, bottom);
                } else
                    rectF = null;

                scheduleRect = new ScheduleRect(header.getHeaderKey(), rectF, startTime, endTime);
                mScheduleRects.add(scheduleRect);
            }
        }
    }


    /**
     * Event 를 Load 합니다.
     */
    private void getLoadEvents() {

        // Get more events if the month is changed.
        if (mEventRects == null)
            mEventRects = new ArrayList<>();
        if (mScheduleViewLoader == null && !isInEditMode())
            throw new IllegalStateException("You must provide a EventLoader");

        // If a refresh was requested then reset some variables.
        if (mRefreshEvents) {
            mEventRects.clear();
//            mPreviousPeriodEvents = null;
//            mCurrentEvents = null;
//            mNextPeriodEvents = null;
//            mFetchedPeriod = -1;
        }

        // ScheduleViewLoader 를 통해 Events 를 가져오는 부분
        if (mScheduleViewLoader != null) {
            if (mRefreshEvents) {
                List<? extends ScheduleViewEvent> currentEvents = null;

                if (mCurrentEvents != null) {
                    currentEvents = mCurrentEvents;
                }

                // CurrentEvents 가 null 인경우 Loader 를 통해 Events Load
                if (currentEvents == null)
                    currentEvents = mScheduleViewLoader.onLoad();


                // Clear events.
                mEventRects.clear();
                sortAndCacheEvents(currentEvents);
                calculateHeaderHeight();

                mCurrentEvents = currentEvents;
//                mFetchedPeriod = periodToFetch;
            }
        }

        // Prepare to calculate positions of each events.
        List<EventRect> tempEvents = mEventRects;
        mEventRects = new ArrayList<EventRect>();

        // Iterate through each day with events to calculate the position of the events.
        while (tempEvents.size() > 0) {
            ArrayList<EventRect> eventRects = new ArrayList<>(tempEvents.size());

            // Get first event for a day.
            EventRect eventRect1 = tempEvents.remove(0);
            eventRects.add(eventRect1);

            int i = 0;
            while (i < tempEvents.size()) {
                // Collect all other events for same day.
                EventRect eventRect2 = tempEvents.get(i);
                if (ScheduleViewUtil.isEventSameHeader(eventRect1.event, eventRect2.event, mViewMode)) {
                    tempEvents.remove(i);
                    eventRects.add(eventRect2);
                } else {
                    i++;
                }
            }
            computePositionOfEvents(eventRects);
        }
    }

    /**
     * Sort and cache events.
     *
     * @param events The events to be sorted and cached.
     */
    private void sortAndCacheEvents(List<? extends ScheduleViewEvent> events) {
        sortEvents(events);
        for (ScheduleViewEvent event : events) {
            cacheEvent(event);
        }
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param events The events to be sorted.
     */
    private void sortEvents(List<? extends ScheduleViewEvent> events) {
        Collections.sort(events, new Comparator<ScheduleViewEvent>() {
            @Override
            public int compare(ScheduleViewEvent event1, ScheduleViewEvent event2) {
                long start1 = event1.getStartTime().getTimeInMillis();
                long start2 = event2.getStartTime().getTimeInMillis();
                int comparator = start1 > start2 ? 1 : (start1 < start2 ? -1 : 0);
                if (comparator == 0) {
                    long end1 = event1.getEndTime().getTimeInMillis();
                    long end2 = event2.getEndTime().getTimeInMillis();
                    comparator = end1 > end2 ? 1 : (end1 < end2 ? -1 : 0);
                }
                return comparator;
            }
        });
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private void cacheEvent(ScheduleViewEvent event) {
        if (event.getStartTime().compareTo(event.getEndTime()) >= 0)
            return;
        List<ScheduleViewEvent> splitEvents = event.splitScheduleViewEvents();
        for (ScheduleViewEvent splitEvent : splitEvents) {
            mEventRects.add(new EventRect(splitEvent, event, null));
        }
    }

    /**
     * 각 이벤트의 좌우 위치를 계산한다. 이 이벤트가 중복되는 경우 특히 유용하다.
     *
     * @param eventRects The events along with their wrapper class.
     */
    private void computePositionOfEvents(List<EventRect> eventRects) {
        // 모든 event 를 비교하여 충돌그룹의 리스트를 만든다.(이 리스트는 동일한 조건의 충돌 event 들의 집합이라고 보면된다.)
        List<List<EventRect>> collisionGroups = new ArrayList<List<EventRect>>();
        for (EventRect eventRect : eventRects) {
            boolean isPlaced = false;

            outerLoop:
            for (List<EventRect> collisionGroup : collisionGroups) {
                for (EventRect groupEvent : collisionGroup) {
//                    if (isEventsCollide(groupEvent.event, eventRect.event) && groupEvent.event.isAllDay() == eventRect.event.isAllDay()) {
                    if (isEventsCollide(groupEvent.event, eventRect.event)) {
                        collisionGroup.add(eventRect);
                        isPlaced = true;
                        break outerLoop;
                    }
                }
            }

            if (!isPlaced) {
                List<EventRect> newGroup = new ArrayList<EventRect>();
                newGroup.add(eventRect);
                collisionGroups.add(newGroup);
            }
        }

        for (List<EventRect> collisionGroup : collisionGroups) {
            expandEventsToMaxWidth(collisionGroup);
        }
    }

    /**
     * event1 과 event2 의 overlap 여부를 반환한다.
     *
     * @param event1 첫번째 event.
     * @param event2 두번째 event..
     * @return 두 event 가 overlap 된다면 true 반환.
     */
    private boolean isEventsCollide(ScheduleViewEvent event1, ScheduleViewEvent event2) {
        long start1 = event1.getStartTime().getTimeInMillis();
        long end1 = event1.getEndTime().getTimeInMillis();
        long start2 = event2.getStartTime().getTimeInMillis();
        long end2 = event2.getEndTime().getTimeInMillis();
        return !((start1 >= end2) || (end1 <= start2));
    }

    /**
     * 가능한 최대 폭 모든 이벤트를 확장합니다. 이벤트는 수평으로 최대 사용 가능한 공간을 차지하려고합니다.
     *
     * @param collisionGroup 서로 중첩되는 event 그룹.
     */
    private void expandEventsToMaxWidth(List<EventRect> collisionGroup) {
        // Expand the events to maximum possible width.
        List<List<EventRect>> columns = new ArrayList<List<EventRect>>();
        columns.add(new ArrayList<EventRect>());
        for (EventRect eventRect : collisionGroup) {
            boolean isPlaced = false;
            for (List<EventRect> column : columns) {
                if (column.size() == 0) {
                    column.add(eventRect);
                    isPlaced = true;
                } else if (!isEventsCollide(eventRect.event, column.get(column.size() - 1).event)) {
                    column.add(eventRect);
                    isPlaced = true;
                    break;
                }
            }
            if (!isPlaced) {
                List<EventRect> newColumn = new ArrayList<EventRect>();
                newColumn.add(eventRect);
                columns.add(newColumn);
            }
        }


        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        int maxRowCount = 0;
        for (List<EventRect> column : columns) {
            maxRowCount = Math.max(maxRowCount, column.size());
        }
        for (int i = 0; i < maxRowCount; i++) {
            // Set the left and right values of the event.
            float j = 0;
            for (List<EventRect> column : columns) {
                if (column.size() >= i + 1) {
                    EventRect eventRect = column.get(i);
                    eventRect.width = 1f / columns.size();
                    eventRect.left = j / columns.size();
                    // TODO: ScheduleViewEvent 에 isAllDay 속성이 없는관계로 아래 부분 수정.
                    /*
                    if(!eventRect.event.isAllDay()) {
                        eventRect.top = eventRect.event.getStartTime().get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.getStartTime().get(Calendar.MINUTE);
                        eventRect.bottom = eventRect.event.getEndTime().get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.getEndTime().get(Calendar.MINUTE);
                    }
                    else{
                        eventRect.top = 0;
                        eventRect.bottom = mAllDayEventHeight;
                    }
                    */
                    eventRect.top = eventRect.event.getStartTime().get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.getStartTime().get(Calendar.MINUTE);
                    eventRect.bottom = eventRect.event.getEndTime().get(Calendar.HOUR_OF_DAY) * 60 + eventRect.event.getEndTime().get(Calendar.MINUTE);

                    mEventRects.add(eventRect);
                }
                j++;
            }
        }
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Public methods.
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    public void goToHour(double hour) {
        if (mAreDimensionsInvalid) {
            mScrollToHour = hour;
            return;
        }

        int verticalOffset = 0;
        if (hour > 24)
            verticalOffset = mHourHeight * 24;
        else if (hour > 0)
            verticalOffset = (int) (mHourHeight * hour);

        if (verticalOffset > mHourHeight * 24 - getHeight() + mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom)
            verticalOffset = (int) (mHourHeight * 24 - getHeight() + mHeaderHeight + mHeaderRowPadding * 2 + mHeaderMarginBottom);

        mCurrentOrigin.y = -verticalOffset;
        invalidate();
    }

    /**
     * Refreshes the view and loads the events again.
     */
    public void notifyDatasetChanged() {
        mRefreshEvents = true;
        invalidate();
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Interfaces.
    //
    /////////////////////////////////////////////////////////////////

    public interface EventClickListener {
        /**
         * Triggered when clicked on one existing event
         *
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        void onEventClick(ScheduleViewEvent event, RectF eventRect);
    }

    public interface EventLongPressListener {
        /**
         * Similar to {@link com.github.sdw8001.scheduleview.WeekView.EventClickListener} but with a long press.
         *
         * @param event:     event clicked.
         * @param eventRect: view containing the clicked event.
         */
        void onEventLongPress(ScheduleViewEvent event, RectF eventRect);
    }

    public interface EmptyViewClickListener {
        /**
         * 빈 Cell 의 Click 이벤트 리스너.
         *
         * @param scheduleRect: {@link com.github.sdw8001.scheduleview.view.ScheduleView.ScheduleRect} 빈 Cell 의 HeaderKey 값과 Row Calendar 값과 RectF 를 갖는 객체.
         */
        void onEmptyViewClicked(ScheduleRect scheduleRect);
    }

    // TODO: Empty 에 대한 Event 객체가 필요할거 같다 위치를 나타낼수 있는 객체의 생성이 필요하고 이를 ScheduleViewEvent 가 상속해서 구현하도록 구조를 바꿔야할거 같다.
    public interface EmptyViewLongPressListener {
        /**
         * 빈 Cell 의 LongPress 이벤트 리스너.
         *
         * @param scheduleRect: {@link com.github.sdw8001.scheduleview.view.ScheduleView.ScheduleRect} 빈 Cell 의 HeaderKey 값과 Row Calendar 값과 RectF 를 갖는 객체.
         */
        void onEmptyViewLongPress(ScheduleRect scheduleRect);
    }

    public interface GroupHeaderClickListener {
        /**
         * {@link GroupHeader} 의 Click 이벤트 리스너.
         *
         * @param groupHeader: {@link GroupHeader} 객체.
         */
        void onGroupHeaderClicked(GroupHeader groupHeader);
    }

    public interface ScrollListener {
        /**
         * Called when the first visible day has changed.
         * <p/>
         * (this will also be called during the first draw of the weekview)
         *
         * @param newFirstVisibleDay The new first visible day
         * @param oldFirstVisibleDay The old first visible day (is null on the first call).
         */
        void onFirstVisibleDayChanged(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay);
    }

    public interface EventDrawListener {
        SpannableStringBuilder onEventDraw(ScheduleViewEvent event, Paint textPaint, int availableWidth, int availableHeight);
    }
}