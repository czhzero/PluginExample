package com.chen.plugin.am;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.RemoteCallbackList;
import android.os.RemoteException;


import com.chen.plugin.aidl.IApplicationCallback;
import com.chen.plugin.helper.Log;
import com.chen.plugin.pm.IPluginManagerImpl;
import com.chen.plugin.pm.parser.PluginPackageParser;

import java.util.List;
import java.util.Map;

/**
 * Created by Andy Zhang(zhangyong232@gmail.com) on 2015/3/10.
 */
public abstract class BaseActivityManagerService {

    private static final String TAG = BaseActivityManagerService.class.getSimpleName();
    protected Context mHostContext;

    public BaseActivityManagerService(Context hostContext) {
        mHostContext = hostContext;
    }

    //查询某个进程中运行的插件包名列表
    public abstract List<String> getPackageNamesByPid(int pid);

    public abstract ActivityInfo selectStubActivityInfo(int callingPid, int callingUid, ActivityInfo targetInfo) throws RemoteException;

    public abstract ServiceInfo selectStubServiceInfo(int callingPid, int callingUid, ServiceInfo targetInfo) throws RemoteException;

    public abstract ServiceInfo getTargetServiceInfo(int callingPid, int callingUid, ServiceInfo stubInfo) throws RemoteException;

    public abstract ProviderInfo selectStubProviderInfo(int callingPid, int callingUid, ProviderInfo targetInfo) throws RemoteException;

    public void onPkgDeleted(Map<String, PluginPackageParser> pluginCache, PluginPackageParser parser, String packageName) throws Exception {
    }

    public void onPkgInstalled(Map<String, PluginPackageParser> pluginCache, PluginPackageParser parser, String packageName) throws Exception {
    }

    public void onCreate(IPluginManagerImpl pluginManagerImpl) throws Exception {
        if (mRemoteCallbackList == null) {
            mRemoteCallbackList = new MyRemoteCallbackList();
        }
    }

    private RemoteCallbackList<IApplicationCallback> mRemoteCallbackList;

    public String getProcessNameByPid(int pid) {
        return null;
    }

    public void onReportMyProcessName(int callingPid, int callingUid, String stubProcessName, String targetProcessName, String targetPkg) {
    }

    public abstract void onActivtyOnNewIntent(int callingPid, int callingUid, ActivityInfo stubInfo, ActivityInfo targetInfo, Intent intent);

    private static class ProcessCookie {
        private ProcessCookie(int pid, int uid) {
            this.pid = pid;
            this.uid = uid;
        }

        private final int pid;
        private final int uid;
    }

    private class MyRemoteCallbackList extends RemoteCallbackList<IApplicationCallback> {
        @Override
        public void onCallbackDied(IApplicationCallback callback, Object cookie) {
            super.onCallbackDied(callback, cookie);
            if (cookie != null && cookie instanceof ProcessCookie) {
                ProcessCookie p = (ProcessCookie) cookie;
                onProcessDied(p.pid, p.uid);
            }
        }
    }


    protected void onProcessDied(int pid, int uid) {
        Log.i(TAG, "onProcessDied,pid=%s,uid=%s", pid, uid);
    }

    protected void sendCallBack(Bundle extra) {
        if (mRemoteCallbackList != null) {
            int i = mRemoteCallbackList.beginBroadcast();
            while (i > 0) {
                i--;
                try {
                    mRemoteCallbackList.getBroadcastItem(i).onCallback(extra);
                } catch (RemoteException e) {
                    // The RemoteCallbackList will take care of removing
                    // the dead object for us.
                }
            }
            mRemoteCallbackList.finishBroadcast();
        }
    }

    public boolean registerApplicationCallback(int callingPid, int callingUid, IApplicationCallback callback) {
        return mRemoteCallbackList.register(callback, new ProcessCookie(callingPid, callingUid));
    }

    public boolean unregisterApplicationCallback(int callingPid, int callingUid, IApplicationCallback callback) {
        return mRemoteCallbackList.unregister(callback);
    }

    public void onActivityCreated(int callingPid, int callingUid, ActivityInfo stubInfo, ActivityInfo targetInfo) {
    }

    public void onActivityDestory(int callingPid, int callingUid, ActivityInfo stubInfo, ActivityInfo targetInfo) {
    }

    public void onServiceCreated(int callingPid, int callingUid, ServiceInfo stubInfo, ServiceInfo targetInfo) {
    }

    public void onServiceDestory(int callingPid, int callingUid, ServiceInfo stubInfo, ServiceInfo targetInfo) {
    }

    public void onProviderCreated(int callingPid, int callingUid, ProviderInfo stubInfo, ProviderInfo targetInfo) {
    }

    public void onDestory() {
        mRemoteCallbackList.kill();
        mRemoteCallbackList = null;
    }
}
