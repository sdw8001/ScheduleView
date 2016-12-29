package com.github.sdw8001.scheduleview.view;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
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
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.Toast;

import com.github.sdw8001.scheduleview.R;
import com.github.sdw8001.scheduleview.event.ScheduleEvent;
import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;
import com.github.sdw8001.scheduleview.loader.EventLoader;
import com.github.sdw8001.scheduleview.loader.HeaderLoader;
import com.github.sdw8001.scheduleview.util.ScheduleTimeManager;
import com.github.sdw8001.scheduleview.util.TimeStartEnd;
import com.github.sdw8001.scheduleview.view.layout.CheckableHeaderView;
import com.github.sdw8001.scheduleview.view.layout.ScheduleCellView;
import com.github.sdw8001.scheduleview.view.layout.ScheduleEventView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-11-07.
 * Test
 */

public class ScheduleViewGroup extends FrameLayout implements View.OnClickListener, ScheduleEventView.OnCheckedChangeListener, ScheduleEventView.OnLongClickListener {

    private enum Direction {
        NONE, LEFT, RIGHT, VERTICAL
    }

    private Context mContext;
    private HeaderLoader mHeaderLoader;
    private EventLoader mEventLoader;

    private boolean mIsZooming;
    private boolean mHorizontalFlingEnabled = true;
    private boolean mVerticalFlingEnabled = false;
    private int mColumnWidth = 1;
    private int mHeaderHeight = 100;
    private int mHourHeight = 200;
    private int mNewHourHeight = -1;
    private int mMinimumFlingVelocity = 0;
    private int mScaledTouchSlop = 0;
    private int mScrollDuration = 100;
    private float mXScrollingSpeed = 1f;
    private PointF mCurrentOrigin = new PointF(0f, 0f);
    private Direction mCurrentScrollDirection = Direction.NONE;
    private Direction mCurrentFlingDirection = Direction.NONE;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetectorCompat mGestureDetector;
    private OverScroller mScroller;
    public ScheduleTimeManager mTimeManager;

    private List<TreeNode<ScheduleHeader>> mHeaders;
    private List<? extends ScheduleEvent> mEvents;

    // 임시 변수. 정확한 로직 확립 후 변경가능성이 큰 변수.
    private int mHeaderRowCount;

    // Attribute 정의
    private int mHeaderBackgroundColor = Color.WHITE;
    private int mCellMargin = 1;
    private int mCellMarginLeft = 1;
    private int mCellMarginTop = 1;
    private int mCellMarginRight = 1;
    private int mCellMarginBottom = 1;
    private int mTimeStartHour = 9;
    private int mTimeStartMinute = 0;
    private int mTimeEndHour = 18;
    private int mTimeEndMinute = 0;
    private int mTimeDuration = 60;

    /**
     * 한 행에 보여질 아이템 수
     */
    private int columnCount = 2;

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
            mHeaderBackgroundColor = a.getColor(R.styleable.ScheduleViewGroup_headerBackgroundColor, mHeaderBackgroundColor);
            mCellMargin = mCellMarginLeft = mCellMarginTop = mCellMarginRight = mCellMarginBottom = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellMargin, mCellMargin);
            mCellMarginLeft = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellMarginLeft, mCellMarginLeft);
            mCellMarginTop = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellMarginTop, mCellMarginTop);
            mCellMarginRight = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellMarginRight, mCellMarginRight);
            mCellMarginBottom = a.getDimensionPixelSize(R.styleable.ScheduleViewGroup_cellMarginBottom, mCellMarginBottom);
            mTimeStartHour = a.getInteger(R.styleable.ScheduleViewGroup_timeStartHour, mTimeStartHour);
            mTimeStartMinute = a.getInteger(R.styleable.ScheduleViewGroup_timeStartMinute, mTimeStartMinute);
            mTimeEndHour = a.getInteger(R.styleable.ScheduleViewGroup_timeEndHour, mTimeEndHour);
            mTimeEndMinute = a.getInteger(R.styleable.ScheduleViewGroup_timeEndMinute, mTimeEndMinute);
            mTimeDuration = a.getInteger(R.styleable.ScheduleViewGroup_timeDuration, mTimeDuration);
        } finally {
            a.recycle();
        }

        initialize();
    }

    private void initialize() {
        this.setClickable(true);
        this.setOnDragListener(new DragListener());
        this.setBackgroundColor(Color.WHITE);
//        this.setMotionEventSplittingEnabled(false);
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();

        // ScheduleTimeManager 설정
        mTimeManager = new ScheduleTimeManager(mTimeStartHour, mTimeStartMinute, mTimeEndHour, mTimeEndMinute, mTimeDuration);

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
                mNewHourHeight = Math.round(mHourHeight * detector.getScaleFactor());

                // 최소 HourHeight 보다 작으면
                if (mNewHourHeight < mMinHourHeight ) {
                    mHourHeight = mMinHourHeight;
                    mNewHourHeight = -1;
                }
                // 최대 HourHeight 보다 크면
                if (mNewHourHeight > mMaxHourHeight) {
                    mHourHeight = mMaxHourHeight;
                    mNewHourHeight = -1;
                }
                requestLayout();
                return true;
            }
        });
    }

    public void loadHeader(Calendar calendar) {
        if (mHeaderLoader != null) {
            mHeaders = mHeaderLoader.onLoad(calendar);
            removeHeaderViews();
            addHeaderViews(mHeaders);
        }
    }

    private ArrayList<CheckableHeaderView> mHeaderReference;
    private ArrayList<ScheduleEventView> mEventReference;

    public void addHeaderViews(List<TreeNode<ScheduleHeader>> headers) {

        if (mHeaderReference != null) {
            mHeaderReference.clear();
            mHeaderReference = null;
        }
        mHeaderReference = new ArrayList<>();

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

        // CheckableHeaderView 를 생성한다.
        for (TreeNode<ScheduleHeader> scheduleHeader : headers) {
            CheckableHeaderView view = new CheckableHeaderView(mContext);
            view.setHeaderNode(scheduleHeader);
            view.setClickable(true);
            view.setBackgroundColor(mHeaderBackgroundColor);
            view.getContents().setText(scheduleHeader.getData().getName());
            view.setOnDragListener(new DragListener());
            this.addView(view);
            view.setMargin(mCellMarginLeft, mCellMarginTop, mCellMarginRight, mCellMarginBottom);
            mHeaderReference.add(view);
            for (TreeNode<ScheduleHeader> child : scheduleHeader.getChildren()) {
                CheckableHeaderView childView = new CheckableHeaderView(mContext);
                childView.setHeaderNode(child);
                childView.setClickable(true);
                childView.setBackgroundColor(mHeaderBackgroundColor);
                childView.getContents().setText(child.getData().getName());
                childView.setOnDragListener(new DragListener());
                this.addView(childView);
                childView.setMargin(mCellMarginLeft, mCellMarginTop, mCellMarginRight, mCellMarginBottom);
                mHeaderReference.add(childView);
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
            for (TreeNode<ScheduleHeader> childNode : headerTreeNode.getChildren()) {
                if (TextUtils.equals(childNode.getParent().getData().getKey(), parentHeaderKey)
                        && TextUtils.equals(childNode.getData().getKey(), headerKey)) {
                    return childNode;
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

            view.setClickable(true);
            view.setCheckable(true);
            view.setOnDragListener(new DragListener());
            this.addView(view);
            view.setMargin(mCellMarginLeft, mCellMarginTop, mCellMarginRight, mCellMarginBottom);
        }
    }

    public void removeHeaderViews() {
        if (mHeaderReference != null) {
            mHeaderReference.clear();
            mHeaderReference = null;
        }

        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (getChildAt(i) instanceof CheckableHeaderView) {
                this.removeView(getChildAt(i));
            }
        }
    }

    public void loadEvent(Calendar calendar) {
        if (mEventLoader != null) {
            mEvents = mEventLoader.onLoad(calendar);
            removeEventViews();
            addEventViews(mEvents);
        }
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
            view.setHeaderNode(scheduleEvent.getHeaderNode());
            view.setTimeStart(scheduleEvent.getStartTime());
            view.setTimeEnd(scheduleEvent.getEndTime());
            view.setContents(scheduleEvent.getContents());
            view.setOnLongClickListener(this);
            view.setOnCheckedChangeListener(this);
            view.setElevation(3);
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

        // 같은 Header 내에 Cell(시간)이 겹치는 Event 에 대하여 Bound 설정(Cell Width 를 분할하여 최대폭 설정)
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
            mEventReference.clear();
            mEventReference = null;
        }

        for (int i = getChildCount() - 1; i >= 0; i--) {
            if (getChildAt(i) instanceof ScheduleEventView) {
                this.removeView(getChildAt(i));
            }
        }
    }

    public void setColor(@ColorInt int color) {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof ScheduleEventView) {
                ScheduleEventView view = (ScheduleEventView) getChildAt(i);
                view.setBackgroundColor(color);
            }
        }
        invalidate();
    }

    @Override
    public void onCheckedChanged(ScheduleEventView checkableView, boolean checked) {
        String message;
        message = checkableView.getContents() + (checked ? "True" : "False");
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
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
                    Log.e("DragEvent", "ACTION_DRAG_STARTED");
                    view.setVisibility(View.INVISIBLE);
                    break;
                // View Drag Entered
                case DragEvent.ACTION_DRAG_ENTERED:
                    Log.e("DragEvent", "ACTION_DRAG_ENTERED");
                    v.setPressed(true);
                    break;
                // View Drag Exited
                case DragEvent.ACTION_DRAG_EXITED:
                    Log.e("DragEvent", "ACTION_DRAG_EXITED");
                    v.setPressed(false);
//                    view.setVisibility(View.VISIBLE);
                    break;
                // View Drag Drop
                case DragEvent.ACTION_DROP:
                    Log.e("DragEvent", "ACTION_DROP");
                    v.setPressed(false);
                    view.setVisibility(View.VISIBLE);
                    break;
                // View Drag Ended
                case DragEvent.ACTION_DRAG_ENDED:
                    Log.e("DragEvent", "ACTION_DRAG_ENDED");
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

    @Override
    public void onClick(View v) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View childView = getChildAt(i);
            if (childView instanceof AppointmentView) {
                AppointmentView childAppView = (AppointmentView) childView;
//                childAppView.setChecked(false);
            }
        }
        if (v instanceof AppointmentView) {
            AppointmentView view = (AppointmentView) v;
//            Log.e(view.getText().toString(), view.isChecked() ? "true" : "false");
//            view.setChecked(!view.isChecked());
        }
        if (v instanceof ScheduleEventView) {
            ScheduleEventView view = (ScheduleEventView) v;

            Toast.makeText(mContext, view.getContents(), Toast.LENGTH_SHORT).show();
        }
    }

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
        final int specModeWidth = MeasureSpec.getMode(widthMeasureSpec);
        final int specModeHeight = MeasureSpec.getMode(heightMeasureSpec);
        final int specSizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec);

        final int desiredWidth = specSizeWidth - getPaddingLeft() - getPaddingRight();
        final int desiredHeight = specSizeHeight - getPaddingTop() - getPaddingBottom();

        // mColumnWidth 의 정의와 개념 : ColumnMarginLeft + ColumnPaddingLeft + ColumnWidth + ColumnPaddingRight + ColumnMarginRight
        mColumnWidth = desiredWidth / columnCount;

        // mHourHeight 설정(zoom 으로 변경된 Scale 적용)
        if (mNewHourHeight > mMinHourHeight && mNewHourHeight < mMaxHourHeight) {
            mCurrentOrigin.y = ((mCurrentOrigin.y - mMidPointY) / mHourHeight) * mNewHourHeight + mMidPointY;
            mHourHeight = mNewHourHeight;
            mNewHourHeight = -1;
        }

        setMeasuredDimension(specSizeWidth, specSizeHeight);

        int count = getChildCount();
        mHeaderRowCount = 1;

        // For 문에서 Measure 정의에 사용될 로컬변수
        int headerSizeWidth;
        int childWidthMeasureSpec, childHeightMeasureSpec;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child instanceof CheckableHeaderView) {
                final CheckableHeaderView headerView = (CheckableHeaderView) child;
                final TreeNode<ScheduleHeader> headerNode = headerView.getHeaderNode();

                int headerChildCount = 1;
                headerSizeWidth = (mColumnWidth - (mCellMarginLeft + mCellMarginRight)) * headerChildCount;

                // Parent Header
                if (headerNode.getChildren().size() > 0) {
                    headerChildCount = headerNode.getChildren().size();
                    // Parent 의 경우 Child 수 만큼 Merge 되기 때문에 Child 들의 사이 Margin (left, right) 값이 추가로 Width 로 들어간다.
                    headerSizeWidth = mColumnWidth * headerChildCount - (mCellMarginLeft + mCellMarginRight);
                    mHeaderRowCount = 2;
                }

                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(headerSizeWidth, MeasureSpec.EXACTLY);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeaderHeight, MeasureSpec.EXACTLY);
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            } else if (child instanceof ScheduleEventView) {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mColumnWidth, MeasureSpec.EXACTLY);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mHourHeight, MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mColumnWidth - (mCellMarginLeft + mCellMarginRight), MeasureSpec.EXACTLY);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mHourHeight, MeasureSpec.EXACTLY);
            }

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }

        // mCurrentOrigin.y 값이 View 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (mCurrentOrigin.y < getHeight() - getDrawEventsTop() - (mCellMarginTop + mHourHeight + mCellMarginBottom) * mTimeManager.getTimeCount())
            mCurrentOrigin.y = getHeight() - getDrawEventsTop() - (mCellMarginTop + mHourHeight + mCellMarginBottom) * mTimeManager.getTimeCount();

        // mCurrentOrigin.y 값이 View 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (mCurrentOrigin.y > 0) {
            mCurrentOrigin.y = 0;
        }

        // Scroll 된 offset 설정
        int shiftColumnCount = (int) -(Math.ceil(mCurrentOrigin.x / mColumnWidth));

        // mCurrentOrigin.x 값이 HeaderViewList 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (shiftColumnCount < 0) {
            shiftColumnCount = 0;
            mCurrentOrigin.x = 0;
            mScroller.forceFinished(true);
        }

        // mCurrentOrigin.x 값이 HeaderViewList 의 범위를 벗어날 경우 사용가능한 값으로 설정
        if (mHeaders != null && shiftColumnCount + columnCount > getHeaderSize() - 1 && getHeaderSize() >= 0) {
            shiftColumnCount = getHeaderSize() - columnCount;
            mCurrentOrigin.x = -(shiftColumnCount * mColumnWidth);
            mScroller.forceFinished(true);
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
        final int count = getChildCount();

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

        // ScheduleViewGroup 의 Child 중 CheckableHeaderView Layout 작업.
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            // Header 영역의 Header Layout 적용
            if (child instanceof CheckableHeaderView) {
                TreeNode<ScheduleHeader> header = ((CheckableHeaderView) child).getHeaderNode();

                // Parent
                if (header.getChildren().size() > 0) {
                    child.layout(headerParentLeftSum,
                            headerParentTop,
                            headerParentLeftSum + child.getMeasuredWidth(),
                            headerParentTop + child.getMeasuredHeight());
                    headerParentLeftSum += child.getMeasuredWidth() + mCellMarginRight + mCellMarginLeft;
                } else {
                    child.layout(headerChildLeftSum,
                            headerChildTop,
                            headerChildLeftSum + child.getMeasuredWidth(),
                            headerChildTop + child.getMeasuredHeight());
                    headerChildLeftSum += child.getMeasuredWidth() + mCellMarginRight + mCellMarginLeft;
                }
            }
        }

        // ScheduleViewGroup 의 Child 중 ScheduleCellView Layout 작업.
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }

            // ScheduleCell 영역의 Layout 적용
            if (child instanceof ScheduleCellView) {
                TreeNode<ScheduleHeader> headerNode = ((ScheduleCellView) child).getHeaderNode();
                CheckableHeaderView headerView = findHeaderView(headerNode);
                int index = mTimeManager.getTimeStartIndex(((ScheduleCellView) child).getTimeStart());

                child.layout(headerView.getBounds().left,
                        eventTop + ((mCellMarginTop + mHourHeight + mCellMarginBottom) * index) + (int) mCurrentOrigin.y,
                        headerView.getBounds().right,
                        eventTop + ((mCellMarginTop + mHourHeight + mCellMarginBottom) * index) + (int) mCurrentOrigin.y + child.getMeasuredHeight());
                // ScheduleEventView 영역의 Layout 적용
            } else if (child instanceof ScheduleEventView) {
                ScheduleEventView eventView = (ScheduleEventView) child;
                TreeNode<ScheduleHeader> headerNode = eventView.getHeaderNode();
                CheckableHeaderView headerView = findHeaderView(headerNode);

                float childTop = mHourHeight * (eventView.top - mTimeManager.getTimeStartMinute()) / mTimeDuration + mCurrentOrigin.y + eventTop + mCellMarginTop;
                float childBottom = mHourHeight * (eventView.bottom - mTimeManager.getTimeStartMinute()) / mTimeDuration + mCurrentOrigin.y + eventTop - (mCellMarginTop + mCellMarginBottom);
                float childLeft = headerView.getBounds().left + eventView.left * headerView.getBounds().width() + mCellMarginLeft;
                float childRight = childLeft + eventView.width * headerView.getBounds().width() - (mCellMarginLeft + mCellMarginRight);

                child.layout((int) childLeft, (int) childTop, (int) childRight, (int) childBottom);
            }
        }
    }

    private CheckableHeaderView findHeaderView(TreeNode<ScheduleHeader> headerNode) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child instanceof CheckableHeaderView) {
                CheckableHeaderView view = (CheckableHeaderView) child;
                if (view.getHeaderNode().equals(headerNode))
                    return view;
            }
        }
        return null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("onSizeChanged", "-onSizeChanged");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.e("onDraw", "-onDraw");
    }

    private int getHeaderSize() {
        int size = 0;
        for (TreeNode<ScheduleHeader> header : mHeaders) {
            if (header.getChildren().size() > 0)
                size += header.getChildren().size();
            else
                size += 1;
        }
        return size;
    }

    private boolean isFocusedHeader = false;
    private CheckableHeaderView focusedHeaderView;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        focusedHeaderView = getPointHeader(event.getX(), event.getY());
        isFocusedHeader = focusedHeaderView != null;
        onTouchEvent(event);
        return false;
    }

    private CheckableHeaderView getPointHeader(float x, float y) {
        for (CheckableHeaderView headerView : mHeaderReference) {
            if (headerView.getBounds().contains((int)x, (int)y)) {
                return headerView;
            }
        }
        return null;
    }

    // 시작 위치를 저장을 위한 변수 
    private float mLastMotionOneX = 0;
    private float mLastMotionOneY = 0;
    private float mLastMotionTwoX = 0;
    private float mLastMotionTwoY = 0;
    private float mMidPointX = 0;
    private float mMidPointY = 0;
    //  마우스 move 로 일정범위 벗어나면 취소하기 위한  값
    private int mTouchSlop;

    private int mMinHourHeight = 70;
    private int mMaxHourHeight = 300;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean onTouched;

        final int action = event.getAction();
        final int pointerCount = event.getPointerCount();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionOneX = event.getX();
                mLastMotionOneY = event.getY();    // 시작 위치 저장
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mLastMotionTwoX = event.getX();
                mLastMotionTwoY = event.getY();
                mMidPointX = (mLastMotionOneX + mLastMotionTwoX) / 2;
                mMidPointY = (mLastMotionOneY + mLastMotionTwoY - getDrawEventsTop() * 2) / 2;
                break;
            case MotionEvent.ACTION_UP:
                setScheduleClick(event);
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
                            getHeight() - getDrawEventsTop() - (mCellMarginTop + mHourHeight + mCellMarginBottom) * mTimeManager.getTimeCount(), 0);
                    break;
                case VERTICAL:
                    mScroller.fling((int) mCurrentOrigin.x, (int) mCurrentOrigin.y,
                            0, (int) velocityY,
                            Integer.MIN_VALUE, Integer.MAX_VALUE,
                            getHeight() - getDrawEventsTop() - (mCellMarginTop + mHourHeight + mCellMarginBottom) * mTimeManager.getTimeCount(), 0);
                    break;
            }

            ViewCompat.postInvalidateOnAnimation(ScheduleViewGroup.this);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (!isFocusedHeader) {
                for (int i = 0; i < getChildCount(); i++) {
                    if (getChildAt(i) instanceof ScheduleEventView) {
                        ScheduleEventView view = (ScheduleEventView) getChildAt(i);
                        if (view.getBounds().contains((int) e.getX(), (int) e.getY())) {
                            // 태그 생성
                            ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());

                            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                            ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
                            DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                            view.startDrag(data, shadowBuilder, view, 0);
                            super.onLongPress(e);
                            break;
                        }
                    }
                }
            }
        }
    };

    private void setScheduleClick(MotionEvent e) {
        View childView;
        CheckableHeaderView headerView = null;
        ScheduleCellView scheduleCellView = null;
        ScheduleEventView scheduleEventView = null;
        for (int i = 0; i < getChildCount(); i++) {
            childView = getChildAt(i);
            if (childView instanceof CheckableHeaderView) {
                headerView = (CheckableHeaderView) childView;
                if (headerView.getBounds().contains((int) e.getX(), (int) e.getY())) {
                    scheduleCellView = null;
                    scheduleEventView = null;
                    break;
                } else {
                    headerView = null;
                }
            }
        }
        if (headerView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                childView = getChildAt(i);
                if (childView instanceof ScheduleEventView) {
                    scheduleEventView = (ScheduleEventView) childView;
                    if (scheduleEventView.getBounds().contains((int) e.getX(), (int) e.getY())) {
                        headerView = null;
                        scheduleCellView = null;
                        break;
                    } else {
                        scheduleEventView = null;
                    }
                }
            }

            if (scheduleEventView == null) {
                for (int i = 0; i < getChildCount(); i++) {
                    childView = getChildAt(i);
                    if (childView instanceof ScheduleCellView) {
                        scheduleCellView = (ScheduleCellView) childView;
                        if (scheduleCellView.getBounds().contains((int) e.getX(), (int) e.getY())) {
                            headerView = null;
                            scheduleEventView = null;
                            break;
                        } else {
                            scheduleCellView = null;
                        }
                    }
                }
            }
        }

        Checkable checkable;

        boolean selectScheduleCell = false;
        final float x = e.getX();
        final float y = e.getY();

        if (headerView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i) instanceof Checkable) {
                    checkable = (Checkable) getChildAt(i);
                    if (checkable instanceof CheckableHeaderView) {
                        continue;
                    }
                    if (scheduleEventView != null && scheduleEventView == checkable) {

                        // EventView 가 선택되어 있는경우 해당 EventView 가 위치한 ScheduleCell Select 하도록 Flag 설정
                        if (checkable.isChecked())
                            selectScheduleCell = true;

                        checkable.toggle();
                        continue;
                    }
                    if (scheduleCellView != null && scheduleCellView == checkable) {
                        continue;
                    }
                    if (checkable.isChecked())
                        checkable.setChecked(false);
                }
            }

            // Event 선택 해제와 함께 Background Cell 을 선택해준다.
            if (selectScheduleCell) {
                ScheduleCellView cellView = findScheduleCellView(x, y);
                cellView.setChecked(true);
            }
        }
    }

    /**
     * 좌표 x, y 를 포함하는 ScheduleCellView 를 반환한다. 해당 View 가 없으면 null 반환.
     * @param x 좌표 x
     * @param y 좌표 y
     * @return 좌표 x, y 를 포함하는 ScheduleCellView
     */
    private ScheduleCellView findScheduleCellView(float x, float y) {
        int childCount = getChildCount();
        ScheduleCellView scheduleCellView;
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i) instanceof ScheduleCellView) {
                scheduleCellView = (ScheduleCellView) getChildAt(i);
                if (scheduleCellView.getBounds().contains((int) x, (int) y))
                    return scheduleCellView;
            }
        }
        return null;
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

    public void setHeaderLoadListener(HeaderLoader.HeaderLoadListener headerLoadListener) {
        if (mHeaderLoader != null)
            this.mHeaderLoader.setHeaderLoadListener(headerLoadListener);
        else
            this.mHeaderLoader = new HeaderLoader(headerLoadListener);
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
