package com.chen.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import com.chen.plugin.core.PluginProcessManager;
import com.chen.plugin.helper.compat.ActivityThreadCompat;
import com.chen.plugin.pm.PluginCrashHandler;
import com.chen.plugin.pm.PluginManager;
import com.chen.plugin.pm.PluginPatchManager;
import com.chen.plugin.reflect.FieldUtils;
import com.chen.plugin.reflect.MethodUtils;
import com.chen.plugin.helper.Log;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by chenzhaohua on 17-7-21.
 * 插件初始化类
 */
public class PluginHelper implements ServiceConnection {

    private static final String TAG = PluginHelper.class.getSimpleName();

    private static PluginHelper sInstance = null;

    private PluginHelper() {
    }


    public static final PluginHelper getInstance() {
        if (sInstance == null) {
            sInstance = new PluginHelper();
        }
        return sInstance;
    }


    public void initPlugin(Context baseContext) {

        long b = System.currentTimeMillis();

        try {

            //start crash handler
            PluginCrashHandler.getInstance().register(baseContext);

            try {
                fixMiUiLbeSecurity();
            } catch (Throwable e) {
                Log.e(TAG, "fixMiUiLbeSecurity has error", e);
            }

            try {
                PluginPatchManager.getInstance().init(baseContext);
                PluginProcessManager.installHook(baseContext);
            } catch (Throwable e) {
                Log.e(TAG, "installHook has error", e);
            }

            try {
                if (PluginProcessManager.isPluginProcess(baseContext)) {
                    PluginProcessManager.setHookEnable(true);
                } else {
                    PluginProcessManager.setHookEnable(false);
                }
            } catch (Throwable e) {
                Log.e(TAG, "setHookEnable has error", e);
            }

            try {
                PluginManager.getInstance().addServiceConnection(getInstance());
                PluginManager.getInstance().init(baseContext);
            } catch (Throwable e) {
                Log.e(TAG, "installHook has error", e);
            }


        } finally {
            Log.i(TAG, "Init plugin in process cost %s ms", (System.currentTimeMillis() - b));
        }
    }

    //解决小米JLB22.0 4.1.1系统自带的小米安全中心（lbe.security.miui）广告拦截组件导致的插件白屏问题
    private void fixMiUiLbeSecurity() throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        //卸载掉LBE安全的ApplicationLoaders.mLoaders钩子
        Class ApplicationLoaders = Class.forName("android.app.ApplicationLoaders");
        Object applicationLoaders = MethodUtils.invokeStaticMethod(ApplicationLoaders, "getDefault");
        Object mLoaders = FieldUtils.readField(applicationLoaders, "mLoaders", true);
        if (mLoaders instanceof HashMap) {
            HashMap oldValue = ((HashMap) mLoaders);
            if ("com.lbe.security.client.ClientContainer$MonitoredLoaderMap".equals(mLoaders.getClass().getName())) {
                HashMap value = new HashMap();
                value.putAll(oldValue);
                FieldUtils.writeField(applicationLoaders, "mLoaders", value, true);
            }
        }

        //卸载掉LBE安全的ActivityThread.mPackages钩子
        Object currentActivityThread = ActivityThreadCompat.currentActivityThread();
        Object mPackages = FieldUtils.readField(currentActivityThread, "mPackages", true);
        if (mPackages instanceof HashMap) {
            HashMap oldValue = ((HashMap) mPackages);
            if ("com.lbe.security.client.ClientContainer$MonitoredPackageMap".equals(mPackages.getClass().getName())) {
                HashMap value = new HashMap();
                value.putAll(oldValue);
                FieldUtils.writeField(currentActivityThread, "mPackages", value, true);
            }
        }

        //当前已经处在主线程消息队列中的所有消息,找出lbe消息并remove之
        if (Looper.getMainLooper() == Looper.myLooper()) {
            final MessageQueue queue = Looper.myQueue();
            try {
                Object mMessages = FieldUtils.readField(queue, "mMessages", true);
                if (mMessages instanceof Message) {
                    findLbeMessageAndRemoveIt((Message) mMessages);
                }
                Log.e(TAG, "getMainLooper MessageQueue.IdleHandler:" + mMessages);
            } catch (Exception e) {
                Log.e(TAG, "fixMiUiLbeSecurity:error on remove lbe message", e);
            }
        }
    }

    //由于消息队列是一个单向链表，我们递归处理。
    //递归当前已经处在主线程消息队列中的所有消息,找出lbe消息并remove之
    private void findLbeMessageAndRemoveIt(Message message) {
        if (message == null) {
            return;
        }
        Runnable callback = message.getCallback();
        if (message.what == 0 && callback != null) {
            if (callback.getClass().getName().indexOf("com.lbe.security.client") >= 0) {
                message.getTarget().removeCallbacks(callback);
            }
        }

        try {
            Object nextObj = FieldUtils.readField(message, "next", true);
            if (nextObj != null) {
                Message next = (Message) nextObj;
                findLbeMessageAndRemoveIt(next);
            }
        } catch (Exception e) {
            Log.e(TAG, "findLbeMessageAndRemoveIt:error on remove lbe message", e);
        }

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        PluginProcessManager.setHookEnable(true, true);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }

}
