package com.github.sdw8001.scheduleview.fragment;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.datetimepicker.date.DatePickerDialog;
import com.github.sdw8001.scheduleview.DateTimeInterpreter;
import com.github.sdw8001.scheduleview.R;
import com.github.sdw8001.scheduleview.header.GroupHeader;
import com.github.sdw8001.scheduleview.interpreter.HeaderInterpreter;
import com.github.sdw8001.scheduleview.listener.CalendarListener;
import com.github.sdw8001.scheduleview.loader.ScheduleLoader;
import com.github.sdw8001.scheduleview.loader.ScheduleViewLoader;
import com.github.sdw8001.scheduleview.util.WeekCurrentDateUtil;
import com.github.sdw8001.scheduleview.view.ScheduleView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDateTime;
import org.joda.time.Weeks;

import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-05-20.
 */
public class WeekScheduleFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private ScheduleView scheduleView;

    private LocalDateTime mStartDate, selectedDate;

    private TextView monthView, nowView, sundayTv, mondayTv, tuesdayTv, wednesdayTv, thursdayTv, fridayTv, saturdayTv;
    private ViewPager pager;
    private LinearLayout mBackground;

    private CalendarListener calendarListener;
    private CalendarAdaptor mAdaptor;

    //Bundle Keys
    public static String DATE_SELECTOR_BACKGROUND = "bg:select:date";
    public static String CURRENT_DATE_BACKGROUND = "bg:current:bg";
    public static String CALENDER_BACKGROUND = "bg:cal";
    public static String NOW_BACKGROUND = "bg:now";
    public static String PRIMARY_BACKGROUND = "bg:primary";
    public static String SECONDARY_BACKGROUND = "bg:secondary";
    public static String PACKAGENAME = "package";
    public static String WEEKCOUNT = "week:count";
    public static String POSITIONKEY = "pos";

    //initial values of calender property
    String selectorDateIndicatorValue = "bg_red";
    int currentDateIndicatorValue = Color.BLACK;
    int primaryTextColor = Color.WHITE;
    int weekCount = 53;//one year
    public static String PAKAGENAMEVALUE = "com.github.sdw8001.scheduleview";

    private static WeekScheduleFragment instance;

    public static WeekScheduleFragment getInstance() {
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(getActivity());
        instance = this;
        scheduleView = new ScheduleView(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekcalendar, container, false);

        pager = (ViewPager) view.findViewById(R.id.vp_pages);
        monthView = (TextView) view.findViewById(R.id.monthTv);
        nowView = (TextView) view.findViewById(R.id.nowTv);
        sundayTv = (TextView) view.findViewById(R.id.week_sunday);
        mondayTv = (TextView) view.findViewById(R.id.week_monday);
        tuesdayTv = (TextView) view.findViewById(R.id.week_tuesday);
        wednesdayTv = (TextView) view.findViewById(R.id.week_wednesday);
        thursdayTv = (TextView) view.findViewById(R.id.week_thursday);
        fridayTv = (TextView) view.findViewById(R.id.week_friday);
        saturdayTv = (TextView) view.findViewById(R.id.week_saturday);
        mBackground = (LinearLayout) view.findViewById(R.id.background);
        if (scheduleView.getParent() == null)
            ((ViewGroup) view.findViewById(R.id.linLayout_Parent)).addView(scheduleView);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nowView.setVisibility(View.GONE);

        /**
         * Customization 값 체크.
         */
        if (getArguments() != null) {
            if (getArguments().containsKey(CALENDER_BACKGROUND))
                mBackground.setBackgroundColor(getArguments().getInt(CALENDER_BACKGROUND));

            if (getArguments().containsKey(DATE_SELECTOR_BACKGROUND))
                selectorDateIndicatorValue = getArguments().getString(DATE_SELECTOR_BACKGROUND);

            if (getArguments().containsKey(CURRENT_DATE_BACKGROUND))
                currentDateIndicatorValue = getArguments().getInt(CURRENT_DATE_BACKGROUND);

            if (getArguments().containsKey(WEEKCOUNT))
                if (getArguments().getInt(WEEKCOUNT) > 0)
                    weekCount = getArguments().getInt(WEEKCOUNT);

            if (getArguments().containsKey(PRIMARY_BACKGROUND)) {
                monthView.setTextColor(getArguments().getInt(PRIMARY_BACKGROUND));
                primaryTextColor = getArguments().getInt(PRIMARY_BACKGROUND);
            }

            if (getArguments().containsKey(SECONDARY_BACKGROUND)) {
                nowView.setTextColor(getArguments().getInt(SECONDARY_BACKGROUND));
                sundayTv.setTextColor(getArguments().getInt(SECONDARY_BACKGROUND));
                mondayTv.setTextColor(getArguments().getInt(SECONDARY_BACKGROUND));
                tuesdayTv.setTextColor(getArguments().getInt(SECONDARY_BACKGROUND));
                wednesdayTv.setTextColor(getArguments().getInt(SECONDARY_BACKGROUND));
                thursdayTv.setTextColor(getArguments().getInt(SECONDARY_BACKGROUND));
                fridayTv.setTextColor(getArguments().getInt(SECONDARY_BACKGROUND));
                saturdayTv.setTextColor(getArguments().getInt(SECONDARY_BACKGROUND));
            }

            if (getArguments().containsKey(PACKAGENAME)) {
                PAKAGENAMEVALUE = getArguments().getString(PACKAGENAME);//its for showing the resource value from the parent package
            }

            if (getArguments().containsKey(NOW_BACKGROUND)) {
                Resources resources = getResources();
                nowView.setBackgroundResource(resources.getIdentifier(getArguments().getString(NOW_BACKGROUND), "drawable", PAKAGENAMEVALUE));
            }
        }
        //----------------------------------------------------------------------------------------------//

        /*Setting Calender Adaptor*/
        mAdaptor = new CalendarAdaptor(getActivity().getSupportFragmentManager());
        pager.setAdapter(mAdaptor);

       /*CalUtil is called*/
        WeekCurrentDateUtil.getInstance().calculate(getActivity());//date calculation called

        selectedDate = WeekCurrentDateUtil.getInstance().getSelectedDate();//sets selected from CalUtil
        mStartDate = WeekCurrentDateUtil.getInstance().getWeekStartDate();//sets start date from CalUtil

        //Setting the month name and selected date listener
        monthView.setText(selectedDate.year().getAsShortText() + "\n" + selectedDate.monthOfYear().getAsShortText().toUpperCase());
        if (calendarListener == null) {
            calendarListener = new CalendarListener() {
                @Override
                public void onSelectPicker() {
                    //User can use any type of pickers here the below picker is only Just a example
                    DatePickerDialog.newInstance(WeekScheduleFragment.this,
                            Calendar.getInstance().get(Calendar.YEAR),
                            Calendar.getInstance().get(Calendar.MONTH),
                            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).show(getActivity().getFragmentManager(), "datePicker");
                }

                @Override
                public void onSelectDate(LocalDateTime selectedDate) {
                    //callback when a date is selcted
//                    mDateSelectedTv.setText("" + selectedDate.getDayOfMonth() + "-" + selectedDate.getMonthOfYear() + "-" + selectedDate.getYear());
                }
            };
        }
        if (calendarListener != null) {
            calendarListener.onSelectDate(mStartDate);
        }


        /*Week change Listener*/
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int weekNumber) {
                int addDays = weekNumber * 7;
                selectedDate = mStartDate.plusDays(addDays); //add 7 days to the selected date
                monthView.setText(selectedDate.year().getAsShortText() + "\n" + selectedDate.monthOfYear().getAsShortText().toUpperCase());

                if (weekNumber == 0) {
                    //the first week comes to view
                    nowView.setVisibility(View.GONE);
                } else {
                    //the first week goes from view nowView set visible for Quick return to first week
                    nowView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        /**
         * Change view to  the date of the current week
         */
        nowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (calendarListener != null)
                    calendarListener.onSelectDate(mStartDate);
                pager.setCurrentItem(0);
            }
        });

        /**
         * For quick selection of a date.Any picker or custom date picker can de used
         */
        monthView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (calendarListener != null)
                    calendarListener.onSelectPicker();
            }
        });
    }

    /**
     * Set set date of the selected week
     *
     * @param calendar
     */
    public void setDateWeek(Calendar calendar) {
        LocalDateTime ldt = LocalDateTime.fromCalendarFields(calendar);
        WeekCurrentDateUtil.getInstance().setSelectedDate(ldt);
        int nextPage = Weeks.weeksBetween(mStartDate, ldt).getWeeks();

        if (nextPage >= 0 && nextPage < weekCount) {
            pager.setCurrentItem(nextPage);
            calendarListener.onSelectDate(ldt);
            WeekFragment fragment = (WeekFragment) pager.getAdapter().instantiateItem(pager, nextPage);
            fragment.ChangeSelector(ldt);
        }
    }

    /**
     * Notify the selected date main page
     *
     * @param mSelectedDate
     */
    public void getSelectedDate(LocalDateTime mSelectedDate) {
        if (calendarListener != null)
            calendarListener.onSelectDate(mSelectedDate);
    }

    /**
     * Adaptor which shows weeks in the view
     */
    private class CalendarAdaptor extends FragmentStatePagerAdapter {
        public CalendarAdaptor(FragmentManager fm) {
            super(fm);
        }

        @Override
        public WeekFragment getItem(int pos) {
            return WeekFragment.newInstance(pos, selectorDateIndicatorValue, currentDateIndicatorValue, primaryTextColor);
        }

        @Override
        public int getCount() {
            return weekCount;
        }
    }

    /**
     * Set setCalendarListener when user click on a date
     *
     * @param calendarListener
     */
    public void setCalendarListener(CalendarListener calendarListener) {
        this.calendarListener = calendarListener;
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        //This is the call back from picker used in the sample you can use custom or any other picker

        //IMPORTANT: get the year,month and date from picker you using and call setDateWeek method
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        setDateWeek(calendar);//Sets the selected date from Picker
    }

    /////////////////////////////////////////////////////////////////
    //
    //      Properties 의 get or set 관련 함수
    //
    /////////////////////////////////////////////////////////////////
    public int getViewMode() {
        if (scheduleView != null)
            return scheduleView.getViewMode();
        return 1;
    }

    public void setViewMode(int viewMode, boolean refresh) {
        if (scheduleView != null)
            scheduleView.setViewMode(viewMode, refresh);
    }

    public GroupHeader getFixedGroupHeader() {
        if (scheduleView != null)
            return scheduleView.getFixedGroupHeader();
        return null;
    }

    public void setFixedGroupHeader(GroupHeader fixedGroupHeader) {
        if (scheduleView != null)
            scheduleView.setFixedGroupHeader(fixedGroupHeader);
    }

    public void setFixedGroupHeader(GroupHeader fixedGroupHeader, boolean refresh) {
        if (scheduleView != null)
            scheduleView.setFixedGroupHeader(fixedGroupHeader, refresh);
    }

    public ScheduleView.EventRect getFocusedEventRect() {
        if (scheduleView != null)
            return scheduleView.getFocusedEventRect();
        return null;
    }

    public void setFocusedEventRect(ScheduleView.EventRect focusedEvent) {
        if (scheduleView != null)
            scheduleView.setFocusedEventRect(focusedEvent);
    }

    public ScheduleView.ScheduleRect getFocusedEmptyScheduleRect() {
        if (scheduleView != null)
            return scheduleView.getFocusedEmptyScheduleRect();
        return null;
    }

    public void setFocusedEmptyScheduleRect(ScheduleView.ScheduleRect focusedEmptyScheduleRect) {
        if (scheduleView != null)
            scheduleView.setFocusedEmptyScheduleRect(focusedEmptyScheduleRect);
    }

    public List<GroupHeader> getGroupHeaderItems() {
        if (scheduleView != null)
            return scheduleView.getGroupHeaderItems();
        return null;
    }

    public void setGroupHeaderItems(List<GroupHeader> groupHeaderItems) {
        if (scheduleView != null)
            scheduleView.setGroupHeaderItems(groupHeaderItems);
    }

    public FloatingActionButton getFloatingActionButton() {
        if (scheduleView != null)
            return scheduleView.getFloatingActionButton();
        return null;
    }

    public void setFloatingActionButton(FloatingActionButton floatingActionButton) {
        if (scheduleView != null)
            scheduleView.setFloatingActionButton(floatingActionButton);
    }

    /**
     * Header 와 Time Column 의 머리글 행에 표시 할 텍스트를 제공하는 interpreter 가져옵니다.
     *
     * @return The date, time interpreter.
     */
    public DateTimeInterpreter getDateTimeInterpreter() {
        if (scheduleView != null)
            return scheduleView.getDateTimeInterpreter();
        return null;
    }

    /**
     * Header 와 Time Column 의 머리글 행에 표시 할 텍스트를 제공하는 interpreter 설정합니다.
     *
     * @param dateTimeInterpreter The date, time interpreter.
     */
    public void setDateTimeInterpreter(DateTimeInterpreter dateTimeInterpreter) {
        if (scheduleView != null)
            scheduleView.setDateTimeInterpreter(dateTimeInterpreter);
    }

    /**
     * Header 와 Time Column 의 머리글 행에 표시 할 텍스트를 제공하는 interpreter 가져옵니다.
     *
     * @return The headerTitle, time interpreter.
     */
    public HeaderInterpreter getHeaderInterpreter() {
        if (scheduleView != null)
            return scheduleView.getHeaderInterpreter();
        return null;
    }

    /**
     * Header 와 Time Column 의 머리글 행에 표시 할 텍스트를 제공하는 interpreter 설정합니다.
     *
     * @param headerInterpreter The headerTitle, time interpreter.
     */
    public void setHeaderInterpreter(HeaderInterpreter headerInterpreter) {
        if (scheduleView != null)
            scheduleView.setHeaderInterpreter(headerInterpreter);
    }

    /**
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    public Calendar getFirstVisibleDay() {
        if (scheduleView != null)
            return scheduleView.getFirstVisibleDay();
        return null;
    }

    /**
     * Get the number of visible days in a week.
     *
     * @return The number of visible days in a week.
     */
    public int getNumberOfVisibleDays() {
        if (scheduleView != null)
            return scheduleView.getNumberOfVisibleDays();
        return 0;
    }

    /**
     * Set the number of visible days in a week.
     *
     * @param numberOfVisibleDays The number of visible days in a week.
     */
    public void setNumberOfVisibleDays(int numberOfVisibleDays) {
        if (scheduleView != null)
            scheduleView.setNumberOfVisibleDays(numberOfVisibleDays);
    }

    public void setNumberOfVisibleDays(int numberOfVisibleDays, boolean refresh) {
        if (scheduleView != null)
            scheduleView.setNumberOfVisibleDays(numberOfVisibleDays, refresh);
    }

    public
    @Nullable
    ScheduleLoader.ScheduleLoadListener getScheduleLoadListener() {
        if (scheduleView != null)
            return scheduleView.getScheduleLoadListener();
        return null;
    }

    public void setScheduleLoadListener(ScheduleLoader.ScheduleLoadListener scheduleLoadListener) {
        if (scheduleView != null)
            scheduleView.setScheduleLoadListener(scheduleLoadListener);
    }

    /**
     * ScheduleView 에서 EventLoader 를 가져옵니다. ViewLoader 를 확장하여 사용자정의 이벤트를 정의할 수 있습니다.
     *
     * @return Event Loader.
     */
    public ScheduleViewLoader getScheduleViewLoader() {
        if (scheduleView != null)
            return scheduleView.getScheduleViewLoader();
        return null;
    }

    /**
     * ScheduleView 에 EventLoader 를 설정합니다. ViewLoader 를 확장하여 사용자정의 이벤트를 정의할 수 있습니다.
     *
     * @param loader Event Loader.
     */
    public void setScheduleViewLoader(ScheduleViewLoader loader) {
        if (scheduleView != null)
            scheduleView.setScheduleViewLoader(loader);
    }

    public void setOnEventClickListener(ScheduleView.EventClickListener listener) {
        if (scheduleView != null)
            scheduleView.setOnEventClickListener(listener);
    }

    public ScheduleView.EventClickListener getEventClickListener() {
        if (scheduleView != null)
            return scheduleView.getEventClickListener();
        return null;
    }

    public ScheduleView.EventLongPressListener getEventLongPressListener() {
        if (scheduleView != null)
            return scheduleView.getEventLongPressListener();
        return null;
    }

    public void setEventLongPressListener(ScheduleView.EventLongPressListener eventLongPressListener) {
        if (scheduleView != null)
            scheduleView.setEventLongPressListener(eventLongPressListener);
    }

    public void setEmptyViewClickListener(ScheduleView.EmptyViewClickListener emptyViewClickListener) {
        if (scheduleView != null)
            scheduleView.setEmptyViewClickListener(emptyViewClickListener);
    }

    public ScheduleView.EmptyViewClickListener getEmptyViewClickListener() {
        if (scheduleView != null)
            return scheduleView.getEmptyViewClickListener();
        return null;
    }

    public void setEmptyViewLongPressListener(ScheduleView.EmptyViewLongPressListener emptyViewLongPressListener) {
        if (scheduleView != null)
            scheduleView.setEmptyViewLongPressListener(emptyViewLongPressListener);
    }

    public ScheduleView.EmptyViewLongPressListener getEmptyViewLongPressListener() {
        if (scheduleView != null)
            return scheduleView.getEmptyViewLongPressListener();
        return null;
    }

    public ScheduleView.GroupHeaderClickListener getGroupHeaderClickListener() {
        if (scheduleView != null)
            return scheduleView.getGroupHeaderClickListener();
        return null;
    }

    public void setGroupHeaderClickListener(ScheduleView.GroupHeaderClickListener groupHeaderClickListener) {
        if (scheduleView != null)
            scheduleView.setGroupHeaderClickListener(groupHeaderClickListener);
    }

    public void setScrollListener(ScheduleView.ScrollListener scrolledListener) {
        if (scheduleView != null)
            scheduleView.setScrollListener(scrolledListener);
    }

    public ScheduleView.ScrollListener getScrollListener() {
        if (scheduleView != null)
            return scheduleView.getScrollListener();
        return null;
    }

    public void setEventDrawListener(ScheduleView.EventDrawListener listener) {
        if (scheduleView != null)
            scheduleView.setEventDrawListener(listener);
    }

    public ScheduleView.EventDrawListener getEventDrawListener() {
        if (scheduleView != null)
            return scheduleView.getEventDrawListener();
        return null;
    }

    /**
     * Refreshes the view and loads the events again.
     */
    public void notifyDatasetChanged() {
        if (scheduleView != null)
            scheduleView.notifyDatasetChanged();
    }
}
