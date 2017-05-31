package com.chen.plugin.pm;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;

import com.chen.plugin.aidl.IApplicationCallback;
import com.chen.plugin.aidl.IPackageDataObserver;
import com.chen.plugin.aidl.IPluginManagerService;

import java.util.List;

/**
 * Created by chenzhaohua on 17/5/18.
 */

public class PluginManagerServiceImpl extends IPluginManagerService.Stub {

    private Context mContext;

    public PluginManagerServiceImpl(Context context) {
        mContext = context;
    }

    public void onCreate() {

    }

    public void onDestroy() {

    }

    @Override
    public boolean waitForReady() throws RemoteException {
        return false;
    }

    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws RemoteException {
        return null;
    }

    @Override
    public boolean isPluginPackage(String packageName) throws RemoteException {
        return false;
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws RemoteException {
        return null;
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws RemoteException {
        return null;
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws RemoteException {
        return null;
    }

    @Override
    public ProviderInfo getProviderInfo(ComponentName className, int flags) throws RemoteException {
        return null;
    }

    @Override
    public ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentReceivers(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public ResolveInfo resolveService(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentContentProviders(Intent intent, String resolvedType, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<ApplicationInfo> getInstalledApplications(int flags) throws RemoteException {
        return null;
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws RemoteException {
        return null;
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<PermissionGroupInfo> getAllPermissionGroups(int flags) throws RemoteException {
        return null;
    }

    @Override
    public ProviderInfo resolveContentProvider(String name, int flags) throws RemoteException {
        return null;
    }

    @Override
    public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) throws RemoteException {

    }

    @Override
    public void clearApplicationUserData(String packageName, IPackageDataObserver observer) throws RemoteException {

    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws RemoteException {
        return null;
    }

    @Override
    public int installPackage(String filepath, int flags) throws RemoteException {
        return 0;
    }

    @Override
    public int deletePackage(String packageName, int flags) throws RemoteException {
        return 0;
    }

    @Override
    public List<ActivityInfo> getReceivers(String packageName, int flags) throws RemoteException {
        return null;
    }

    @Override
    public List<IntentFilter> getReceiverIntentFilter(ActivityInfo info) throws RemoteException {
        return null;
    }

    @Override
    public int checkSignatures(String pkg1, String pkg2) throws RemoteException {
        return 0;
    }

    @Override
    public ActivityInfo selectStubActivityInfo(ActivityInfo targetInfo) throws RemoteException {
        return null;
    }

    @Override
    public ActivityInfo selectStubActivityInfoByIntent(Intent targetIntent) throws RemoteException {
        return null;
    }

    @Override
    public ServiceInfo selectStubServiceInfo(ServiceInfo targetInfo) throws RemoteException {
        return null;
    }

    @Override
    public ServiceInfo selectStubServiceInfoByIntent(Intent targetIntent) throws RemoteException {
        return null;
    }

    @Override
    public ServiceInfo getTargetServiceInfo(ServiceInfo stubInfo) throws RemoteException {
        return null;
    }

    @Override
    public ProviderInfo selectStubProviderInfo(String name) throws RemoteException {
        return null;
    }

    @Override
    public List<String> getPackageNameByPid(int pid) throws RemoteException {
        return null;
    }

    @Override
    public String getProcessNameByPid(int pid) throws RemoteException {
        return null;
    }

    @Override
    public boolean killBackgroundProcesses(String packageName) throws RemoteException {
        return false;
    }

    @Override
    public boolean killApplicationProcess(String pluginPackageName) throws RemoteException {
        return false;
    }

    @Override
    public boolean forceStopPackage(String pluginPackageName) throws RemoteException {
        return false;
    }

    @Override
    public boolean registerApplicationCallback(IApplicationCallback callback) throws RemoteException {
        return false;
    }

    @Override
    public boolean unregisterApplicationCallback(IApplicationCallback callback) throws RemoteException {
        return false;
    }

    @Override
    public void onActivityCreated(ActivityInfo stubInfo, ActivityInfo targetInfo) throws RemoteException {

    }

    @Override
    public void onActivityDestory(ActivityInfo stubInfo, ActivityInfo targetInfo) throws RemoteException {

    }

    @Override
    public void onServiceCreated(ServiceInfo stubInfo, ServiceInfo targetInfo) throws RemoteException {

    }

    @Override
    public void onServiceDestory(ServiceInfo stubInfo, ServiceInfo targetInfo) throws RemoteException {

    }

    @Override
    public void onProviderCreated(ProviderInfo stubInfo, ProviderInfo targetInfo) throws RemoteException {

    }

    @Override
    public void reportMyProcessName(String stubProcessName, String targetProcessName, String targetPkg) throws RemoteException {

    }

    @Override
    public void onActivtyOnNewIntent(ActivityInfo stubInfo, ActivityInfo targetInfo, Intent intent) throws RemoteException {

    }

    @Override
    public int getMyPid() throws RemoteException {
        return 0;
    }
}
