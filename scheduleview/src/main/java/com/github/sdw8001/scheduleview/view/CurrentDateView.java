package com.github.sdw8001.scheduleview.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.Gravity;

import com.github.sdw8001.scheduleview.R;
import com.github.sdw8001.scheduleview.interpreter.TimeInterpreter;
import com.github.sdw8001.scheduleview.view.layout.CheckableLinearLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by sdw80 on 2017-01-13.
 * 오늘날짜 버튼 View
 */

public class CurrentDateView extends AppCompatButton {

    private Calendar currentDate;
    private TimeInterpreter timeInterpreter;
    @ColorInt
    private int backgroundColor = Color.LTGRAY;

    public CurrentDateView(Context context) {
        this(context, null);
    }

    public CurrentDateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();

        Drawable icon = getResources().getDrawable(R.drawable.ic_today_black_24dp);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        this.setText(getTimeInterpreter().interpretYearMonthDay(currentDate));
        this.setGravity(Gravity.CENTER);
        this.setCompoundDrawables(null, icon, null, null);
        this.setClickable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            this.setStateListAnimator(null);
    }

    private void initialize() {
        if (currentDate == null)
            currentDate = Calendar.getInstance();
    }

    public Calendar getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Calendar currentDate) {
        this.currentDate = currentDate;
        this.setText(getTimeInterpreter().interpretYearMonthDay(currentDate));
    }

    @Override
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        this.setBackground(CheckableLinearLayout.getCheckableStateDrawable(new ColorDrawable(color)));
    }

    @ColorInt
    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    @Override
    public float getElevation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return super.getElevation();
        } else {
            return ViewCompat.getElevation(this);
        }
    }

    @Override
    public void setElevation(float elevation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.setElevation(elevation);
        } else {
            ViewCompat.setElevation(this, elevation);
        }
    }

    public TimeInterpreter getTimeInterpreter() {
        if (timeInterpreter == null) {
            timeInterpreter = new TimeInterpreter() {
                @Override
                public String interpretYearMonthDay(Calendar calendar) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy" + System.getProperty("line.separator") + "MM/dd", Locale.getDefault());
                        return sdf.format(calendar.getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }

                @Override
                public String interpretTime(Calendar calendar) {
                    try {
                        SimpleDateFormat sdf = DateFormat.is24HourFormat(getContext()) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("a hh:mm", Locale.getDefault());
                        return sdf.format(calendar.getTime());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }
            };
        }
        return timeInterpreter;
    }

    public void setTimeInterpreter(TimeInterpreter timeInterpreter) {
        this.timeInterpreter = timeInterpreter;
        invalidate();
    }
}
