package com.github.sdw8001.scheduleview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.annotation.IntDef;
import android.widget.CheckedTextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sdw80 on 2016-11-10.
 *
 */

public class AppointmentView extends CheckedTextView {

    @Retention(RetentionPolicy.RUNTIME)
    @IntDef(flag = true, value = {OVAL, RECT, ROUND_RECT})
    public @interface ShapeViewMode {

    }
    public static final int OVAL = 1;
    public static final int RECT = 1 << 2;
    public static final int ROUND_RECT = 1 << 3;

    @ShapeViewMode
    int shapeViewMode = AppointmentView.ROUND_RECT;
    private int selectionColor = Color.GRAY;
    private Drawable customBackground;
    private Drawable selectionDrawable;

    private final Rect tempRect = new Rect();
    private final int fadeTime;

    public AppointmentView(Context context) {
        super(context);
        this.setClickable(true);
        fadeTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        setSelectionColor(this.selectionColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        }
    }

    public void setSelectionColor(int color) {
        this.selectionColor = color;
        regenerateBackground();
    }

    public void setSelectionDrawable(Drawable drawable) {
        if (drawable == null) {
            this.selectionDrawable = null;
        } else {
            this.selectionDrawable = drawable.getConstantState().newDrawable(getResources());
        }
        regenerateBackground();
    }

    public void setCustomBackground(Drawable drawable) {
        if (drawable == null) {
            this.customBackground = null;
        } else {
            this.customBackground = drawable.getConstantState().newDrawable(getResources());
        }
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        calculateBounds(right - left, bottom - top);
        regenerateBackground();
    }

    private void calculateBounds(int width, int height) {
        final int radius = Math.min(height, width);
        // Lollipop platform bug. Rect offset needs to be divided by 4 instead of 2
        final int offsetDivisor = Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP ? 4 : 2;
        final int offset = Math.abs(height - width) / offsetDivisor;

        if (width >= height) {
            tempRect.set(offset, 0, radius + offset, height);
        } else {
            tempRect.set(0, offset, width, radius + offset);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // customBackground 그리기
        if (customBackground != null) {
            customBackground.setBounds(tempRect);
            customBackground.setState(getDrawableState());
            customBackground.draw(canvas);
        }
        super.onDraw(canvas);
    }

    private void regenerateBackground() {
        if (selectionDrawable != null) {
            setBackgroundDrawable(selectionDrawable);
        } else {
            setBackgroundDrawable(generateBackground(selectionColor, fadeTime, tempRect));
        }
    }

    private Drawable generateBackground(int color, int fadeTime, Rect bounds) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.setExitFadeDuration(fadeTime);

        // checked
        drawable.addState(new int[]{android.R.attr.state_checked}, generateOptionalDrawable(color, true));

        // pressed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable.addState(new int[]{android.R.attr.state_pressed}, generateRippleDrawable(color, bounds));
        } else {
            drawable.addState(new int[]{android.R.attr.state_pressed}, generateOptionalDrawable(color, false));
        }

        // unchecked
        drawable.addState(new int[]{}, generateOptionalDrawable(color, false));

        return drawable;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable generateRippleDrawable(final int color, Rect bounds) {
        ColorStateList list = ColorStateList.valueOf(color);
        Drawable mask = generateOptionalDrawable(Color.WHITE, false);
        RippleDrawable rippleDrawable = new RippleDrawable(list, null, mask);
//        API 21
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            rippleDrawable.setBounds(bounds);
        }

//        API 22. Technically harmless to leave on for API 21 and 23, but not worth risking for 23+
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
            int center = (bounds.left + bounds.right) / 2;
            rippleDrawable.setHotspotBounds(center, bounds.top, center, bounds.bottom);
        }

        return rippleDrawable;
    }

    private Drawable generateOptionalDrawable(final int color, boolean useStroke) {
        return generateOptionalDrawable(color, shapeViewMode, useStroke);
    }

    private Drawable generateOptionalDrawable(final int color, @ShapeViewMode final int shapeViewMode, boolean useStroke) {
        switch (shapeViewMode) {
            case OVAL:
                return generateCircleDrawable(color, useStroke);
            case RECT:
                return generateRectDrawable(color, useStroke);
            case ROUND_RECT:
                return generateRoundRectDrawable(color, useStroke);
        }
        return null;
    }

    private static Drawable generateRoundRectDrawable(final int color, boolean useStroke) {
        float[] outerRadius = new float[] { 6, 6, 6, 6, 6, 6, 6, 6 };
        UsableStrokeDrawable drawable = new UsableStrokeDrawable(new RoundRectShape(outerRadius, null, null));
        drawable.setFillColor(color);
        drawable.setUseStroke(useStroke);
        return drawable;
    }

    private static Drawable generateRectDrawable(final int color, boolean useStrokeBorder) {
        UsableStrokeDrawable drawable = new UsableStrokeDrawable(new RectShape());
        drawable.setFillColor(color);
        drawable.setUseStroke(useStrokeBorder);
        return drawable;
    }

    private static Drawable generateCircleDrawable(final int color, boolean useStrokeBorder) {
        UsableStrokeDrawable drawable = new UsableStrokeDrawable(new OvalShape());
        drawable.setFillColor(color);
        drawable.setUseStroke(useStrokeBorder);
        return drawable;
    }
}
