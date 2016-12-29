package com.github.sdw8001.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.github.sdw8001.scheduleview.event.ScheduleEvent;
import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;
import com.github.sdw8001.scheduleview.loader.EventLoader;
import com.github.sdw8001.scheduleview.loader.HeaderLoader;
import com.github.sdw8001.scheduleview.view.ScheduleViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ViewGroupActivity extends AppCompatActivity implements HeaderLoader.HeaderLoadListener, EventLoader.EventLoadListener {
    ScheduleViewGroup scheduleViewGroup;
    ArrayList<ScheduleHeader> headers;
    ArrayList<ScheduleEvent> events;

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
                scheduleViewGroup.setColor(Color.GREEN);
            }
        });
        findViewById(R.id.ButtonColorRed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleViewGroup.setColor(Color.RED);
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
        scheduleViewGroup.loadHeader(Calendar.getInstance());
        scheduleViewGroup.loadEvent(Calendar.getInstance());
    }

    @Override
    public List<TreeNode<ScheduleHeader>> onHeaderLoad(Calendar dateCalendar) {
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

        dateCalendar.set(Calendar.YEAR, 2016);
        dateCalendar.set(Calendar.MONTH, 10);
        dateCalendar.set(Calendar.DAY_OF_MONTH, 28);

        event = new ScheduleEvent();
        dateCalendar.set(Calendar.HOUR_OF_DAY, 9);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 10);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(60);
        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "1"));
        event.setContents("Test0");
        events.add(event);

        event = new ScheduleEvent();
        dateCalendar.set(Calendar.HOUR_OF_DAY, 11);
        dateCalendar.set(Calendar.MINUTE, 15);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 13);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(105);
        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "1"));
        event.setContents("Test1");
        events.add(event);

        event = new ScheduleEvent();
        dateCalendar.set(Calendar.HOUR_OF_DAY, 9);
        dateCalendar.set(Calendar.MINUTE, 15);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 10);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(45);
        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "2"));
        event.setContents("Test2");
        events.add(event);

        event = new ScheduleEvent();
        dateCalendar.set(Calendar.HOUR_OF_DAY, 9);
        dateCalendar.set(Calendar.MINUTE, 15);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 10);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(45);
        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000001", "1"));
        event.setContents("Test3");
        events.add(event);

        event = new ScheduleEvent();
        dateCalendar.set(Calendar.HOUR_OF_DAY, 9);
        dateCalendar.set(Calendar.MINUTE, 0);
        event.setStartTime((Calendar)dateCalendar.clone());
        dateCalendar.set(Calendar.HOUR_OF_DAY, 9);
        dateCalendar.set(Calendar.MINUTE, 30);
        event.setEndTime((Calendar)dateCalendar.clone());
        event.setDurationTime(30);
//        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "3"));
        event.setHeaderNode(scheduleViewGroup.getHeaderNode("00000235", "2"));
        event.setContents("Test4");
        events.add(event);
        return events;
    }
}
