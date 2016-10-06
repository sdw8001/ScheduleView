package com.github.sdw8001.sample;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;

import com.github.sdw8001.scheduleview.event.ScheduleViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sdw80 on 2016-04-27.
 */
public class AppointmentEvent {
    /**
     * Appointment Primary Key : "2016042770302039001"
     */
    private String seq;
    /**
     * Appointment Time : "13:00"
     */
    private String appTime;
    /**
     * Appointment Date : "20160427"
     */
    private String date;
    /**
     * Appointment Patient ID : "70302039"
     */
    private String id;
    /**
     * Appointment Name (예약 Text 에 사용) : "덴탑/19830409009"
     */
    private String name;
    /**
     * Appointment DoctorId : "00000023"
     */
    private String doctorId;
    /**
     * 예약 의사의 Order 순서 : 3
     */
    private int doctorOrderSeq;
    /**
     * 예약 분단위 기간 : 90
     */
    private int duration;
    /**
     * To Do : ""
     */
    private String todo;
    /**
     * SetDoctorChair 의 Seq 번호 : 4
     */
    private String slot;
    /**
     * SetDoctorChair 의 Seq 에 해당되는 SlotName(ChairName 과 동일한 의미) : "1"
     */
    private String slotName;
    /**
     * Appointment Patient 의 고객구분 색상(Appointment Event 의 BackgroundColor 값으로 쓰이지만 AppColor 의 값보다 우선순위가 낮다) : -256
     * SpecialType 코드값이 0(예약) 일 경우 White Color,
     * SpecialType 코드값이 9(접수) 일 경우 Gray Color,
     */
    private int specialType;
    private int cnt;
    /**
     * Patient Chart Id : "19830409009"
     */
    private String chartId;
    /**
     * Patient Name : "덴탑"
     */
    private String name2;
    /**
     * Tel No.
     */
    private String telNo;
    /**
     * CellPhone No.
     */
    private String cellPhone;
    /**
     *
     */
    // TODO : CrmGubun 정의는 일단 보류.
    private String crmGubun;
    /**
     *
     */
    // TODO : Nurse 정의는 일단 보류.
    private String nurse;
    /**
     * 스케쥴러 표시할때 합쳐진 스케쥴 칸 수 : 3
     */
    private int mergeCnt;
    /**
     * 진료종류 :
     */
    private String detailAppType;
    /**
     *
     */
    // TODO: appType 정의는 일단 보류, 잘 못찾겠음 아직. 급한부분은 아닌듯하여 다음으로
    private String appType;
    private String changeType;
    private String treatmentKind;
    private String nonePatientPhone;
    private boolean smsSendYn;
    private int disSeq2;
    /**
     * SetDoctorChair 의 Seq 에 해당되는 SlotName(ChairName 과 동일한 의미) : "1"
     */
    private String chairName;
    private int appSeq;
    private boolean isReceipted;
    private int toothStatus;
    private int toothStatus2;
    private String keyValue;
    /**
     * 이행여부 Flag.
     * N : 예약미이행 없음
     * C : 예약미이행 C/A (예약 Cell 배경에 C/A 표시)
     * B : 예약미이행 B/A (예약 Cell 배경에 B/A 표시)
     */
    private String executeFlag;
    private boolean autoSmsSend;
    private boolean gSmsSend;
    /**
     * Appointment Background Color 값(SpetialType 값보다 우선순위가 높다)
     * 예약 시 "Color 선택" 값과 예약 상태에 따른 코드 값에 매핑되는 Color 를 담는다.
     * Appointment Background Color 를 설정할 때,
     * 우선적으로 이 값의 코드와 Color 설정 표에서 매핑되는 Color 를 찾는다.
     *
     * 2016-04-27 일 기준,
     * SpecialType 과 AppColor 모두 Appointment Cell 의 Background 값 설정에 사용되는데 우선순위는 아래와 같다.
     * 1순위 : SpecialType 이 0(예약) 인 경우 와 9(접수) 인 경우
     * 2순위 : AppColor 의 Code 와 매핑되는 컬러가 있는 경우
     * 3순위 : SpecialType 의 Color 값 (고객구분 설정 색상)
     */
    private int appColor;
    private int etcCnt;
    private int disSeq;

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getAppTime() {
        return appTime;
    }

    public void setAppTime(String appTime) {
        this.appTime = appTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public int getDoctorOrderSeq() {
        return doctorOrderSeq;
    }

    public void setDoctorOrderSeq(int doctorOrderSeq) {
        this.doctorOrderSeq = doctorOrderSeq;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public int getSpecialType() {
        return specialType;
    }

    public void setSpecialType(int specialType) {
        this.specialType = specialType;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public String getChartId() {
        return chartId;
    }

    public void setChartId(String chartId) {
        this.chartId = chartId;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getCellPhone() {
        return cellPhone;
    }

    public void setCellPhone(String cellPhone) {
        this.cellPhone = cellPhone;
    }

    public String getCrmGubun() {
        return crmGubun;
    }

    public void setCrmGubun(String crmGubun) {
        this.crmGubun = crmGubun;
    }

    public String getNurse() {
        return nurse;
    }

    public void setNurse(String nurse) {
        this.nurse = nurse;
    }

    public int getMergeCnt() {
        return mergeCnt;
    }

    public void setMergeCnt(int mergeCnt) {
        this.mergeCnt = mergeCnt;
    }

    public String getDetailAppType() {
        return detailAppType;
    }

    public void setDetailAppType(String detailAppType) {
        this.detailAppType = detailAppType;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getTreatmentKind() {
        return treatmentKind;
    }

    public void setTreatmentKind(String treatmentKind) {
        this.treatmentKind = treatmentKind;
    }

    public String getNonePatientPhone() {
        return nonePatientPhone;
    }

    public void setNonePatientPhone(String nonePatientPhone) {
        this.nonePatientPhone = nonePatientPhone;
    }

    public boolean isSmsSendYn() {
        return smsSendYn;
    }

    public void setSmsSendYn(boolean smsSendYn) {
        this.smsSendYn = smsSendYn;
    }

    public int getDisSeq2() {
        return disSeq2;
    }

    public void setDisSeq2(int disSeq2) {
        this.disSeq2 = disSeq2;
    }

    public String getChairName() {
        return chairName;
    }

    public void setChairName(String chairName) {
        this.chairName = chairName;
    }

    public int getAppSeq() {
        return appSeq;
    }

    public void setAppSeq(int appSeq) {
        this.appSeq = appSeq;
    }

    public boolean isReceipted() {
        return isReceipted;
    }

    public void setIsReceipted(boolean isReceipted) {
        this.isReceipted = isReceipted;
    }

    public int getToothStatus() {
        return toothStatus;
    }

    public void setToothStatus(int toothStatus) {
        this.toothStatus = toothStatus;
    }

    public int getToothStatus2() {
        return toothStatus2;
    }

    public void setToothStatus2(int toothStatus2) {
        this.toothStatus2 = toothStatus2;
    }

    public String getKeyValue() {
        return keyValue;
    }

    public void setKeyValue(String keyValue) {
        this.keyValue = keyValue;
    }

    public String getExecuteFlag() {
        return executeFlag;
    }

    public void setExecuteFlag(String executeFlag) {
        this.executeFlag = executeFlag;
    }

    public boolean isAutoSmsSend() {
        return autoSmsSend;
    }

    public void setAutoSmsSend(boolean autoSmsSend) {
        this.autoSmsSend = autoSmsSend;
    }

    public boolean isGSmsSend() {
        return gSmsSend;
    }

    public void setGSmsSend(boolean gSmsSend) {
        this.gSmsSend = gSmsSend;
    }

    public int getAppColor() {
        return appColor;
    }

    public void setAppColor(int appColor) {
        this.appColor = appColor;
    }

    public int getEtcCnt() {
        return etcCnt;
    }

    public void setEtcCnt(int etcCnt) {
        this.etcCnt = etcCnt;
    }

    public int getDisSeq() {
        return disSeq;
    }

    public void setDisSeq(int disSeq) {
        this.disSeq = disSeq;
    }


    private List<TypeDetailData> mTypeDetailDataList;

    public ScheduleViewEvent toScheduleViewEvent(){
        mTypeDetailDataList = new ArrayList<>();
        TypeDetailData typeDetailData = new TypeDetailData();
        typeDetailData.setCode("001");
        typeDetailData.setTypeDetail("s");
        typeDetailData.setTypeDetailForeColor(-16777216);
        typeDetailData.setTypeDetailBackColor(-65536);
        mTypeDetailDataList.add(typeDetailData);

        typeDetailData = new TypeDetailData();
        typeDetailData.setCode("002");
        typeDetailData.setTypeDetail("V");
        typeDetailData.setTypeDetailForeColor(-12566464);
        typeDetailData.setTypeDetailBackColor(-16711936);
        mTypeDetailDataList.add(typeDetailData);

        typeDetailData = new TypeDetailData();
        typeDetailData.setCode("003");
        typeDetailData.setTypeDetail("I");
        typeDetailData.setTypeDetailForeColor(-2039584);
        typeDetailData.setTypeDetailBackColor(-16777216);
        mTypeDetailDataList.add(typeDetailData);

        Calendar startTime, endTime;
        ScheduleViewEvent event;

        event = new ScheduleViewEvent();
        startTime = (Calendar) Calendar.getInstance().clone();
        startTime.set(Calendar.YEAR, Integer.valueOf(this.date.substring(0, 4)));
        startTime.set(Calendar.MONTH, Integer.valueOf(this.date.substring(4, 6)));
        startTime.set(Calendar.DAY_OF_MONTH, Integer.valueOf(this.date.substring(6, 8)));
        startTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(this.appTime.substring(0, 2)));
        startTime.set(Calendar.MINUTE, Integer.valueOf(this.appTime.substring(3, 5)));

        endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.MINUTE, this.duration);

        event.setKey(this.getSeq());
        event.setHeaderKey(this.slot);
        event.setParentHeaderKey(this.doctorId);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setSplitUsing(ScheduleViewEvent.SPLIT_USING_KEY);
        event.setBackgroundColor(this.appColor);
        int typeColor;
        switch (cnt) {
            case 4:
                typeColor = Color.RED;
                break;
            case 2:
                typeColor = Color.rgb(0, 160, 0);//Green
                break;
            case 3:
                typeColor = Color.BLUE;
                break;
            default:
                typeColor = this.appColor;
                break;

        }
        event.setTypeColor(typeColor);
        String typeDetail = "G";
        int typeForeColor = this.appColor;
        int typeBackColor = this.appColor;
        switch (this.detailAppType) {
            case "0":
                typeDetail = "G";
                typeForeColor = Color.parseColor("#9C27B0"); // Purple 500
                break;
            case "1":
                typeDetail = "S";
                typeForeColor = Color.parseColor("#4CAF50"); // Green 500
                break;
            case "2":
                typeDetail = "R";
                typeForeColor = Color.parseColor("#FF9800"); // Orange 500
                break;
            case "3":
                typeDetail = "N";
                typeForeColor = Color.parseColor("#1976D2"); // Blue 700
                break;
            default:
                for(TypeDetailData data : mTypeDetailDataList) {
                    if (data.getCode().equals(this.detailAppType)) {
                        typeDetail = data.getTypeDetail();
                        typeForeColor = data.getTypeDetailForeColor();
                        typeBackColor = data.getTypeDetailBackColor();
                        break;
                    }
                }
        }
        event.setTypeDetail(typeDetail);
        event.setTypeDetailForeColor(typeForeColor);
        event.setTypeDetailBackColor(typeBackColor);

        return event;
    }
}
