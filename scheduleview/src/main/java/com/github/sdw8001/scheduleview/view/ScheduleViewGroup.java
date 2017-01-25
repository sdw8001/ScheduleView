package com.github.sdw8001.scheduleview.view;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.github.sdw8001.scheduleview.R;
import com.github.sdw8001.scheduleview.event.ScheduleEvent;
import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;
import com.github.sdw8001.scheduleview.interpreter.TimeInterpreter;
import com.github.sdw8001.scheduleview.loader.EventLoader;
import com.github.sdw8001.scheduleview.loader.HeaderLoader;
import com.github.sdw8001.scheduleview.util.ScheduleTimeManager;
import com.github.sdw8001.scheduleview.util.ScheduleViewUtil;
import com.github.sdw8001.scheduleview.util.TimeStartEnd;
import com.github.sdw8001.scheduleview.view.layout.CheckableHeaderView;
import com.github.sdw8001.scheduleview.view.layout.ScheduleCellView;
import com.github.sdw8001.scheduleview.view.layout.ScheduleEventView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

/**
 * Created by sdw80 on 2016-11-07.
 * Test
 */

public class ScheduleViewGroup extends FrameLayout implements ScheduleCellView.OnCheckedChangeListener,
        ScheduleEventView.OnCheckedChangeListener, ScheduleEventView.OnLongClickListener {

    private enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    private enum ScaleDirection {
        NONE, HORIZONTAL, VERTICAL
    }

    private Context mContext;
    private HeaderLoader mHeaderLoader;
    private EventLoader mEventLoader;

    private boolean mIsZooming;
    private boolean mHorizontalFlingEnabled = true;
    private boolean mVerticalFlingEnabled = false;
    private int mColumnWidth = 1;
    private int mNewHourHeight = -1;
    private int mMinimumFlingVelocity = 0;
    private int mScaledTouchSlop = 0;
    private int mScrollDuration = 100;
    private float mXScrollingSpeed = 1f;
    private PointF mCurrentOrigin = new PointF(0f, 0f);
    private Direction mCurrentScrollDirection = Direction.NONE;
    private Direction mCurrentFlingDirection = Direction.NONE;
    private ScaleDirection mCurrentScaleDirection = ScaleDirection.NONE;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetectorCompat mGestureDetector;
    private OverScroller mScroller;
    public ScheduleTimeManager mTimeManager;

    private List<TreeNode<ScheduleHeader>> mHeaders;
    private List<? extends ScheduleEvent> mEvents;

    // Reference 형 List. HeaderView, CellView, EventView 목록을 가지고있으며 활용하기 위한 객체.
    private ArrayList<CheckableHeaderView> mHeaderReference;
    private ArrayList<ScheduleCellView> mCellReference;
    private ArrayList<ScheduleEventView> mEventReference;

    // 임시 변수. 정확한 로직 확립 후 변경가능성이 큰 변수.
    private Paint mTimeBackgroundPaint;
    private Paint mTimeTextPaint;
    private Paint mTimeLinePaint;
    private Paint mTimeCurrentLinePaint;
    private int mTimeWidth = 100;

    // Attribute 정의
    private int mCellHeight = 200;
    private int mCellMargin = 1;
    private int mCellMarginLeft = 1;
    private int mCellMarginTop = 1;
    private int mCellMarginRight = 1;
    private int mCellMarginBottom = 1;
    private int mColumnCount = 2; //화면에 보여질 아이템 수
    private int mEventTextSize = 10;
    private int mEventTypeDetailTextSize = 12;
    private int mHeaderBackgroundColor = Color.WHITE;
    private int mHeaderHeight = 50;
    private int mHeaderTextSize = 12;
    private int mTimeDuration = 60;
    private int mTimeEndHour = 18;
    private int mTimeEndMinute = 0;
    private int mTimeStartHour = 9;
    private int mTimeStartMinute = 0;
    private int mTimePadding = 10;
    private int mTimeTextColor = Color.BLACK;
    private int mTimeTextSize = 12;

    private TimeInterpreter mTimeInterpreter;
    private OnCellCheckedChangeListener mOnCellCheckedChangeListener;
    private OnEventCheckedChangeListener mOnEventCheckedChangeListener;
    private OnEventDroppedListener mOnEventDroppedListener;

    public interface OnCellCheckedChangeListener {
        void onCellCheckedChanged(ScheduleViewGroup scheduleViewGroup, ScheduleCellView checkedScheduleCellView , boolean checked);
    }

    public interface OnEventCheckedChangeListener {
        void onEventCheckedChanged(ScheduleViewGroup scheduleViewGroup, ScheduleEventView checkedScheduleEventView , boolean checked);
    }

    public interface OnEventDroppedListener {
        void onEventDropped(ScheduleCellView droppedCellView, ScheduleEventView eventView, DragEvent event);
    }

    public ScheduleViewGroup(Context context) {
        this(context, null);
    }

    public ScheduleViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScheduleViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mContext = context;
        fadeTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

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

        // Get the attribute values (if any).
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScheduleViewGroup, 0, 0);
        try {
            mCellHeight = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellHeight, mCellHeight);
            mCellMargin = mCellMarginLeft = mCellMarginTop = mCellMarginRight = mCellMarginBottom = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellMargin, mCellMargin);
            mCellMarginLeft = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellMarginLeft, mCellMarginLeft);
            mCellMarginTop = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellMarginTop, mCellMarginTop);
            mCellMarginRight = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellMarginRight, mCellMarginRight);
            mCellMarginBottom = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellMarginBottom, mCellMarginBottom);
            mColumnCount = a.getInteger(R.styleable.ScheduleViewGroup_columnCount, mColumnCount);
            mEventTextSize = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_eventTextSize, mEventTextSize);
            mEventTypeDetailTextSize = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_eventTypeDetailTextSize, mEventTypeDetailTextSize);
            mHeaderBackgroundColor = a.getColor(R.styleable.ScheduleViewGroup_headerBackgroundColor, mHeaderBackgroundColor);
            mHeaderHeight = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_headerHeight, mHeaderHeight);
            mHeaderTextSize = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_headerTextSize, mHeaderTextSize);
            mTimeDuration = a.getInteger(R.styleable.ScheduleViewGroup_timeDuration, mTimeDuration);
            mTimeEndHour = a.getInteger(R.styleable.ScheduleViewGroup_timeEndHour, mTimeEndHour);
            mTimeEndMinute = a.getInteger(R.styleable.ScheduleViewGroup_timeEndMinute, mTimeEndMinute);
            mTimeStartHour = a.getInteger(R.styleable.ScheduleViewGroup_timeStartHour, mTimeStartHour);
            mTimeStartMinute = a.getInteger(R.styleable.ScheduleViewGroup_timeStartMinute, mTimeStartMinute);
            mTimePadding = a.getInteger(R.styleable.ScheduleViewGroup_timePadding, mTimePadding);
            mTimeTextColor = a.getColor(R.styleable.ScheduleViewGroup_timeTextColor, mTimeTextColor);
            mTimeTextSize = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_timeTextSize, mTimeTextSize);
        } finally {
            a.recycle();
        }
        initialize();

        // 현재일자 View
        Drawable icon = getResources().getDrawable(R.drawable.ic_today_black_24dp);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        mCurrentDateView = new CurrentDateView(mContext);
        mCurrentDateView.setTextSize(this.mTimeTextSize - 2);
        mCurrentDateView.setElevation(10);
        mCurrentDateView.setBackgroundColor(mHeaderBackgroundColor);
        mCurrentDateView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // CalendarListener 에 onSelectPicker 이벤트를 전달하고
                mDateCalendarListener.onSelectPicker(mCurrentDateView.getCurrentDate());

                // 기기에 SoundEffect Click 효과를 수행한다.
                playSoundEffect(SoundEffectConstants.CLICK);
            }
        });
        this.addView(mCurrentDateView);
    }

    /******************************** getter and setter ****************************************************
     * 각 속성별로 로직 또는 디자인에 영향을 미치게 되는데 옵션별 Test 미실행 상태.
     * 속성별 Test 필요함.
     */
    public int getCellHeight() {
        return mCellHeight;
    }

    public void setCellHeight(int mCellHeight) {
        this.mCellHeight = mCellHeight;
    }

    public int getCellMargin() {
        return mCellMargin;
    }

    public void setCellMargin(int cellMargin) {
        this.mCellMargin = cellMargin;
    }

    public int getCellMarginBottom() {
        return mCellMarginBottom;
    }

    public void setCellMarginBottom(int cellMarginBottom) {
        this.mCellMarginBottom = cellMarginBottom;
    }

    public int getCellMarginLeft() {
        return mCellMarginLeft;
    }

    public void setCellMarginLeft(int cellMarginLeft) {
        this.mCellMarginLeft = cellMarginLeft;
    }

    public int getCellMarginRight() {
        return mCellMarginRight;
    }

    public void setCellMarginRight(int cellMarginRight) {
        this.mCellMarginRight = cellMarginRight;
    }

    public int getCellMarginTop() {
        return mCellMarginTop;
    }

    public void setCellMarginTop(int cellMarginTop) {
        this.mCellMarginTop = cellMarginTop;
    }

    public int getColumnCount() {
        return mColumnCount;
    }

    public void setColumnCount(int columnCount) {
        int headerChildSize = getHeaderSize();
        if (columnCount > headerChildSize)
            columnCount = headerChildSize;

        this.mColumnCount = columnCount;
    }

    public int getEventTextSize() {
        return mEventTextSize;
    }

    public void setEventTextSize(int eventTextSize) {
        this.mEventTextSize = eventTextSize;
    }

    public int getHeaderBackgroundColor() {
        return mHeaderBackgroundColor;
    }

    public void setHeaderBackgroundColor(int headerBackgroundColor) {
        this.mHeaderBackgroundColor = headerBackgroundColor;
    }

    public int getHeaderHeight() {
        return mHeaderHeight;
    }

    public void setHeaderHeight(int headerHeight) {
        this.mHeaderHeight = headerHeight;
    }

    public int getHeaderTextSize() {
        return mHeaderTextSize;
    }

    public void setHeaderTextSize(int headerTextSize) {
        this.mHeaderTextSize = headerTextSize;
    }

    public int getEventTypeDetailTextSize() {
        return mEventTypeDetailTextSize;
    }

    public void setEventTypeDetailTextSize(int eventTypeDetailTextSize) {
        this.mEventTypeDetailTextSize = eventTypeDetailTextSize;
    }

    public int getTimeTextSize() {
        return mTimeTextSize;
    }

    public void setTimeTextSize(int timeTextSize) {
        this.mTimeTextSize = timeTextSize;
        mCurrentDateView.setTextSize(timeTextSize - 2);
        mTimeTextPaint.setTextSize(ScheduleViewUtil.getResizedDensity(getContext(), timeTextSize));
    }

    public int getTimeTextColor() {
        return mTimeTextColor;
    }

    public void setTimeTextColor(int timeTextColor) {
        this.mTimeTextColor = timeTextColor;
    }

    public int getTimeStartMinute() {
        return mTimeStartMinute;
    }

    public void setTimeStartMinute(int timeStartMinute) {
        this.mTimeStartMinute = timeStartMinute;
    }

    public int getTimeStartHour() {
        return mTimeStartHour;
    }

    public void setTimeStartHour(int timeStartHour) {
        this.mTimeStartHour = timeStartHour;
    }

    public int getTimeEndMinute() {
        return mTimeEndMinute;
    }

    public void setTimeEndMinute(int timeEndMinute) {
        this.mTimeEndMinute = timeEndMinute;
    }

    public int getTimeEndHour() {
        return mTimeEndHour;
    }

    public void setTimeEndHour(int timeEndHour) {
        this.mTimeEndHour = timeEndHour;
    }

    public int getTimeDuration() {
        return mTimeDuration;
    }

    public void setTimeDuration(int timeDuration) {
        this.mTimeDuration = timeDuration;
    }

    public int getTimePadding() {
        return mTimePadding;
    }

    public void setTimePadding(int timePadding) {
        this.mTimePadding = timePadding;
    }

    public DateCalendarListener getDateCalendarListener() {
        return mDateCalendarListener;
    }

    public void setDateCalendarListener(DateCalendarListener dateCalendarListener) {
        this.mDateCalendarListener = dateCalendarListener;
    }

    public Calendar getCurrentDate() {
        if (mCurrentDateView != null) {
            return mCurrentDateView.getCurrentDate();
        }
        return null;
    }

    public void setCurrentDate(Calendar calendar) {
        if (mCurrentDateView != null) {
            mCurrentDateView.setCurrentDate(calendar);
        }
    }
    private CurrentDateView mCurrentDateView;
    private DateCalendarListener mDateCalendarListener;

    public interface DateCalendarListener {
        /**
         * DatePicker 가 선택되었을때 Listener 에게 알립니다.
         */
        void onSelectPicker(Calendar calendar);
    }
    /************************************************************************************/

    private void initialize() {
        this.setClickable(true);
        this.setBackgroundColor(Color.WHITE);

        // ScheduleTimeManager 설정
        mTimeManager = new ScheduleTimeManager(mTimeStartHour, mTimeStartMinute, mTimeEndHour, mTimeEndMinute, mTimeDuration);

        // Measure settings for time column.
        mTimeBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeBackgroundPaint.setColor(mHeaderBackgroundColor);
        mTimeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTimeTextPaint.setTextAlign(Paint.Align.RIGHT);
        mTimeTextPaint.setTextSize(ScheduleViewUtil.getResizedDensity(getContext(), this.mTimeTextSize));
        mTimeTextPaint.setColor(mTimeTextColor);
        Rect rect = new Rect();
        mTimeTextPaint.getTextBounds("00 PM", 0, "00 PM".length(), rect);

        // 시간 구분 Paint 설정
        mTimeLinePaint = new Paint();
        mTimeLinePaint.setStyle(Paint.Style.STROKE);
        mTimeLinePaint.setStrokeWidth(1);
        mTimeLinePaint.setColor(Color.DKGRAY);
        mTimeCurrentLinePaint = new Paint();
        mTimeCurrentLinePaint.setStyle(Paint.Style.STROKE);
        mTimeCurrentLinePaint.setStrokeWidth(2);
        mTimeCurrentLinePaint.setColor(Color.RED);

        // TimeWidth 설정
        calculateTimeWidth();

        // ScaledMinimumFlingVelocity, ScaledTouchSlop 초기화.
        mMinimumFlingVelocity = ViewConfiguration.get(mContext).getScaledMinimumFlingVelocity();
        mScaledTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

        // Scroller, GestureDetector, ScaleDetector 초기화.
        mScroller = new OverScroller(mContext, new FastOutLinearInInterpolator());
        mGestureDetector = new GestureDetectorCompat(mContext, mGestureListener);
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
                if (mCurrentScaleDirection == ScaleDirection.VERTICAL) {
                    // 세로 Scale 적용

                    mNewHourHeight = Math.round(mCellHeight * detector.getScaleFactor());

                    // 최소 HourHeight 보다 작으면
                    if (mNewHourHeight <= mMinHourHeight) {
                        mCellHeight = mMinHourHeight;
                        mNewHourHeight = -1;
                    }
                    // 최대 HourHeight 보다 크면
                    if (mNewHourHeight >= mMaxHourHeight) {
                        mCellHeight = mMaxHourHeight;
                        mNewHourHeight = -1;
                    }
                } else if (mCurrentScaleDirection == ScaleDirection.HORIZONTAL) {
                    // 가로 Scale 적용
                    mScaleFactor = detector.getScaleFactor();
                }
                requestLayout();
                return true;
            }
        });
    }

    /**
     * Refreshes the view and loads the events again.
     */
    public void notifyDataSetChanged() {
        mFirstLoad = true;
        refactoringEvents();
        requestLayout();
    }

    /**
     * 왼쪽 Row 의 Time 표시 Text 를 비교하여 가장 긴 문자열에 맞는 timeWidth 값을 계산한다.
     * mTimeWidth = 가장 긴 Time 문자열의 int 형 Width 값
     */
    private void calculateTimeWidth() {
        this.mTimeWidth = 0;
        for (TimeStartEnd timeStartEnd : mTimeManager.getTimeStartEndList()) {
            // Measure time string and get max width.
            String time = getTimeInterpreter().interpretTime(timeStartEnd.getTimeStart());
            if (time == null)
                throw new IllegalStateException("A DateTimeInterpreter must not return null time");
            mTimeWidth = Math.max(mTimeWidth, (int) mTimeTextPaint.measureText(time));
        }
        mTimeWidth = mTimeWidth + mTimePadding * 2;
    }

    public void loadHeader() {
        if (mHeaderLoader != null) {
            if (mHeaders != null)
                mHeaders.clear();
            mFirstLoad = true;
            mHeaders = mHeaderLoader.onLoad();
            removeHeaderViews();
            removeCellViews();
            addCellViews(mHeaders);
            addHeaderViews(mHeaders);
        }
    }

    /**
     * Header Checked 이벤트를 제공하지 않음. 추후 필요시 이벤트핸들러 만들어서 제공해야함. OnEventCheckedChangeListener 참조
     * Checkable 은 가능하나 이벤트 미제공으로 강제로 Checked = false, Checkable = false
     * @param headers
     */
    public void addHeaderViews(List<TreeNode<ScheduleHeader>> headers) {

        if (mHeaderReference != null) {
            mHeaderReference.clear();
            mHeaderReference = null;
        }
        mHeaderReference = new ArrayList<>();

        // CheckableHeaderView 를 생성한다.
        for (TreeNode<ScheduleHeader> scheduleHeader : headers) {
            CheckableHeaderView view = new CheckableHeaderView(mContext);
            view.setHeaderNode(scheduleHeader);
            // Checkable 은 가능하나 이벤트 미제공으로 강제로 Checked = false, Checkable = false
            view.setChecked(false);
            view.setCheckable(false);
            view.setBackgroundColor(mHeaderBackgroundColor);
            view.setContentsTextSize(mHeaderTextSize);
            view.getContents().setText(scheduleHeader.getData().getName());
            this.addView(view);
            view.setMargin(mCellMarginLeft, mCellMarginTop, mCellMarginRight, mCellMarginBottom);
            mHeaderReference.add(view);
            for (TreeNode<ScheduleHeader> child : scheduleHeader.getChildren()) {
                CheckableHeaderView childView = new CheckableHeaderView(mContext);
                childView.setHeaderNode(child);
                // Checkable 은 가능하나 이벤트 미제공으로 강제로 Checked = false, Checkable = false
                childView.setChecked(false);
                childView.setCheckable(false);
                childView.setBackgroundColor(mHeaderBackgroundColor);
                childView.setContentsTextSize(mHeaderTextSize);
                childView.getContents().setText(child.getData().getName());
                this.addView(childView);
                childView.setMargin(mCellMarginLeft, mCellMarginTop, mCellMarginRight, mCellMarginBottom);
                mHeaderReference.add(childView);
            }
        }
    }

    public void addCellViews(List<TreeNode<ScheduleHeader>> headers) {
        if (mCellReference != null) {
            mCellReference.clear();
            mCellReference = null;
        }
        mCellReference = new ArrayList<>();

        // Header 의 Column 구성 수에 맞게 ScheduleCellView 를 Time 정보에 맞게 생성한다.
        for (TreeNode<ScheduleHeader> scheduleHeader : headers) {
            if (scheduleHeader.getChildren().size() > 0) {
                // Child 가 있는경우
                for (TreeNode<ScheduleHeader> childHeader : scheduleHeader.getChildren()) {
                    addScheduleCellViews(childHeader, mTimeManager);
                }
            } else {
                // Child 가 없고, 자기자신만 있는경우
                addScheduleCellViews(scheduleHeader, mTimeManager);
            }
        }
    }

    /**
     * 정보와 일치하는 ScheduleHeader 를 찾아 반환합니다.
     *
     * @param parentHeaderKey 부모 Header Key
     * @param headerKey       Header Key
     * @return ScheduleHeader
     */
    public TreeNode<ScheduleHeader> getHeaderNode(String parentHeaderKey, String headerKey) {
        for (TreeNode<ScheduleHeader> headerTreeNode : mHeaders) {
            if (headerTreeNode.getChildren().size() == 0) {
                // Child 가 없는 경우
                if (TextUtils.equals(headerTreeNode.getData().getKey(), headerKey))
                    return headerTreeNode;
            } else {
                // Child 가 있는 경우
                for (TreeNode<ScheduleHeader> childNode : headerTreeNode.getChildren()) {
                    if (TextUtils.equals(childNode.getParent().getData().getKey(), parentHeaderKey)
                            && TextUtils.equals(childNode.getData().getKey(), headerKey)) {
                        return childNode;
                    }
                }
            }
        }
        return null;
    }

    /**
     * <pre>
     * Header 정보와 timeManager 를 이용해 시작시간 부터 종료시간 까지 TimeDuration 으로 분할하여,
     * ScheduleCellView 를 생성하고 ScheduleViewGroup 에 추가한다.
     *
     * 이 때, ScheduleCellView 는 자신의 속성과 위치에 대한 정보로 Header 정보와 Time 정보를 갖는다.
     * </pre>
     *
     * @param headerTreeNode Header 정보를 갖고있는 객체.
     * @param timeManager    Time 정보(TimeStart, TimeEnd, TimeDuration)를 갖고있는 TimeManager 객체.
     */
    private void addScheduleCellViews(TreeNode<ScheduleHeader> headerTreeNode, ScheduleTimeManager timeManager) {
        ScheduleCellView view;

        for (TimeStartEnd timeStartEnd : timeManager.getTimeStartEndList()) {
            view = new ScheduleCellView(mContext);
            view.setHeaderNode(headerTreeNode);
            view.setTimeStart(timeStartEnd.getTimeStart());
            view.setTimeEnd(timeStartEnd.getTimeEnd());

            view.setElevation(1);
            view.setClickable(true);
            view.setCheckable(true);
            view.setOnCheckedChangeListener(this);
            view.setOnDragListener(new DragListener());
            this.addView(view);
            view.setMargin(mCellMarginLeft, mCellMarginTop, mCellMarginRight, mCellMarginBottom);
            mCellReference.add(view);
        }
    }

    public void removeHeaderViews() {
        if (mHeaderReference != null) {
            for (View headerView : mHeaderReference)
                this.removeView(headerView);

            mHeaderReference.clear();
            mHeaderReference = null;
        }
    }

    public void removeCellViews() {
        if (mCellReference != null) {
            for (View cellView : mCellReference)
                this.removeView(cellView);

            mCellReference.clear();
            mCellReference = null;
        }
    }

    public void loadEvent() {
        if (mEventLoader != null) {
            if (mEvents != null)
                mEvents.clear();
            mEvents = mEventLoader.onLoad(mCurrentDateView.getCurrentDate());
            removeEventViews();
            addEventViews(mEvents);
        }
    }

    /**
     * CurrentDate 를 설정하고, calendar 에 해당되는 Event 를 Load 한다.
     * @param calendar loadEvent 에 사용되는 calendar
     */
    public void loadEvent(Calendar calendar) {
        mCurrentDateView.setCurrentDate(calendar);
        loadEvent();
    }

    public void addEventViews(List<? extends ScheduleEvent> events) {
        if (mEventReference != null) {
            mEventReference.clear();
            mEventReference = null;
        }
        mEventReference = new ArrayList<>();

        int index = 0;
        for (ScheduleEvent scheduleEvent : events) {
            ScheduleEventView view = new ScheduleEventView(mContext);

            // Set Margin
            view.setTag("scheduleCardView" + String.valueOf(index));
            view.setEvent(scheduleEvent);
            view.setOnLongClickListener(this);
            view.setOnCheckedChangeListener(this);
            view.setElevation(1);
            view.setEventTextSize(mEventTextSize);
            view.setEventTypeDetailTextSize(mEventTypeDetailTextSize);
            this.addView(view);
            view.setMargin(mCellMarginLeft, mCellMarginTop, mCellMarginRight, mCellMarginBottom);
            mEventReference.add(view);
            index = index + 1;
        }

        for (CheckableHeaderView headerView : mHeaderReference) {
            headerView.setElevation(5);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                bringChildToFront(headerView);
            }
        }

        // 현재일자 View
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Lollipop 이전 버전에서는 bringChildToFront 로 Z순서 변경
            bringChildToFront(mCurrentDateView);
        }

        // 같은 Header 내에 Cell(시간)이 겹치는 Event 에 대하여 Bound 설정(Cell Width 를 분할하여 최대폭 설정)
        refactoringEvents();
    }

    /**
     * 같은 Header 내에 Cell(시간)이 겹치는 Event 에 대하여 Bound 설정(Cell Width 를 분할하여 최대폭 설정)
     */
    private void refactoringEvents() {
        List<ScheduleEventView> tempEvents = mEventReference;
        mEventReference = new ArrayList<>();
        while (tempEvents.size() > 0) {
            ArrayList<ScheduleEventView> eventReferences = new ArrayList<>(tempEvents.size());

            // 첫 이벤트를 표본으로 remove 하여 가져온다.
            ScheduleEventView eventView = tempEvents.remove(0);
            eventReferences.add(eventView);

            // 표본과 같은 Header 만 tempEvents 에서 찾아 List 에 담는다.
            int i = 0;
            while (i < tempEvents.size()) {
                ScheduleEventView eventViewCompare = tempEvents.get(i);
                if (ScheduleEventView.isEventSameHeader(eventView.getHeaderNode(), eventViewCompare.getHeaderNode())) {
                    tempEvents.remove(i);
                    eventReferences.add(eventViewCompare);
                } else {
                    i++;
                }
            }

            // 같은 Header 의 EventView 들의 폭을 계산하여 Bound 재설정
            computePositionOfEvents(eventReferences);
        }
    }

    /**
     * 각 이벤트의 좌우 위치를 분할하여 Bound 속성을 재설정한다. 이벤트가 표시되는 Cell 이 겹치는 경우에 대하여.
     *
     * @param eventReferences 같은 Header 에 속한 이벤트 목록.
     */
    private void computePositionOfEvents(List<ScheduleEventView> eventReferences) {
        // eventReference 의 event 들을 비교하여 표시되는 Cell 이 겹치는 경우 그룹 List 로 만든다.
        List<List<ScheduleEventView>> collisionGroups = new ArrayList<>();
        for (ScheduleEventView eventView : eventReferences) {
            boolean isPlaced = false;

            outerLoop:
            for (List<ScheduleEventView> collisionGroup : collisionGroups) {
                for (ScheduleEventView groupEvent : collisionGroup) {
                    if (isEventsCollide(groupEvent.getTimeStart(), groupEvent.getTimeEnd(), eventView.getTimeStart(), eventView.getTimeEnd())) {
                        collisionGroup.add(eventView);
                        isPlaced = true;
                        break outerLoop;
                    }
                }
            }

            if (!isPlaced) {
                List<ScheduleEventView> newGroup = new ArrayList<>();
                newGroup.add(eventView);
                collisionGroups.add(newGroup);
            }
        }

        // 표시되는 Cell 이 충돌되는 Event 들에 대해서 좌우 폭(Bound 속성)을 재설정 한다.
        for (List<ScheduleEventView> collisionGroup : collisionGroups) {
            expandEventsToMaxWidth(collisionGroup);
        }
    }

    /**
     * 가능한 최대 폭 모든 이벤트를 확장합니다. 이벤트는 수평으로 최대 사용 가능한 공간을 차지하려고합니다.
     *
     * @param collisionGroup 서로 중첩되는 event 그룹.
     */
    private void expandEventsToMaxWidth(List<ScheduleEventView> collisionGroup) {
        List<List<ScheduleEventView>> columns = new ArrayList<>();
        columns.add(new ArrayList<ScheduleEventView>());
        for (ScheduleEventView eventView : collisionGroup) {
            boolean isPlaced = false;
            for (List<ScheduleEventView> column : columns) {
                if (column.size() == 0) {
                    column.add(eventView);
                    isPlaced = true;
                } else if (!isEventsCollide(eventView.getTimeStart(), eventView.getTimeEnd(), column.get(column.size() - 1).getTimeStart(), column.get(column.size() - 1).getTimeEnd())) {
                    column.add(eventView);
                    isPlaced = true;
                    break;
                }
            }

            if (!isPlaced) {
                List<ScheduleEventView> newColumn = new ArrayList<>();
                newColumn.add(eventView);
                columns.add(newColumn);
            }
        }

        // 좌우 Width 분할 계산
        int maxRowCount = 0;
        for (List<ScheduleEventView> column : columns) {
            maxRowCount = Math.max(maxRowCount, column.size());
        }
        for (int i = 0; i < maxRowCount; i++) {
            // Set the left and right values of the event.
            float j = 0;
            for (List<ScheduleEventView> column : columns) {
                if (column.size() >= i + 1) {
                    ScheduleEventView eventView = column.get(i);
                    eventView.width = 1f / columns.size();
                    eventView.left = j / columns.size();
                    eventView.top = eventView.getTimeStart().get(Calendar.HOUR_OF_DAY) * 60 + eventView.getTimeStart().get(Calendar.MINUTE);
                    eventView.bottom = eventView.getTimeEnd().get(Calendar.HOUR_OF_DAY) * 60 + eventView.getTimeEnd().get(Calendar.MINUTE);

                    mEventReference.add(eventView);
                }
                j++;
            }
        }
    }

    /**
     * event1 과 event2 의 overlap 여부를 반환한다.
     *
     * @param timeStartEvent1 첫번째 event start time.
     * @param timeEndEvent1   첫번째 event end time.
     * @param timeStartEvent2 두번째 event start time.
     * @param timeEndEvent2   두번째 event end time.
     * @return 두 event 가 overlap 된다면 true 반환.
     */
    private static boolean isEventsCollide(Calendar timeStartEvent1, Calendar timeEndEvent1, Calendar timeStartEvent2, Calendar timeEndEvent2) {
        long start1 = timeStartEvent1.getTimeInMillis();
        long end1 = timeEndEvent1.getTimeInMillis();
        long start2 = timeStartEvent2.getTimeInMillis();
        long end2 = timeEndEvent2.getTimeInMillis();
        return !((start1 >= end2) || (end1 <= start2));
    }

    public void removeEventViews() {
        if (mEventReference != null) {
            for (View eventView : mEventReference)
                this.removeView(eventView);

            mEventReference.clear();
            mEventReference = null;
        }
    }

    public void setEventColor(ScheduleEvent event, @ColorInt int color) {
        if (mEventReference != null) {
            for (ScheduleEventView eventView : mEventReference) {
                if (eventView.getEvent().equals(event))
                    event.setBackgroundColor(color);
            }
        }
        invalidate();
    }

    @Override
    public void onCheckedChanged(ScheduleCellView checkableView, boolean checked) {
        if (checked) {
            unCheckedListQueue.offer(checkableView);
            executeUnCheckListQueue(checkableView);
        }
        if (mOnCellCheckedChangeListener != null)
            mOnCellCheckedChangeListener.onCellCheckedChanged(this, checkableView, checked);

        checkableView.setElevation(checked ? 4 : 1);
    }

    @Override
    public void onCheckedChanged(ScheduleEventView checkableView, boolean checked) {
        if (checked) {
            unCheckedListQueue.offer(checkableView);
            executeUnCheckListQueue(checkableView);
        } else {
            if (toggleListQueue.size() > 0) {
                toggleListQueue.poll().setChecked(true);
            }
        }
        if (mOnEventCheckedChangeListener != null)
            mOnEventCheckedChangeListener.onEventCheckedChanged(this, checkableView, checked);

        checkableView.setElevation(checked ? 4 : 1);
    }

    @Override
    public boolean onLongClick(View v) {
        if (v instanceof ScheduleEventView) {
            return true;
        }
        return false;
    }

    class DragListener implements OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final View view = (View) event.getLocalState();

            // 이벤트 시작
            switch (event.getAction()) {
                // View Drag Started
                case DragEvent.ACTION_DRAG_STARTED:
                    view.setVisibility(View.INVISIBLE);
                    break;
                // View Drag Entered
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setPressed(true);
                    break;
                // View Drag Exited
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setPressed(false);
                    break;
                // View Drag Drop
                case DragEvent.ACTION_DROP:
                    v.setPressed(false);
                    view.setVisibility(View.VISIBLE);

                    ScheduleCellView scheduleCellView = (ScheduleCellView) v;
                    ScheduleEventView scheduleEventView = (ScheduleEventView) view;

                    // Dropped 정보 Event 에 Update
                    scheduleEventView.setHeaderNode(scheduleCellView.getHeaderNode());
                    scheduleEventView.setTimeStart(scheduleCellView.getTimeStart());
                    long endTimeMillis = scheduleCellView.getTimeStart().getTimeInMillis() + scheduleEventView.getEvent().getDurationTime() * 60 * 1000;
                    Calendar endTimeCalendar = Calendar.getInstance();
                    endTimeCalendar.setTimeInMillis(endTimeMillis);
                    scheduleEventView.setTimeEnd(endTimeCalendar);
                    notifyDataSetChanged();

                    if (mOnEventDroppedListener != null)
                        mOnEventDroppedListener.onEventDropped(scheduleCellView, scheduleEventView, event);
                    break;
                // View Drag Ended
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setPressed(false);
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setVisibility(VISIBLE);
                        }
                    });
                    break;
            }
            return true;
        }
    }

    private float mScaleFactor = 1;
    private boolean mFirstLoad = true;
    private int mDesiredWidth;

    /*
     * 넘어오는 파라메터는 부모뷰로부터 결정된 치수제한을 의미한다.
     * 또한 파라메터에는 bit 연산자를 사용해서 모드와 크기를 같이 담고있다.
     * 모드는 MeasureSpec.getMode(spec) 형태로 얻어오며 다음과 같은 3종류가 있다.
     *         MeasureSpec.AT_MOST : wrap_content (뷰 내부의 크기에 따라 크기가 달라짐)
     *         MeasureSpec.EXACTLY : fill_parent, match_parent (외부에서 이미 크기가 지정되었음)
     *         MeasureSpec.UNSPECIFIED : MODE 가 셋팅되지 않은 크기가 넘어올때 (대부분 이 경우는 없다)
     *
     *   fill_parent, match_parent 를 사용하면 윗단에서 이미 크기가 계산되어 EXACTLY 로 넘어온다.
     *   이러한 크기는 MeasureSpec.getSize(spec) 으로 얻어낼 수 있다.
     *
     *   이 메소드에서는 setMeasuredDimension(measuredWidth,measuredHeight) 를 호출해 주어야 하는데
     *   super.onMeasure() 에서는 기본으로 이를 기본으로 계산하는 함수를 포함하고 있다.
     *
     *   만약 xml 에서 크기를 wrap_content 로 설정했다면 이 함수에서 크기를 계산해서 셋팅해 줘야한다.
     *   그렇지 않으면 무조껀 fill_parent 로 나오게 된다.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int specSizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        final int desiredWidth = mDesiredWidth = specSizeWidth - getPaddingLeft() - getPaddingRight() - mTimeWidth;
        final int desiredHeight = specSizeHeight - getPaddingTop() - getPaddingBottom();

        final int timeRowCount = mTimeManager.getTimeCount();

        // Layout 의 Width 값이 정해지는 시점에서 mColumnCount 를 이용해 계산하여 mColumnWidth 를 설정해야 하기 때문에 onMeasure 에서 계산한다.
        // mFirstLoad 일 경우만 실행하는 이유는, Layout 의 Scale 기능으로 인해 mColumnWidth 값은 계속적으로 변경되기 때문이다.
        if (mFirstLoad) {
            // ColumnSize 가 Header 의 size 보다 크면 Header 의 Size 로 대체
            int headerChildCount = getHeaderSize();
            if (mColumnCount > headerChildCount)
                mColumnCount = headerChildCount;

            mColumnWidth = desiredWidth / mColumnCount;
            mFirstLoad = false;
        }

        // ScaleFactor 적용 (zoom 비율 적용)
        if (mScaleFactor != 1) {
            // CurrentOriginX 계산
            if ((int) (mColumnWidth * mScaleFactor) < mDesiredWidth)
                mCurrentOrigin.x = ((mCurrentOrigin.x - mMidPointX) / mColumnWidth) * mColumnWidth * mScaleFactor + mMidPointX;

            // Scaled ColumnWidth 계산
            mColumnWidth = (int) (mColumnWidth * mScaleFactor);

            if (mColumnWidth * getHeaderSize() < desiredWidth)
                mColumnWidth = desiredWidth / getHeaderSize();

            if (mColumnWidth > desiredWidth)
                mColumnWidth = desiredWidth;

            mScaleFactor = 1;
        }

        /* MinHourHeight 동적 정의. EventDrawRect 범위의 Height 값과 TimeStart, TimeEnd, TimeDuration 값에 따른 Row 수에 영향을 받아
           동적으로 MinHeight 를 계산한다. */
        mMinHourHeight = (desiredHeight - getDrawEventsTop() - (mCellMarginTop + mCellMarginBottom) * timeRowCount) / timeRowCount ;

        // mCellHeight 설정(zoom 으로 변경된 Scale 적용)
        if (mNewHourHeight > mMinHourHeight && mNewHourHeight < mMaxHourHeight) {
            mCurrentOrigin.y = ((mCurrentOrigin.y - mMidPointY) / mCellHeight) * mNewHourHeight + mMidPointY;
            mCellHeight = mNewHourHeight;
            mNewHourHeight = -1;
        }

        // mCurrentOrigin.y 값이 View 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (mCurrentOrigin.y < getHeight() - getDrawEventsTop() - (mCellMarginTop + mCellHeight + mCellMarginBottom) * mTimeManager.getTimeCount())
            mCurrentOrigin.y = getHeight() - getDrawEventsTop() - (mCellMarginTop + mCellHeight + mCellMarginBottom) * mTimeManager.getTimeCount();

        // mCurrentOrigin.y 값이 View 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (mCurrentOrigin.y > 0) {
            mCurrentOrigin.y = 0;
        }

        // Scroll 된 offset 설정
        int shiftColumnCount = (int) -(Math.ceil(mCurrentOrigin.x / mColumnWidth));

        // mCurrentOrigin.x 값이 HeaderViewList 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (shiftColumnCount < 0) {
            mCurrentOrigin.x = 0;
            mScroller.forceFinished(true);
        }

        // mCurrentOrigin.x 값이 HeaderViewList 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (mHeaders != null && (-mCurrentOrigin.x) + desiredWidth > getHeaderSize() * mColumnWidth && getHeaderSize() >= 0) {
            mCurrentOrigin.x = desiredWidth - (getHeaderSize() * mColumnWidth);
            mScroller.forceFinished(true);
        }

        setMeasuredDimension(specSizeWidth, specSizeHeight);

        // Measure 정의에 사용될 로컬변수
        int headerSizeWidth;
        int childWidthMeasureSpec, childHeightMeasureSpec;

        // Header Today 버튼 measure
        if (mCurrentDateView != null) {
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mTimeWidth, MeasureSpec.EXACTLY);
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getDrawEventsTop() - getPaddingTop(), MeasureSpec.EXACTLY);
            mCurrentDateView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

        // HeaderView measure
        if (mHeaderReference != null) {
            for (CheckableHeaderView headerView : mHeaderReference) {
                final TreeNode<ScheduleHeader> headerNode = headerView.getHeaderNode();

                int headerChildCount = 1;
                headerSizeWidth = (mColumnWidth - (mCellMarginLeft + mCellMarginRight)) * headerChildCount;

                // Parent Header
                if (headerNode.getChildren().size() > 0) {
                    headerChildCount = headerNode.getChildren().size();
                    // Parent 의 경우 Child 수 만큼 Merge 되기 때문에 Child 들의 사이 Margin (left, right) 값이 추가로 Width 로 들어간다.
                    headerSizeWidth = mColumnWidth * headerChildCount - (mCellMarginLeft + mCellMarginRight);
                }

                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(headerSizeWidth, MeasureSpec.EXACTLY);
                if (headerChildCount == 1 && headerNode.getParent() == null)  // Parent 와 Child 가 없는경우
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeaderHeight * 2, MeasureSpec.EXACTLY); // TODO : mHeaderHeight * 2 를 했는데 일단 TreeNode 를 2단계로 한정지었기 때문.
                else // Child 가 있는경우
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeaderHeight, MeasureSpec.EXACTLY);

                headerView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }

        // CellView measure
        if (mCellReference != null) {
            for (ScheduleCellView cellView : mCellReference) {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mColumnWidth - (mCellMarginLeft + mCellMarginRight), MeasureSpec.EXACTLY);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mCellHeight, MeasureSpec.EXACTLY);
                cellView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }

        // EventView measure
        if (mEventReference != null) {
            for (ScheduleEventView eventView : mEventReference) {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mColumnWidth, MeasureSpec.EXACTLY);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec((mCellHeight + mCellMarginTop + mCellMarginBottom) * (eventView.getEvent().getDurationTime() / mTimeDuration), MeasureSpec.EXACTLY);
                eventView.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }


    /**
     * 1. ScheduleTimeManager 를 활용하여 Header 의 각 Column View 별로 TimeDuration 에 맞게 ScheduleCellView 를 Layout 시킨다.
     * 2. 각 Cell 에 해당되는 ScheduleCellView 는 Schedule 정보를 갖는다. Time 정보와 Header 정보를 포함한다.
     * 3. ScheduleCellView 는 원래는 CardView 를 상속하여 구성하였으나, Test 중 롤리팝 이전버전의 장비에서 margin 이 다르게 디자인되어 보이므로
     * ViewGroup 혹은 CheckableLinearLayout 을 활상속받아 정의하도록 한다. ScheduleCellView 의 extends 관계 수정 필요.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int parentLeft = getPaddingLeft() + (int) mCurrentOrigin.x;
        final int parentWidth = right - left - parentLeft - getPaddingRight();
        final int parentTop = getPaddingTop();
        final int parentHeight = bottom - top - parentTop - getPaddingBottom();

        final int headerParentLeft = parentLeft + mCellMarginLeft;
        final int headerParentTop = parentTop + mCellMarginTop;
        final int headerChildTop = headerParentTop + (mHeaderHeight + mCellMarginBottom) + mCellMarginTop;
        int headerParentLeftSum = headerParentLeft;
        int headerChildLeftSum = headerParentLeft;

        final int eventTop = headerChildTop + mCellMarginTop + mHeaderHeight + mCellMarginBottom;

        // Header Today 버튼 layout
        if (mCurrentDateView != null)
            mCurrentDateView.layout(getPaddingLeft(), getPaddingTop(), mTimeWidth - 1, getDrawEventsTop() - getPaddingTop());

        // HeaderView layout
        if (mHeaderReference != null) {
            for (CheckableHeaderView headerView : mHeaderReference) {
                TreeNode<ScheduleHeader> header = headerView.getHeaderNode();

                if (header.getParent() == null) {
                    // Parent
                    headerView.layout(headerParentLeftSum + mTimeWidth,
                            headerParentTop,
                            headerParentLeftSum + headerView.getMeasuredWidth()  + mTimeWidth,
                            headerParentTop + headerView.getMeasuredHeight());
                    headerParentLeftSum += headerView.getMeasuredWidth() + mCellMarginRight + mCellMarginLeft;
                } else {
                    // Child
                    headerView.layout(headerChildLeftSum + mTimeWidth,
                            headerChildTop,
                            headerChildLeftSum + headerView.getMeasuredWidth() + mTimeWidth,
                            headerChildTop + headerView.getMeasuredHeight());
                    headerChildLeftSum += headerView.getMeasuredWidth() + mCellMarginRight + mCellMarginLeft;
                }
            }
        }

        // CellView layout
        if (mCellReference != null) {
            for (ScheduleCellView cellView : mCellReference) {
                TreeNode<ScheduleHeader> headerNode = cellView.getHeaderNode();
                CheckableHeaderView headerView = findHeaderView(headerNode);
                int index = mTimeManager.getTimeStartIndex(cellView.getTimeStart());

                cellView.layout(headerView.getBounds().left,
                        eventTop + ((mCellMarginTop + mCellHeight + mCellMarginBottom) * index) + (int) mCurrentOrigin.y,
                        headerView.getBounds().right,
                        eventTop + ((mCellMarginTop + mCellHeight + mCellMarginBottom) * index) + (int) mCurrentOrigin.y + cellView.getMeasuredHeight());
            }
        }

        // EventView layout
        int timeStartMinute = mTimeManager.getTimeStartMinute();
        float topMinute, bottomMinute;
        if (mEventReference != null) {
            for (ScheduleEventView eventView : mEventReference) {
                TreeNode<ScheduleHeader> headerNode = eventView.getHeaderNode();
                CheckableHeaderView headerView = findHeaderView(headerNode);

                topMinute = eventView.top - timeStartMinute;
                bottomMinute = eventView.bottom - timeStartMinute;

                float childTop = mCellHeight * topMinute / mTimeDuration + mCurrentOrigin.y + eventTop;
                childTop += topMinute / mTimeDuration * (mCellMarginTop + mCellMarginBottom);
                float childBottom = mCellHeight * bottomMinute / mTimeDuration + mCurrentOrigin.y + eventTop;
                childBottom += bottomMinute / mTimeDuration * (mCellMarginTop + mCellMarginBottom) - (mCellMarginTop + mCellMarginBottom);
                float childLeft = headerView.getBounds().left + eventView.left * headerView.getBounds().width() + mCellMarginLeft;
                float childRight = childLeft + eventView.width * headerView.getBounds().width() - (mCellMarginLeft + mCellMarginRight);

                eventView.layout((int) childLeft, (int) childTop, (int) childRight, (int) childBottom);
            }
        }
    }


    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // 시간 분할 그리기
        canvas.clipRect(getPaddingLeft(), getDrawEventsTop(), mTimeWidth, getHeight(), Region.Op.REPLACE);
        int height = getDrawEventsTop() + (mCellMarginTop + mCellHeight + mCellMarginBottom) * mTimeManager.getTimeCount();
        canvas.drawRect(new RectF(getPaddingLeft(), getPaddingTop(), mTimeWidth, height), mTimeBackgroundPaint);
        float top = getDrawEventsTop() + mCurrentOrigin.y;
        for (TimeStartEnd timeStartEnd : mTimeManager.getTimeStartEndList()) {
            String time = getTimeInterpreter().interpretTime(timeStartEnd.getTimeStart());
            canvas.drawText(time, mTimeWidth - mTimePadding, top + 20, mTimeTextPaint);
            canvas.drawLine(getPaddingLeft() + 4, top - 1, mTimeWidth - 4, top - 1, mTimeLinePaint);
            top += (mCellMarginTop + mCellHeight + mCellMarginBottom);
        }

        // 현재시각 그리기
        top = getDrawEventsTop() + mCurrentOrigin.y;
        float beforeNow = (mTimeManager.getTimeMinute(Calendar.getInstance()) - mTimeManager.getTimeStartMinute()) * mCellHeight / 60;
        canvas.drawLine(getPaddingLeft() + 4, top + beforeNow, mTimeWidth - 4, top + beforeNow, mTimeCurrentLinePaint);
    }

    private CheckableHeaderView findHeaderView(TreeNode<ScheduleHeader> headerNode) {
        if (mHeaderReference != null) {
            for (CheckableHeaderView headerView : mHeaderReference) {
                if (headerView.getHeaderNode().equals(headerNode))
                    return headerView;
            }
        }
        return null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("onSizeChanged", "-onSizeChanged");
    }

    private int getHeaderSize() {
        if (mHeaders == null)
            return 0;

        int size = 0;
        for (TreeNode<ScheduleHeader> header : mHeaders) {
            if (header.getChildren().size() > 0)
                size += header.getChildren().size();
            else
                size += 1;
        }
        return size;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        onTouchEvent(event);
        return false;
    }

    // 이동거리 저장을 위한 변수 
    private float mDistanceX = 0;
    private float mDistanceY = 0;
    private float mMidPointX = 0;
    private float mMidPointY = 0;

    private int mMinHourHeight = 70;
    private int mMaxHourHeight = 500;
    private float mMovedSlop = 15;
    private float mTouchDownX, mTouchDownY;
    private boolean isScheduleTouched;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean onTouched;

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mTouchDownX = event.getX();
                mTouchDownY = event.getY();
                isScheduleTouched = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() > 1) {
                    final float currentDistanceX = Math.abs(event.getX() - event.getX(event.findPointerIndex(1)));
                    final float currentDistanceY = Math.abs(event.getY() - event.getY(event.findPointerIndex(1)));

                    // X축과 Y축의 각 두 Pointer 의 이동 거리를 계산하여 더 많이 이동한 거리에 맞도록 ScaleDirection 설정.
                    if (Math.abs(currentDistanceX - mDistanceX) > Math.abs(currentDistanceY - mDistanceY)) {
                        mCurrentScaleDirection = ScaleDirection.HORIZONTAL;
                    } else {
                        mCurrentScaleDirection = ScaleDirection.VERTICAL;
                    }

                    // 다음 Move 시 사용하기위한 이전 DistanceX,Y 값 설정.
                    mDistanceX = currentDistanceX;
                    mDistanceY = currentDistanceY;
                }
                if (Math.abs(mTouchDownX - event.getX()) > mMovedSlop || Math.abs(mTouchDownY - event.getY()) > mMovedSlop)
                    isScheduleTouched = false;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mMidPointX = (event.getX() + event.getX(event.findPointerIndex(1))) / 2;
                mMidPointY = (event.getY() + event.getY(event.findPointerIndex(1)) - getDrawEventsTop() * 2) / 2;
                break;

            case MotionEvent.ACTION_UP:
                if (isScheduleTouched) {
                    setScheduleClick(event);
                }
                break;
        }

        mScaleDetector.onTouchEvent(event);
        onTouched = mGestureDetector.onTouchEvent(event);

        // mGestureDetector 체크가 끝난 후, mCurrentFlingDirection 과 mCurrentScrollDirection 의 상태를 설정한다.
        if (event.getAction() == MotionEvent.ACTION_UP && !mIsZooming && mCurrentFlingDirection == Direction.NONE) {
            if (mCurrentScrollDirection == Direction.RIGHT || mCurrentScrollDirection == Direction.LEFT) {
                goToNearestOrigin();
            }
            mCurrentScrollDirection = Direction.NONE;
        }

        return onTouched;
    }

    private void goToNearestOrigin() {
//        double leftDays = mCurrentOrigin.x / (mWidthPerDay + mColumnGap);
        double shiftColumnCount = mCurrentOrigin.x / mColumnWidth;

        switch (mCurrentFlingDirection) {
            case NONE:
                // snap to nearest day
                shiftColumnCount = Math.round(shiftColumnCount);
                break;
            case LEFT:
                // snap to last day
                shiftColumnCount = Math.floor(shiftColumnCount);
                break;
            case RIGHT:
                // snap to next day
                shiftColumnCount = Math.ceil(shiftColumnCount);
                break;
            default:
                // snap to nearest day
                shiftColumnCount = Math.round(shiftColumnCount);
                break;
        }
        int nearestColumnX = (int) (mCurrentOrigin.x - (shiftColumnCount * mColumnWidth));
        // nearestColumnX 좌표에 이동할 때, 좌표가 오른쪽 끝 좌표이면 이동하지않는다. nearestColumnX = 0
        if ((int) (Math.abs(mCurrentOrigin.x) + mDesiredWidth) == getHeaderSize() * mColumnWidth) {
            nearestColumnX = 0;
        }

        if (nearestColumnX != 0) {
            // Stop current animation.
            mScroller.forceFinished(true);
            // Snap to date.
            mScroller.startScroll((int) mCurrentOrigin.x, (int) mCurrentOrigin.y, -nearestColumnX, 0, mScrollDuration);
            ViewCompat.postInvalidateOnAnimation(ScheduleViewGroup.this);
        }
        // Reset scrolling and fling direction.
        mCurrentScrollDirection = mCurrentFlingDirection = Direction.NONE;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

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
            requestLayout();
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
                case NONE:
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
                case LEFT:
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX < -mScaledTouchSlop)) {
                        mCurrentScrollDirection = Direction.RIGHT;
                    }
                    break;
                case RIGHT:
                    // Change direction if there was enough change.
                    if (Math.abs(distanceX) > Math.abs(distanceY) && (distanceX > mScaledTouchSlop)) {
                        mCurrentScrollDirection = Direction.LEFT;
                    }
                    break;
            }

            // Calculate the new origin after scroll.
            switch (mCurrentScrollDirection) {
                case LEFT:
                case RIGHT:
                    mCurrentOrigin.x -= distanceX * mXScrollingSpeed;
                    ViewCompat.postInvalidateOnAnimation(ScheduleViewGroup.this);
                    requestLayout();
                    break;
                case VERTICAL:
                    mCurrentOrigin.y -= distanceY;
                    ViewCompat.postInvalidateOnAnimation(ScheduleViewGroup.this);
                    requestLayout();
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
                            getHeight() - getDrawEventsTop() - (mCellMarginTop + mCellHeight + mCellMarginBottom) * mTimeManager.getTimeCount(), 0);
                    break;
                case VERTICAL:
                    mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y,
                            0, (int) velocityY,
                            Integer.MIN_VALUE, Integer.MAX_VALUE,
                            getHeight() - getDrawEventsTop() - (mCellMarginTop + mCellHeight + mCellMarginBottom) * mTimeManager.getTimeCount(), 0);
                    break;
            }

            ViewCompat.postInvalidateOnAnimation(ScheduleViewGroup.this);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mEventReference != null) {
                for (ScheduleEventView eventView : mEventReference) {
                    if (eventView.getBounds().contains((int) e.getX(), (int) e.getY())) {
                        // 태그 생성
                        ClipData.Item item = new ClipData.Item((CharSequence) eventView.getTag());

                        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                        ClipData data = new ClipData(eventView.getTag().toString(), mimeTypes, item);
                        DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(eventView);
                        eventView.startDrag(data, shadowBuilder, eventView, 0);
                        super.onLongPress(e);
                        break;
                    }
                }
            }
        }
    };

    // unCheckedListQueue 와 toggleListQueue 를 이용해서 Only one check cell or event 관계와 동일 event 재선택 시 backCell 선택 로직을 구현하도록 한다.
    // 두개의 Queue 와 setScheduleClick, OnCellChanged, OnEventChanged 이벤트를 통하여 구현.
    // 분석이 필요할 때는 디버그 걸고 로직분석 해야할듯 하다. 더 좋은 방법이 있다면 적용이 필요할거 같다.
    Queue<Checkable> unCheckedListQueue = new LinkedList<>();
    Queue<Checkable> toggleListQueue = new LinkedList<>();
    Checkable checkView = null;
    private void setScheduleClick(MotionEvent e) {

        if (checkView != null) {
            if (checkView.isChecked()) {
                if (checkView instanceof ScheduleEventView) { // EventView 가 Checked 면
                    if (mEventReference != null) {
                        // EventView 를 돌면서
                        for (ScheduleEventView view : mEventReference) {
                            // 현재 TouchUp Point 에 해당되는 EventView 와 CheckView 가 같은 EventView 라면
                            if (view.equals(checkView) && view.getBounds().contains((int) e.getX(), (int) e.getY())) {
                                // CellView
                                if (mCellReference != null) {
                                    for (ScheduleCellView cellView : mCellReference) {
                                        // 해당 Point 의 CellView 를 선택
                                        if (cellView.getBounds().contains((int) e.getX(), (int) e.getY())) {
                                            checkView = cellView;
                                            // 현재 setScheduleClick 의 경우 직접적인 Device 의 touch 로 인한 Check 변경이 해당 로직보다 나중에 타게된다.
                                            // 그래서 이부분에서 cellView.toggle() 을 통해 선택을 하면 나중에 Device 에서 Touch 된 EventView 의 Check 로직으로 인해
                                            // 선택되야 할 cellView 가 다시 선택 해제되므로 toggleListQueue 에 넣어
                                            // Device 에서 Touch 되어 check false 되는 EventView 의 조건에 해당 Queue 데이터를 체크하여 Checked 되도록 후처리 하였다.
                                            // 더 좋은 구현을 찾지못해 현재 시간적인 이유로 위와 같이 처리하였다.
                                            toggleListQueue.offer(cellView);
                                            return;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else if (checkView instanceof ScheduleCellView) { // CellView 가 Checked 이면
                    // CellView
                    if (mCellReference != null) {
                        // CellView 를 돌면서
                        for (ScheduleCellView view : mCellReference) {
                            // 현재 TouchUp Point 에 해당되는 CellView 와 CheckView 가 같은 CellView 라면
                            if (view.equals(checkView) && view.getBounds().contains((int) e.getX(), (int) e.getY())) {
                                return;
                            }
                        }
                    }
                }
            }
            checkView = null;
        }

        // HeaderView
        if (mHeaderReference != null) {
            for (CheckableHeaderView view : mHeaderReference) {
                if (view.getBounds().contains((int) e.getX(), (int) e.getY())) {
                    checkView = view;
                    return;
                }
            }
        }

        // EventView
        if (mEventReference != null) {
            for (ScheduleEventView view : mEventReference) {
                if (view.getBounds().contains((int) e.getX(), (int) e.getY())) {
                    checkView = view;
                    return;
                }
            }
        }

        // CellView
        if (mCellReference != null) {
            for (ScheduleCellView view : mCellReference) {
                if (view.getBounds().contains((int) e.getX(), (int) e.getY())) {
                    checkView = view;
                    return;
                }
            }
        }
    }

    private void executeUnCheckListQueue(Checkable cellView) {
        Checkable unCheckCellView;
        while (unCheckedListQueue.size() > 0) {
            unCheckCellView = unCheckedListQueue.poll();
            if (unCheckCellView != null) {
                if (unCheckCellView.equals(cellView)) {
                    unCheckedListQueue.offer(cellView);
                    return;
                } else {
                    unCheckCellView.setChecked(false);
                }
            }
        }
    }

    private int getDrawEventsTop() {
        return getPaddingTop() + (mCellMarginTop + mHeaderHeight + mCellMarginBottom) * 2;
    }

    // Loader getter & setter (header, event)
    public HeaderLoader getHeaderLoader() {
        return mHeaderLoader;
    }

    public void setHeaderLoader(HeaderLoader headerLoader) {
        this.mHeaderLoader = headerLoader;
    }

    public EventLoader getEventLoader() {
        return mEventLoader;
    }

    public void setEventLoader(EventLoader eventLoader) {
        this.mEventLoader = eventLoader;
    }

    @Nullable
    public HeaderLoader.HeaderLoadListener getHeaderLoadListener() {
        if (mHeaderLoader != null)
            return mHeaderLoader.getHeaderLoadListener();
        else
            return null;
    }

    public TimeInterpreter getTimeInterpreter() {
        if (mTimeInterpreter == null) {
            mTimeInterpreter = new TimeInterpreter() {
                @Override
                public String interpretYearMonthDay(Calendar calendar) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + System.getProperty("line.separator") + "MM/dd", Locale.getDefault());
                        return sdf.format(calendar.getTime());
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
        return mTimeInterpreter;
    }

    public void setTimeInterpreter(TimeInterpreter timeInterpreter) {
        this.mTimeInterpreter = timeInterpreter;
        calculateTimeWidth();
    }

    public void setHeaderLoadListener(HeaderLoader.HeaderLoadListener headerLoadListener) {
        if (mHeaderLoader != null)
            this.mHeaderLoader.setHeaderLoadListener(headerLoadListener);
        else
            this.mHeaderLoader = new HeaderLoader(headerLoadListener);
    }

    public OnCellCheckedChangeListener getOnCellCheckedChangeListener() {
        return mOnCellCheckedChangeListener;
    }

    public void setOnCellCheckedChangeListener(OnCellCheckedChangeListener mOnCellCheckedChangeListener) {
        this.mOnCellCheckedChangeListener = mOnCellCheckedChangeListener;
    }

    public OnEventCheckedChangeListener getOnEventCheckedChangeListener() {
        return mOnEventCheckedChangeListener;
    }

    public void setOnEventCheckedChangeListener(OnEventCheckedChangeListener mOnEventCheckedChangeListener) {
        this.mOnEventCheckedChangeListener = mOnEventCheckedChangeListener;
    }

    public OnEventDroppedListener getOnEventDroppedListener() {
        return mOnEventDroppedListener;
    }

    public void setOnEventDroppedListener(OnEventDroppedListener onEventDroppedListener) {
        this.mOnEventDroppedListener = onEventDroppedListener;
    }

    @Nullable
    public EventLoader.EventLoadListener getEventLoadListener() {
        if (mEventLoader != null)
            return mEventLoader.getEventLoadListener();
        else
            return null;
    }

    public void setEventLoadListener(EventLoader.EventLoadListener eventLoadListener) {
        if (mEventLoader != null)
            this.mEventLoader.setEventLoadListener(eventLoadListener);
        else
            this.mEventLoader = new EventLoader(eventLoadListener);
    }

    private int selectionColor = Color.WHITE;
    private final Rect tempRect = new Rect();
    private final int fadeTime;

    // generateBackgroundDrawable
    private void regenerateBackground() {
        setBackgroundDrawable(generateBackground(selectionColor, fadeTime, tempRect));
    }

    private Drawable generateBackground(int color, int fadeTime, Rect bounds) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.setExitFadeDuration(fadeTime);

        // checked
        drawable.addState(new int[]{android.R.attr.state_checked}, generateOptionalDrawable(color, true));

        // pressed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.addState(new int[]{android.R.attr.state_pressed}, generateRippleDrawable(color, bounds));
        } else {
            drawable.addState(new int[]{android.R.attr.state_pressed}, generateOptionalDrawable(color, false));
        }

        // unchecked
        drawable.addState(new int[]{}, generateOptionalDrawable(color, false));

        return drawable;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable generateRippleDrawable(final int color, Rect bounds) {
        ColorStateList list = ColorStateList.valueOf(color);
        Drawable mask = generateOptionalDrawable(Color.WHITE, false);
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

    private Drawable generateOptionalDrawable(final int color, boolean useStroke) {
        return generateRectDrawable(color, useStroke);
    }

    private static Drawable generateRectDrawable(final int color, boolean useStrokeBorder) {
        UsableStrokeDrawable drawable = new UsableStrokeDrawable(new RectShape());
        drawable.setFillColor(color);
        drawable.setUseStroke(useStrokeBorder);
        return drawable;
    }
}
