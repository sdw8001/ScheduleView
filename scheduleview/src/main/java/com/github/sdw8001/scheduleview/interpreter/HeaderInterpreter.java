package com.github.sdw8001.scheduleview.interpreter;

import com.github.sdw8001.scheduleview.header.Header;

/**
 * Created by sdw80 on 2016-04-22.
 */
public interface HeaderInterpreter {
    String interpretHeaderColumn(Header header);

    String interpretTime(int hour);
}
