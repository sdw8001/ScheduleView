package com.github.sdw8001.sample;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.DragEvent;
import android.view.View;
import android.widget.Toast;

import com.android.datetimepicker.date.DatePickerDialog;
import com.github.sdw8001.scheduleview.event.ScheduleEvent;
import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;
import com.github.sdw8001.scheduleview.loader.EventLoader;
import com.github.sdw8001.scheduleview.loader.HeaderLoader;
import com.github.sdw8001.scheduleview.view.ScheduleViewGroup;
import com.github.sdw8001.scheduleview.view.layout.ScheduleCellView;
import com.github.sdw8001.scheduleview.view.layout.ScheduleEventView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewGroupActivity extends AppCompatActivity implements HeaderLoader.HeaderLoadListener,
        EventLoader.EventLoadListener,
        ScheduleViewGroup.OnCellCheckedChangeListener,
        ScheduleViewGroup.OnEventCheckedChangeListener,
        ScheduleViewGroup.OnEventDroppedListener,
        ScheduleViewGroup.DateCalendarListener,
        DatePickerDialog.OnDateSetListener {

    public static final int HEADER_PARENT_MODE = 1;
    public static final int HEADER_CHILD_MODE = 1 << 2;

    private int headerMode = HEADER_CHILD_MODE;
    ScheduleViewGroup scheduleViewGroup;
    ArrayList<ScheduleHeader> headers;
    ArrayList<ScheduleEvent> events;
    TestDialogFragment eventTestDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scheduleViewGroup = (ScheduleViewGroup) findViewById(R.id.ScheduleViewGroup);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        findViewById(R.id.ButtonColorGreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleViewGroup.setEventColor(events.get(1), Color.GREEN);
            }
        });
        findViewById(R.id.ButtonColorRed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleViewGroup.setEventColor(events.get(1), Color.RED);
            }
        });
        findViewById(R.id.ButtonHeaderParent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                headerMode = HEADER_PARENT_MODE;
                scheduleViewGroup.loadHeader();
                scheduleViewGroup.loadEvent();
            }
        });
        findViewById(R.id.ButtonHeaderChild).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                headerMode = HEADER_CHILD_MODE;
                scheduleViewGroup.loadHeader();
                scheduleViewGroup.loadEvent();
            }
        });
        findViewById(R.id.ButtonSetColumnCount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleViewGroup.setColumnCount(3);
                scheduleViewGroup.notifyDataSetChanged();
            }
        });

        scheduleViewGroup.setHeaderLoadListener(this);
        scheduleViewGroup.setEventLoadListener(this);
        scheduleViewGroup.setOnCellCheckedChangeListener(this);
        scheduleViewGroup.setOnEventCheckedChangeListener(this);
        scheduleViewGroup.setOnEventDroppedListener(this);
        scheduleViewGroup.setDateCalendarListener(this);
        scheduleViewGroup.loadHeader();
        scheduleViewGroup.loadEvent(Calendar.getInstance());
        scheduleViewGroup.setColumnCount(10);
    }

    @Override
    public List<TreeNode<ScheduleHeader>> onHeaderLoad() {

        List<TreeNode<ScheduleHeader>> treeNodes = new ArrayList<>();
        TreeNode<ScheduleHeader> treeNode;
        headers = new ArrayList<>();
        if (headerMode == HEADER_CHILD_MODE) {
            {
                // Parent
                ScheduleHeader scheduleHeader = new ScheduleHeader();
                scheduleHeader.setKey("00000235");
                scheduleHeader.setName("권한자");
                treeNode = new TreeNode<>(scheduleHeader);

                // Child
                scheduleHeader = new ScheduleHeader();
                scheduleHeader.setKey("1");
                scheduleHeader.setName("Chair A");
                treeNode.addChild(scheduleHeader);
                // Child
                scheduleHeader = new ScheduleHeader();
                scheduleHeader.setKey("2");
                scheduleHeader.setName("Chair B");
                treeNode.addChild(scheduleHeader);
                // Child
                scheduleHeader = new ScheduleHeader();
                scheduleHeader.setKey("3");
                scheduleHeader.setName("Chair C");
                treeNode.addChild(scheduleHeader);

                treeNodes.add(treeNode);
            }

            {
                // Parent
                ScheduleHeader scheduleHeader = new ScheduleHeader();
                scheduleHeader.setKey("00000001");
                scheduleHeader.setName("나의사");
                treeNode = new TreeNode<>(scheduleHeader);

                // Child
                scheduleHeader = new ScheduleHeader();
                scheduleHeader.setKey("1");
                scheduleHeader.setName("나의사체어1");
                treeNode.addChild(scheduleHeader);
                // Child
                scheduleHeader = new ScheduleHeader();
                scheduleHeader.setKey("2");
                scheduleHeader.setName("나의사체어2");
                treeNode.addChild(scheduleHeader);

                treeNodes.add(treeNode);
            }
        } else if (headerMode == HEADER_PARENT_MODE) {
            {
                // Parent
                ScheduleHeader scheduleHeader = new ScheduleHeader();
                scheduleHeader.setKey("00000235");
                scheduleHeader.setName("권한자");
                treeNode = new TreeNode<>(scheduleHeader);
                treeNodes.add(treeNode);
            }
            {
                // Parent
                ScheduleHeader scheduleHeader = new ScheduleHeader();
                scheduleHeader.setKey("00000001");
                scheduleHeader.setName("나의사");
                treeNode = new TreeNode<>(scheduleHeader);
                treeNodes.add(treeNode);
            }
        }

        return treeNodes;
    }

    @Override
    public List<? extends ScheduleEvent> onEventLoad(Calendar dateCalendar) {
        events = new ArrayList<>();
        ScheduleEvent event;

        event = new ScheduleEvent();
        event.setKey("2016042706041308008");
        dateCalendar.set(Calendar.HOUR_OF_DAY, 10);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 11);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(60);
        if (headerMode == HEADER_CHILD_MODE)
            event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "1"));
        else
            event.setHeaderNode(scheduleViewGroup.getHeaderNode(null, "00000235"));
//        event.setContents("Test0");
        event.setContents("Test01234567890abcdefghijklmnopqrstuvwxyz" + "\r\n" + "가나다라마바사아자차카타파하" + "\r\n" + "I love korea. what's your name? Hi, nice to meet you.");
        event.setBackgroundColor(-32640);
        event.setTypeColor(Color.RED);
        event.setTypeDetail("s");
        event.setTypeDetailForeColor(-16777216);
        event.setTypeDetailBackColor(-65536);
        events.add(event);

        event = new ScheduleEvent();
        event.setKey("2016042770303788005");
        dateCalendar.set(Calendar.HOUR_OF_DAY, 11);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 13);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(120);
        if (headerMode == HEADER_CHILD_MODE)
            event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "1"));
        else
            event.setHeaderNode(scheduleViewGroup.getHeaderNode(null, "00000235"));
        event.setContents("Test1");
        event.setBackgroundColor(-256);
        event.setTypeColor(Color.BLUE);
        event.setTypeDetail("V");
        event.setTypeDetailForeColor(-12566464);
        event.setTypeDetailBackColor(-16711936);
        events.add(event);

        event = new ScheduleEvent();
        event.setKey("2016042770302891002");
        dateCalendar.set(Calendar.HOUR_OF_DAY, 10);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 13);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(180);
        if (headerMode == HEADER_CHILD_MODE)
            event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "2"));
        else
            event.setHeaderNode(scheduleViewGroup.getHeaderNode(null, "00000235"));
        event.setContents("Test2");
        event.setBackgroundColor(Color.GRAY);
        event.setTypeColor(Color.WHITE);
        event.setTypeDetail("I");
        event.setTypeDetailForeColor(-2039584);
        event.setTypeDetailBackColor(-16777216);
        events.add(event);

        event = new ScheduleEvent();
        event.setKey("2016042770302039001");
        dateCalendar.set(Calendar.HOUR_OF_DAY, 9);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 15);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(360);
        if (headerMode == HEADER_CHILD_MODE)
            event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000001", "1"));
        else
            event.setHeaderNode(scheduleViewGroup.getHeaderNode(null, "00000001"));
        event.setContents("Test3");
        event.setBackgroundColor(Color.WHITE);
        event.setTypeColor(Color.GRAY);
        event.setTypeDetail("G");
        event.setTypeDetailForeColor(-12566464);
        event.setTypeDetailBackColor(-16711936);
        events.add(event);

        event = new ScheduleEvent();
        event.setKey("2016042770302039004");
        dateCalendar.set(Calendar.HOUR_OF_DAY, 12);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 15);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(180);
        if (headerMode == HEADER_CHILD_MODE)
            event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "2"));
        else
            event.setHeaderNode(scheduleViewGroup.getHeaderNode(null, "00000235"));
        event.setContents("Test4");
        event.setBackgroundColor(-4194304);
        event.setTypeColor(Color.MAGENTA);
        event.setTypeDetail("R");
        event.setTypeDetailForeColor(-12566464);
        event.setTypeDetailBackColor(-16711936);
        events.add(event);
        return events;
    }

    @Override
    public void onSelectPicker(Calendar calendar) {
        DatePickerDialog.newInstance(ViewGroupActivity.this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show(getFragmentManager(), "date_picker");
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        scheduleViewGroup.setCurrentDate(calendar); //Sets the selected date from Picker
        scheduleViewGroup.loadEvent();
        Toast.makeText(this, year + "년 " + monthOfYear + 1 + "월 " + dayOfMonth + "일", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCellCheckedChanged(ScheduleViewGroup scheduleViewGroup, ScheduleCellView checkedScheduleCellView, boolean checked) {

    }

    @Override
    public void onEventCheckedChanged(ScheduleViewGroup scheduleViewGroup, ScheduleEventView checkedScheduleEventView, boolean checked) {
        if (checked) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");

            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);

            if (eventTestDialogFragment == null)
                eventTestDialogFragment = TestDialogFragment.newInstance(DialogFragment.STYLE_NORMAL, 0);

//            eventTestDialogFragment.setCancelable(true);
//            eventTestDialogFragment.setMenuVisibility(true);
            eventTestDialogFragment.setUserVisibleHint(true);
            eventTestDialogFragment.setHasOptionsMenu(true);
            eventTestDialogFragment.setRetainInstance(true);
            eventTestDialogFragment.show(fragmentTransaction, "dialog");
        }
    }

    @Override
    public void onEventDropped(ScheduleCellView droppedCellView, ScheduleEventView eventView, DragEvent event) {
//        eventView.setTimeStart(droppedCellView.getTimeStart());
//        long endTimeMillis = droppedCellView.getTimeStart().getTimeInMillis() + eventView.getEvent().getDurationTime() * 60 * 1000;
//        Calendar endTimeCalendar = Calendar.getInstance();
//        endTimeCalendar.setTimeInMillis(endTimeMillis);
//        eventView.setTimeEnd(endTimeCalendar);
//        scheduleViewGroup.updateEvent(eventView.getEvent());
//        scheduleViewGroup.notifyDataSetChanged();
    }
}
