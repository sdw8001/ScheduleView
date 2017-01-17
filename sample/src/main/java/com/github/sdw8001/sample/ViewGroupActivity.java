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
import android.view.View;

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

public class ViewGroupActivity extends AppCompatActivity implements HeaderLoader.HeaderLoadListener, EventLoader.EventLoadListener, ScheduleViewGroup.OnCellCheckedChangeListener, ScheduleViewGroup.OnEventCheckedChangeListener, ScheduleViewGroup.DateCalendarListener, DatePickerDialog.OnDateSetListener {
    ScheduleViewGroup scheduleViewGroup;
    ArrayList<ScheduleHeader> headers;
    ArrayList<ScheduleEvent> events;
    TestDialogFragment cellTestDialogFragment;
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
        findViewById(R.id.ButtonEventLoad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleViewGroup.loadEvent(Calendar.getInstance());
            }
        });
        findViewById(R.id.ButtonEventRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleViewGroup.removeEventViews();
            }
        });

        scheduleViewGroup.setHeaderLoadListener(this);
        scheduleViewGroup.setEventLoadListener(this);
        scheduleViewGroup.setOnCellCheckedChangeListener(this);
        scheduleViewGroup.setOnEventCheckedChangeListener(this);
        scheduleViewGroup.setDateCalendarListener(this);
        scheduleViewGroup.loadHeader();
        scheduleViewGroup.loadEvent(Calendar.getInstance());
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
    }

    @Override
    public List<TreeNode<ScheduleHeader>> onHeaderLoad() {
        List<TreeNode<ScheduleHeader>> treeNodes = new ArrayList<>();
        TreeNode<ScheduleHeader> treeNode;
        headers = new ArrayList<>();

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

        return treeNodes;
    }

    @Override
    public List<? extends ScheduleEvent> onEventLoad(Calendar dateCalendar) {
        events = new ArrayList<>();
        ScheduleEvent event;

        event = new ScheduleEvent();
        dateCalendar.set(Calendar.HOUR_OF_DAY, 10);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 11);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(60);
        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "1"));
        event.setContents("Test0");
        event.setBackgroundColor(-32640);
        event.setTypeColor(Color.RED);
        event.setTypeDetail("s");
        event.setTypeDetailForeColor(-16777216);
        event.setTypeDetailBackColor(-65536);
        events.add(event);

        event = new ScheduleEvent();
        dateCalendar.set(Calendar.HOUR_OF_DAY, 11);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 13);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(105);
        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "1"));
        event.setContents("Test1");
        event.setBackgroundColor(-256);
        event.setTypeColor(Color.BLUE);
        event.setTypeDetail("V");
        event.setTypeDetailForeColor(-12566464);
        event.setTypeDetailBackColor(-16711936);
        events.add(event);

        event = new ScheduleEvent();
        dateCalendar.set(Calendar.HOUR_OF_DAY, 10);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 13);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(45);
        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "2"));
        event.setContents("Test2");
        event.setBackgroundColor(Color.GRAY);
        event.setTypeColor(Color.WHITE);
        event.setTypeDetail("I");
        event.setTypeDetailForeColor(-2039584);
        event.setTypeDetailBackColor(-16777216);
        events.add(event);

        event = new ScheduleEvent();
        dateCalendar.set(Calendar.HOUR_OF_DAY, 9);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 15);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(45);
        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000001", "1"));
        event.setContents("Test3");
        event.setBackgroundColor(Color.WHITE);
        event.setTypeColor(Color.GRAY);
        event.setTypeDetail("G");
        event.setTypeDetailForeColor(-12566464);
        event.setTypeDetailBackColor(-16711936);
        events.add(event);

        event = new ScheduleEvent();
        dateCalendar.set(Calendar.HOUR_OF_DAY, 12);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 15);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(30);
        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "2"));
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
    public void onCellCheckedChanged(ScheduleViewGroup scheduleViewGroup, ScheduleCellView checkedScheduleCellView, boolean checked) {
        if (checked) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");

            if (prev != null) {
                fragmentTransaction.remove(prev);
            }
            fragmentTransaction.addToBackStack(null);

            if (cellTestDialogFragment == null)
                cellTestDialogFragment = TestDialogFragment.newInstance(DialogFragment.STYLE_NORMAL, 0);

            cellTestDialogFragment.setCancelable(true);
            cellTestDialogFragment.setMenuVisibility(true);
            cellTestDialogFragment.setUserVisibleHint(true);
            cellTestDialogFragment.setHasOptionsMenu(true);
            cellTestDialogFragment.setRetainInstance(true);
            cellTestDialogFragment.show(fragmentTransaction, "dialog");
        }
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
}
