package com.jkingone.utils;

import android.util.Log;

/**
 * Created by Administrator on 2017/6/10.
 */

public final class LogUtils {

    private static final String TAG = "JKMusic";

    private LogUtils() {
    }

    public static void d(String message) {
        log(Log.DEBUG, null, message);
    }

    public static void i(String message) {
        log(Log.INFO, null, message);
    }

    public static void w(String message) {
        log(Log.WARN, null, message);
    }

    public static void e(Throwable ex) {
        log(Log.ERROR, ex, null);
    }

    public static void e(String message) {
        log(Log.ERROR, null, message);
    }

    public static void e(Throwable ex, String message) {
        log(Log.ERROR, ex, message);
    }

    private static void log(int priority, Throwable ex, String message) {
        String log;
        if (ex == null) {
            log = message;
        } else {
            String logMessage = message == null ? ex.getMessage() : message;
            String logBody = Log.getStackTraceString(ex);
            log = logMessage + "\n" + logBody;
        }
        Log.println(priority, TAG, log);
    }
}
