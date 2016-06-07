package com.github.sdw8001.sample;

import com.github.sdw8001.scheduleview.header.GroupHeader;
import com.github.sdw8001.scheduleview.header.Header;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by sdw80 on 2016-04-25.
 */
public class DoctorHeader extends GroupHeader {
    private String doctorId;
    private String seq;
    private String doctorName;

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
        this.setHeaderKey(doctorId);
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
        this.setHeaderName(doctorName);
    }

    public DoctorHeader(Calendar calendar, String doctorId, String doctorName, String seq) {
        super(calendar, null, null, doctorName, doctorId, new ArrayList<Header>());
        this.setCalendar(calendar);
        this.setDoctorId(doctorId);
        this.setDoctorName(doctorName);
        this.setSeq(seq);
    }

    public GroupHeader getGroupHeader(){
        return this;
    }
}
