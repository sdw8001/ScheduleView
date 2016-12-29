package com.github.sdw8001.scheduleview.loader;

import android.support.annotation.NonNull;

import com.github.sdw8001.scheduleview.header.ScheduleHeader;
import com.github.sdw8001.scheduleview.header.TreeNode;

import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-11-28.
 * Event Loader.
 */

public class HeaderLoader implements IHeaderLoader {

    private HeaderLoadListener mHeaderLoadListener;

    public HeaderLoader(@NonNull HeaderLoadListener headerLoadListener) {
        this.mHeaderLoadListener = headerLoadListener;
    }

    public HeaderLoadListener getHeaderLoadListener() {
        return mHeaderLoadListener;
    }

    public void setHeaderLoadListener(HeaderLoadListener headerLoadListener) {
        this.mHeaderLoadListener = headerLoadListener;
    }

    @Override
    public List<TreeNode<ScheduleHeader>> onLoad(Calendar calendar) {
        if (mHeaderLoadListener != null)
            return mHeaderLoadListener.onHeaderLoad(calendar);
        else
            return null;
    }

    public interface HeaderLoadListener {
        /**
         * ScheduleViewGroup 데이터 로드 Listener 입니다. onEventLoad() 를 통해 Events 를 Load 합니다.
         * @param dateCalendar Load 에 사용될 선택된 Calendar
         * @return ScheduleHeader 목록을 반환합니다.
         */
        List<TreeNode<ScheduleHeader>> onHeaderLoad(Calendar dateCalendar);
    }
}
