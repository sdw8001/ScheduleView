package com.github.sdw8001.scheduleview.util;

import android.os.Build;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by sdw80 on 2016-05-17.
 */
public class IdGen {
    final static private AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    static private int _generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    static public int generateViewId() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return _generateViewId();
        } else {
            return View.generateViewId();
        }
    }
}

