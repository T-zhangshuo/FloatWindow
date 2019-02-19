package com.yhao.floatwindow.utils;

import android.util.Log;

public class LogUtils {
    private static final String LOG_TAG = "DarkAx";

    public static void e(String msg) {
        Log.e(LOG_TAG, msg);
    }

    public static void d(String msg) {
        Log.d(LOG_TAG, msg);
    }

    public static void d(String msg, Throwable throwable) {
        Log.d(LOG_TAG, msg);
    }

    public static void i(String msg) {
        Log.i(LOG_TAG, msg);
    }
}