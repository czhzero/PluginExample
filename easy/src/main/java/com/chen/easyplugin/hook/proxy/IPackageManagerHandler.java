package com.chen.easyplugin.hook.proxy;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import com.chen.easyplugin.core.Bundle;
import com.chen.easyplugin.core.PluginModule;
import com.chen.easyplugin.pm.PluginManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenzhaohua on 17/4/7.
 */
public class IPackageManagerHandler implements InvocationHandler {

    Object mBase;
    PluginModule module;
    //系统已安装的应用包名
    private List<String> packageNames;

    public IPackageManagerHandler(Object pm, PluginModule module) {
        this.mBase = pm;
        this.module = module;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("getPackageInfo".equals(method.getName())) {
            if (args != null) {
                final int index = 0;
                String packageName = null;
                if (args.length > index) {
                    if (args[index] != null && args[index] instanceof String) {
                        packageName = (String) args[index];
                    }
                }
                if (!isInstalled(packageName)) {
                    Bundle bundle = PluginManager.getInstance().getBundleByPackageName(packageName);
                    if (bundle != null) {
                        PackageInfo packageInfo = bundle.getBundleModule().getPackageInfo();
                        if (packageInfo != null) {
                            return packageInfo;
                        }
                    }
                }
            }
        } else if ("getApplicationInfo".equals(method.getName())) {
            if (args != null) {
                final int index = 0;
                String packageName = null;
                if (args.length > index) {
                    if (args[index] != null && args[index] instanceof String) {
                        packageName = (String) args[index];
                    }
                }
                if (!isInstalled(packageName)) {
                    Bundle bundle = PluginManager.getInstance().getBundleByPackageName(packageName);
                    if (bundle != null) {
                        ApplicationInfo appInfo = bundle.getBundleModule().getApplicationInfo();
                        if (appInfo != null) {
                            return appInfo;
                        }
                    }
                }
            }
        }else if ("getActivityInfo".equals(method.getName())) {
            if (args != null) {
                final int index0 = 0;
                if (args.length >= 2 && args[index0] instanceof ComponentName) {
                    ComponentName componentName = (ComponentName) args[index0];
                    String packageName = componentName.getPackageName();
                    if (!isInstalled(packageName)) {
                        Bundle bundle = PluginManager.getInstance().getBundleByPackageName(packageName);
                        if (bundle != null) {
                            ActivityInfo info = bundle.getBundleModule().getActivityInfo(componentName.getClassName());
                            if (info != null) {
                                return info;
                            }
                        }
                    }
                }
            }
        }
        return method.invoke(mBase, args);
    }

    /**
     * 是否已在系统安装
     * @param packageName
     * @return
     */
    private boolean isInstalled(String packageName) {
        if (packageNames == null) {
            List<PackageInfo> packageInfos = PluginManager.getInstance().getHostContext().getPackageManager().getInstalledPackages(0);
            packageNames = new ArrayList<String>();
            if (packageInfos != null) {
                for (int i = 0; i < packageInfos.size(); i++) {
                    String packName = packageInfos.get(i).packageName;
                    packageNames.add(packName);
                }
            }
        }
        return packageNames.contains(packageName);
    }
}
