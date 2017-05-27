package com.exoticdevs.helper;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * Created by bassant on 5/27/17.
 */

public class DeviceUtils {
    /**
     * get screen height
     *
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics outMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(outMetrics);
        int screenH = outMetrics.heightPixels;

        return screenH;
    }
}
