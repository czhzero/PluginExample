package com.chen.plugin.hook.proxy;

import android.content.Context;
import android.util.AndroidRuntimeException;

import com.chen.plugin.hook.BaseHookHandle;
import com.chen.easyplugin.reflect.FieldUtils;
import com.chen.easyplugin.reflect.Utils;
import com.chen.easyplugin.utils.LogUtils;
import com.chen.plugin.hook.handle.IActivityManagerHookHandle;
import com.chen.plugin.utils.MyProxy;
import com.chen.plugin.utils.compat.ActivityManagerNativeCompat;
import com.chen.plugin.utils.compat.IActivityManagerCompat;
import com.chen.plugin.utils.compat.SingletonCompat;

import java.util.List;

/**
 * Created by chenzhaohua on 17/5/16.
 *
 * 对ActivityManagerNative类的成员对象gDefault进行hook。
 *
 * 4.0.3之前，gDefault直接为IActivityManager类型，
 * 之后，gDefault为Singleton类型
 *
 */
public class IActivityManagerHook extends ProxyHook {

    private static final String TAG = IActivityManagerHook.class.getSimpleName();

    public IActivityManagerHook(Context hostContext) {
        super(hostContext);
    }

    @Override
    protected BaseHookHandle createHookHandle() {
        return new IActivityManagerHookHandle(mHostContext);
    }

    @Override
    protected void onInstall(ClassLoader classLoader) throws Throwable {

        Class cls = ActivityManagerNativeCompat.Class();
        Object obj = FieldUtils.readStaticField(cls, "gDefault");

        if (obj == null) {
            ActivityManagerNativeCompat.getDefault();
            obj = FieldUtils.readStaticField(cls, "gDefault");
        }

        //Android 4.0.3 版本之前, gDefault 是 IActivityManager 对象， 之后是Singleton对象
        if (IActivityManagerCompat.isIActivityManager(obj)) {

            //保存hook前的IActivityManager对象
            setOldObj(obj);

            List<Class<?>> interfaces = Utils.getAllInterfaces(mOldObj.getClass());
            Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
            Object proxyIActivityManager = MyProxy.newProxyInstance(mOldObj.getClass().getClassLoader(), ifs, this);

            //替换掉IActivityManager对象
            FieldUtils.writeStaticField(cls, "gDefault", proxyIActivityManager);

            LogUtils.i(TAG, "Install ActivityManager Hook 1 old=%s,new=%s", mOldObj, proxyIActivityManager);

        } else if (SingletonCompat.isSingleton(obj)) {

            Object iActivityManagerObject = FieldUtils.readField(obj, "mInstance");

            if (iActivityManagerObject == null) {
                SingletonCompat.get(obj);
                iActivityManagerObject = FieldUtils.readField(obj, "mInstance");
            }

            //保存hook前的IActivityManager对象
            setOldObj(iActivityManagerObject);

            List<Class<?>> interfaces = Utils.getAllInterfaces(mOldObj.getClass());
            Class[] ifs = interfaces != null && interfaces.size() > 0 ? interfaces.toArray(new Class[interfaces.size()]) : new Class[0];
            Object proxyIActivityManager = MyProxy.newProxyInstance(mOldObj.getClass().getClassLoader(), ifs, this);

            //替换掉IActivityManager对象
            FieldUtils.writeField(obj, "mInstance", proxyIActivityManager);

            LogUtils.i(TAG, "Install ActivityManager Hook 2 old=%s,new=%s", mOldObj.toString(), proxyIActivityManager);


//            //Singleton已经改为hide类，不能再用这种方式进行hook
//            FieldUtils.writeStaticField(cls, "gDefault", new android.util.Singleton<Object>() {
//                @Override
//                protected Object create() {
//                    LogUtils.e(TAG, "Install ActivityManager 3 Hook  old=%s,new=%s", mOldObj, proxyIActivityManager);
//                    return proxyIActivityManager;
//                }
//            });

        } else {
            throw new AndroidRuntimeException("Can not install IActivityManagerNative hook");
        }
    }
}
