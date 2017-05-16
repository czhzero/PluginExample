package com.chen.easyplugin.utils.compat;

/**
 * Created by chenzhaohua on 17/5/16.
 */
public class IActivityManagerCompat {

    private static Class sClass;

    public static Class Class() throws ClassNotFoundException {
        if (sClass == null) {
            sClass = Class.forName("android.app.IActivityManager");
        }
        return sClass;
    }

    public static boolean isIActivityManager(Object obj) {
        if (obj == null) {
            return false;
        } else {
            try {
                Class clazz = Class();
                return clazz.isInstance(obj);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
    }
}
