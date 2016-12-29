package com.github.sdw8001.sample;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.datetimepicker.date.DatePickerDialog;
import com.github.sdw8001.scheduleview.event.ScheduleViewEvent;
import com.github.sdw8001.scheduleview.header.GroupHeader;
import com.github.sdw8001.scheduleview.header.Header;
import com.github.sdw8001.scheduleview.loader.ScheduleLoader;
import com.github.sdw8001.scheduleview.util.ScheduleViewUtil;
import com.github.sdw8001.scheduleview.view.ScheduleView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-04-21.
 */
public class ScheduleViewActivity extends AppCompatActivity
        implements ScheduleLoader.ScheduleLoadListener,
        ScheduleView.EventDrawListener,
        ScheduleView.EventClickListener,
        ScheduleView.EventLongPressListener,
        ScheduleView.EmptyViewClickListener,
        ScheduleView.GroupHeaderClickListener,
        ScheduleView.DateCalendarListener,
        DatePickerDialog.OnDateSetListener {

    private final static String SAVE_INSTANCE_STATE_SCHEDULE_FOCUSED_DATE = "SAVE_INSTANCE_STATE_SCHEDULE_FOCUSED_DATE";
    private final static String SAVE_INSTANCE_STATE_SCHEDULE_FIXED_GROUP_HEADER = "SAVE_INSTANCE_STATE_SCHEDULE_FIXED_GROUP_HEADER";

    private List<TypeDetailData> mTypeDetailDataList;
    private List<AppointmentEvent> mAppointmentList;
    private ScheduleView mScheduleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduleview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mScheduleView.getFocusedEventRect() != null) {
                    Snackbar.make(view, mScheduleView.getFocusedEventRect().originalEvent.getKey(), Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        mScheduleView = (ScheduleView) findViewById(R.id.scheduleView);
        mScheduleView.setTimeStartHour(9);
        mScheduleView.setTimeStartMinute(0);
        mScheduleView.setTimeEndHour(19);
        mScheduleView.setTimeEndMinute(30);
        mScheduleView.setTimeDuration(30);
        mScheduleView.setNumberOfVisibleDays(5);
        mScheduleView.setScheduleLoadListener(this);
        mScheduleView.setEventDrawListener(this);
        mScheduleView.setOnEventClickListener(this);
        mScheduleView.setEventLongPressListener(this);
        mScheduleView.setEmptyViewClickListener(this);
        mScheduleView.setGroupHeaderClickListener(this);
        mScheduleView.setDateCalendarListener(this);
        mScheduleView.setFloatingActionButton(fab);

        List<GroupHeader> headers = new ArrayList<>();
        DoctorHeader doctorHeader;

        doctorHeader = new DoctorHeader(ScheduleViewUtil.today(), "00000235", "권한자", "01");
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "권한자", "00000235", "2", "2"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "권한자", "00000235", "3", "3"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "권한자", "00000235", "4", "4"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "권한자", "00000235", "5", "5"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "권한자", "00000235", "6", "6"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "권한자", "00000235", "1", "1->2"));
        headers.add(doctorHeader.getGroupHeader());

        doctorHeader = new DoctorHeader(ScheduleViewUtil.today(), "00000023", "나의사", "02");
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "나의사", "00000023", "4", "1"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "나의사", "00000023", "5", "2"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "나의사", "00000023", "6", "3"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "나의사", "00000023", "2", "4"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "나의사", "00000023", "1", "5"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "나의사", "00000023", "3", "6"));
        headers.add(doctorHeader.getGroupHeader());

        doctorHeader = new DoctorHeader(ScheduleViewUtil.today(), "00000228", "덴탑", "03");
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "덴탑", "00000228", "1", "1"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "덴탑", "00000228", "2", "2"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "덴탑", "00000228", "3", "3"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "덴탑", "00000228", "4", "4"));
        headers.add(doctorHeader.getGroupHeader());

        doctorHeader = new DoctorHeader(ScheduleViewUtil.today(), "00000181", "새코디", "04");
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "새코디", "00000181", "1", "1"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "새코디", "00000181", "2", "2"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "새코디", "00000181", "3", "3"));
        doctorHeader.getSubHeaders().add(new Header(ScheduleViewUtil.today(), "새코디", "00000181", "4", "4"));
        headers.add(doctorHeader.getGroupHeader());

        mScheduleView.setGroupHeaderItems(headers);
        mScheduleView.setViewMode(ScheduleView.VIEW_CHILD, false);
        mScheduleView.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_INSTANCE_STATE_SCHEDULE_FOCUSED_DATE, mScheduleView.getFocusedWeekDate());
        outState.putParcelable(SAVE_INSTANCE_STATE_SCHEDULE_FIXED_GROUP_HEADER, mScheduleView.getFixedGroupHeader());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mScheduleView.setFocusedWeekDate((Calendar)savedInstanceState.getSerializable(SAVE_INSTANCE_STATE_SCHEDULE_FOCUSED_DATE), false);
        mScheduleView.setFixedGroupHeader((GroupHeader) savedInstanceState.getParcelable(SAVE_INSTANCE_STATE_SCHEDULE_FIXED_GROUP_HEADER));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scheduleview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_doctor:
                if (mScheduleView != null && mScheduleView.getViewMode() != ScheduleView.VIEW_PARENT)
                    mScheduleView.setViewMode(ScheduleView.VIEW_PARENT, true);
                return true;

            case R.id.action_chair:
                if (mScheduleView != null && mScheduleView.getViewMode() != ScheduleView.VIEW_CHILD)
                    mScheduleView.setViewMode(ScheduleView.VIEW_CHILD, true);
                return true;

            case R.id.action_showDatePicker:
                onSelectPicker(Calendar.getInstance());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public List<? extends ScheduleViewEvent> onScheduleLoad(Calendar dateCalendar) {
        Toast.makeText(this, dateCalendar.get(Calendar.YEAR) + "년" + (dateCalendar.get(Calendar.MONTH) + 1) + "월" + dateCalendar.get(Calendar.DAY_OF_MONTH) + "일 onScheduleLoad", Toast.LENGTH_SHORT).show();
        if (mAppointmentList == null)
            mAppointmentList = new ArrayList<>();

        List<ScheduleViewEvent> matchedEvents = new ArrayList<>();
        AppointmentEvent appointmentEvent;

        appointmentEvent = new AppointmentEvent();
        appointmentEvent.setSeq("2016042806031505005");
        appointmentEvent.setAppTime("10:30");
        appointmentEvent.setDate("20160427");
        appointmentEvent.setId("06031505");
        appointmentEvent.setName("신현목/19311202001");
        appointmentEvent.setDoctorId("00000023");
        appointmentEvent.setDuration(90);
        appointmentEvent.setTodo("");
        appointmentEvent.setSlot("4");
        appointmentEvent.setSlotName("1");
        appointmentEvent.setSpecialType(-32640);
        appointmentEvent.setCnt(2);
        appointmentEvent.setChartId("19311202001");
        appointmentEvent.setName2("신현목");
        appointmentEvent.setTelNo("041-579-1565");
        appointmentEvent.setCellPhone("");
        appointmentEvent.setCrmGubun("");
        appointmentEvent.setNurse("");
        appointmentEvent.setMergeCnt(3);
        appointmentEvent.setDetailAppType("001");
        appointmentEvent.setAppType("0");
        appointmentEvent.setChangeType("1");
        appointmentEvent.setTreatmentKind("0");
        appointmentEvent.setNonePatientPhone("");
        appointmentEvent.setSmsSendYn(true);
        appointmentEvent.setDisSeq2(0);
        appointmentEvent.setChairName("1");
        appointmentEvent.setAppSeq(1);
        appointmentEvent.setIsReceipted(false);
        appointmentEvent.setToothStatus(0);
        appointmentEvent.setToothStatus2(0);
        appointmentEvent.setKeyValue("2016042706031505001");
        appointmentEvent.setExecuteFlag("N");
        appointmentEvent.setAutoSmsSend(true);
        appointmentEvent.setGSmsSend(false);
        appointmentEvent.setAppColor(-32640);
        appointmentEvent.setEtcCnt(1);
        appointmentEvent.setDisSeq(1);
        mAppointmentList.add(appointmentEvent);
        matchedEvents.add(appointmentEvent.toScheduleViewEvent());

        appointmentEvent = new AppointmentEvent();
        appointmentEvent.setSeq("2016042770302039004");
        appointmentEvent.setAppTime("12:30");
        appointmentEvent.setDate("20160427");
        appointmentEvent.setId("70302039");
        appointmentEvent.setName("덴탑/19830409009");
        appointmentEvent.setDoctorId("00000023");
        appointmentEvent.setDuration(30);
        appointmentEvent.setTodo("Todo 노트내용, #11,12,13,14,15,21,22,23,24,25 ");
        appointmentEvent.setSlot("4");
        appointmentEvent.setSlotName("1");
        appointmentEvent.setSpecialType(-256);
        appointmentEvent.setCnt(4);
        appointmentEvent.setChartId("19830409009");
        appointmentEvent.setName2("덴탑");
        appointmentEvent.setTelNo("");
        appointmentEvent.setCellPhone("010-1111-1111");
        appointmentEvent.setCrmGubun("동의서 OK");
        appointmentEvent.setNurse("");
        appointmentEvent.setMergeCnt(1);
        appointmentEvent.setDetailAppType("0");
        appointmentEvent.setAppType("0");
        appointmentEvent.setChangeType("1");
        appointmentEvent.setTreatmentKind("0");
        appointmentEvent.setNonePatientPhone("");
        appointmentEvent.setSmsSendYn(true);
        appointmentEvent.setDisSeq2(0);
        appointmentEvent.setChairName("1");
        appointmentEvent.setAppSeq(1);
        appointmentEvent.setIsReceipted(false);
        appointmentEvent.setToothStatus(0);
        appointmentEvent.setToothStatus2(0);
        appointmentEvent.setKeyValue("2016042770302039003");
        appointmentEvent.setExecuteFlag("N");
        appointmentEvent.setAutoSmsSend(true);
        appointmentEvent.setGSmsSend(false);
        appointmentEvent.setAppColor(-256);
        appointmentEvent.setEtcCnt(1);
        appointmentEvent.setDisSeq(1);
        mAppointmentList.add(appointmentEvent);
        matchedEvents.add(appointmentEvent.toScheduleViewEvent());

        appointmentEvent = new AppointmentEvent();
        appointmentEvent.setSeq("2016042770302039001");
        appointmentEvent.setAppTime("13:00");
        appointmentEvent.setDate("20160427");
        appointmentEvent.setId("70302039");
        appointmentEvent.setName("덴탑/19830409009");
        appointmentEvent.setDoctorId("00000023");
        appointmentEvent.setDuration(90);
        appointmentEvent.setTodo("");
        appointmentEvent.setSlot("4");
        appointmentEvent.setSlotName("1");
        appointmentEvent.setSpecialType(-256);
        appointmentEvent.setCnt(0);
        appointmentEvent.setChartId("19830409009");
        appointmentEvent.setName2("덴탑");
        appointmentEvent.setTelNo("");
        appointmentEvent.setCellPhone("010-1111-1111");
        appointmentEvent.setCrmGubun("동의서OK");
        appointmentEvent.setNurse("");
        appointmentEvent.setMergeCnt(3);
        appointmentEvent.setDetailAppType("003");
        appointmentEvent.setAppType("0");
        appointmentEvent.setChangeType("1");
        appointmentEvent.setTreatmentKind("0");
        appointmentEvent.setNonePatientPhone("");
        appointmentEvent.setSmsSendYn(true);
        appointmentEvent.setDisSeq2(0);
        appointmentEvent.setChairName("1");
        appointmentEvent.setAppSeq(1);
        appointmentEvent.setIsReceipted(false);
        appointmentEvent.setToothStatus(0);
        appointmentEvent.setToothStatus2(0);
        appointmentEvent.setKeyValue("");
        appointmentEvent.setExecuteFlag("N");
        appointmentEvent.setAutoSmsSend(true);
        appointmentEvent.setGSmsSend(false);
        appointmentEvent.setAppColor(-256);
        appointmentEvent.setEtcCnt(1);
        appointmentEvent.setDisSeq(1);
        mAppointmentList.add(appointmentEvent);
        matchedEvents.add(appointmentEvent.toScheduleViewEvent());

        appointmentEvent = new AppointmentEvent();
        appointmentEvent.setSeq("2016042770302891002");
        appointmentEvent.setAppTime("12:00");
        appointmentEvent.setDate("20160427");
        appointmentEvent.setId("70302891");
        appointmentEvent.setName("부산[덴탑]/20000000005");
        appointmentEvent.setDoctorId("00000235");
        appointmentEvent.setDuration(30);
        appointmentEvent.setTodo("");
        appointmentEvent.setSlot("2");
        appointmentEvent.setSlotName("2");
        appointmentEvent.setSpecialType(9);
        appointmentEvent.setCnt(3);
        appointmentEvent.setChartId("20000000005");
        appointmentEvent.setName2("부산[덴탑]");
        appointmentEvent.setTelNo("");
        appointmentEvent.setCellPhone("");
        appointmentEvent.setCrmGubun("");
        appointmentEvent.setNurse("");
        appointmentEvent.setMergeCnt(1);
        appointmentEvent.setDetailAppType("3");
        appointmentEvent.setAppType("0");
        appointmentEvent.setChangeType("9");
        appointmentEvent.setTreatmentKind("3");
        appointmentEvent.setNonePatientPhone("");
        appointmentEvent.setSmsSendYn(true);
        appointmentEvent.setDisSeq2(0);
        appointmentEvent.setChairName("2");
        appointmentEvent.setAppSeq(1);
        appointmentEvent.setIsReceipted(false);
        appointmentEvent.setToothStatus(0);
        appointmentEvent.setToothStatus2(0);
        appointmentEvent.setKeyValue("2016042770302891001");
        appointmentEvent.setExecuteFlag("N");
        appointmentEvent.setAutoSmsSend(true);
        appointmentEvent.setGSmsSend(false);
        appointmentEvent.setAppColor(Color.GRAY);
        appointmentEvent.setEtcCnt(1);
        appointmentEvent.setDisSeq(1);
        mAppointmentList.add(appointmentEvent);
        matchedEvents.add(appointmentEvent.toScheduleViewEvent());

        appointmentEvent = new AppointmentEvent();
        appointmentEvent.setSeq("2016042770303788005");
        appointmentEvent.setAppTime("13:00");
        appointmentEvent.setDate("20160427");
        appointmentEvent.setId("70303788");
        appointmentEvent.setName("유아/880897");
        appointmentEvent.setDoctorId("00000235");
        appointmentEvent.setDuration(30);
        appointmentEvent.setTodo("");
        appointmentEvent.setSlot("2");
        appointmentEvent.setSlotName("2");
        appointmentEvent.setSpecialType(0);
        appointmentEvent.setCnt(0);
        appointmentEvent.setChartId("880897");
        appointmentEvent.setName2("유아");
        appointmentEvent.setTelNo("");
        appointmentEvent.setCellPhone("");
        appointmentEvent.setCrmGubun("");
        appointmentEvent.setNurse("");
        appointmentEvent.setMergeCnt(1);
        appointmentEvent.setDetailAppType("2");
        appointmentEvent.setAppType("0");
        appointmentEvent.setChangeType("1");
        appointmentEvent.setTreatmentKind("0");
        appointmentEvent.setNonePatientPhone("");
        appointmentEvent.setSmsSendYn(true);
        appointmentEvent.setDisSeq2(0);
        appointmentEvent.setChairName("2");
        appointmentEvent.setAppSeq(1);
        appointmentEvent.setIsReceipted(false);
        appointmentEvent.setToothStatus(0);
        appointmentEvent.setToothStatus2(0);
        appointmentEvent.setKeyValue("2016042770303788002");
        appointmentEvent.setExecuteFlag("B");
        appointmentEvent.setAutoSmsSend(true);
        appointmentEvent.setGSmsSend(false);
        appointmentEvent.setAppColor(Color.WHITE);
        appointmentEvent.setEtcCnt(1);
        appointmentEvent.setDisSeq(1);
        mAppointmentList.add(appointmentEvent);
        matchedEvents.add(appointmentEvent.toScheduleViewEvent());

        appointmentEvent = new AppointmentEvent();
        appointmentEvent.setSeq("2016042706041308008");
        appointmentEvent.setAppTime("13:00");
        appointmentEvent.setDate("20160427");
        appointmentEvent.setId("06041308");
        appointmentEvent.setName("유정희/19250905001");
        appointmentEvent.setDoctorId("00000235");
        appointmentEvent.setDuration(60);
        appointmentEvent.setTodo("");
        appointmentEvent.setSlot("1");
        appointmentEvent.setSlotName("1->2");
        appointmentEvent.setSpecialType(-8323073);
        appointmentEvent.setCnt(0);
        appointmentEvent.setChartId("19250905001");
        appointmentEvent.setName2("유정희");
        appointmentEvent.setTelNo("031-413-8143");
        appointmentEvent.setCellPhone("011-9892-6824");
        appointmentEvent.setCrmGubun("");
        appointmentEvent.setNurse("");
        appointmentEvent.setMergeCnt(2);
        appointmentEvent.setDetailAppType("1");
        appointmentEvent.setAppType("0");
        appointmentEvent.setChangeType("1");
        appointmentEvent.setTreatmentKind("1");
        appointmentEvent.setNonePatientPhone("");
        appointmentEvent.setSmsSendYn(true);
        appointmentEvent.setDisSeq2(8);
        appointmentEvent.setChairName("1->2");
        appointmentEvent.setAppSeq(1);
        appointmentEvent.setIsReceipted(false);
        appointmentEvent.setToothStatus(0);
        appointmentEvent.setToothStatus2(0);
        appointmentEvent.setKeyValue("");
        appointmentEvent.setExecuteFlag("C");
        appointmentEvent.setAutoSmsSend(true);
        appointmentEvent.setGSmsSend(false);
        appointmentEvent.setAppColor(-4194304);
        appointmentEvent.setEtcCnt(1);
        appointmentEvent.setDisSeq(2);
        mAppointmentList.add(appointmentEvent);
        matchedEvents.add(appointmentEvent.toScheduleViewEvent());

        return matchedEvents;
    }

    @Override
    public SpannableStringBuilder onEventDraw(ScheduleViewEvent event, Paint textPaint, int availableWidth, int availableHeight) {

        // Prepare the name of the event.
        SpannableStringBuilder bob = null;
        if (event.getKey() != null) {
            for (AppointmentEvent appointmentEvent : mAppointmentList) {
                if (appointmentEvent.getSeq() == event.getKey()) {
                    bob = new SpannableStringBuilder();

                    String firstLine = appointmentEvent.getName() + "[" + appointmentEvent.getDuration() + "]";
                    String secondLine = appointmentEvent.getChairName();
                    String thirdLine = appointmentEvent.getTodo();

                    bob.append(firstLine);
                    if (secondLine.trim() != null && secondLine != "") {
                        bob.append("\n");
                        bob.append(secondLine);
                    }
                    if (thirdLine.trim() != null && thirdLine != "") {
                        bob.append("\n");
                        bob.append(thirdLine);
                    }
                    bob.setSpan(new StyleSpan(Typeface.NORMAL), 0, bob.length(), 0);
                    break;
                }
            }
        }
        return bob;
    }

    @Override
    public void onEventClick(ScheduleViewEvent event, RectF eventRect) {
        if (event.getKey() != null) {
            for (AppointmentEvent appointmentEvent : mAppointmentList) {
                if (appointmentEvent.getSeq() == event.getKey()) {
                    String firstLine = appointmentEvent.getName() + "[" + appointmentEvent.getDuration() + "]";
                    String secondLine = appointmentEvent.getChairName();
                    String thirdLine = appointmentEvent.getTodo();
                    Toast.makeText(this, "EventClicked : " + firstLine + secondLine + thirdLine, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

    @Override
    public void onEventLongPress(ScheduleViewEvent event, RectF eventRect) {
        if (event.getKey() != null) {
            for (AppointmentEvent appointmentEvent : mAppointmentList) {
                if (appointmentEvent.getSeq() == event.getKey()) {
                    String firstLine = appointmentEvent.getName() + "[" + appointmentEvent.getDuration() + "]";
                    String secondLine = appointmentEvent.getChairName();
                    String thirdLine = appointmentEvent.getTodo();
                    Toast.makeText(this, "EventLongPressed : " + firstLine + secondLine + thirdLine, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    }

    @Override
    public void onEmptyViewClicked(ScheduleView.ScheduleRect scheduleRect) {
        if (scheduleRect != null) {
            String firstLine = scheduleRect.getHeaderKey() + "["
                    + scheduleRect.getStartTime().get(Calendar.YEAR) + "-"
                    + (scheduleRect.getStartTime().get(Calendar.MONTH) + 1) + "-"
                    + scheduleRect.getStartTime().get(Calendar.DAY_OF_MONTH) + " "
                    + scheduleRect.getStartTime().get(Calendar.HOUR_OF_DAY) + ":"
                    + scheduleRect.getStartTime().get(Calendar.MINUTE) + "]"
                    + scheduleRect.getParentHeaderKey() + ":" + scheduleRect.getParentHeaderName() + ","
                    + scheduleRect.getHeaderKey() + ":" + scheduleRect.getHeaderName();
            Toast.makeText(this, "EmptyClicked : " + firstLine, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGroupHeaderClicked(GroupHeader groupHeader) {
        if (groupHeader != null) {
            if (mScheduleView.getFixedGroupHeader() == null) {
                this.mScheduleView.setFixedGroupHeader(groupHeader);
            } else {
                this.mScheduleView.setFixedGroupHeader(null, false);
            }

            Toast.makeText(this, groupHeader.getHeaderName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSelectPicker(Calendar calendar) {
        DatePickerDialog.newInstance(ScheduleViewActivity.this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "date_picker");
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        mScheduleView.setFocusedWeekDate(calendar, true);//Sets the selected date from Picker
    }
}
