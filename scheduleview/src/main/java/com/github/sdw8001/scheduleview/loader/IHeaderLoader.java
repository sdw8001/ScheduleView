package com.github.sdw8001.scheduleview.loader;

import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;

import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-11-28.
 *
 */

public interface IHeaderLoader {

    /**
     * Events 목록 Load
     * @return ScheduleEvent 목록
     */
    List<TreeNode<ScheduleHeader>> onLoad();
}
