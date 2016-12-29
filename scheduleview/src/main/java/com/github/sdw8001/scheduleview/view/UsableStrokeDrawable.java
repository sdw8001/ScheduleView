package com.github.sdw8001.scheduleview.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.support.annotation.ColorInt;

/**
 * Created by sdw80 on 2016-11-11.
 *
 */

public class UsableStrokeDrawable extends ShapeDrawable {
    private Paint fillPaint;
    private Paint strokePaint;
    private boolean useStroke = true;

    public UsableStrokeDrawable(Shape shape) {
        super(shape);
        fillPaint = this.getPaint();
        fillPaint.setAntiAlias(true);
        fillPaint.setShadowLayer(5, 0, 0, Color.GRAY);
        strokePaint = new Paint(fillPaint);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(5);
        strokePaint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Shape shape, Canvas canvas, Paint paint) {
        super.onDraw(shape, canvas, paint);
        shape.draw(canvas, fillPaint);
        if (useStroke)
            shape.draw(canvas, strokePaint);
    }

    public void setFillColor(@ColorInt int color) {
        this.fillPaint.setColor(color);
        invalidateSelf();
    }

    public void setStrokeColor(@ColorInt int color) {
        this.strokePaint.setColor(color);
        invalidateSelf();
    }

    public void setUseStroke(boolean useStroke) {
        this.useStroke = useStroke;
        invalidateSelf();
    }
}
