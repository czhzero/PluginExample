package com.chen.easyplugin.hook.proxy;

import android.content.Context;
import android.content.pm.PackageManager;

import com.chen.easyplugin.hook.BaseHookHandle;
import com.chen.easyplugin.reflect.FieldUtils;
import com.chen.easyplugin.reflect.Utils;
import com.chen.easyplugin.utils.MyProxy;
import com.chen.easyplugin.utils.compat.ActivityThreadCompat;

import java.util.List;

/**
 * Created by chenzhaohua on 17/5/16.
 *
 * 对ActivityThread类的IPackageManager类型的成员变量sPackageManager进行hook。
 * 对PackageManager类的IPackageManager类型的成员变量mPM进行hook。
 */
public class IPackageManagerHook extends ProxyHook {

    private static final String TAG = IPackageManagerHook.class.getSimpleName();

    public IPackageManagerHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return null;
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {

        Object currentActivityThread = ActivityThreadCompat.currentActivityThread();

        //保存旧的IPackageManager对象
        setOldObj(FieldUtils.readField(currentActivityThread, "sPackageManager"));

        Class<?> iPmClass = mOldObj.getClass();
        List<Class<?>> interfaces = Utils.getAllInterfaces(iPmClass);
        Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
        Object proxyIPackageManager = MyProxy.newProxyInstance(iPmClass.getClassLoader(), ifs, this);
        FieldUtils.writeField(currentActivityThread, "sPackageManager", proxyIPackageManager);

        PackageManager pm = mHostContext.getPackageManager();
        Object mPM = FieldUtils.readField(pm, "mPM");
        if (mPM != proxyIPackageManager) {
            FieldUtils.writeField(pm, "mPM", proxyIPackageManager);
        }
    }


    /**
     * 用ActivityThread类的sPackageManager替换PackageManager的mPM
     *
     * @param context
     */
    public static void fixContextPackageManager(Context context) {
        try {
            Object currentActivityThread = ActivityThreadCompat.currentActivityThread();
            Object newPm = FieldUtils.readField(currentActivityThread, "sPackageManager");
            PackageManager pm = context.getPackageManager();
            Object mPM = FieldUtils.readField(pm, "mPM");
            if (mPM != newPm) {
                FieldUtils.writeField(pm, "mPM", newPm);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
