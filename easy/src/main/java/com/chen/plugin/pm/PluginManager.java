package com.chen.plugin.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.chen.plugin.reflect.MethodUtils;
import com.chen.easyplugin.utils.LogUtils;
import com.chen.plugin.aidl.IApplicationCallback;
import com.chen.plugin.aidl.IPackageDataObserver;
import com.chen.plugin.aidl.IPluginManagerService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenzhaohua on 17/5/18.
 * <p>
 * PluginManagerService 实现
 */
public class PluginManager implements ServiceConnection {

    public static final String ACTION_PACKAGE_ADDED = "com.morgoo.doirplugin.PACKAGE_ADDED";
    public static final String ACTION_PACKAGE_REMOVED = "com.morgoo.doirplugin.PACKAGE_REMOVED";
    public static final String ACTION_DROIDPLUGIN_INIT = "com.morgoo.droidplugin.ACTION_DROIDPLUGIN_INIT";
    public static final String ACTION_MAINACTIVITY_ONCREATE = "com.morgoo.droidplugin.ACTION_MAINACTIVITY_ONCREATE";
    public static final String ACTION_MAINACTIVITY_ONDESTORY = "com.morgoo.droidplugin.ACTION_MAINACTIVITY_ONDESTORY";
    public static final String ACTION_SETTING = "com.morgoo.droidplugin.ACTION_SETTING";
    public static final String ACTION_SHORTCUT_PROXY = "com.morgoo.droidplugin.ACTION_SHORTCUT_PROXY";


    public static final String EXTRA_PID = "com.morgoo.droidplugin.EXTRA_PID";
    public static final String EXTRA_PACKAGENAME = "com.morgoo.droidplugin.EXTRA_EXTRA_PACKAGENAME";

    public static final String STUB_AUTHORITY_NAME = "com.morgoo.droidplugin_stub";
    public static final String EXTRA_APP_PERSISTENT = "com.morgoo.droidplugin.EXTRA_APP_PERSISTENT";

    public static final int INSTALL_FAILED_NO_REQUESTEDPERMISSION = -100001;
    public static final int STUB_NO_ACTIVITY_MAX_NUM = 4;

    private static final String TAG = PluginManager.class.getSimpleName();


    private Context mHostContext;
    private static PluginManager sInstance = null;
    private IPluginManagerService mPluginManager;
    private Object mWaitLock = new Object();


    private List<WeakReference<ServiceConnection>> sServiceConnection = Collections.synchronizedList(new ArrayList<WeakReference<ServiceConnection>>(1));


    @Override
    public void onServiceConnected(final ComponentName componentName, final IBinder iBinder) {

        mPluginManager = IPluginManagerService.Stub.asInterface(iBinder);

        new Thread() {
            @Override
            public void run() {
                try {

                    mPluginManager.waitForReady();

                    mPluginManager.registerApplicationCallback(new IApplicationCallback.Stub() {
                        @Override
                        public Bundle onCallback(Bundle extra) throws RemoteException {
                            return extra;
                        }
                    });

                    Iterator<WeakReference<ServiceConnection>> iterator = sServiceConnection.iterator();
                    while (iterator.hasNext()) {
                        WeakReference<ServiceConnection> wsc = iterator.next();
                        ServiceConnection sc = wsc != null ? wsc.get() : null;
                        if (sc != null) {
                            sc.onServiceConnected(componentName, iBinder);
                        } else {
                            iterator.remove();
                        }
                    }

                    mPluginManager.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {
                            onServiceDisconnected(componentName);
                        }
                    }, 0);

                    LogUtils.i(TAG, "PluginManager ready!");

                } catch (Throwable e) {
                    LogUtils.e(TAG, "Lost the mPluginManager connect...", e);
                } finally {
                    try {
                        synchronized (mWaitLock) {
                            mWaitLock.notifyAll();
                        }
                    } catch (Exception e) {
                        LogUtils.i(TAG, "PluginManager notifyAll:" + e.getMessage());
                    }
                }
            }
        }.start();

        LogUtils.i(TAG, "onServiceConnected connected OK!");
    }


    @Override
    public void onServiceDisconnected(ComponentName componentName) {

        LogUtils.i(TAG, "onServiceDisconnected disconnected!");

        mPluginManager = null;

        Iterator<WeakReference<ServiceConnection>> iterator = sServiceConnection.iterator();
        while (iterator.hasNext()) {
            WeakReference<ServiceConnection> wsc = iterator.next();
            ServiceConnection sc = wsc != null ? wsc.get() : null;
            if (sc != null) {
                sc.onServiceDisconnected(componentName);
            } else {
                iterator.remove();
            }
        }

        //服务连接断开，需要重新连接。
        connectToService();
    }



    public void waitForConnected() {
        if (isConnected()) {
            return;
        } else {
            try {
                synchronized (mWaitLock) {
                    mWaitLock.wait();
                }
            } catch (InterruptedException e) {
                LogUtils.i(TAG, "waitForConnected:" + e.getMessage());
            }
            LogUtils.i(TAG, "waitForConnected finish");
        }
    }


    /**
     * 提供超时设置的waitForConnected版本
     *
     * @param timeout，当超时时间大于0时超时设置生效
     */
    public void waitForConnected(long timeout) {
        if (timeout > 0) {
            if (isConnected()) {
                return;
            } else {
                try {
                    synchronized (mWaitLock) {
                        mWaitLock.wait(timeout);
                    }
                } catch (InterruptedException e) {
                    LogUtils.i(TAG, "waitForConnected:" + e.getMessage());
                }
                LogUtils.i(TAG, "waitForConnected finish");
            }
        } else {
            waitForConnected();
        }
    }


    public void connectToService() {

        if (mPluginManager == null) {
            LogUtils.d(TAG, "try connectToService");
            try {
                Intent intent = new Intent(mHostContext, PluginManagerService.class);
                intent.setPackage(mHostContext.getPackageName());
                mHostContext.startService(intent);
                mHostContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                LogUtils.e(TAG, "connectToService", e);
            }
        }
    }


    public void addServiceConnection(ServiceConnection sc) {
        sServiceConnection.add(new WeakReference<ServiceConnection>(sc));
    }

    public void removeServiceConnection(ServiceConnection sc) {
        Iterator<WeakReference<ServiceConnection>> iterator = sServiceConnection.iterator();
        while (iterator.hasNext()) {
            WeakReference<ServiceConnection> wsc = iterator.next();
            if (wsc.get() == sc) {
                iterator.remove();
            }
        }
    }


    public void init(Context hostContext) {
        mHostContext = hostContext;
        connectToService();
    }

    public Context getHostContext() {
        return mHostContext;
    }

    public boolean isConnected() {
        return mHostContext != null && mPluginManager != null;
    }

    public static PluginManager getInstance() {
        if (sInstance == null) {
            sInstance = new PluginManager();
        }
        return sInstance;
    }




    //////////////////////////
    //  API
    //////////////////////////
    public PackageInfo getPackageInfo(String packageName, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getPackageInfo(packageName, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getPackageInfo", e);
        }
        return null;
    }


    public boolean isPluginPackage(String packageName) throws RemoteException {
        try {
            if (mHostContext == null) {
                return false;
            }
            if (TextUtils.equals(mHostContext.getPackageName(), packageName)) {
                return false;
            }

            if (mPluginManager != null && packageName != null) {
                return mPluginManager.isPluginPackage(packageName);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "isPluginPackage", e);
        }
        return false;
    }

    public boolean isPluginPackage(ComponentName className) throws RemoteException {
        if (className == null) {
            return false;
        }
        return isPluginPackage(className.getPackageName());
    }




    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException, RemoteException {

        try {
            if (className == null) {
                return null;
            }
            if (mPluginManager != null && className != null) {
                return mPluginManager.getActivityInfo(className, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "getActivityInfo RemoteException", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "getActivityInfo", e);
        }
        return null;
    }



    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException, RemoteException {
        if (className == null) {
            return null;
        }
        try {
            if (mPluginManager != null && className != null) {
                return mPluginManager.getReceiverInfo(className, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getReceiverInfo", e);
        }
        return null;
    }

    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException, RemoteException {
        if (className == null) {
            return null;
        }
        try {
            if (mPluginManager != null && className != null) {
                return mPluginManager.getServiceInfo(className, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getServiceInfo", e);
        }
        return null;
    }

    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException, RemoteException {
        if (className == null) {
            return null;
        }
        try {
            if (mPluginManager != null && className != null) {
                return mPluginManager.getProviderInfo(className, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getProviderInfo", e);
        }
        return null;
    }

    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.resolveIntent(intent, resolvedType, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "resolveIntent", e);
        }
        return null;
    }

    public ResolveInfo resolveService(Intent intent, String resolvedType, Integer flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.resolveService(intent, resolvedType, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "resolveService", e);
        }
        return null;
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentActivities(intent, resolvedType, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "queryIntentActivities RemoteException", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "queryIntentActivities", e);
        }
        return null;
    }

    public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentReceivers(intent, resolvedType, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "queryIntentReceivers", e);
        }
        return null;
    }

    public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentServices(intent, resolvedType, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "queryIntentServices RemoteException", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "queryIntentServices", e);
        }
        return null;
    }

    public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && intent != null) {
                return mPluginManager.queryIntentContentProviders(intent, resolvedType, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "queryIntentContentProviders", e);
        }
        return null;
    }

    public List<PackageInfo> getInstalledPackages(int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getInstalledPackages(flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "getInstalledPackages RemoteException", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "getInstalledPackages", e);
        }
        return null;
    }

    public List<ApplicationInfo> getInstalledApplications(int flags) throws RemoteException {

        try {
            if (mPluginManager != null) {
                return mPluginManager.getInstalledApplications(flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getInstalledApplications", e);
        }
        return null;
    }

    public PermissionInfo getPermissionInfo(String name, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && name != null) {
                return mPluginManager.getPermissionInfo(name, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getPermissionInfo", e);
        }
        return null;
    }

    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && group != null) {
                return mPluginManager.queryPermissionsByGroup(group, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "queryPermissionsByGroup", e);
        }
        return null;
    }

    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && name != null) {
                return mPluginManager.getPermissionGroupInfo(name, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getPermissionGroupInfo", e);
        }
        return null;
    }

    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getAllPermissionGroups(flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getAllPermissionGroups", e);
        }
        return null;
    }

    public ProviderInfo resolveContentProvider(String name, Integer flags) throws RemoteException {
        try {
            if (mPluginManager != null && name != null) {
                return mPluginManager.resolveContentProvider(name, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "resolveContentProvider", e);
        }
        return null;
    }

    public void deleteApplicationCacheFiles(String packageName, final Object observer/*android.content.pm.IPackageDataObserver*/) throws RemoteException {
        try {
            if (mPluginManager != null && packageName != null) {
                mPluginManager.deleteApplicationCacheFiles(packageName, new IPackageDataObserver.Stub() {

                    @Override
                    public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                        if (observer != null) {
                            try {
                                MethodUtils.invokeMethod(observer, "onRemoveCompleted", packageName, succeeded);
                            } catch (Exception e) {
                                RemoteException exception = new RemoteException();
                                exception.initCause(exception);
                                throw exception;
                            }
                        }
                    }
                });
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "deleteApplicationCacheFiles", e);
        }
    }

    public void clearApplicationUserData(String packageName, final Object observer/*android.content.pm.IPackageDataObserver*/) throws RemoteException {
        try {
            if (mPluginManager != null && packageName != null) {
                mPluginManager.clearApplicationUserData(packageName, new IPackageDataObserver.Stub() {

                    @Override
                    public void onRemoveCompleted(String packageName, boolean succeeded) throws RemoteException {
                        if (observer != null) {
                            try {
                                MethodUtils.invokeMethod(observer, "onRemoveCompleted", packageName, succeeded);
                            } catch (Exception e) {
                                RemoteException exception = new RemoteException();
                                exception.initCause(exception);
                                throw exception;
                            }
                        }
                    }
                });
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "clearApplicationUserData", e);
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws RemoteException {
        try {
            if (mPluginManager != null && packageName != null) {
                return mPluginManager.getApplicationInfo(packageName, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "getApplicationInfo RemoteException", e);
        } catch (Exception e) {
            LogUtils.e(TAG, "getApplicationInfo", e);
        }
        return null;
    }

    public ActivityInfo selectStubActivityInfo(ActivityInfo pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubActivityInfo(pluginInfo);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "selectStubActivityInfo", e);
        }
        return null;
    }

    public ActivityInfo selectStubActivityInfo(Intent pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubActivityInfoByIntent(pluginInfo);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "selectStubActivityInfo", e);
        }
        return null;
    }

    public ServiceInfo selectStubServiceInfo(ServiceInfo pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubServiceInfo(pluginInfo);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "selectStubServiceInfo", e);
        }
        return null;
    }

    public ServiceInfo selectStubServiceInfo(Intent pluginInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubServiceInfoByIntent(pluginInfo);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "selectStubServiceInfo", e);
        }
        return null;
    }

    public ProviderInfo selectStubProviderInfo(String name) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.selectStubProviderInfo(name);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "selectStubProviderInfo", e);
        }
        return null;
    }

    public ActivityInfo resolveActivityInfo(Intent intent, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                if (intent.getComponent() != null) {
                    return mPluginManager.getActivityInfo(intent.getComponent(), flags);
                } else {
                    ResolveInfo resolveInfo = mPluginManager.resolveIntent(intent, intent.resolveTypeIfNeeded(mHostContext.getContentResolver()), flags);
                    if (resolveInfo != null && resolveInfo.activityInfo != null) {
                        return resolveInfo.activityInfo;
                    }
                }
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
            return null;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "selectStubActivityInfo", e);
        }
        return null;
    }

    public ServiceInfo resolveServiceInfo(Intent intent, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                if (intent.getComponent() != null) {
                    return mPluginManager.getServiceInfo(intent.getComponent(), flags);
                } else {
                    ResolveInfo resolveInfo = mPluginManager.resolveIntent(intent, intent.resolveTypeIfNeeded(mHostContext.getContentResolver()), flags);
                    if (resolveInfo != null && resolveInfo.serviceInfo != null) {
                        return resolveInfo.serviceInfo;
                    }
                }
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
            return null;
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "resolveServiceInfo", e);
        }
        return null;
    }

    public void killBackgroundProcesses(String packageName) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.killBackgroundProcesses(packageName);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "killBackgroundProcesses", e);
        }
    }

    public void forceStopPackage(String packageName) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.forceStopPackage(packageName);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "forceStopPackage", e);
        }

    }

    public boolean killApplicationProcess(String packageName) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.killApplicationProcess(packageName);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "killApplicationProcess", e);
        }
        return false;
    }

    public List<ActivityInfo> getReceivers(String packageName, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getReceivers(packageName, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getReceivers", e);
        }
        return null;
    }

    public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getReceiverIntentFilter(info);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getReceiverIntentFilter", e);
        }
        return null;
    }

    public ServiceInfo getTargetServiceInfo(ServiceInfo info) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getTargetServiceInfo(info);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getTargetServiceInfo", e);
        }
        return null;
    }

    public int installPackage(String filepath, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                int result = mPluginManager.installPackage(filepath, flags);
                LogUtils.w(TAG, String.format("%s install result %d", filepath, result));
                return result;
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "forceStopPackage", e);
        }
        return -1;
    }

    public List<String> getPackageNameByPid(int pid) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getPackageNameByPid(pid);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "forceStopPackage", e);
        }
        return null;
    }


    public String getProcessNameByPid(int pid) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getProcessNameByPid(pid);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "forceStopPackage", e);
        }
        return null;
    }

    public void onActivityCreated(ActivityInfo stubInfo, ActivityInfo targetInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onActivityCreated(stubInfo, targetInfo);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "onActivityCreated", e);
        }
    }

    public void onActivityDestory(ActivityInfo stubInfo, ActivityInfo targetInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onActivityDestory(stubInfo, targetInfo);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "onActivityDestory", e);
        }
    }

    public void onServiceCreated(ServiceInfo stubInfo, ServiceInfo targetInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onServiceCreated(stubInfo, targetInfo);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "onServiceCreated", e);
        }
    }


    public void onServiceDestory(ServiceInfo stubInfo, ServiceInfo targetInfo) {
        try {
            if (mPluginManager != null) {
                mPluginManager.onServiceDestory(stubInfo, targetInfo);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "onServiceDestory", e);
        }
    }

    public void onProviderCreated(ProviderInfo stubInfo, ProviderInfo targetInfo) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onProviderCreated(stubInfo, targetInfo);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "onProviderCreated", e);
        }
    }

    public void reportMyProcessName(String stubProcessName, String targetProcessName, String targetPkg) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.reportMyProcessName(stubProcessName, targetProcessName, targetPkg);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "reportMyProcessName", e);
        }
    }

    public void deletePackage(String packageName, int flags) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.deletePackage(packageName, flags);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "deletePackage", e);
        }
    }


    public int checkSignatures(String pkg0, String pkg1) throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.checkSignatures(pkg0, pkg1);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
                return PackageManager.SIGNATURE_NO_MATCH;
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "deletePackage", e);
            return PackageManager.SIGNATURE_NO_MATCH;
        }
    }

    public void onActivtyOnNewIntent(ActivityInfo stubInfo, ActivityInfo targetInfo, Intent intent) throws RemoteException {
        try {
            if (mPluginManager != null) {
                mPluginManager.onActivtyOnNewIntent(stubInfo, targetInfo, intent);
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "onActivtyOnNewIntent", e);
        }
    }

    public int getMyPid() throws RemoteException {
        try {
            if (mPluginManager != null) {
                return mPluginManager.getMyPid();
            } else {
                LogUtils.w(TAG, "Plugin Package Manager Service not be connect");
                return -1;
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtils.e(TAG, "getMyPid", e);
            return -1;
        }
    }

}
