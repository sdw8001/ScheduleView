package com.github.sdw8001.scheduleview.fragment;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.sdw8001.scheduleview.R;
import com.github.sdw8001.scheduleview.listener.CalendarListener;
import com.github.sdw8001.scheduleview.util.IdGen;
import com.github.sdw8001.scheduleview.util.WeekCurrentDateUtil;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalDateTime;
import org.joda.time.Weeks;

import java.util.Calendar;

/**
 * Created by sdw80 on 2016-05-17.
 */
public class WeekCalendar extends Fragment {
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
    public static String PAKAGENAMEVALUE = "com.ramzcalender";

    private static WeekCalendar instance;

    public static WeekCalendar getInstance() {
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(getActivity());
        instance = this;
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

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        nowView.setVisibility(View.GONE);

        /**
         * Customization 값 체크.
         */
        if (getArguments().containsKey(CALENDER_BACKGROUND))
            mBackground.setBackgroundColor(getArguments().getInt(CALENDER_BACKGROUND));

        if (getArguments().containsKey(DATE_SELECTOR_BACKGROUND))
            selectorDateIndicatorValue = getArguments().getString(DATE_SELECTOR_BACKGROUND);

        if (getArguments().containsKey(CURRENT_DATE_BACKGROUND))
            currentDateIndicatorValue = getArguments().getInt(CURRENT_DATE_BACKGROUND);

        if (getArguments().containsKey(WEEKCOUNT))
            if (getArguments().getInt(WEEKCOUNT) > 0) weekCount = getArguments().getInt(WEEKCOUNT);

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
        //----------------------------------------------------------------------------------------------//

        /*Setting Calender Adaptor*/
        mAdaptor = new CalendarAdaptor(getActivity().getSupportFragmentManager());
        pager.setAdapter(mAdaptor);

       /*CalUtil is called*/
        WeekCurrentDateUtil.getInstance().calculate(getActivity());//date calculation called

        selectedDate = WeekCurrentDateUtil.getInstance().getSelectedDate();//sets selected from CalUtil
        mStartDate = WeekCurrentDateUtil.getInstance().getWeekStartDate();//sets start date from CalUtil

        //Setting the month name and selected date listener
        monthView.setText(selectedDate.monthOfYear().getAsShortText() + " " + selectedDate.year().getAsShortText().toUpperCase());
        calendarListener.onSelectDate(mStartDate);

        /*Week change Listener*/
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int weekNumber) {
                int addDays = weekNumber * 7;
                selectedDate = mStartDate.plusDays(addDays); //add 7 days to the selected date
                monthView.setText(selectedDate.monthOfYear().getAsShortText() + "-" + selectedDate.year().getAsShortText().toUpperCase());

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
}
