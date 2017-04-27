package com.chen.easyplugin.stub;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;

import com.chen.easyplugin.core.Bundle;
import com.chen.easyplugin.core.PluginContextWrapper;
import com.chen.easyplugin.core.PluginModule;
import com.chen.easyplugin.pm.PluginManager;
import com.chen.easyplugin.pm.PluginUtils;
import com.chen.easyplugin.utils.LogUtils;
import com.chen.easyplugin.utils.ReflectUtils;


/**
 * Created by chenzhaohua on 17/4/7.
 */
public class BundleServiceContainer extends Service {

    /**
     * 注册5个代理容器给Bundle Service使用
     * 如果你的项目需要更多的service，可以自己添加，并修改BundleManager中的设置的最大Service数量的值
     */
    public static class Proxy1 extends BundleServiceContainer {
    }

    public static class Proxy2 extends BundleServiceContainer {
    }

    public static class Proxy3 extends BundleServiceContainer {
    }

    public static class Proxy4 extends BundleServiceContainer {
    }

    public static class Proxy5 extends BundleServiceContainer {
    }

    private Service service;
    private String className; //真正要运行的Bundle的service
    private PluginManager pluginManager;

    @Override
    public IBinder onBind(Intent intent) {
        if (service == null) {
            loadBundleService(intent);
        }
        if (service != null) {
            return service.onBind(getBundleIntent(intent));
        }
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        if (service != null) {
            service.onRebind(getBundleIntent(intent));
        }
        super.onRebind(intent);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        if (service != null) {
            service.unbindService(conn);
            return;
        }
        super.unbindService(conn);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pluginManager = PluginManager.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (service == null) {
            loadBundleService(intent);
        }
        if (service != null) {
            return service.onStartCommand(getBundleIntent(intent), flags, startId);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        if (service != null) {
            LogUtils.i("BundleServiceContainer", "onStart [" + className + "]");
            service.onStart(getBundleIntent(intent), startId);
            return;
        }
        super.onStart(intent, startId);
    }

    @Override
    public void onLowMemory() {
        if (service != null) {
            service.onLowMemory();
        }
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        if (service != null) {
            service.onTrimMemory(level);
        }
        super.onTrimMemory(level);
    }

    @Override
    public void onDestroy() {
        if (service != null) {
            LogUtils.i("BundleServiceContainer", "onDestroy [" + className + "]");
            pluginManager.removeProxyService(className);
            service.onDestroy();
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (service != null) {
            service.onConfigurationChanged(newConfig);
        }
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 从代理封装的Intent获取启动Service的源Intent
     */
    private Intent getBundleIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        Intent bundleIntent = intent.getParcelableExtra(PluginUtils.EXTRA_BUNDLE_INTENT);
        return bundleIntent;
    }

    private void loadBundleService(Intent intent) {
        if (intent == null) {
            return;
        }
        try {
            ComponentName componentName = intent.getParcelableExtra("componentName");
            className = componentName.getClassName();
            String packageName = componentName.getPackageName();
            Bundle bundle = pluginManager.getBundleByPackageName(packageName);
            if (bundle == null || bundle.getBundleModule() == null) {
                LogUtils.e("BundleServiceContainer", "[loadBundleService] bundle =" + bundle +
                        " packageName[" + packageName + "] className[" + className + "]");
                return;
            }
            PluginModule bundleModule = bundle.getBundleModule();
            if (bundleModule == null) {
                LogUtils.e("BundleServiceContainer", "[loadBundleService] bundleModule =" + bundleModule);
                return;
            }

            Context newBase = new PluginContextWrapper(bundleModule.getPulginApplication());
            ClassLoader classLoader = bundle.getBundleClassLoader();
            try {
                Class<?> c = classLoader.loadClass(className);
                service = (Service) c.newInstance();
                // 获取attach方法需要的参数
                Object activityThread = ReflectUtils.readField(this, "mThread");
                IBinder token = ReflectUtils.readField(this, "mToken");
                Object activityManager = ReflectUtils.readField(this, "mActivityManager");
                // 调用attach方法
                ReflectUtils.invoke(Service.class, service, "attach",
                        new Class[]{Context.class, activityThread.getClass(), String.class, IBinder.class, Application.class, Object.class},
                        new Object[]{newBase, activityThread, BundleServiceContainer.class.getName(), token, bundleModule.getPulginApplication(), activityManager});
                // 调用onCreate
                service.onCreate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
