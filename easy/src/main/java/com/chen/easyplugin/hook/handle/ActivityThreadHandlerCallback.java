package com.chen.easyplugin.hook.handle;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;


import com.chen.easyplugin.core.Bundle;
import com.chen.easyplugin.core.PluginContextThemeWrapper;
import com.chen.easyplugin.core.PluginModule;
import com.chen.easyplugin.pm.PluginManager;
import com.chen.easyplugin.pm.PluginUtils;
import com.chen.easyplugin.utils.ReflectUtils;

import java.lang.reflect.Field;

/**
 * Created by chenzhaohua on 17/4/7.
 */
public class ActivityThreadHandlerCallback implements Handler.Callback {

    public static final int LAUNCH_ACTIVITY = 100;

    Handler mBase;

    public ActivityThreadHandlerCallback(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case LAUNCH_ACTIVITY:
                handleLaunchActivity(msg);
                return true; //直接退出，handleLaunchActivity负责处理
        }
        mBase.handleMessage(msg);
        return true;
    }

    private void handleLaunchActivity(Message msg) {
        try {
            Object obj = msg.obj; //ActivityClientRecord
            Intent stubIntent = ReflectUtils.readField(obj, "intent");
            PluginModule module = null;
            Intent bundleIntent = stubIntent.getParcelableExtra(PluginUtils.EXTRA_BUNDLE_INTENT);
            if (bundleIntent != null) {
                ComponentName componentName = bundleIntent.getComponent();
                String packageName = componentName.getPackageName();
                Bundle bundle = PluginManager.getInstance().getBundleByPackageName(packageName);
                if (bundle != null) {
                    //stubIntent.setComponent(componentName);
                    module = bundle.getBundleModule();

                    ClassLoader bundleClassLoader = bundle.getBundleClassLoader();
                    setIntentClassLoader(bundleIntent, bundleClassLoader);

                    ReflectUtils.writeField(obj, "intent", bundleIntent);

                    Field activityInfoField = obj.getClass().getDeclaredField("activityInfo");
                    activityInfoField.setAccessible(true);

                    // 根据 getPackageInfo 根据这个 包名获取 LoadedApk的信息; 因此这里我们需要手动填上, 从而能够命中缓存
                    ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(obj);

                    activityInfo.applicationInfo = module.getApplicationInfo();
                }
            }
            // 更换完args后转到原来的处理流程
            mBase.handleMessage(msg);
            //处理完成后，对此Activity的上下文进行
            Activity activity = ReflectUtils.readField(obj, "activity");
            Context base = ReflectUtils.readField(activity, "mBase");
            PluginContextThemeWrapper newBase = new PluginContextThemeWrapper(base, 0);
            if (module != null) {
                newBase.setBundleModule(module);
            }
            ReflectUtils.writeField(activity, "mBase", newBase);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setIntentClassLoader(Intent intent, ClassLoader classLoader) {
        try {
            android.os.Bundle mExtras = ReflectUtils.readField(intent, "mExtras");
            if (mExtras != null) {
                mExtras.setClassLoader(classLoader);
            } else {
                android.os.Bundle value = new android.os.Bundle();
                value.setClassLoader(classLoader);
                ReflectUtils.writeField(intent, "mExtras", value);
            }
        } catch (Exception e) {
        } finally {
            intent.setExtrasClassLoader(classLoader);
        }
    }
}
