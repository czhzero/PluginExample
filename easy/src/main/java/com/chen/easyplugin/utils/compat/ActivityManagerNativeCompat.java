package com.chen.easyplugin.utils.compat;

import com.chen.easyplugin.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by chenzhaohua on 17/5/16.
 */
public class ActivityManagerNativeCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException {
        if (sClass == null) {
            sClass = Class.forName("android.app.ActivityManagerNative");
        }
        return sClass;
    }

    public static Object getDefault() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return MethodUtils.invokeStaticMethod(Class(), "getDefault");
    }

}
