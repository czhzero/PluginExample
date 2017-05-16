package com.chen.easyplugin.hook.proxy;

import android.os.Handler;


import com.chen.easyplugin.hook.handle.ActivityThreadHandlerCallback;
import com.chen.easyplugin.hook.handle.IActivityManagerHandler;
import com.chen.easyplugin.hook.handle.IPackageManagerHandler;
import com.chen.easyplugin.pm.PluginManager;
import com.chen.easyplugin.utils.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by chenzhaohua on 17/4/7.
 */
public class FrameworkHookHelper {



    public static void hookActivityManagerNative() throws ClassNotFoundException,
            NoSuchFieldException, IllegalAccessException {
        Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");

        Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
        gDefaultField.setAccessible(true);

        Object gDefault = gDefaultField.get(null);

        // gDefault是一个 android.util.Singleton对象; 我们取出这个单例里面的字段
        Class<?> singleton = Class.forName("android.util.Singleton");
        Field mInstanceField = singleton.getDeclaredField("mInstance");
        mInstanceField.setAccessible(true);

        // ActivityManagerNative 的gDefault对象里面原始的 IActivityManager对象
        Object rawIActivityManager = mInstanceField.get(gDefault);

        // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活
        Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class<?>[]{iActivityManagerInterface}, new IActivityManagerHandler(rawIActivityManager));
        mInstanceField.set(gDefault, proxy);
    }


    /**
     * 替换ActivityThread的成员变量mH,替换Handler类型变量mH的mCallback对象
     *
     * @throws Exception
     */
    public static void hookActivityThreadHandler() throws Exception {
        // 先获取到当前的ActivityThread对象
        Object currentActivityThread = PluginManager.getInstance().getActivityThread();
        // 由于ActivityThread一个进程只有一个,我们获取这个对象的mH
        Handler mH = ReflectUtils.readField(currentActivityThread, "mH");
        ReflectUtils.writeField(mH, "mCallback", new ActivityThreadHandlerCallback(mH));
    }




    public static void hookPackageManager() throws Exception {

        // 这一步是因为 initializeJavaContextClassLoader 这个方法内部无意中检查了这个包是否在系统安装
        // 如果没有安装, 直接抛出异常, 这里需要临时Hook掉 PMS, 绕过这个检查.

        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        // 获取ActivityThread里面原始的 sPackageManager
        Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
        sPackageManagerField.setAccessible(true);
        Object sPackageManager = sPackageManagerField.get(currentActivityThread);

        // 准备好代理对象, 用来替换原始的对象
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                new Class<?>[]{iPackageManagerInterface},
                new IPackageManagerHandler(sPackageManager, null));

        // 1. 替换掉ActivityThread里面的 sPackageManager 字段
        sPackageManagerField.set(currentActivityThread, proxy);
    }

}
