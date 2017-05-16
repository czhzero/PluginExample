package com.chen.easyplugin.utils;



/**
 * Created by chenzhaohua on 17/4/7.
 */
public class LogUtils {

    private static final int VERBOSE = android.util.Log.VERBOSE;
    private static final int DEBUG = android.util.Log.DEBUG;
    private static final int INFO = android.util.Log.INFO;
    private static final int WARN = android.util.Log.WARN;
    private static final int ERROR = android.util.Log.ERROR;
    private static final int ASSERT = android.util.Log.ASSERT;

    private static final String TAG = "czh";

    private static boolean sDebug = false;

    public static boolean isDebug() {
        return sDebug;
    }

    public static boolean isLoggable(int i) {
        return isDebug();
    }

    public static void v(String tag, String msg) {
        v(tag, "%s", null, msg);
    }

    public static void v(String tag, String format, Object... args) {
        v(tag, format, null, args);
    }

    public static void v(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(VERBOSE)) {
            return;
        }
        println(VERBOSE, tag, format, args, tr);
    }


    public static void d(String tag, String msg) {
        d(tag, "%s", null, msg);
    }

    public static void d(String tag, String format, Object... args) {
        d(tag, format, null, args);
    }

    public static void d(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(DEBUG)) {
            return;
        }
        println(DEBUG, tag, format, args, tr);
    }



    public static void i(String tag, String msg) {
        i(tag, "%s", null, msg);
    }

    public static void i(String tag, String format, Object... args) {
        i(tag, format, null, args);
    }

    public static void i(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(INFO)) {
            return;
        }
        println(INFO, tag, format, args, tr);
    }



    public static void w(String tag, String msg) {
        w(tag, "%s", null, msg);
    }

    public static void w(String tag, String format, Object... args) {
        w(tag, format, null, args);
    }

    public static void w(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(WARN)) {
            return;
        }
        println(WARN, tag, format, args, tr);
    }

    public static void w(String tag, Throwable tr) {
        w(tag, "Log.warn", tr);
    }


    public static void e(String tag, String msg) {
        e(tag, "%s", null, msg);
    }


    public static void e(String tag, String format, Object... args) {
        e(tag, format, null, args);
    }

    public static void e(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(ERROR)) {
            return;
        }
        println(ERROR, tag, format, args, tr);
    }

    public static void wtf(String tag, String format, Object... args) {
        wtf(tag, format, null, args);
    }

    public static void wtf(String tag, Throwable tr) {
        wtf(tag, "wtf", tr);
    }

    public static void wtf(String tag, String format, Throwable tr, Object... args) {
        if (!isLoggable(ASSERT)) {
            return;
        }
        println(ASSERT, tag, format, args, tr);
    }


    private static void println(final int level, final String tag, final String format, final Object[] args, final Throwable tr) {

//        logToFile(level, tag, format, args, tr);

        String message;

        if (args != null && args.length > 0) {
            message = String.format(format, args);
        } else {
            message = format;
        }

        if (tr != null) {
            message += android.util.Log.getStackTraceString(tr);
        }

        android.util.Log.println(level, tag, message);
    }


}
