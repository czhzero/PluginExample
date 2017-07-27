package com.chen.plugin.am;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;


import com.chen.plugin.core.Env;
import com.chen.plugin.stub.ActivityStub;
import com.chen.plugin.stub.ContentProviderStub;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenzhaohua on 17-7-21.
 * 预先注册(占坑)进程信息管理
 */
class StaticProcessList {

    /**
     * 根据CATEGORY_ACTIVITY_PROXY_STUB筛选的进程信息
     * 预先占坑的进程信息,包括ActivityInfo,ServiceInfo,ProviderInfo
     */
    private Map<String, ProcessItem> items = new HashMap<>(10);

    /**
     * 非CATEGORY_ACTIVITY_PROXY_STUB的其他额外进程
     */
    private List<String> mOtherProcessNames = new ArrayList<>();


    /**
     * 预先注册的进程Item
     */
    private class ProcessItem {

        private String name;

        //key=ActivityInfo.name,value=ActivityInfo
        private Map<String, ActivityInfo> activityInfos = new HashMap<>(4);
        //key=ServiceInfo.name,value=ServiceInfo
        private Map<String, ServiceInfo> serviceInfos = new HashMap<>(1);
        //key=ProviderInfo.authority,value=ProviderInfo
        private Map<String, ProviderInfo> providerInfos = new HashMap<>(1);

        private void addActivityInfo(ActivityInfo info) {
            if (!activityInfos.containsKey(info.name)) {
                activityInfos.put(info.name, info);
            }
        }


        private void addServiceInfo(ServiceInfo info) {
            if (!serviceInfos.containsKey(info.name)) {
                serviceInfos.put(info.name, info);
            }
        }


        private void addProviderInfo(ProviderInfo info) {
            if (!providerInfos.containsKey(info.authority)) {
                providerInfos.put(info.authority, info);
            }
        }
    }


    void onCreate(Context hostContext) throws NameNotFoundException {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Env.CATEGORY_ACTIVITY_PROXY_STUB);
        intent.setPackage(hostContext.getPackageName());


        PackageManager pm = hostContext.getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        for (ResolveInfo activity : activities) {
            addActivityInfo(activity.activityInfo);
        }

        List<ResolveInfo> services = pm.queryIntentServices(intent, 0);
        for (ResolveInfo service : services) {
            addServiceInfo(service.serviceInfo);
        }

        PackageInfo packageInfo = pm.getPackageInfo(hostContext.getPackageName(), PackageManager.GET_PROVIDERS);

        if (packageInfo.providers != null && packageInfo.providers.length > 0) {
            for (ProviderInfo providerInfo : packageInfo.providers) {
                if (providerInfo.name != null && providerInfo.name.startsWith(ContentProviderStub.class.getName())) {
                    addProviderInfo(providerInfo);
                }
            }
        }

        mOtherProcessNames.clear();

        PackageInfo packageInfo1 = pm.getPackageInfo(hostContext.getPackageName(), PackageManager.GET_ACTIVITIES
                | PackageManager.GET_RECEIVERS
                | PackageManager.GET_PROVIDERS
                | PackageManager.GET_SERVICES);
        if (packageInfo1.activities != null) {
            for (ActivityInfo info : packageInfo1.activities) {
                if (!mOtherProcessNames.contains(info.processName) && !items.containsKey(info.processName)) {
                    mOtherProcessNames.add(info.processName);
                }
            }
        }

        if (packageInfo1.receivers != null) {
            for (ActivityInfo info : packageInfo1.receivers) {
                if (!mOtherProcessNames.contains(info.processName) && !items.containsKey(info.processName)) {
                    mOtherProcessNames.add(info.processName);
                }
            }
        }

        if (packageInfo1.providers != null) {
            for (ProviderInfo info : packageInfo1.providers) {
                if (!mOtherProcessNames.contains(info.processName) && !items.containsKey(info.processName)) {
                    mOtherProcessNames.add(info.processName);
                }
            }
        }

        if (packageInfo1.services != null) {
            for (ServiceInfo info : packageInfo1.services) {
                if (!mOtherProcessNames.contains(info.processName) && !items.containsKey(info.processName)) {
                    mOtherProcessNames.add(info.processName);
                }
            }
        }

    }

    public List<String> getOtherProcessNames() {
        return mOtherProcessNames;
    }

    private void addActivityInfo(ActivityInfo info) {
        if (TextUtils.isEmpty(info.processName)) {
            info.processName = info.packageName;
        }
        ProcessItem item = items.get(info.processName);
        if (item == null) {
            item = new ProcessItem();
            item.name = info.processName;
            items.put(info.processName, item);
        }
        item.addActivityInfo(info);
    }

    ActivityInfo findActivityInfoForName(String processName, String activityName) {
        ProcessItem item = items.get(processName);
        if (item != null && item.activityInfos != null) {
            return item.activityInfos.get(activityName);
        }
        return null;
    }

    ActivityInfo findActivityInfoForLaunchMode(String processName, int launchMode) {
        ProcessItem item = items.get(processName);
        if (item != null && item.activityInfos != null) {
            for (ActivityInfo info : item.activityInfos.values()) {
                if (info.launchMode == launchMode) {
                    return info;
                }
            }
        }
        return null;
    }


    private void addServiceInfo(ServiceInfo info) {
        if (TextUtils.isEmpty(info.processName)) {
            info.processName = info.packageName;
        }
        ProcessItem item = items.get(info.processName);
        if (item == null) {
            item = new ProcessItem();
            item.name = info.processName;
            items.put(info.processName, item);
        }
        item.addServiceInfo(info);
    }

    ServiceInfo findServiceInfoForName(String processName, String serviceInfoName) {
        ProcessItem item = items.get(processName);
        if (item != null && item.serviceInfos != null) {
            return item.serviceInfos.get(serviceInfoName);
        }
        return null;
    }


    private void addProviderInfo(ProviderInfo info) {
        if (TextUtils.isEmpty(info.processName)) {
            info.processName = info.packageName;
        }
        ProcessItem item = items.get(info.processName);
        if (item == null) {
            item = new ProcessItem();
            item.name = info.processName;
            items.put(info.processName, item);
        }
        item.addProviderInfo(info);
    }

    ProviderInfo findProviderInfoForAuthority(String processName, String authority) {
        ProcessItem item = items.get(processName);
        if (item != null && item.providerInfos != null) {
            return item.providerInfos.get(authority);
        }
        return null;
    }

    List<String> getProcessNames() {
        return new ArrayList<String>(items.keySet());
    }

    List<ActivityInfo> getActivityInfoForProcessName(String processName) {
        ProcessItem item = items.get(processName);
        ArrayList<ActivityInfo> activityInfos = new ArrayList<ActivityInfo>(item.activityInfos.values());
        Collections.sort(activityInfos, sComponentInfoComparator);
        return activityInfos;
    }


    private static final Comparator<ComponentInfo> sComponentInfoComparator = new Comparator<ComponentInfo>() {
        @Override
        public int compare(ComponentInfo lhs, ComponentInfo rhs) {
            return Collator.getInstance().compare(lhs.name, rhs.name);
        }
    };

    List<ActivityInfo> getActivityInfoForProcessName(String processName, boolean dialogStyle) {
        ProcessItem item = items.get(processName);
        Collection<ActivityInfo> values = item.activityInfos.values();
        ArrayList<ActivityInfo> activityInfos = new ArrayList<ActivityInfo>();
        for (ActivityInfo info : values) {
            if (dialogStyle) {
                if (info.name.startsWith(ActivityStub.Dialog.class.getName())) {
                    activityInfos.add(info);
                }
            } else {
                if (!info.name.startsWith(ActivityStub.Dialog.class.getName())) {
                    activityInfos.add(info);
                }
            }
        }

        Collections.sort(activityInfos, sComponentInfoComparator);
        return activityInfos;
    }


    List<ServiceInfo> getServiceInfoForProcessName(String processName) {
        ProcessItem item = items.get(processName);
        ArrayList<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>(item.serviceInfos.values());
        Collections.sort(serviceInfos, sComponentInfoComparator);
        return serviceInfos;
    }

    List<ProviderInfo> getProviderInfoForProcessName(String processName) {
        ProcessItem item = items.get(processName);
        ArrayList<ProviderInfo> providerInfos = new ArrayList<ProviderInfo>(item.providerInfos.values());
        Collections.sort(providerInfos, sComponentInfoComparator);
        return providerInfos;
    }

    void clear() {
        items.clear();
    }
}
