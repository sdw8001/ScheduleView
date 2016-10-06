package com.github.sdw8001.scheduleview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
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
import android.view.ViewOutlineProvider;
import android.widget.OverScroller;

import com.github.sdw8001.scheduleview.R;
import com.github.sdw8001.scheduleview.event.ScheduleViewEvent;
import com.github.sdw8001.scheduleview.header.GroupHeader;
import com.github.sdw8001.scheduleview.header.Header;
import com.github.sdw8001.scheduleview.interpreter.DateTimeInterpreter;
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
 *
 */
public class ScheduleView extends View {

    private enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    private enum TouchedKind {
        WEEK_DATE_SEEKER_AREA, SCHEDULE_AREA, WEEK_CURRENT_DATE_AREA, NONE_TOUCH
    }

    public static final int VIEW_PARENT = 1;
    public static final int VIEW_CHILD = 2;

    private final Context mContext;

    private boolean mAreDimensionsInvalid = true;
    private boolean mIsZooming;
    private boolean mRefreshEvents = false;

    private Calendar mFirstVisibleDay;  // TODO: FirtVisibleDay 는 Calendar 형식이다. 날짜가아닌 다른 Object 를 List up 할 때 해당 변의 선언과 사용부분을 적절히 수정해야 한다.
    private RectF mCurrentDateRect;
    private List<WeekDateSeekerRect> mWeekDateSeekerRect;
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
    private Paint mEventTypeColorPaint;
    private Paint mEventTypeDetailBackPaint;
    private Paint mEventTypeDetailForePaint;
    private Paint mFocusedEmptyEventPaint;
    private Paint mFocusedEventPaint;

    // Item 의 Width 값
    private float mWidthPerDay;

    // WeekDateSeeker Item 의 Width 값
    private float mWeekWidthPerDay;

    private float mHeaderMarginBottom;
    private int mMinimumFlingVelocity = 0;
    private int mScaledTouchSlop = 0;
    private int mOverlappingEventGap = 0;
    private int mDefaultEventColor;
    private int mEventCornerRadius = 0;
    private int mCachedNumberOfVisible = 3;

    // WeekDateSelector 관련
    private RectF mWeekSeekerGestureRect;
    private float mWeekDateSeekerWidth; // WeekDateSeeker 가로크기
    private float mWeekDateSeekerPortraitHeight = 130; // WeekDateSeeker 세로크기
    private float mWeekSelectedDayCircleRate = 0.7F; // WeekDateSeeker 선택 일 배경 Circle 지름 비율
    private float mWeekDateSeekerHeight; // WeekDateSeeker 세로크기
    private float mWeekDateYearMonthWidth; // WeekDateSeeker 의 YearMonth 표시영역의 가로크기 (Orientation 에 따라 영역의 위치가 바뀔때 사용)
    private float mWeekDateYearMonthHeight; // WeekDateSeeker 의 YearMonth 표시영역의 세로크기 (Orientation 에 따라 영역의 위치가 바뀔때 사용)
    private float mWeekDateDayOfWeekHeight; // WeekDateSeeker 의 DayOfWeek(요일) 표시영역의 세로크기 (Orientation 에 따라 영역의 위치가 바뀔때 사용)
    private float mWeekDateDayOfMonthHeight; // WeekDateSeeker 의 DayOfMonth(월 기준 일자) 표시영역의 세로크기 (Orientation 에 따라 영역의 위치가 바뀔때 사용)
    private int mWeekDateSeekerBackgroundColor;
    private Paint mWeekDateSeekerSeparatorPaint;
    private Paint mWeekDateSeekerBackgroundPaint;
    private Paint mWeekDateSelectedBackgroundPaint;
    private TextPaint mWeekDateDayOfWeekTodayTextPaint;
    private TextPaint mWeekDateDayOfMonthTodayTextPaint;
    private TextPaint mWeekDateYearMonthTextPaint;
    private TextPaint mWeekDateDayOfMonthTextPaint;
    private TextPaint mWeekDateDayOfWeekTextPaint;
    private int mWeekDateSeekerTextSize = 12;
    private int mWeekDateDayOfMonthTextSize = 16;
    private int mWeekDateDayOfWeekTextSize = 12;
    private int mWeekDateSeekerTextPadding = 8;
    private int mWeekDateSeekerTextColor;
    private int mWeekDateSeekerTodayTextColor;
    private int mWeekDateSelectedBackgroundColor;
    private int mWeekScrollDuration = 250;
    private TouchedKind mTouchedKind = TouchedKind.NONE_TOUCH;

    private PointF mWeekCurrentOrigin = new PointF(0f, 0f);
    private Direction mWeekCurrentScrollDirection = Direction.NONE;
    private Direction mWeekCurrentFlingDirection = Direction.NONE;
    private GestureDetectorCompat mWeekGestureDetector;
    private OverScroller mWeekScroller;

    // Time 범위 관련 변수
    private Calendar mTimeStart = Calendar.getInstance();
    private Calendar mTimeEnd = Calendar.getInstance();

    // Attributes 의 초기값과 함께 선언
    private int mTypeColor = Color.rgb(255, 0, 0);
    private boolean mUseTypeColor = false;
    private int mTimeStartHour = 0;
    private int mTimeStartMinute = 0;
    private int mTimeEndHour = 24;
    private int mTimeEndMinute = 0;
    private int mTimeDuration = 60;
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
    private int mFocusedEmptyEventColor = Color.rgb(32, 32, 255);
    private int mFocusedEventColor = Color.rgb(255, 32, 32);
    private int mFocusedEmptyEventStrokeWidth = 3;
    private int mFocusedEventStrokeWidth = 3;
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
    private int mOrientation;
    private Calendar mFocusedWeekDate = null;
    private EventRect mFocusedEventRect = null;
    private ScheduleRect mFocusedEmptyEventRect = null;
    private boolean mCellFocusable = true;
    private boolean mEventRectShadowEnabled = true;
    private boolean mHeaderRowShadowEnabled = true;
    private boolean mHorizontalFlingEnabled = true;
    private boolean mVerticalFlingEnabled = true;
    private boolean mShowDistinctWeekendColor = false;
    private boolean mShowDistinctPastFutureColor = false;
    private boolean mWeekDateVisible = false;
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
    private DateCalendarListener mDateCalendarListener;

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
        mOrientation = mContext.getResources().getConfiguration().orientation;

        int[] usingThemeAttrs = new int[]{
                R.attr.colorPrimary, // 0
                R.attr.colorPrimaryDark, // 1
                R.attr.colorAccent, // 2
                R.attr.colorControlNormal, // 3
                R.attr.colorControlActivated, // 4
                R.attr.colorControlHighlight, // 5
                android.R.attr.textColorPrimary, // 6
                android.R.attr.textColorPrimaryInverse, // 7
                android.R.attr.textColorSecondary, // 8
                android.R.attr.textColorSecondaryInverse, // 9
                android.R.attr.textColorHighlight, // 10
                android.R.attr.textColorHighlightInverse // 11
        };

        TypedArray ta = context.obtainStyledAttributes(attrs, usingThemeAttrs);

        mWeekDateSeekerTodayTextColor = ta.getColor(6, mHeaderColumnTextColor);
        mWeekDateSelectedBackgroundColor = ta.getColor(2, mWeekDateSelectedBackgroundColor);
        mWeekDateSeekerBackgroundColor = ta.getColor(0, mWeekDateSeekerBackgroundColor);


        // Get the attribute values (if any).
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScheduleView, 0, 0);
        try {
//            mFirstDayOfWeek = a.getInteger(R.styleable.ScheduleView_firstDayOfWeek, mFirstDayOfWeek);
            mTimeStartHour = a.getInteger(R.styleable.ScheduleView_timeStartHour, mTimeStartHour);
            mTimeStartMinute = a.getInteger(R.styleable.ScheduleView_timeStartMinute, mTimeStartMinute);
            mTimeEndHour = a.getInteger(R.styleable.ScheduleView_timeEndHour, mTimeEndHour);
            mTimeEndMinute = a.getInteger(R.styleable.ScheduleView_timeEndMinute, mTimeEndMinute);
            mTimeDuration = a.getInteger(R.styleable.ScheduleView_timeDuration, mTimeDuration);
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
            mFocusedEmptyEventColor = a.getColor(R.styleable.ScheduleView_focusedEmptyEventColor, mFocusedEmptyEventColor);
            mFocusedEventColor = a.getColor(R.styleable.ScheduleView_focusedEventColor, mFocusedEventColor);
            mFocusedEmptyEventStrokeWidth = a.getDimensionPixelSize(R.styleable.ScheduleView_focusedEmptyEventStrokeWidth, mFocusedEmptyEventStrokeWidth);
            mFocusedEventStrokeWidth = a.getDimensionPixelSize(R.styleable.ScheduleView_focusedEventStrokeWidth, mFocusedEventStrokeWidth);
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
            mWeekDateVisible = a.getBoolean(R.styleable.ScheduleView_weekDateVisible, mWeekDateVisible);
            mAllDayEventHeight = a.getDimensionPixelSize(R.styleable.ScheduleView_allDayEventHeight, mAllDayEventHeight);
            mScrollDuration = a.getInt(R.styleable.ScheduleView_scrollDuration, mScrollDuration);
            mHeaderType = a.getInteger(R.styleable.ScheduleView_headerType, mHeaderType);
        } finally {
            a.recycle();
        }

        mDefaultEventColor = Color.parseColor("#9fc6e7");

        init();
    }

    private void init() {
        // Scrolling initialization.
        mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);
        mScroller = new OverScroller(mContext, new FastOutLinearInInterpolator());

        //TODO: Time 범위설정
        mTimeStart.set(Calendar.HOUR_OF_DAY, mTimeStartHour);
        mTimeStart.set(Calendar.MINUTE, mTimeStartMinute);
        mTimeEnd.set(Calendar.HOUR_OF_DAY, mTimeEndHour);
        mTimeEnd.set(Calendar.MINUTE, mTimeEndMinute);

        //TODO:WeekDateSeeker
        mWeekGestureDetector = new GestureDetectorCompat(mContext, mWeekGestureListener);
        mWeekScroller = new OverScroller(mContext, new FastOutLinearInInterpolator());

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

        //TODO:WeekDateSeeker
        mWeekDateSeekerHeight = mOrientation == Configuration.ORIENTATION_LANDSCAPE ? mWeekDateSeekerPortraitHeight * 0.6F : mWeekDateSeekerPortraitHeight;
        mWeekDateYearMonthHeight = mOrientation == Configuration.ORIENTATION_LANDSCAPE ? mWeekDateSeekerHeight : (float) (mWeekDateSeekerHeight * 0.4);
        mWeekDateDayOfWeekHeight = mOrientation == Configuration.ORIENTATION_LANDSCAPE ? (float) (mWeekDateSeekerHeight * 0.4) : (float) (mWeekDateSeekerHeight * 0.2);
        mWeekDateDayOfMonthHeight = mOrientation == Configuration.ORIENTATION_LANDSCAPE ? (float) (mWeekDateSeekerHeight * 0.6) : (float) (mWeekDateSeekerHeight * 0.4);
//        mWeekDateSeekerBackgroundColor = ContextCompat.getColor(mContext, R.color.colorPrimary);
//        mWeekDateSeekerBackgroundColor = mContext.getTheme().ob
        // WeekDateSeeker Selected Date Background Paint
        mWeekDateSelectedBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWeekDateSelectedBackgroundPaint.setColor(mWeekDateSelectedBackgroundColor);
        // WeekDateSeeker Background Paint
        mWeekDateSeekerBackgroundPaint = new Paint();
        mWeekDateSeekerBackgroundPaint.setColor(mWeekDateSeekerBackgroundColor);
        mWeekDateSeekerTextColor = ContextCompat.getColor(mContext, R.color.white);

        // WeekDateSeeker 년/월 TextPaint
        mWeekDateYearMonthTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mWeekDateYearMonthTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mWeekDateYearMonthTextPaint.setTextSize(mWeekDateSeekerTextSize);
        mWeekDateYearMonthTextPaint.setColor(mWeekDateSeekerTextColor);
        // WeekDateSeeker 요일 TextPaint
        mWeekDateDayOfWeekTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mWeekDateDayOfWeekTextPaint.setTextAlign(Paint.Align.CENTER);
        mWeekDateDayOfWeekTextPaint.setTextSize(mWeekDateDayOfWeekTextSize);
        mWeekDateDayOfWeekTextPaint.setColor(mWeekDateSeekerTextColor);
        // WeekDateSeeker 월기준일자 TextPaint
        mWeekDateDayOfMonthTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mWeekDateDayOfMonthTextPaint.setTextAlign(Paint.Align.CENTER);
        mWeekDateDayOfMonthTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mWeekDateDayOfMonthTextPaint.setTextSize(mWeekDateDayOfMonthTextSize);
        mWeekDateDayOfMonthTextPaint.setColor(mWeekDateSeekerTextColor);
        // WeekDateSeeker Today 요일 TextPaint
        mWeekDateDayOfWeekTodayTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mWeekDateDayOfWeekTodayTextPaint.setTextAlign(Paint.Align.CENTER);
        mWeekDateDayOfWeekTodayTextPaint.setTextSize(mWeekDateDayOfWeekTextSize);
        mWeekDateDayOfWeekTodayTextPaint.setColor(mWeekDateSeekerTodayTextColor);
        // WeekDateSeeker Today 월기준일자 TextPaint
        mWeekDateDayOfMonthTodayTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mWeekDateDayOfMonthTodayTextPaint.setTextAlign(Paint.Align.CENTER);
        mWeekDateDayOfMonthTodayTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mWeekDateDayOfMonthTodayTextPaint.setTextSize(mWeekDateDayOfMonthTextSize);
        mWeekDateDayOfMonthTodayTextPaint.setColor(mWeekDateSeekerTodayTextColor);
        // WeekDateSeeker 구분선 TextPaint
        mWeekDateSeekerSeparatorPaint = new Paint();
        mWeekDateSeekerSeparatorPaint.setStyle(Paint.Style.STROKE);
        mWeekDateSeekerSeparatorPaint.setStrokeWidth(1);
        mWeekDateSeekerSeparatorPaint.setColor(mWeekDateSeekerTextColor);

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
            mHeaderBackgroundPaint.setShadowLayer(mHeaderRowShadowRadius, 0, 0, Color.LTGRAY);
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

        // Prepare event background color.
        mEventBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEventBackgroundPaint.setColor(Color.rgb(174, 208, 238));
        if (mEventRectShadowEnabled) {
            mEventBackgroundPaint.setShadowLayer(mEventRectShadowRadius, 0, 0, Color.GRAY);
            this.setLayerType(LAYER_TYPE_SOFTWARE, mEventBackgroundPaint);
        }

        // Prepare event type paint.
        mEventTypeColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEventTypeColorPaint.setColor(Color.rgb(174, 208, 238));

        // Prepare event TypeDetailFore paint.
        mEventTypeDetailForePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEventTypeDetailForePaint.setTextAlign(Paint.Align.CENTER);
        mEventTypeDetailForePaint.setTextSize(mTextSize);
        mEventTypeDetailForePaint.setShadowLayer(mEventRectShadowRadius / 2, 0, 0, Color.GRAY);
        mEventTypeDetailForePaint.setFakeBoldText(true);

        // Prepare event TypeDetailBack paint.
        mEventTypeDetailBackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEventTypeDetailBackPaint.setShadowLayer(mEventRectShadowRadius, 0, 0, Color.GRAY);


        // Prepare cell focused paint.
        mFocusedEmptyEventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFocusedEmptyEventPaint.setColor(mFocusedEmptyEventColor);
        mFocusedEmptyEventPaint.setStyle(Paint.Style.STROKE);
        mFocusedEmptyEventPaint.setStrokeWidth(mFocusedEmptyEventStrokeWidth);
        mFocusedEmptyEventPaint.setPathEffect(new DashPathEffect(new float[]{10, 3}, 0));

        // Prepare cell focused paint.
        mFocusedEventPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFocusedEventPaint.setColor(mFocusedEventColor);
        mFocusedEventPaint.setStyle(Paint.Style.STROKE);
        mFocusedEventPaint.setStrokeWidth(mFocusedEventStrokeWidth);
        mFocusedEventPaint.setPathEffect(new DashPathEffect(new float[]{10, 3}, 0));

        // Prepare header column background color.
        mHeaderColumnBackgroundPaint = new Paint();
        mHeaderColumnBackgroundPaint.setColor(mHeaderColumnBackgroundColor);

        // Prepare event text size and color.
        mEventTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        mEventTextPaint.setStyle(Paint.Style.FILL);
        mEventTextPaint.setColor(mEventTextColor);
        mEventTextPaint.setTextSize(mEventTextSize);

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
        int start = 0, end = getRowCount();

        for (int i = start; i < end; i++) {
            // Measure time string and get max width.
            Calendar calendar = (Calendar) mTimeStart.clone();
            calendar.add(Calendar.MINUTE, mTimeDuration * i);
            String time = getDateTimeInterpreter().interpretTime(calendar);
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            mTimeTextWidth = Math.max(mTimeTextWidth, mTimeTextPaint.measureText(time));
        }
    }

    private int getRowCount() {
        return getTotalMinute() / mTimeDuration;
    }

    private int getTotalMinute() {
        return (int) (mTimeEnd.getTimeInMillis() - mTimeStart.getTimeInMillis()) / (1000 * 60);
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
                            (int) -(mHourHeight * getRowCount() + getDrawEventsTop() - getHeight()), 0);
                    break;
                case VERTICAL:
                    mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y,
                            0, (int) velocityY,
                            Integer.MIN_VALUE, Integer.MAX_VALUE,
                            (int) -(mHourHeight * getRowCount() + getDrawEventsTop() - getHeight()), 0);
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

                        // FocusedEventRect 가 현재 EventRect 와 같은 Rect 를 선택 했을 때, 해당 Point 의 EmptyEventRect 로 Focus 를 이동한다.
                        if (mFocusedEventRect != null && getFocusedEventRect().rectF == event.rectF) {
                            if (mEmptyViewClickListener != null && isContainsContentsArea(e) && clickEmptyRect(e)) {
                                return super.onSingleTapConfirmed(e);
                            }
                        }

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
            if (mEmptyViewClickListener != null && isContainsContentsArea(e) && clickEmptyRect(e)) {
                return super.onSingleTapConfirmed(e);
            }

            // if 내부는 GroupHeaderClickListener 에 이벤트를 전달하는 역할
            if (mGroupHeaderClickListener != null && mGroupHeaderItems != null) {
                // mGroupHeaderItem 의 RectF 가 화면에 여러개 노출될때 마지막 노출된 GroupHeader 의 RectF 만 올바르게 RectF 설정이 되어서 뒤에서부터 체크
                for (int i = mGroupHeaderItems.size() - 1; i >= 0; i--) {
                    // TouchPoint 가 GroupHeader 의 RectF 에 포함되면 이벤트 전달.
                    if (mGroupHeaderItems.get(i).getRectF() != null && mGroupHeaderItems.get(i).getRectF().contains(e.getX(), e.getY())) {
                        playSoundEffect(SoundEffectConstants.CLICK);
                        mGroupHeaderClickListener.onGroupHeaderClicked(mGroupHeaderItems.get(i));
                        invalidate();
                        return super.onSingleTapConfirmed(e);
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

    private final GestureDetector.SimpleOnGestureListener mWeekGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            if (mTouchedKind == TouchedKind.WEEK_DATE_SEEKER_AREA)
                goToNearestWeekDate();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mTouchedKind == TouchedKind.WEEK_DATE_SEEKER_AREA) {
                switch (mWeekCurrentScrollDirection) {
                    case NONE: {
                        // Allow scrolling only in one direction.
                        if (Math.abs(distanceX) > Math.abs(distanceY)) {
                            if (distanceX > 0) {
                                mWeekCurrentScrollDirection = Direction.LEFT;
                            } else {
                                mWeekCurrentScrollDirection = Direction.RIGHT;
                            }
                        }
                        break;
                    }
                    case LEFT: {
                        // Change direction if there was enough change.
                        if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX < -mScaledTouchSlop)) {
                            mWeekCurrentScrollDirection = Direction.RIGHT;
                        }
                        break;
                    }
                    case RIGHT: {
                        // Change direction if there was enough change.
                        if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX > mScaledTouchSlop)) {
                            mWeekCurrentScrollDirection = Direction.LEFT;
                        }
                        break;
                    }
                }

                // Calculate the new origin after scroll.
                switch (mWeekCurrentScrollDirection) {
                    case LEFT:
                    case RIGHT:
                        mWeekCurrentOrigin.x -= distanceX * mXScrollingSpeed;
                        ViewCompat.postInvalidateOnAnimation(ScheduleView.this);
                        break;
                }
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            if ((mWeekCurrentFlingDirection == Direction.LEFT && !mHorizontalFlingEnabled) ||
                    (mWeekCurrentFlingDirection == Direction.RIGHT && !mHorizontalFlingEnabled)) {
                return true;
            }

            mWeekScroller.forceFinished(true);

            mWeekCurrentFlingDirection = mWeekCurrentScrollDirection;
            switch (mWeekCurrentFlingDirection) {
                case LEFT:
                case RIGHT:
                    mWeekScroller.fling((int) mWeekCurrentOrigin.x, (int) mWeekCurrentOrigin.y,
                            (int) (velocityX * mXScrollingSpeed), 0,
                            Integer.MIN_VALUE, Integer.MAX_VALUE,
                            0, 0);
                    break;
            }

            ViewCompat.postInvalidateOnAnimation(ScheduleView.this);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            // if 내부는 mWeekDateClickListener 에 이벤트를 전달하는 역할
            if (mWeekDateSeekerRect != null) {
                List<WeekDateSeekerRect> reversedWeekDateRects = mWeekDateSeekerRect;

                // reversedEventRects 를 역으로 뒤집는다.
                Collections.reverse(reversedWeekDateRects);

                // reversedEventRects 의 item 들을 반복하며
                for (WeekDateSeekerRect weekDateSeekerRect : reversedWeekDateRects) {

                    // TouchPoint 가 EventRect 에 포함되면
                    if (weekDateSeekerRect.rectF != null && weekDateSeekerRect.rectF.contains(e.getX(), e.getY())) {

                        mFocusedWeekDate = weekDateSeekerRect.getDate();

                        // EventClickListener 에 이벤트를 전달하고
                        setFocusedWeekDate(weekDateSeekerRect.getDate(), false);

                        // 기기에 SoundEffect Click 효과를 수행한다.
                        playSoundEffect(SoundEffectConstants.CLICK);

                        return super.onSingleTapConfirmed(e);
                    }
                }
            }

            // if 내부는 mCalendarListener 에 이벤트를 전달하는 역할, TouchPoint 가 EventRect 에 포함되면
            if (mCurrentDateRect != null && mDateCalendarListener != null && mCurrentDateRect.contains(e.getX(), e.getY())) {
                // CalendarListener 에 onSelectPicker 이벤트를 전달하고
                mDateCalendarListener.onSelectPicker(mFocusedWeekDate);

                // 기기에 SoundEffect Click 효과를 수행한다.
                playSoundEffect(SoundEffectConstants.CLICK);

                invalidate();
                return super.onSingleTapConfirmed(e);
            }

            return super.onSingleTapConfirmed(e);
        }
    };

    /**
     * MotionEvent 의 Point 가 EmptyEventRect 에 Click 가능한경우 mEmptyViewClickListener 에 onEmptyViewClicked Event 를 전달하고 true 를 반환한다.
     *
     * @param e MotionEvent
     * @return 빈 Rect 영역 선택여부 반환.
     */
    private boolean clickEmptyRect(MotionEvent e) {
        for (ScheduleRect scheduleRect : mScheduleRects) {

            if (mFixedGroupHeader != null && !mFixedGroupHeader.getHeaderKey().equals(scheduleRect.getParentHeaderKey()))
                continue;

            // TouchPoint 가 EventRect 에 포함되면
            if (scheduleRect.rectF != null && scheduleRect.rectF.contains(e.getX(), e.getY())) {
                playSoundEffect(SoundEffectConstants.CLICK);
                setFocusedEmptyScheduleRect(scheduleRect);
                mEmptyViewClickListener.onEmptyViewClicked(scheduleRect);
                invalidate();
                return true;
            }
        }
        return false;
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Draw 관련 함수
    //
    /////////////////////////////////////////////////////////////////

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mAreDimensionsInvalid = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new CustomOutline(w, h));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class CustomOutline extends ViewOutlineProvider {
        int width, height;

        CustomOutline(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRect(0, 0, width, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCurrentDateRect = null;

        // Schedule Header 와 Event 를 그린다.
        drawHeaderRowAndEvents(canvas);

        // Schedule 의 Vertical Time  axes/separators 를 그린다.
        drawTimeColumnAndAxes(canvas);

        // WeekDateSeeker 를 그린다.
        if (mWeekDateVisible)
            drawWeekCalendar(canvas);
    }

    private void drawWeekCalendar(Canvas canvas) {

        mWeekDateSeekerWidth = this.getMeasuredWidth();
        mWeekDateYearMonthWidth = mHeaderColumnWidth;
        Calendar today = ScheduleViewUtil.today();

        // View 의 Width 값에 대하여 하루 WeekWidth 값을 계산한다.
        mWeekWidthPerDay = mWeekDateSeekerWidth;
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
            mWeekWidthPerDay = mWeekWidthPerDay - mWeekDateYearMonthWidth;
        mWeekWidthPerDay = mWeekWidthPerDay / 7; // 주 7일로 하루 WeekWidth 값은 7등분

        // 0. WeekDateSeeker Gesture Rect 영역 초기화
        if (mWeekSeekerGestureRect == null) {
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
                mWeekSeekerGestureRect = new RectF(mWeekDateYearMonthWidth, 0, mWeekDateSeekerWidth, mWeekDateSeekerHeight);
            else
                mWeekSeekerGestureRect = new RectF(0, mWeekDateYearMonthHeight, mWeekDateSeekerWidth, mWeekDateSeekerHeight);
        }

        // 1. WeekDateSeeker Background 영역 그리기
        canvas.clipRect(0, 0, mWeekDateSeekerWidth, mWeekDateSeekerHeight, Region.Op.REPLACE);
        canvas.drawRect(0, 0, mWeekDateSeekerWidth, mWeekDateSeekerHeight, mWeekDateSeekerBackgroundPaint);

        // 2. WeekDateYearMonth 관련 그리기
        int textLayoutWidth = mOrientation == Configuration.ORIENTATION_LANDSCAPE ?
                (int) mWeekDateYearMonthWidth - mWeekDateSeekerTextPadding * 2 : (int) mWeekDateSeekerWidth - mWeekDateSeekerTextPadding * 2;
        int year, month, dayOfMonth;
        if (mFocusedWeekDate == null) {
            mFocusedWeekDate = ScheduleViewUtil.today();
        }
        year = mFocusedWeekDate.get(Calendar.YEAR);
        month = mFocusedWeekDate.get(Calendar.MONTH);
        dayOfMonth = mFocusedWeekDate.get(Calendar.DAY_OF_MONTH);

        StaticLayout textLayout = new StaticLayout(year + "\n" + (month + 1) + "월 " + dayOfMonth + "일",
                mWeekDateYearMonthTextPaint,
                textLayoutWidth,
                Layout.Alignment.ALIGN_CENTER, 1.0f, 1.0f, false);

        int startPointY = mOrientation == Configuration.ORIENTATION_LANDSCAPE ?
                (int) (mWeekDateSeekerHeight - textLayout.getHeight()) / 2 : (int) (mWeekDateYearMonthHeight - textLayout.getHeight()) / 2;

        float textMaxWidth = 0;

        // textLayout 의 Max LineWidth 구하기
        for (int i = 0; i < textLayout.getLineCount(); i++) {
            if (textLayout.getLineWidth(i) > textMaxWidth)
                textMaxWidth = textLayout.getLineWidth(i);
        }

        // 현재 선택 년월일 Rect 설정
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
            mCurrentDateRect = new RectF((mWeekDateYearMonthWidth - textMaxWidth) / 2,
                    mWeekDateSeekerTextPadding,
                    ((mWeekDateYearMonthWidth - textMaxWidth) / 2) + textMaxWidth,
                    mWeekDateYearMonthHeight - mWeekDateSeekerTextPadding);
        else
            mCurrentDateRect = new RectF((mWeekDateSeekerWidth - textMaxWidth) / 2,
                    mWeekDateSeekerTextPadding,
                    ((mWeekDateSeekerWidth - textMaxWidth) / 2) + textMaxWidth,
                    mWeekDateYearMonthHeight - mWeekDateSeekerTextPadding);

        canvas.save();
        canvas.translate(mWeekDateSeekerTextPadding, startPointY);
        textLayout.draw(canvas);
        canvas.restore();


        // 3. YearMonth 와 WeekSeeker 영역 구분선 그리기
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
            canvas.drawLine(mWeekDateYearMonthWidth - 1, 0, mWeekDateYearMonthWidth - 1, mWeekDateSeekerHeight, mWeekDateSeekerSeparatorPaint);
        else
            canvas.drawLine(0, mWeekDateYearMonthHeight, mWeekDateSeekerWidth, mWeekDateYearMonthHeight, mWeekDateSeekerSeparatorPaint);

        // 4. WeekSeeker 그리기
        // Consider scroll offset.
        int leftDaysWithGaps = (int) -(Math.ceil(mWeekCurrentOrigin.x / mWeekWidthPerDay));
        float startFromPixel = mWeekCurrentOrigin.x + mWeekWidthPerDay * leftDaysWithGaps;
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
            startFromPixel = startFromPixel + mWeekDateYearMonthWidth;
        float startPixel = startFromPixel;

        // Prepare to iterate for each day.
        Calendar day;

        // WeekDateSeeker(Scroll 가능 영역) 의 ClipRect 설정.
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
            canvas.clipRect(mWeekDateYearMonthWidth, 0, mWeekDateSeekerWidth, mWeekDateSeekerHeight, Region.Op.REPLACE);
        else
            canvas.clipRect(0, mWeekDateYearMonthHeight, mWeekDateSeekerWidth, mWeekDateSeekerHeight, Region.Op.REPLACE);

        // WeekDateSeekerRect 초기화
        if (mWeekDateSeekerRect != null) {
            mWeekDateSeekerRect.clear();
            mWeekDateSeekerRect = null;
        }
        mWeekDateSeekerRect = new ArrayList<>();

        for (int dayNumber = leftDaysWithGaps + 1; dayNumber <= leftDaysWithGaps + 7 + 1; dayNumber++) {
            // Check if the day is today.
            day = (Calendar) today.clone();
            day.add(Calendar.DATE, dayNumber - 1);
            boolean sameDay = ScheduleViewUtil.isSameDay(day, today);

            // WeekDateSeekerRect 영역 list 에 추가
            if (mWeekDateSeekerRect != null) {
                RectF rectF;
                float centerX, radius, sumSpace = 5;
                centerX = startPixel + mWeekWidthPerDay / 2;
                radius = mWeekDateDayOfMonthHeight * mWeekSelectedDayCircleRate / 2;
                if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    rectF = new RectF(centerX - radius - sumSpace, 0, centerX + radius + sumSpace, mWeekDateSeekerHeight);
                } else {
                    rectF = new RectF(centerX - radius - sumSpace, mWeekDateYearMonthHeight, centerX + radius + sumSpace, mWeekDateSeekerHeight);
                }
                mWeekDateSeekerRect.add(new WeekDateSeekerRect((Calendar) day.clone(), rectF));
            }

            // 요일 그리기
            String dayOfWeekLabel = getDateTimeInterpreter().interpretDayOfWeek(day);
            if (dayOfWeekLabel == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null date");
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE)
                canvas.drawText(dayOfWeekLabel,
                        startPixel + mWeekWidthPerDay / 2,
                        (mWeekDateSeekerHeight - mWeekDateDayOfMonthHeight) / 2,
                        sameDay ? mWeekDateDayOfWeekTodayTextPaint : mWeekDateDayOfWeekTextPaint);
            else
                canvas.drawText(dayOfWeekLabel,
                        startPixel + mWeekWidthPerDay / 2,
                        mWeekDateYearMonthHeight + (mWeekDateSeekerHeight - mWeekDateYearMonthHeight - mWeekDateDayOfMonthHeight + mWeekDateDayOfWeekTextSize) / 2,
                        sameDay ? mWeekDateDayOfWeekTodayTextPaint : mWeekDateDayOfWeekTextPaint);

            // Day 그리기
            String dayOfMonthLabel = getDateTimeInterpreter().interpretDayOfMonth(day);
            if (dayOfMonthLabel == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null date");
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                // 그려줄 day 가 선택된 Date 와 같거나, 선택된 Date 가 없을때 today 와 같은 경우 Circle 을 그린다.
                if ((mFocusedWeekDate != null && mFocusedWeekDate.equals(day))
                        || mFocusedWeekDate == null && day.equals(today)) {
                    canvas.drawCircle(startPixel + mWeekWidthPerDay / 2,
                            mWeekDateDayOfWeekHeight + (mWeekDateSeekerHeight - mWeekDateDayOfWeekHeight - mWeekDateDayOfMonthTextSize * 0.8F) / 2,
                            mWeekDateDayOfMonthHeight * mWeekSelectedDayCircleRate / 2,
                            mWeekDateSelectedBackgroundPaint);
                }
                canvas.drawText(dayOfMonthLabel,
                        startPixel + mWeekWidthPerDay / 2,
                        mWeekDateDayOfWeekHeight + (mWeekDateSeekerHeight - mWeekDateDayOfWeekHeight) / 2,
                        sameDay ? mWeekDateDayOfMonthTodayTextPaint : mWeekDateDayOfMonthTextPaint);
            } else {
                // 그려줄 day 가 선택된 Date 와 같거나, 선택된 Date 가 없을때 today 와 같은 경우 Circle 을 그린다.
                if ((mFocusedWeekDate != null && mFocusedWeekDate.equals(day))
                        || mFocusedWeekDate == null && day.equals(today)) {
                    canvas.drawCircle(startPixel + mWeekWidthPerDay / 2,
                            mWeekDateYearMonthHeight + mWeekDateDayOfWeekHeight + (mWeekDateSeekerHeight - mWeekDateYearMonthHeight - mWeekDateDayOfWeekHeight) / 2,
                            mWeekDateDayOfMonthHeight * mWeekSelectedDayCircleRate / 2,
                            mWeekDateSelectedBackgroundPaint);
                }
                canvas.drawText(dayOfMonthLabel,
                        startPixel + mWeekWidthPerDay / 2,
                        mWeekDateYearMonthHeight + mWeekDateDayOfWeekHeight + (mWeekDateSeekerHeight - mWeekDateYearMonthHeight - mWeekDateDayOfWeekHeight + mWeekDateDayOfMonthTextSize * 0.8F) / 2,
                        sameDay ? mWeekDateDayOfMonthTodayTextPaint : mWeekDateDayOfMonthTextPaint);
            }

            startPixel += mWeekWidthPerDay;
        }
    }

    private void calculateHeaderHeight() {
        mHeaderHeight = mHeaderTextHeight;
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

        if (mAreDimensionsInvalid) {
            mEffectiveMinHourHeight = Math.max(mMinHourHeight, (int) ((getHeight() - (getDrawHeaderTop() + getDrawHeaderHeight() + mHeaderMarginBottom)) / getRowCount()));

            mAreDimensionsInvalid = false;
            if (mScrollToDay != null)
//                setFocusedWeekDate(mScrollToDay);   // TODO: setFocusedWeekDate() 라는 일자별 이동 함수 관련된것을 다른 Object Header 의 형식으로 바꿔야한다

                mAreDimensionsInvalid = false;
            if (mScrollToHour >= 0)
                goToHour(mScrollToHour);

            mScrollToDay = null;
            mScrollToHour = -1;
            mAreDimensionsInvalid = false;
        }

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
        if (mCurrentOrigin.y < getHeight() - mHourHeight * getRowCount() - getDrawEventsTop())
            mCurrentOrigin.y = getHeight() - mHourHeight * getRowCount() - getDrawEventsTop();

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
        if (mGroupHeaderItems != null && leftDaysWithGaps + mNumberOfVisibleDays > getHeaderItemSize() - 1 && getHeaderItemSize() - mNumberOfVisibleDays >= 0) {
            leftDaysWithGaps = getHeaderItemSize() - mNumberOfVisibleDays;
            mCurrentOrigin.x = -(leftDaysWithGaps * (mWidthPerDay + mColumnGap));
            mScroller.forceFinished(true);
            startFromPixel = mCurrentOrigin.x + (mWidthPerDay + mColumnGap) * leftDaysWithGaps + mHeaderColumnWidth;
            startPixel = startFromPixel;
        }

        // 각 시간에 구분선을 그리기 위해 준비
        int lineCount = (int) ((getHeight() - (getDrawHeaderTop() + getDrawHeaderHeight() + mHeaderMarginBottom)) / mHourHeight) + 1;
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
        canvas.clipRect(getDrawEventsLeft(), getDrawEventsTop(), getDrawEventsLeft() + getDrawEventsWidth(), getDrawEventsTop() + getDrawEventHeight(), Region.Op.REPLACE);

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
                if (mFocusedWeekDate == null)
                    mFocusedWeekDate = ScheduleViewUtil.today();

                getLoadEvents(mFocusedWeekDate);
                mRefreshEvents = false;
            }

            // 각 Day Event Cell 에 Background 를 그린다.
            float start = (startPixel < mHeaderColumnWidth ? mHeaderColumnWidth : startPixel);
            if (mWidthPerDay + startPixel - start > 0) {
                canvas.drawRect(start, getDrawEventsTop(), startPixel + mWidthPerDay, getHeight(), mDayBackgroundPaint);
            }

            // hourLines 배열에 시간 분할선을 그리기 위한 현재 HeaderColumn 의 index 에 해당되는 x,y 좌표들을 설정한다.
            int i = 0;
            for (int hourNumber = 0; hourNumber < getRowCount(); hourNumber++) {
                float top = getDrawEventsTop() + mCurrentOrigin.y + mHourHeight * hourNumber;
                if (top > getDrawEventsTop() - mHourSeparatorHeight && top < getHeight() && startPixel + mWidthPerDay - start > 0) {
                    hourLines[i * 4] = start;
                    hourLines[i * 4 + 1] = top;
                    hourLines[i * 4 + 2] = startPixel + mWidthPerDay;
                    hourLines[i * 4 + 3] = top;
                    i++;
                }
            }

            // 시간별 구분선을 그린다
            canvas.drawLines(hourLines, mHourSeparatorPaint);

            // Empty Base Events 에 RectF 를 설정한다.
            drawBaseEvent(dayNumber, startPixel);

            // Events 를 그린다
            drawEvents(getHeaderItem(dayNumber), startPixel, canvas);

            // In the next iteration, start from the next day.
            startPixel += mWidthPerDay + mColumnGap;
        }
        if (mCellFocusable && getFocusedEmptyScheduleRect() != null && getFocusedEmptyScheduleRect().rectF != null) {
            canvas.drawRoundRect(getFocusedEmptyScheduleRect().rectF, mEventCornerRadius, mEventCornerRadius, mFocusedEmptyEventPaint);
        }

        // 현재 일자 표시 그리기 (왼쪽 상단의 첫 Cell) WeekDateVisible 이 false 일 때만 현재 년월일 표시
        if (!mWeekDateVisible) {
            canvas.clipRect(0, 0, mTimeTextWidth + mHeaderColumnPadding * 2 - 1, mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount(), Region.Op.REPLACE);
            canvas.drawRect(0, 0, mTimeTextWidth + mHeaderColumnPadding * 2 - 1, mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount(), mHeaderBackgroundPaint);
            int textLayoutWidth = (int) mTimeTextWidth + mHeaderColumnPadding * 2 - 1;
            int year, month, dayOfMonth;
            if (mFocusedWeekDate == null)
                mFocusedWeekDate = ScheduleViewUtil.today();

            year = mFocusedWeekDate.get(Calendar.YEAR);
            month = mFocusedWeekDate.get(Calendar.MONTH);
            dayOfMonth = mFocusedWeekDate.get(Calendar.DAY_OF_MONTH);

            StaticLayout textLayout = new StaticLayout(year + "\n" + (month + 1) + "월 " + dayOfMonth + "일",
                    mWeekDateYearMonthTextPaint,
                    textLayoutWidth,
                    Layout.Alignment.ALIGN_CENTER, 1.0f, 1.0f, false);

            int startPointY = getDrawHeaderTop() + ((int) getDrawHeaderHeight() - textLayout.getHeight()) / 2;

            float textMaxWidth = 0;

            // textLayout 의 Max LineWidth 구하기
            for (int i = 0; i < textLayout.getLineCount(); i++) {
                if (textLayout.getLineWidth(i) > textMaxWidth)
                    textMaxWidth = textLayout.getLineWidth(i);
            }

            // 현재 선택 년월일 Rect 설정
            mCurrentDateRect = new RectF(0, getDrawHeaderTop(), textLayoutWidth, getDrawHeaderTop() + getDrawHeaderHeight());

            canvas.save();
            canvas.translate(0, startPointY);
            textLayout.draw(canvas);
            canvas.restore();
        }

        // Header 를 Paint 하기위한 영역을 ClipRect 로 지정합니다.
        canvas.clipRect(getDrawHeaderLeft(), getDrawHeaderTop(), getDrawHeaderLeft() + getDrawHeaderWidth(), getDrawHeaderTop() + getDrawHeaderHeight(), Region.Op.REPLACE);

        // Header Background 를 그린다.
//        canvas.drawRect(0, 0, getWidth(), mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount(), mHeaderBackgroundPaint);


        // GroupHeaderItems 캐시 삭제
        if (mGroupHeaderItems != null) {
            for (GroupHeader groupHeader : mGroupHeaderItems) {
                groupHeader.setRectF(null);
            }
        }

        // Header Text 를 그린다.
        startPixel = startFromPixel;
        String tempGroupHeaderKey = null;
        int gap = leftDaysWithGaps;
        for (int dayNumber = leftDaysWithGaps; dayNumber <= leftDaysWithGaps + mNumberOfVisibleDays; dayNumber++) {
            if (mGroupHeaderItems == null || dayNumber < 0 || dayNumber > getHeaderItemSize() - 1)
                break;

            // 이상하게 clip y 값을 0 으로 해도 text 그릴때 mHeaderTextHeight 정도 마이너스되서 계산된다. 그래서 y 값을 임의로 mHeaderTextHeight 더해줌
//            float startHeaderY = mHeaderTextHeight;
            float startHeaderY = getDrawHeaderTop() + mHeaderTextHeight;

            // Header Group Text 를 그린다.
            if (mViewMode == VIEW_CHILD) {
                startHeaderY = startHeaderY + mHeaderRowPadding;
                GroupHeader groupHeader = getGroupHeaderItemToChildIndex(dayNumber);
                if (groupHeader != null && !groupHeader.getHeaderKey().equals(tempGroupHeaderKey)) {
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
            // FixedGroupHeader 와 groupHeader 가 참조는 다르지만 값이 같으면 대체하여 반환.
            for (GroupHeader groupHeader : mGroupHeaderItems) {
                if (mFixedGroupHeader != groupHeader && mFixedGroupHeader.equals(groupHeader)) {
                    mFixedGroupHeader = groupHeader;
                    break;
                }
            }
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
                return mGroupHeaderItems.toArray(new Header[1]);
            case VIEW_CHILD:
                List<Header> headers = new ArrayList<>();

                for (GroupHeader groupHeader : mGroupHeaderItems) {
                    if (groupHeader.getSubHeaders() == null)
                        continue;

                    headers.addAll(groupHeader.getSubHeaders());
                }
                return headers.toArray(new Header[1]);
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
     * @param index index
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

    private void drawBaseEvent(int columnIndex, float startFromPixel) {
        int columnGap = getRowCount() * columnIndex;
        List<ScheduleRect> scheduleRects = new ArrayList<>();
        if (mFixedGroupHeader != null) {
            for (ScheduleRect rect : mScheduleRects) {
                if (mFixedGroupHeader.getHeaderKey().equals(rect.getParentHeaderKey()))
                    scheduleRects.add(rect);
            }
        } else {
            scheduleRects = mScheduleRects;
        }
        for (int i = columnGap; i < getRowCount() + columnGap; i++) {

            // Calculate top.
//            float top = mHourHeight * getRowCount() * (scheduleRects.get(i).startTime.get(Calendar.HOUR_OF_DAY) * mTimeDuration + scheduleRects.get(i).startTime.get(Calendar.MINUTE)) / getTotalMinute()
//                    + mCurrentOrigin.y + getDrawEventsTop() + mEventMarginTop;
            float top = mHourHeight * getRowCount() * (scheduleRects.get(i).startTime.getTimeInMillis() - mTimeStart.getTimeInMillis()) / (1000 * 60) / getTotalMinute()
                    + mCurrentOrigin.y + getDrawEventsTop() + mEventMarginTop;

            // Calculate bottom.
//            float bottom = mHourHeight * getRowCount() * ((scheduleRects.get(i).endTime.get(Calendar.DAY_OF_MONTH) - scheduleRects.get(i).startTime.get(Calendar.DAY_OF_MONTH)) * mTimeDuration * getRowCount() + scheduleRects.get(i).endTime.get(Calendar.HOUR_OF_DAY) * mTimeDuration + scheduleRects.get(i).endTime.get(Calendar.MINUTE)) / getTotalMinute()
//                    + mCurrentOrigin.y + getDrawEventsTop() - mEventMarginBottom;
            float bottom = mHourHeight * getRowCount() * ((scheduleRects.get(i).startTime.getTimeInMillis() - mTimeStart.getTimeInMillis()) / (1000 * 60) + mTimeDuration) / getTotalMinute()
                    + mCurrentOrigin.y + getDrawEventsTop() - mEventMarginBottom;

            // Calculate left.
            float left = startFromPixel + 0F * mWidthPerDay + mEventMarginLeft;
            if (left < startFromPixel)
                left += mOverlappingEventGap;

            // Calculate right.
            float right = left + 1F * mWidthPerDay - mEventMarginRight;
            if (right < startFromPixel + mWidthPerDay)
                right -= mOverlappingEventGap;

            // Draw the event and the event name on top of it.
            if (left <= right && left <= getWidth() && top <= getHeight() && right >= mHeaderColumnWidth && bottom >= getDrawEventsTop()) {
                scheduleRects.get(i).rectF = new RectF(left, top, right, bottom);
            } else
                scheduleRects.get(i).rectF = null;

            if (getFocusedEmptyScheduleRect() != null &&
                    getFocusedEmptyScheduleRect().headerKey.equals(scheduleRects.get(i).headerKey) &&
                    getFocusedEmptyScheduleRect().startTime == scheduleRects.get(i).startTime) {
                getFocusedEmptyScheduleRect().rectF = scheduleRects.get(i).rectF;
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
                    float top = mHourHeight * (mEventRects.get(i).top - (mTimeStart.get(Calendar.HOUR_OF_DAY) * 60 + mTimeStart.get(Calendar.MINUTE))) / mTimeDuration
                            + mCurrentOrigin.y + getDrawEventsTop() + mEventMarginTop;

                    // Calculate bottom.
                    float bottom = mHourHeight * (mEventRects.get(i).bottom - (mTimeStart.get(Calendar.HOUR_OF_DAY) * 60 + mTimeStart.get(Calendar.MINUTE))) / mTimeDuration
                            + mCurrentOrigin.y + getDrawEventsTop() - (mEventMarginTop + mEventMarginBottom);

                    // Calculate left.
                    float left = startFromPixel + mEventRects.get(i).left * mWidthPerDay + mEventMarginLeft;
                    if (left < startFromPixel)
                        left += mOverlappingEventGap;

                    // Calculate right.
                    float right = left + mEventRects.get(i).width * mWidthPerDay - (mEventMarginLeft + mEventMarginRight);
                    if (right < startFromPixel + mWidthPerDay)
                        right -= mOverlappingEventGap;

                    // Draw the event and the event name on top of it.
                    if (left <= right && left <= getWidth() && top <= getHeight() && right >= mHeaderColumnWidth && bottom >= getDrawEventsTop()) {
                        mEventRects.get(i).rectF = new RectF(left, top, right, bottom);

                        // Draw Background
                        if (mEventRects.get(i).event.getBackgroundColor() == 0)
                            mEventBackgroundPaint.setColor(mDefaultEventColor);
                        else
                            mEventBackgroundPaint.setColor(mEventRects.get(i).event.getBackgroundColor());
                        canvas.drawRoundRect(mEventRects.get(i).rectF, mEventCornerRadius, mEventCornerRadius, mEventBackgroundPaint);

                        // Draw TypeColor
                        float typeColorWidth = mEventCornerRadius + 3;
                        mEventTypeColorPaint.setColor(mEventRects.get(i).event.getTypeColor());
                        canvas.drawRoundRect(new RectF(left, top, left + mEventCornerRadius * 2, bottom), mEventCornerRadius, mEventCornerRadius, mEventTypeColorPaint);
                        mEventTypeColorPaint.setColor(mEventBackgroundPaint.getColor());
                        canvas.drawRect(new RectF(left + typeColorWidth, top, left + mEventCornerRadius * 2 + 1, mEventRects.get(i).rectF.bottom), mEventTypeColorPaint);

                        // Draw TypeDetail
                        float typeDetailStringWidth = 30;
                        float typeDetailPadding = 4;
                        float typeDetailWidth = typeDetailStringWidth + typeDetailPadding * 2;
                        float typeDetailX = left + typeDetailWidth / 2;
                        float typeDetailY = top + typeColorWidth + typeDetailWidth / 2 - mEventTypeDetailForePaint.getTextSize() / 4;
                        float eventTitleX = left + typeDetailWidth;
                        float eventTitleY = top;
                        float typeDetailLeft = left + typeDetailPadding;
                        float typeDetailTop = top + typeDetailPadding;
                        float typeDetailRight = typeDetailLeft + typeDetailStringWidth;
                        float typeDetailBottom = typeDetailTop + typeDetailStringWidth;
                        float typeDetailCornerRadius = mEventCornerRadius / 2;

                        // Event 의 TypeColor 와 BackgroundColor 가 다른 경우, Type 의 구분이 있으므로 X Position 을 TypeColorWidth 만큼 더해준다.
                        if (mEventRects.get(i).event.getTypeColor() != mEventBackgroundPaint.getColor()) {
                            typeDetailX += typeColorWidth;
                            eventTitleX += typeColorWidth;
                            typeDetailLeft += typeColorWidth;
                            typeDetailRight += typeColorWidth;
                        }
                        mEventTypeDetailForePaint.setColor(mEventRects.get(i).event.getTypeDetailForeColor());
                        mEventTypeDetailBackPaint.setColor(mEventRects.get(i).event.getTypeDetailBackColor());
                        if (!("G,S,R,N").contains(mEventRects.get(i).event.getTypeDetail()))
                            canvas.drawRoundRect(new RectF(typeDetailLeft, typeDetailTop, typeDetailRight, typeDetailBottom), typeDetailCornerRadius, typeDetailCornerRadius, mEventTypeDetailBackPaint);
                        canvas.drawText(mEventRects.get(i).event.getTypeDetail(), typeDetailX, typeDetailY, mEventTypeDetailForePaint);

                        if (mCellFocusable && getFocusedEventRect() != null && getFocusedEventRect().originalEvent.getKey().equals(mEventRects.get(i).originalEvent.getKey()))
                            canvas.drawRoundRect(mEventRects.get(i).rectF, mEventCornerRadius, mEventCornerRadius, mFocusedEventPaint);

                        // Draw EventTitle
                        drawEventTitle(mEventRects.get(i).event, mEventRects.get(i).rectF, canvas, eventTitleX, eventTitleY);
                    } else
                        mEventRects.get(i).rectF = null;
                }
            }
        }
    }

    private void drawTimeColumnAndAxes(Canvas canvas) {
        // TimeColumn Header 의 배경을 그립니다.
        canvas.drawRect(0, getDrawHeaderTop() + getDrawHeaderHeight(), mHeaderColumnWidth, getHeight(), mHeaderColumnBackgroundPaint);

        // 왼쪽의 Time Column Header 를 Paint 하기위한 영역을 ClipRect 로 지정합니다.
        canvas.clipRect(0, getDrawHeaderTop() + getDrawHeaderHeight(), mHeaderColumnWidth, getHeight(), Region.Op.REPLACE);

        // 왼쪽의 Time Column Header 에 시간 별 Text 를 그립니다.
        int start = 0, end = (int) (mTimeEnd.getTimeInMillis() - mTimeStart.getTimeInMillis()) / (1000 * 60 * mTimeDuration);
        for (int i = start; i < end; i++) {
            float top = getDrawHeaderTop() + getDrawHeaderHeight() + mCurrentOrigin.y + mHourHeight * i + mHeaderMarginBottom;

            // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the point at the bottom-right corner..
            Calendar calendar = (Calendar) mTimeStart.clone();
            calendar.add(Calendar.MINUTE, mTimeDuration * i);
            String time = getDateTimeInterpreter().interpretTime(calendar);
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            if (top < getHeight())
                canvas.drawText(time, mTimeTextWidth + mHeaderColumnPadding, top + mTimeTextHeight, mTimeTextPaint);
        }
    }

    /**
     * Draw the name of the event on top of the event rectangle.
     *
     * @param event        The event of which the title (and location) should be drawn.
     * @param rect         The rectangle on which the text is to be drawn.
     * @param canvas       The canvas to draw upon.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     * @param originalTop  The original top position of the rectangle. The rectangle may have some of its portion outside of the visible area.
     */
    private void drawEventTitle(ScheduleViewEvent event, RectF rect, Canvas canvas, float originalLeft, float originalTop) {
        if (rect.right - rect.left - mEventPadding * 2 < 0) return;
        if (rect.bottom - rect.top - mEventPadding * 2 < 0) return;

        int availableHeight = (int) (rect.bottom - originalTop - mEventPadding * 2);
        int availableWidth = (int) (rect.right - originalLeft - mEventPadding * 2);

        if (availableWidth < mEventTextPaint.getTextSize() + mEventPadding)
            return;

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
        return e.getX() > mHeaderColumnWidth && e.getY() > (getDrawHeaderTop() + getDrawHeaderHeight() + mHeaderMarginBottom);
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
    //      OnDraw 좌표 관련 함수
    //
    /////////////////////////////////////////////////////////////////

    /**
     * Header 가 그려지는 Rect 의 Left 값을 반환한다.
     *
     * @return ScheduleView 의 Header 가 그려질 Rect 영역의 Left 값.
     */
    private float getDrawHeaderLeft() {
        return mHeaderColumnWidth;
    }

    /**
     * Header 가 그려지는 Rect 의 Top 값을 반환한다.
     *
     * @return ScheduleView 의 Header 가 그려질 Rect 영역의 Top 값.
     */
    private int getDrawHeaderTop() {
        if (mWeekDateVisible)
            return (int) mWeekDateSeekerHeight;
        else
            return 0;
    }

    /**
     * Header 가 그려지는 Rect 의 Width 값을 반환한다.
     *
     * @return ScheduleView 의 Header 가 그려질 Rect 영역의 Width 값.
     */
    private int getDrawHeaderWidth() {
        return getWidth();
    }

    /**
     * Header 가 그려지는 Rect 의 Height 값을 반환한다.
     *
     * @return ScheduleView 의 Header 가 그려질 Rect 영역의 Height 값.
     */
    private float getDrawHeaderHeight() {
        return mHeaderHeight + mHeaderRowPadding * 2 * getHeaderRowCount();
    }

    /**
     * Event 들이 그려지는 Rect 의 Left 값을 반환한다.
     *
     * @return ScheduleView 의 ScheduleEvent 가 그려질 Rect 영역의 Left 값.
     */
    private float getDrawEventsLeft() {
        return mHeaderColumnWidth;
    }

    /**
     * Event 들이 그려지는 Rect 의 Top 값을 반환한다.
     *
     * @return ScheduleView 의 ScheduleEvent 가 그려질 Rect 영역의 Top 값.
     */
    private float getDrawEventsTop() {
        return getDrawHeaderTop() + getDrawHeaderHeight() + mHeaderMarginBottom + mTimeTextHeight / 2;
    }

    /**
     * Event 들이 그려지는 Rect 의 Width 값을 반환한다.
     *
     * @return ScheduleView 의 ScheduleEvent 가 그려질 Rect 영역의 Width 값.
     */
    private int getDrawEventsWidth() {
        return getWidth() - (int) getDrawEventsLeft();
    }

    /**
     * Event 들이 그려지는 Rect 의 Height 값을 반환한다.
     *
     * @return ScheduleView 의 ScheduleEvent 가 그려질 Rect 영역의 Height 값.
     */
    private int getDrawEventHeight() {
        return getHeight() - (int) getDrawEventsTop();
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Scrolling 동작관련 함수
    //
    /////////////////////////////////////////////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean val;
        if (event.getAction() == MotionEvent.ACTION_DOWN && mTouchedKind == TouchedKind.NONE_TOUCH) {
            if (mWeekDateVisible && mWeekSeekerGestureRect.contains(event.getX(), event.getY()))
                mTouchedKind = TouchedKind.WEEK_DATE_SEEKER_AREA;
            else if (mCurrentDateRect.contains(event.getX(), event.getY()))
                mTouchedKind = TouchedKind.WEEK_CURRENT_DATE_AREA;
            else
                mTouchedKind = TouchedKind.SCHEDULE_AREA;
        }

        if (mTouchedKind == TouchedKind.WEEK_DATE_SEEKER_AREA) {
            // Action MouseEvent in WeekDateSeeker Area
            val = mWeekGestureDetector.onTouchEvent(event);

            // mWeekGestureDetector 체크가 끝난 후, mWeekCurrentFlingDirection 과 mWeekCurrentScrollDirection 의 상태를 설정한다.
            if (event.getAction() == MotionEvent.ACTION_UP && mWeekCurrentFlingDirection == Direction.NONE) {
                if (mWeekCurrentScrollDirection == Direction.RIGHT || mWeekCurrentScrollDirection == Direction.LEFT) {
                    goToNearestWeekDate();
                }
                mWeekCurrentScrollDirection = Direction.NONE;
            }
        } else if (mTouchedKind == TouchedKind.WEEK_CURRENT_DATE_AREA) {
            // Action MouseEvent in WeekDateSeeker Area
            val = mWeekGestureDetector.onTouchEvent(event);
        } else {
            // Action MouseEvent in ScheduleView Area
            mScaleDetector.onTouchEvent(event);
            val = mGestureDetector.onTouchEvent(event);

            // mGestureDetector 체크가 끝난 후, mCurrentFlingDirection 과 mCurrentScrollDirection 의 상태를 설정한다.
            if (event.getAction() == MotionEvent.ACTION_UP && !mIsZooming && mCurrentFlingDirection == Direction.NONE) {
                if (mCurrentScrollDirection == Direction.RIGHT || mCurrentScrollDirection == Direction.LEFT) {
                    goToNearestOrigin();
                }
                mCurrentScrollDirection = Direction.NONE;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP)
            mTouchedKind = TouchedKind.NONE_TOUCH;

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

    private void goToNearestWeekDate() {
        double leftDays = mWeekCurrentOrigin.x / mWeekWidthPerDay;

        if (mWeekCurrentFlingDirection != Direction.NONE) {
            // snap to nearest day
            leftDays = Math.round(leftDays);
        } else if (mWeekCurrentScrollDirection == Direction.LEFT) {
            // snap to last day
            leftDays = Math.floor(leftDays);
        } else if (mWeekCurrentScrollDirection == Direction.RIGHT) {
            // snap to next day
            leftDays = Math.ceil(leftDays);
        } else {
            // snap to nearest day
            leftDays = Math.round(leftDays);
        }

        // 현재 X값에 가장 가까운 WeekDate 구하고
        int nearestWeekDate = (int) (mWeekCurrentOrigin.x - leftDays * mWeekWidthPerDay);

        if (nearestWeekDate != 0) {
            // Stop current animation.
            mWeekScroller.forceFinished(true);
            // Snap to date.
            mWeekScroller.startScroll((int) mWeekCurrentOrigin.x, (int) mWeekCurrentOrigin.y, -nearestWeekDate, 0, (int) (Math.abs(nearestWeekDate) / mWeekWidthPerDay * mWeekScrollDuration));
            ViewCompat.postInvalidateOnAnimation(ScheduleView.this);
        }
        // Reset scrolling and fling direction.
        mWeekCurrentScrollDirection = mWeekCurrentFlingDirection = Direction.NONE;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        // WeekDateSeeker Scroll
        if (mWeekScroller.isFinished()) {
            if (mWeekCurrentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestWeekDate();
            }
        } else {
            if (mWeekCurrentFlingDirection != Direction.NONE && forceFinishScroll(mWeekScroller)) {
                goToNearestWeekDate();
            } else if (mWeekScroller.computeScrollOffset()) {
                mWeekCurrentOrigin.y = mWeekScroller.getCurrY();
                mWeekCurrentOrigin.x = mWeekScroller.getCurrX();
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        // Schedule Scroll
        if (mScroller.isFinished()) {
            if (mCurrentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestOrigin();
            }
        } else {
            if (mCurrentFlingDirection != Direction.NONE && forceFinishScroll(mScroller)) {
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
     * @param scroller OverScroller
     * @return true 이면 Scrolling 작업을 중지하고 End Scrolling 관련 Animation 을 실행한다.
     */
    private boolean forceFinishScroll(OverScroller scroller) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && scroller.getCurrVelocity() <= mMinimumFlingVelocity;
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
        this.mFixedGroupHeader = null;
        this.setNumberOfVisibleDays(mCachedNumberOfVisible, refresh);
        if (refresh)
            notifyDataSetChanged();
    }

    public List<? extends ScheduleViewEvent> getCurrentEvents() {
        return mCurrentEvents;
    }

    public GroupHeader getFixedGroupHeader() {
        return mFixedGroupHeader;
    }

    public void setFixedGroupHeader(GroupHeader fixedGroupHeader) {
        setFixedGroupHeader(fixedGroupHeader, false);
    }

    public void setFixedGroupHeader(GroupHeader fixedGroupHeader, boolean refresh) {
        if (mViewMode == VIEW_CHILD) {
            this.mFixedGroupHeader = fixedGroupHeader;
            if (fixedGroupHeader != null) {
                this.mNumberOfVisibleDays = mFixedGroupHeader.getSubHeaders().size();
            } else {
                this.mNumberOfVisibleDays = mCachedNumberOfVisible > getHeaderItemSize() ? getHeaderItemSize() : mCachedNumberOfVisible;
            }
            mCurrentOrigin.x = 0;
            mCurrentOrigin.y = 0;
            if (refresh) {
                invalidate();
            }
        }
    }

    public Calendar getFocusedWeekDate() {
        return mFocusedWeekDate;
    }

    public void setFocusedWeekDate(Calendar calendar, boolean changeCurrentOriginX) {
        if (changeCurrentOriginX) {
            Calendar today = ScheduleViewUtil.today();
            long leftDaysWithGaps = -(ScheduleViewUtil.resetDay(calendar).getTimeInMillis() - today.getTimeInMillis()) / (getRowCount() * mTimeDuration * 60 * 1000);
            mWeekCurrentOrigin.x = leftDaysWithGaps * mWeekWidthPerDay;
        }

        if (calendar != null) {
            mFocusedWeekDate = calendar;
            mRefreshEvents = true;
            invalidate();
        }
    }

    public EventRect getFocusedEventRect() {
        return mFocusedEventRect;
    }

    public void setFocusedEventRect(EventRect focusedEvent) {
        this.mFocusedEventRect = focusedEvent;
        this.mFocusedEmptyEventRect = null;
    }

    public ScheduleRect getFocusedEmptyScheduleRect() {
        return mFocusedEmptyEventRect;
    }

    public void setFocusedEmptyScheduleRect(ScheduleRect focusedEmptyScheduleRect) {
        this.mFocusedEmptyEventRect = focusedEmptyScheduleRect;
        this.mFocusedEventRect = null;
    }

    public List<GroupHeader> getGroupHeaderItems() {
        return mGroupHeaderItems;
    }

    public void setGroupHeaderItems(List<GroupHeader> groupHeaderItems) {
        this.mGroupHeaderItems = groupHeaderItems;
        this.setNumberOfVisibleDays(this.mNumberOfVisibleDays);
    }

    public void setGroupHeaderItems(List<GroupHeader> groupHeaderItems, boolean refresh) {
        this.mGroupHeaderItems = groupHeaderItems;
        this.setNumberOfVisibleDays(this.mNumberOfVisibleDays, refresh);
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
                        return sdf.format(date.getTime()).toUpperCase();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }

                @Override
                public String interpretDayOfMonth(Calendar date) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault()); // LENGTH_LONG
                        return sdf.format(date.getTime()).toUpperCase();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }

                @Override
                public String interpretDayOfWeek(Calendar date) {
                    try {
//                        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault()); // LENGTH_LONG
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.ENGLISH); // LENGTH_LONG
                        return sdf.format(date.getTime()).toUpperCase();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }

                @Override
                public String interpretTime(Calendar calendar) {

                    try {
                        SimpleDateFormat sdf = DateFormat.is24HourFormat(getContext()) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("a hh:mm", Locale.getDefault());
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
                        SimpleDateFormat sdf = DateFormat.is24HourFormat(getContext()) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("a hh:mm", Locale.getDefault());
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
        if (refresh) {
            mCurrentOrigin.x = 0;
            mCurrentOrigin.y = 0;
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
     * Type Color 를 반환합니다.
     * @return Type Color
     */
    public int getTypeColor() {
        return mTypeColor;
    }

    /**
     * Type Color 를 설정합니다.
     * @param mTypeColor Type Color
     */
    public void setTypeColor(int mTypeColor) {
        this.mTypeColor = mTypeColor;
    }

    /**
     * Type Color 사용여부를 반환합니다.
     * @return Type Color 사용여부
     */
    public boolean isUseTypeColor() {
        return mUseTypeColor;
    }

    /**
     * Type Color 사용여부를 설정합니다.
     * @param mUseTypeColor Type Color 사용여부
     */
    public void setUseTypeColor(boolean mUseTypeColor) {
        this.mUseTypeColor = mUseTypeColor;
    }

    /**
     * 시작 Hour 를 반환합니다
     * @return 시작 Hour
     */
    public int getTimeStartHour() {
        return mTimeStartHour;
    }

    /**
     * 시작 Hour 를 설정합니다.
     * @param mTimeStartHour 시작 Hour
     */
    public void setTimeStartHour(int mTimeStartHour) {
        this.mTimeStartHour = mTimeStartHour;
        mTimeStart.set(Calendar.HOUR_OF_DAY, mTimeStartHour);
    }

    /**
     * 시작 Minute 를 반환합니다
     * @return 시작 Minute
     */
    public int getTimeStartMinute() {
        return mTimeStartMinute;
    }

    /**
     * 시작 Minute 를 설정합니다.
     * @param mTimeStartMinute 시작 Minute
     */
    public void setTimeStartMinute(int mTimeStartMinute) {
        this.mTimeStartMinute = mTimeStartMinute;
        mTimeStart.set(Calendar.MINUTE, mTimeStartMinute);
    }

    /**
     * 마지막 Hour 를 반환합니다.
     * @return 마지막 Hour
     */
    public int getTimeEndHour() {
        return mTimeEndHour;
    }

    /**
     * 마지막 Hour 를 설정합니다.
     * @param mTimeEndHour 마지막 Hour
     */
    public void setTimeEndHour(int mTimeEndHour) {
        this.mTimeEndHour = mTimeEndHour;
        mTimeEnd.set(Calendar.HOUR_OF_DAY, mTimeEndHour);
    }

    /**
     * 마지막 Minute 를 반환합니다.
     * @return 마지막 Minute
     */
    public int getTimeEndMinute() {
        return mTimeEndMinute;
    }

    /**
     * 마지막 Minute 를 설정합니다.
     * @param mTimeEndMinute 마지막 Minute
     */
    public void setTimeEndMinute(int mTimeEndMinute) {
        this.mTimeEndMinute = mTimeEndMinute;
        mTimeEnd.set(Calendar.MINUTE, mTimeEndMinute);
    }

    /**
     * Time Duration 을 반환합니다.
     * @return Time Duration
     */
    public int getTimeDuration() {
        return mTimeDuration;
    }

    /**
     * Time Duration 을 설정합니다.
     * @param mTimeDuration Time Duration
     */
    public void setTimeDuration(int mTimeDuration) {
        this.mTimeDuration = mTimeDuration;
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

    public void setGroupHeaderClickListener(GroupHeaderClickListener groupHeaderClickListener) {
        this.mGroupHeaderClickListener = groupHeaderClickListener;
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

    public DateCalendarListener getDateCalendarListener() {
        return mDateCalendarListener;
    }

    public void setDateCalendarListener(DateCalendarListener dateCalendarListener) {
        this.mDateCalendarListener = dateCalendarListener;
    }


    /////////////////////////////////////////////////////////////////
    //
    //      Event Rect Class 및 관련 Method
    //
    /////////////////////////////////////////////////////////////////

    /**
     * WeekDateSeeker 의 DateRect Class 입니다.
     * 해당 일자를 Calendar 로 갖고 있으며 WeekDateSeeker 영역에 해당 일자의 RectF 를 갖고있습니다.
     */
    public class WeekDateSeekerRect {
        private Calendar date;
        private RectF rectF;

        public WeekDateSeekerRect(Calendar date, RectF rectF) {
            this.date = date;
            this.rectF = rectF;
        }

        public Calendar getDate() {
            return date;
        }

        public void setDate(Calendar date) {
            this.date = date;
        }

        public RectF getRectF() {
            return rectF;
        }

        public void setRectF(RectF rectF) {
            this.rectF = rectF;
        }
    }

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
         * <p>
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
        private String parentHeaderKey;
        private String parentHeaderName;
        private String headerKey;
        private String headerName;
        private RectF rectF;
        private Calendar startTime;
        private Calendar endTime;

        /**
         */
        public ScheduleRect(String parentHeaderKey, String parentHeaderName, String headerKey, String headerName, RectF rectF, Calendar startTime, Calendar endTime) {
            this.parentHeaderKey = parentHeaderKey;
            this.parentHeaderName = parentHeaderName;
            this.headerKey = headerKey;
            this.headerName = headerName;
            this.rectF = rectF;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getParentHeaderKey() {
            return parentHeaderKey;
        }

        public void setParentHeaderKey(String parentHeaderKey) {
            this.parentHeaderKey = parentHeaderKey;
        }

        public String getParentHeaderName() {
            return parentHeaderName;
        }

        public void setParentHeaderName(String parentHeaderName) {
            this.parentHeaderName = parentHeaderName;
        }

        public String getHeaderKey() {
            return headerKey;
        }

        public void setHeaderKey(String headerKey) {
            this.headerKey = headerKey;
        }

        public String getHeaderName() {
            return headerName;
        }

        public void setHeaderName(String headerName) {
            this.headerName = headerName;
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
    }

    private void getLoadBaseEventRect(float startFromPixel) {
        if (getHeaderItems() == null)
            return;

        ScheduleRect scheduleRect;
        RectF rectF;

        for (Header header : getHeaderItems()) {
            for (int i = 0; i < getRowCount(); i++) {
                /*
                Calendar startTime = ScheduleViewUtil.today();
                startTime.set(Calendar.HOUR_OF_DAY, i);
                startTime.set(Calendar.MINUTE, 0);
                startTime.set(Calendar.SECOND, 0);
                startTime.set(Calendar.MILLISECOND, 0);
                Calendar endTime = (Calendar) startTime.clone();
                endTime.set(Calendar.HOUR_OF_DAY, i + 1);
                */
                Calendar startTime, endTime;
//                startTime = ScheduleViewUtil.today();
                startTime = (Calendar) mTimeStart.clone();
                startTime.add(Calendar.MINUTE, mTimeDuration * i);
                endTime = (Calendar) startTime.clone();
                endTime.add(Calendar.MINUTE, mTimeDuration);

                // Calculate top.
//                float top = mHourHeight * getRowCount() * (startTime.get(Calendar.HOUR_OF_DAY) * 60 + startTime.get(Calendar.MINUTE)) / getTotalMinute()
//                        + mCurrentOrigin.y + getDrawEventsTop() + mEventMarginTop;
                float top = mHourHeight * i
                        + mCurrentOrigin.y + getDrawEventsTop() + mEventMarginTop;

                // Calculate bottom.
//                float bottom = mHourHeight * getRowCount() * ((endTime.get(Calendar.DAY_OF_MONTH) - startTime.get(Calendar.DAY_OF_MONTH)) * 60 * 24 + endTime.get(Calendar.HOUR_OF_DAY) * 60 + endTime.get(Calendar.MINUTE)) / getTotalMinute()
//                        + mCurrentOrigin.y + getDrawEventsTop() - mEventMarginBottom;
                float bottom = mHourHeight * i + mHourHeight
                        + mCurrentOrigin.y + getDrawEventsTop() - mEventMarginBottom;

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
                        bottom > getDrawEventsTop()) {
                    rectF = new RectF(left, top, right, bottom);
                } else
                    rectF = null;

                scheduleRect = new ScheduleRect(header.getParentHeaderKey(), header.getParentHeaderName(), header.getHeaderKey(), header.getHeaderName(), rectF, startTime, endTime);
                mScheduleRects.add(scheduleRect);
            }
        }
    }


    /**
     * Event 를 Load 합니다.
     */
    private void getLoadEvents(Calendar dateCalendar) {

        // Get more events if the month is changed.
        if (mEventRects == null)
            mEventRects = new ArrayList<>();
        if (mScheduleViewLoader == null && !isInEditMode())
            throw new IllegalStateException("You must provide a EventLoader");

        // If a refresh was requested then reset some variables.
        if (mEventRects == null || mRefreshEvents) {
            mEventRects.clear();
            if (mCurrentEvents != null) {
                mCurrentEvents.clear();
                mCurrentEvents = null;
            }

            // ScheduleViewLoader 를 통해 Events 를 가져오는 부분
            if (mScheduleViewLoader != null) {
                List<? extends ScheduleViewEvent> currentEvents;

                // RefreshEvents 이므로 CurrentEvents 를 Loader 를 통해 Events Load
                currentEvents = mScheduleViewLoader.onLoad(dateCalendar);

                // Clear events.
                mEventRects.clear();
                sortAndCacheEvents(currentEvents);
                calculateHeaderHeight();

                mCurrentEvents = currentEvents;
            }
        }


        // Prepare to calculate positions of each events.
        List<EventRect> tempEvents = mEventRects;
        mEventRects = new ArrayList<>();

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
        if (events == null)
            return;

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
        List<List<EventRect>> collisionGroups = new ArrayList<>();
        for (EventRect eventRect : eventRects) {
            boolean isPlaced = false;

            outerLoop:
            for (List<EventRect> collisionGroup : collisionGroups) {
                for (EventRect groupEvent : collisionGroup) {
                    if (isEventsCollide(groupEvent.event, eventRect.event)) {
                        collisionGroup.add(eventRect);
                        isPlaced = true;
                        break outerLoop;
                    }
                }
            }

            if (!isPlaced) {
                List<EventRect> newGroup = new ArrayList<>();
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
        List<List<EventRect>> columns = new ArrayList<>();
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
                List<EventRect> newColumn = new ArrayList<>();
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
        if (hour > getRowCount())
            verticalOffset = mHourHeight * getRowCount();
        else if (hour > 0)
            verticalOffset = (int) (mHourHeight * hour);

        if (verticalOffset > mHourHeight * getRowCount() - getHeight() + getDrawHeaderTop() + getDrawHeaderHeight() + mHeaderMarginBottom)
            verticalOffset = (int) (mHourHeight * getRowCount() - getHeight() + getDrawHeaderTop() + getDrawHeaderHeight() + mHeaderMarginBottom);

        mCurrentOrigin.y = -verticalOffset;
        invalidate();
    }

    /**
     * Refreshes the view and loads the events again.
     */
    public void notifyDataSetChanged() {
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
         * Similar to {@link com.github.sdw8001.scheduleview.event.ScheduleViewEvent} but with a long press.
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
         * <p>
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

    public interface DateCalendarListener {
        /**
         * DatePicker 가 선택되었을때 Listener 에게 알립니다.
         */
        void onSelectPicker(Calendar calendar);
    }
}
