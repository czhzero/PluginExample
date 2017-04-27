package com.chen.easyplugin.utils;


import android.util.Log;



/**
 * Created by chenzhaohua on 17/4/7.
 */
public class LogUtils {

    private static final String TAG = "czh";

    public static void i(String tag, String msg) {
        Log.i(TAG, tag + " " + msg);
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + " " + msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + " " + msg);
    }

}
